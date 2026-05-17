package com.SakuraMochi17.kaisatsumod.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTransferGate extends TileEntity {
    // 乗り換え元（降車駅）のデータ
    public boolean isLinked1 = false;
    public int linked1X, linked1Y, linked1Z;

    // 乗り換え先（乗車駅）のデータ
    public boolean isLinked2 = false;
    public int linked2X, linked2Y, linked2Z;

    public void setLink1(int x, int y, int z) {
        this.isLinked1 = true;
        this.linked1X = x; this.linked1Y = y; this.linked1Z = z;
        this.markDirty();
        if (this.worldObj != null) this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public void setLink2(int x, int y, int z) {
        this.isLinked2 = true;
        this.linked2X = x; this.linked2Y = y; this.linked2Z = z;
        this.markDirty();
        if (this.worldObj != null) this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public void resetLinks() {
        this.isLinked1 = false;
        this.isLinked2 = false;
        this.markDirty();
        if (this.worldObj != null) this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.isLinked1 = nbt.getBoolean("IsLinked1");
        this.linked1X = nbt.getInteger("Linked1X");
        this.linked1Y = nbt.getInteger("Linked1Y");
        this.linked1Z = nbt.getInteger("Linked1Z");

        this.isLinked2 = nbt.getBoolean("IsLinked2");
        this.linked2X = nbt.getInteger("Linked2X");
        this.linked2Y = nbt.getInteger("Linked2Y");
        this.linked2Z = nbt.getInteger("Linked2Z");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("IsLinked1", this.isLinked1);
        nbt.setInteger("Linked1X", this.linked1X);
        nbt.setInteger("Linked1Y", this.linked1Y);
        nbt.setInteger("Linked1Z", this.linked1Z);

        nbt.setBoolean("IsLinked2", this.isLinked2);
        nbt.setInteger("Linked2X", this.linked2X);
        nbt.setInteger("Linked2Y", this.linked2Y);
        nbt.setInteger("Linked2Z", this.linked2Z);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }
}
