package com.SakuraMochi17.kaisatsumod.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTransferGate extends TileEntity {
    // --- 出場用の連携データ（リンク1） ---
    public boolean isLinked1 = false;
    public int linked1X;
    public int linked1Y;
    public int linked1Z;

    // --- 入場用の連携データ（リンク2） ---
    public boolean isLinked2 = false;
    public int linked2X;
    public int linked2Y;
    public int linked2Z;

    // 連携リセット用メソッド
    public void resetLinks() {
        this.isLinked1 = false;
        this.isLinked2 = false;
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.isLinked1 = nbt.getBoolean("isLinked1");
        this.linked1X = nbt.getInteger("linked1X");
        this.linked1Y = nbt.getInteger("linked1Y");
        this.linked1Z = nbt.getInteger("linked1Z");

        this.isLinked2 = nbt.getBoolean("isLinked2");
        this.linked2X = nbt.getInteger("linked2X");
        this.linked2Y = nbt.getInteger("linked2Y");
        this.linked2Z = nbt.getInteger("linked2Z");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isLinked1", this.isLinked1);
        nbt.setInteger("linked1X", this.linked1X);
        nbt.setInteger("linked1Y", this.linked1Y);
        nbt.setInteger("linked1Z", this.linked1Z);

        nbt.setBoolean("isLinked2", this.isLinked2);
        nbt.setInteger("linked2X", this.linked2X);
        nbt.setInteger("linked2Y", this.linked2Y);
        nbt.setInteger("linked2Z", this.linked2Z);
    }
}
