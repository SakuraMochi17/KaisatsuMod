package com.SakuraMochi17.kaisatsumod.tileentity;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTicketMachine extends TileEntity implements IInventory {
    private ItemStack[] inventory = new ItemStack[15];

    // ★追加：ステッキ連携用のデータ構造
    public boolean isLinked = false;
    public int linkedX, linkedY, linkedZ;

    // ▼ 既存のものをこれに書き換える
    public void setLinkedStation(int x, int y, int z) {
        this.isLinked = true;
        this.linkedX = x;
        this.linkedY = y;
        this.linkedZ = z;
        this.markDirty();
        // ★追加：連携された瞬間にクライアント（画面）へデータを同期する
        if (this.worldObj != null) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public int getSizeInventory() { return inventory.length; }

    @Override
    public ItemStack getStackInSlot(int index) { return inventory[index]; }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (this.inventory[index] != null) {
            ItemStack itemstack;
            if (this.inventory[index].stackSize <= count) {
                itemstack = this.inventory[index];
                this.inventory[index] = null;
                this.markDirty();
                return itemstack;
            } else {
                itemstack = this.inventory[index].splitStack(count);
                if (this.inventory[index].stackSize == 0) this.inventory[index] = null;
                this.markDirty();
                return itemstack;
            }
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (this.inventory[index] != null) {
            ItemStack itemstack = this.inventory[index];
            this.inventory[index] = null;
            return itemstack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventory[index] = stack;
        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }
        this.markDirty();
    }

    @Override
    public String getInventoryName() { return "Ticket Machine"; }

    @Override
    public boolean hasCustomInventoryName() { return false; }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}
    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0) return stack.getItem() instanceof ItemICCard;
        if (index >= 1 && index <= 9) return KaisatsuModMain.getMoneyValue(stack) > 0;
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList nbttaglist = nbt.getTagList("Items", 10);
        this.inventory = new ItemStack[this.getSizeInventory()];
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound tag = nbttaglist.getCompoundTagAt(i);
            byte b0 = tag.getByte("Slot");
            if (b0 >= 0 && b0 < this.inventory.length) {
                this.inventory[b0] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
        // ★追加：連携座標の読み込み
        this.isLinked = nbt.getBoolean("IsLinked");
        this.linkedX = nbt.getInteger("LinkedX");
        this.linkedY = nbt.getInteger("LinkedY");
        this.linkedZ = nbt.getInteger("LinkedZ");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.inventory.length; ++i) {
            if (this.inventory[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)i);
                this.inventory[i].writeToNBT(tag);
                nbttaglist.appendTag(tag);
            }
        }
        nbt.setTag("Items", nbttaglist);
        // ★追加：連携座標の保存
        nbt.setBoolean("IsLinked", this.isLinked);
        nbt.setInteger("LinkedX", this.linkedX);
        nbt.setInteger("LinkedY", this.linkedY);
        nbt.setInteger("LinkedZ", this.linkedZ);
    }
    // ▼ ファイルの一番下（最後の } の手前）にこれを追加する
    @Override
    public net.minecraft.network.Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }
}
