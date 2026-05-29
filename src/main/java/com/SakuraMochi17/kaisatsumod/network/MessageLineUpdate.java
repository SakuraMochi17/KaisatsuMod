package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityLineManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MessageLineUpdate implements IMessage {
    public int x, y, z;
    public String oldLineID, newLineID, lineName, companyName;
    public int baseFare;
    public double costPerBlock;
    public List<String> lineStations;

    public MessageLineUpdate() { lineStations = new ArrayList<>(); }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.oldLineID = ByteBufUtils.readUTF8String(buf);
        this.newLineID = ByteBufUtils.readUTF8String(buf);
        this.lineName = ByteBufUtils.readUTF8String(buf);
        this.companyName = ByteBufUtils.readUTF8String(buf);
        this.baseFare = buf.readInt();
        this.costPerBlock = buf.readDouble();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) this.lineStations.add(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.oldLineID != null ? this.oldLineID : "");
        ByteBufUtils.writeUTF8String(buf, this.newLineID != null ? this.newLineID : "");
        ByteBufUtils.writeUTF8String(buf, this.lineName != null ? this.lineName : "");
        ByteBufUtils.writeUTF8String(buf, this.companyName != null ? this.companyName : "");
        buf.writeInt(this.baseFare);
        buf.writeDouble(this.costPerBlock);
        buf.writeInt(this.lineStations.size());
        for (String s : this.lineStations) ByteBufUtils.writeUTF8String(buf, s);
    }

    public static class Handler implements IMessageHandler<MessageLineUpdate, IMessage> {
        @Override
        public IMessage onMessage(MessageLineUpdate message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            KaisatsuNetworkData data = KaisatsuNetworkData.get(world);

            if (data == null || message.newLineID.isEmpty()) return null;

            // IDが変わった場合は古いデータを削除
            if (!message.oldLineID.isEmpty() && !message.oldLineID.equals(message.newLineID)) {
                data.companyLines.remove(message.oldLineID);
            }

            // 新しいデータを登録
            KaisatsuNetworkData.LineData lineData = new KaisatsuNetworkData.LineData(message.newLineID, message.lineName, message.companyName, message.baseFare, message.costPerBlock);
            lineData.stationOrder.addAll(message.lineStations);
            data.companyLines.put(message.newLineID, lineData);
            data.markDirty(); // セーブデータに書き込み

            // ブロック側のIDも更新
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityLineManager) {
                ((TileEntityLineManager) te).lineID = message.newLineID;
                te.markDirty();
            }

            player.addChatMessage(new ChatComponentText("§a路線データをネットワークに登録しました: " + message.lineName));
            return null;
        }
    }
}
