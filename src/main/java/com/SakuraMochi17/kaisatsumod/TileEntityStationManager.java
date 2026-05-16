package com.SakuraMochi17.kaisatsumod;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityStationManager extends TileEntity {
    public String lineID = "line_yamanote";
    public String stationName = "東京";

    // ★追加：ワールド読み込み後に一度だけレジストリに再登録するためのフラグ
    private boolean isInitialized = false;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.lineID = nbt.getString("LineID");
        this.stationName = nbt.getString("StationName");
        // ロード時はまだ初期化されていないのでフラグを下げる
        this.isInitialized = false;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("LineID", this.lineID);
        nbt.setString("StationName", this.stationName);
    }

    // ★重要：Minecraft 1.7.10が毎Tick呼び出すアップデート処理
    @Override
    public void updateEntity() {
        // サーバー側かつ、まだ初期化（再登録）が済んでいない場合
        if (this.worldObj != null && !this.worldObj.isRemote && !this.isInitialized) {
            int dimID = this.worldObj.provider.dimensionId;
            // メモリ上のレジストリに、ディスクから読み込んだ自分の情報を復活させる
            StationRegistry.registerStation(dimID, this.xCoord, this.yCoord, this.zCoord, this.lineID, this.stationName);

            // フラグを立てて、次回以降のTickでは処理をスキップさせる（負荷対策）
            this.isInitialized = true;
            System.out.println("[KaisatsuMod] 駅をレジストリに復元しました: " + this.stationName + " (" + this.xCoord + "," + this.zCoord + ")");
        }
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

    public void updateStationInfo(String lineID, String stationName) {
        this.lineID = lineID;
        this.stationName = stationName;
        this.markDirty();

        if (this.worldObj != null && !this.worldObj.isRemote) {
            int dimID = this.worldObj.provider.dimensionId;
            StationRegistry.registerStation(dimID, this.xCoord, this.yCoord, this.zCoord, lineID, stationName);
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public void invalidate() {
        if (this.worldObj != null && !this.worldObj.isRemote) {
            int dimID = this.worldObj.provider.dimensionId;
            StationRegistry.removeStation(dimID, this.xCoord, this.yCoord, this.zCoord);
        }
        super.invalidate();
    }
}
