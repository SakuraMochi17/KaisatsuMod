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

    public static class Handler implements IMessageHandler<MessageStationUpdate, IMessage> {
        @Override
        public IMessage onMessage(MessageStationUpdate message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            KaisatsuNetworkData data = KaisatsuNetworkData.get(world);
            if (data == null) return null;

            // ★安全装置1: 名前の重複チェック（自分自身の座標以外で、既に同じ名前が存在するか）
            if (!message.newStationName.equals("未設定") && data.globalStations.containsKey(message.newStationName)) {
                KaisatsuNetworkData.StationCoords existingCoords = data.globalStations.get(message.newStationName);
                if (existingCoords.x != message.x || existingCoords.y != message.y || existingCoords.z != message.z) {
                    player.addChatMessage(new ChatComponentText("§cエラー: 「" + message.newStationName + "」は既に別の場所で登録されています！§r"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return null; // 登録処理を安全に中断
                }
            }

            // 名前が変わった場合の処理
            if (!message.oldStationName.equals("未設定") && !message.oldStationName.isEmpty() && !message.oldStationName.equals(message.newStationName)) {
                // 古い駅座標を消去
                data.globalStations.remove(message.oldStationName);

                // ★安全装置2: 路線リストの自動追従（リネーム処理）
                // 路線管理ブロック側のリストに古い駅名が残っている場合、新しい駅名に自動で書き換えて改札エラーを防ぐ
                for (KaisatsuNetworkData.LineData line : data.companyLines.values()) {
                    for (int i = 0; i < line.stationOrder.size(); i++) {
                        if (line.stationOrder.get(i).equals(message.oldStationName)) {
                            line.stationOrder.set(i, message.newStationName); // 新しい名前に差し替え
                        }
                    }
                }
            }

            // 新しい駅名と座標をグローバル辞書に登録
            if (!message.newStationName.equals("未設定") && !message.newStationName.isEmpty()) {
                data.globalStations.put(message.newStationName, new KaisatsuNetworkData.StationCoords(message.x, message.y, message.z));
            }

            data.markDirty(); // セーブデータへの変更を確定

            // TileEntityの同期
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityStationManager) {
                ((TileEntityStationManager) te).updateStationName(message.newStationName);
            }

            player.addChatMessage(new ChatComponentText("§a駅の登録・同期を完了しました: " + message.newStationName));
            world.playSoundAtEntity(player, "random.levelup", 1.0F, 1.0F);

            return null;
        }
    }
}
