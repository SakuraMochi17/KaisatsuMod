package com.SakuraMochi17.kaisatsumod.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityStaffTerminal extends TileEntity implements IInventory {
    public String stationName = "未設定";

    // スロットを10個に拡張します
    // 0:対象の証明書, 1:支払用IC, 2~5:現金投入, 6~9:釣銭
    private ItemStack[] inventory = new ItemStack[10];

    // ==========================================
    // セーブ＆ロード処理（駅名とインベントリの中身）
    // ==========================================
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("stationName")) this.stationName = nbt.getString("stationName");

        NBTTagList nbttaglist = nbt.getTagList("Items", 10);
        this.inventory = new ItemStack[this.getSizeInventory()];
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbt1 = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbt1.getByte("Slot");
            if (b0 >= 0 && b0 < this.inventory.length) {
                this.inventory[b0] = ItemStack.loadItemStackFromNBT(nbt1);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("stationName", this.stationName);

        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.inventory.length; ++i) {
            if (this.inventory[i] != null) {
                NBTTagCompound nbt1 = new NBTTagCompound();
                nbt1.setByte("Slot", (byte) i);
                this.inventory[i].writeToNBT(nbt1);
                nbttaglist.appendTag(nbt1);
            }
        }
        nbt.setTag("Items", nbttaglist);
    }

    // クライアント（画面）との駅名同期用パケット
    @Override
    public net.minecraft.network.Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("stationName", this.stationName);
        return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.S35PacketUpdateTileEntity pkt) {
        this.stationName = pkt.func_148857_g().getString("stationName");
    }

    // ==========================================
    // インベントリ（IInventory）の必須メソッド群
    // ==========================================
    @Override public int getSizeInventory() { return this.inventory.length; }
    @Override public ItemStack getStackInSlot(int slot) { return this.inventory[slot]; }
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (this.inventory[slot] != null) {
            ItemStack itemstack;
            if (this.inventory[slot].stackSize <= amount) {
                itemstack = this.inventory[slot];
                this.inventory[slot] = null;
                this.markDirty();
                return itemstack;
            } else {
                itemstack = this.inventory[slot].splitStack(amount);
                if (this.inventory[slot].stackSize == 0) this.inventory[slot] = null;
                this.markDirty();
                return itemstack;
            }
        } else { return null; }
    }
    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (this.inventory[slot] != null) {
            ItemStack itemstack = this.inventory[slot];
            this.inventory[slot] = null;
            return itemstack;
        } else { return null; }
    }
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.inventory[slot] = stack;
        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }
        this.markDirty();
    }
    @Override public String getInventoryName() { return "container.staffTerminal"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) { return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistanceSq((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D; }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }
}