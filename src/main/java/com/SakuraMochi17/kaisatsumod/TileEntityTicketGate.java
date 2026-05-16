package com.SakuraMochi17.kaisatsumod;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTicketGate extends TileEntity {
    public int gateMode = 0; // 0: 双方向, 1: 入場専用, 2: 出場専用

    // ★ステッキで連携された駅管理ブロックの座標
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
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.gateMode = nbt.getInteger("GateMode");
        this.isLinked = nbt.getBoolean("IsLinked");
        this.linkedX = nbt.getInteger("LinkedX");
        this.linkedY = nbt.getInteger("LinkedY");
        this.linkedZ = nbt.getInteger("LinkedZ");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("GateMode", this.gateMode);
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
