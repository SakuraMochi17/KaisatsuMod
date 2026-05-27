package com.SakuraMochi17.kaisatsumod.tileentity;

import com.SakuraMochi17.kaisatsumod.core.StationRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityStationManager extends TileEntity {
    public String lineID = "line_yamanote";
    public String stationName = "東京";
    public String nextStation1 = "未設定"; // ★追加
    public String nextStation2 = "未設定"; // ★追加

    private boolean isInitialized = false;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.lineID = nbt.getString("LineID");
        this.stationName = nbt.getString("StationName");
        // ★追加
        this.nextStation1 = nbt.hasKey("Next1") ? nbt.getString("Next1") : "未設定";
        this.nextStation2 = nbt.hasKey("Next2") ? nbt.getString("Next2") : "未設定";
        this.isInitialized = false;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("LineID", this.lineID);
        nbt.setString("StationName", this.stationName);
        nbt.setString("Next1", this.nextStation1); // ★追加
        nbt.setString("Next2", this.nextStation2); // ★追加
    }

    @Override
    public void updateEntity() {
        if (this.worldObj != null && !this.worldObj.isRemote && !this.isInitialized) {
            int dimID = this.worldObj.provider.dimensionId;
            // ★修正: レジストリ登録時に隣接駅も渡す
            StationRegistry.registerStation(dimID, this.xCoord, this.yCoord, this.zCoord, this.lineID, this.stationName, this.nextStation1, this.nextStation2);
            this.isInitialized = true;
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

    // ★修正: パケットから情報を受け取るメソッドを拡張
    public void updateStationInfo(String lineID, String stationName, String next1, String next2) {
        this.lineID = lineID;
        this.stationName = stationName;
        this.nextStation1 = next1;
        this.nextStation2 = next2;
        this.markDirty();

        if (this.worldObj != null && !this.worldObj.isRemote) {
            int dimID = this.worldObj.provider.dimensionId;
            StationRegistry.registerStation(dimID, this.xCoord, this.yCoord, this.zCoord, lineID, stationName, next1, next2);
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public void invalidate() {
        // ★ここにあった removeStation の処理を削除！
        // チャンクがアンロードされただけで消えてしまうのを防ぐため、
        // 削除処理は BlockStationManager の breakBlock に任せます。
        super.invalidate();
    }
}
