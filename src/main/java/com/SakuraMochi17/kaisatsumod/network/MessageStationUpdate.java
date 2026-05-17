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

    public MessageStationUpdate() {}

    public MessageStationUpdate(int x, int y, int z, String lineID, String stationName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.lineID = lineID;
        this.stationName = stationName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.lineID = ByteBufUtils.readUTF8String(buf);
        this.stationName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.lineID);
        ByteBufUtils.writeUTF8String(buf, this.stationName);
    }

    public static class Handler implements IMessageHandler<MessageStationUpdate, IMessage> {
        @Override
        public IMessage onMessage(MessageStationUpdate message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            // ★追加: 重複登録チェックロジック
            boolean isDuplicate = false;
            for (StationRegistry.StationData data : StationRegistry.registry.values()) {
                if (data.lineID.equals(message.lineID) && data.stationName.equals(message.stationName)) {
                    // 自分自身の座標"以外"で、同じデータが見つかったら重複とみなす
                    if (data.x != message.x || data.y != message.y || data.z != message.z) {
                        isDuplicate = true;
                        break;
                    }
                }
            }

            // 重複していた場合はエラーメッセージを出し、登録処理をキャンセルする
            if (isDuplicate) {
                player.addChatMessage(new ChatComponentText("§cエラー: その路線と駅名の組み合わせ（" + message.stationName + "）は既に別の場所に登録されています！"));
                world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                return null;
            }

            // 重複がなければ正常に登録・更新
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityStationManager) {
                TileEntityStationManager manager = (TileEntityStationManager) te;
                manager.lineID = message.lineID;
                manager.stationName = message.stationName;
                manager.markDirty();

                int dimID = world.provider.dimensionId;
                StationRegistry.registerStation(dimID, message.x, message.y, message.z, message.lineID, message.stationName);

                player.addChatMessage(new ChatComponentText("§a駅管理ブロックを更新しました: " + message.stationName));
                world.playSoundAtEntity(player, "random.levelup", 1.0F, 1.0F);
            }
            return null;
        }
    }
}
