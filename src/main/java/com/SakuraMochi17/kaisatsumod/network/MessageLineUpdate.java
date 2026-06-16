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
    public int mode; // 0:会社名登録, 1:路線保存, 2:路線削除
    public int x, y, z;
    public String companyName;

    public String oldLineID, newLineID, lineName;
    public int baseFare;
    public double costPerBlock;
    public List<String> lineStations;

    public MessageLineUpdate() { lineStations = new ArrayList<>(); }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.mode = buf.readInt();
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.companyName = ByteBufUtils.readUTF8String(buf);

        if (this.mode == 1 || this.mode == 2) {
            this.oldLineID = ByteBufUtils.readUTF8String(buf);
            this.newLineID = ByteBufUtils.readUTF8String(buf);
            this.lineName = ByteBufUtils.readUTF8String(buf);
            this.baseFare = buf.readInt();
            this.costPerBlock = buf.readDouble();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) this.lineStations.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.mode);
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.companyName != null ? this.companyName : "");

        if (this.mode == 1 || this.mode == 2) {
            ByteBufUtils.writeUTF8String(buf, this.oldLineID != null ? this.oldLineID : "");
            ByteBufUtils.writeUTF8String(buf, this.newLineID != null ? this.newLineID : "");
            ByteBufUtils.writeUTF8String(buf, this.lineName != null ? this.lineName : "");
            buf.writeInt(this.baseFare);
            buf.writeDouble(this.costPerBlock);
            buf.writeInt(this.lineStations.size());
            for (String s : this.lineStations) ByteBufUtils.writeUTF8String(buf, s);
        }
    }

    public static class Handler implements IMessageHandler<MessageLineUpdate, IMessage> {
        @Override
        public IMessage onMessage(MessageLineUpdate message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            KaisatsuNetworkData data = KaisatsuNetworkData.get(world);

            if (data == null) return null;

            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityLineManager) {
                String oldCompany = ((TileEntityLineManager) te).companyName;
                ((TileEntityLineManager) te).companyName = message.companyName;
                te.markDirty();

                // 会社名が変更された場合、その会社の全路線の companyName も書き換える
                if (!oldCompany.equals(message.companyName) && !oldCompany.isEmpty()) {
                    for (KaisatsuNetworkData.LineData ld : data.companyLines.values()) {
                        if (oldCompany.equals(ld.companyName)) {
                            ld.companyName = message.companyName;
                        }
                    }
                    data.markDirty();
                    if (message.mode == 0) player.addChatMessage(new ChatComponentText("§a会社名を「" + message.companyName + "」に変更・更新しました。"));
                } else if (message.mode == 0) {
                    player.addChatMessage(new ChatComponentText("§a会社名「" + message.companyName + "」をブロックに登録しました。"));
                }
            }

            if (message.mode == 1) { // 路線保存
                if (message.newLineID.isEmpty()) return null;
                if (!message.oldLineID.isEmpty() && !message.oldLineID.equals(message.newLineID)) {
                    data.companyLines.remove(message.oldLineID);
                }
                KaisatsuNetworkData.LineData lineData = new KaisatsuNetworkData.LineData(
                        message.newLineID, message.lineName, message.companyName, message.baseFare, message.costPerBlock);
                lineData.stationOrder.addAll(message.lineStations);
                data.companyLines.put(message.newLineID, lineData);
                data.markDirty();
                player.addChatMessage(new ChatComponentText("§a路線データ「" + message.lineName + "」をネットワークに保存しました。"));
            }
            else if (message.mode == 2) { // 路線削除
                if (!message.oldLineID.isEmpty()) {
                    data.companyLines.remove(message.oldLineID);
                    data.markDirty();
                    player.addChatMessage(new ChatComponentText("§c路線データをネットワークから削除しました。"));
                }
            }

            return null;
        }
    }
}
