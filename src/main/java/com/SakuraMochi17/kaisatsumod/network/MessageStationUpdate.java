package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.core.StationRegistry;
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
    public String lineID;
    public String stationName;
    public String nextStation1; // ★追加
    public String nextStation2; // ★追加

    public MessageStationUpdate() {}

    // ★修正: コンストラクタに隣接駅を追加
    public MessageStationUpdate(int x, int y, int z, String lineID, String stationName, String next1, String next2) {
        this.x = x; this.y = y; this.z = z;
        this.lineID = lineID; this.stationName = stationName;
        this.nextStation1 = next1; this.nextStation2 = next2;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.lineID = ByteBufUtils.readUTF8String(buf);
        this.stationName = ByteBufUtils.readUTF8String(buf);
        this.nextStation1 = ByteBufUtils.readUTF8String(buf); // ★追加
        this.nextStation2 = ByteBufUtils.readUTF8String(buf); // ★追加
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.lineID);
        ByteBufUtils.writeUTF8String(buf, this.stationName);
        ByteBufUtils.writeUTF8String(buf, this.nextStation1 != null ? this.nextStation1 : "未設定");
        ByteBufUtils.writeUTF8String(buf, this.nextStation2 != null ? this.nextStation2 : "未設定");
    }

    public static class Handler implements IMessageHandler<MessageStationUpdate, IMessage> {
        @Override
        public IMessage onMessage(MessageStationUpdate message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            boolean isDuplicate = false;
            for (StationRegistry.StationData data : StationRegistry.registry.values()) {
                if (data.lineID.equals(message.lineID) && data.stationName.equals(message.stationName)) {
                    if (data.x != message.x || data.y != message.y || data.z != message.z) {
                        isDuplicate = true; break;
                    }
                }
            }

            if (isDuplicate) {
                player.addChatMessage(new ChatComponentText("§cエラー: その路線と駅名の組み合わせは既に別の場所に登録されています！"));
                world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                return null;
            }

            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityStationManager) {
                TileEntityStationManager manager = (TileEntityStationManager) te;
                // ★修正: 隣接駅も渡して更新する
                manager.updateStationInfo(message.lineID, message.stationName, message.nextStation1, message.nextStation2);

                player.addChatMessage(new ChatComponentText("§a駅を更新しました: " + message.stationName + " (隣接: " + message.nextStation1 + " / " + message.nextStation2 + ")"));
                world.playSoundAtEntity(player, "random.levelup", 1.0F, 1.0F);
            }
            return null;
        }
    }
}
