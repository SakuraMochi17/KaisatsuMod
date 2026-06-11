package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStationManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class MessageStationUpdate implements IMessage {
    public int x, y, z;
    public String oldStationName;
    public String newStationName;

    public MessageStationUpdate() {}

    public MessageStationUpdate(int x, int y, int z, String oldName, String newName) {
        this.x = x; this.y = y; this.z = z;
        this.oldStationName = oldName != null ? oldName : "";
        this.newStationName = newName != null ? newName : "";
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.oldStationName = ByteBufUtils.readUTF8String(buf);
        this.newStationName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.oldStationName);
        ByteBufUtils.writeUTF8String(buf, this.newStationName);
    }

    // MessageStationUpdate.java の中にある Handler クラスの onMessage メソッドを修正

    // MessageStationUpdate.java の下部にある Handler クラスをこれで上書きします

    public static class Handler implements IMessageHandler<MessageStationUpdate, IMessage> {
        @Override
        public IMessage onMessage(MessageStationUpdate message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStationManager) {
                com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStationManager stationTE = (com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStationManager) te;

                // 変更前の古い駅名を退避
                String oldStationName = stationTE.stationName;

                // ★修正: message.stationName ではなく message.newStationName を使う！
                stationTE.stationName = message.newStationName;
                stationTE.markDirty();

                // グローバルネットワークデータへの自動座標登録処理
                com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData data = com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.get(world);
                if (data != null && message.newStationName != null && !message.newStationName.isEmpty()) {

                    // 1. もし駅名が変更されていたら、古い名前の駅座標データは削除する
                    if (oldStationName != null && !oldStationName.equals("未設定") && !oldStationName.equals(message.newStationName)) {
                        data.globalStations.remove(oldStationName);
                    }

                    // 2. 新しい駅名と、このブロックの座標(x, y, z)をセットで登録する！
                    data.globalStations.put(message.newStationName, new com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.StationCoords(message.x, message.y, message.z));
                    data.markDirty(); // セーブデータに書き込み

                    player.addChatMessage(new net.minecraft.util.ChatComponentText("§a[駅管理] 「" + message.newStationName + "」駅の座標をネットワークに登録しました。§r"));
                }
            }
            return null;
        }
    }
}
