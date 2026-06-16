package com.SakuraMochi17.kaisatsumod.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class TileEntityFareChart extends TileEntity {
    public String stationName = "未設定";

    public static class NodeData {
        public String name;
        public int fare;
        public String parent;
        public int depth;
        public String lineName;
        public boolean isLoop;
        public boolean isCutoff; // ★追加：運賃オーバーでカットされた「線の先」を示すフラグ

        public NodeData(String name, int fare, String parent, int depth, String lineName, boolean isLoop, boolean isCutoff) {
            this.name = name; this.fare = fare; this.parent = parent; this.depth = depth; this.lineName = lineName; this.isLoop = isLoop; this.isCutoff = isCutoff;
        }
    }

    public List<NodeData> nodeList = new ArrayList<>();

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.stationName = nbt.getString("stationName");
        this.nodeList.clear();
        if (nbt.hasKey("NodeList")) {
            NBTTagList list = (NBTTagList) nbt.getTag("NodeList");
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                this.nodeList.add(new NodeData(
                        tag.getString("Name"), tag.getInteger("Fare"),
                        tag.getString("Parent"), tag.getInteger("Depth"), tag.getString("Line"), tag.getBoolean("IsLoop"), tag.getBoolean("IsCutoff")
                ));
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (this.stationName != null) nbt.setString("stationName", this.stationName);
        NBTTagList list = new NBTTagList();
        for (NodeData data : this.nodeList) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Name", data.name);
            tag.setInteger("Fare", data.fare);
            tag.setString("Parent", data.parent);
            tag.setInteger("Depth", data.depth);
            tag.setString("Line", data.lineName);
            tag.setBoolean("IsLoop", data.isLoop);
            tag.setBoolean("IsCutoff", data.isCutoff); // ★追加
            list.appendTag(tag);
        }
        nbt.setTag("NodeList", list);
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
