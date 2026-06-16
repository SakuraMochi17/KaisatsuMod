package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.tileentity.*; // ★追加
import com.SakuraMochi17.kaisatsumod.core.*; // ★追加
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
import java.util.List; // ★追加

public class MessageSaveBlockStation implements IMessage {
    public int x, y, z;
    public String stationName;

    public MessageSaveBlockStation() {}

    public MessageSaveBlockStation(int x, int y, int z, String stationName) {
        this.x = x; this.y = y; this.z = z;
        this.stationName = stationName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.stationName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.stationName);
    }

    public static class Handler implements IMessageHandler<MessageSaveBlockStation, IMessage> {
        @Override
        public IMessage onMessage(MessageSaveBlockStation message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te == null) return null;

            boolean success = false;

            // ① 券売機の場合
            if (te instanceof TileEntityTicketMachine) {
                ((TileEntityTicketMachine) te).stationName = message.stationName;
                success = true;
            }
            // ② 自動改札機の場合
            else if (te instanceof TileEntityTicketGate) {
                ((TileEntityTicketGate) te).stationName = message.stationName;
                success = true;
            }
            // ③ 乗車駅証明書発行機の場合
            else if (te instanceof TileEntityCertificateMachine) {
                ((TileEntityCertificateMachine) te).stationName = message.stationName;
                success = true;
            }
            // ④ 駅員端末（窓口精算機）の場合
            else if (te instanceof com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal) {
                ((com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal) te).stationName = message.stationName;
                success = true;
            }
            // ★⑤ 追加：運賃表（路線図モニター）の場合
            // ⑤ 運賃表（路線図モニター）の場合
            else if (te instanceof TileEntityFareChart) {
                TileEntityFareChart chartTE = (TileEntityFareChart) te;
                chartTE.stationName = message.stationName;

                int maxDepth = 10;
                List<TileEntityFareChart.NodeData> rawNodes = KaisatsuNetworkManager.buildRouteTree(world, message.stationName, maxDepth);

                chartTE.nodeList.clear();
                List<String> visibleStations = new ArrayList<>();

                // ★1. まず2000円以下の表示対象駅をすべてリストアップして登録
                for (TileEntityFareChart.NodeData node : rawNodes) {
                    if (node.fare <= 2000) {
                        visibleStations.add(node.name);
                        chartTE.nodeList.add(new TileEntityFareChart.NodeData(
                                node.name, node.fare, node.parent, node.depth, node.lineName, node.isLoop, false
                        ));
                    }
                }

                // ★2. 2000円を超えた駅で、親が表示されている場合は「線の先（Cutoff）」として登録
                // （※その先で路線が終わっている場合はそもそも探索されないため、自然と線は引かれません）
                for (TileEntityFareChart.NodeData node : rawNodes) {
                    if (node.fare > 2000 && visibleStations.contains(node.parent)) {
                        chartTE.nodeList.add(new TileEntityFareChart.NodeData(
                                node.name, node.fare, node.parent, node.depth, node.lineName, node.isLoop, true
                        ));
                    }
                }
                success = true;
            }


            if (success) {
                te.markDirty();
                world.markBlockForUpdate(message.x, message.y, message.z); // クライアントへ同期
                player.addChatMessage(new ChatComponentText("§a[設定ツール] ブロックの所属駅を「" + message.stationName + "」に設定しました。§r"));
            }

            return null;
        }
    }
}
