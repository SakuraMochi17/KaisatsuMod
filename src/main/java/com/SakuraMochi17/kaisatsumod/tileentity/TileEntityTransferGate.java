package com.SakuraMochi17.kaisatsumod.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTransferGate extends TileEntity {
    // --- 出場用の連携データ（リンク1：旧リンクワンド用） ---
    public boolean isLinked1 = false;
    public int linked1X;
    public int linked1Y;
    public int linked1Z;

    // --- 入場用の連携データ（リンク2：旧リンクワンド用） ---
    public boolean isLinked2 = false;
    public int linked2X;
    public int linked2Y;
    public int linked2Z;

    // ===============================================
    // ★追加：設定ツール用の新しい駅名データ
    // ===============================================
    public String exitStationName = "未設定";  // 乗り換え元（出場する駅・A線）
    public String entryStationName = "未設定"; // 乗り換え先（入場する駅・B線）

    // 連携リセット用メソッド
    public void resetLinks() {
        this.isLinked1 = false;
        this.isLinked2 = false;
        this.exitStationName = "未設定";
        this.entryStationName = "未設定";
        this.markDirty();
        if (this.worldObj != null) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
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

        // ★追加：駅名の読み出し
        if (nbt.hasKey("exitStationName")) {
            this.exitStationName = nbt.getString("exitStationName");
        }
        if (nbt.hasKey("entryStationName")) {
            this.entryStationName = nbt.getString("entryStationName");
        }
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

        // ★追加：駅名の保存
        nbt.setString("exitStationName", this.exitStationName);
        nbt.setString("entryStationName", this.entryStationName);
    }

    // ===============================================
    // ★追加：サーバーとクライアント(画面)のデータ同期処理
    // ===============================================
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
