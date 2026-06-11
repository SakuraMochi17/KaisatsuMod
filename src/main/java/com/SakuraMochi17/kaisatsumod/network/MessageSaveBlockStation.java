package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityCertificateMachine;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketGate; // ★ご自身の環境の改札機TE
// import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityCertificateMachine; // ★今後作る証明書発行機TE
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

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
            // ② 自動改札機の場合 (クラス名は環境に合わせて調整してください)
            else if (te instanceof TileEntityTicketGate) {
                ((TileEntityTicketGate) te).stationName = message.stationName; // ※もし変数名が違えば調整
                success = true;
            }
            // ③ 今後作る乗車駅証明書発行機の場合も、ここへ追加するだけで共通処理可能！
            else if (te instanceof TileEntityCertificateMachine) {
                ((TileEntityCertificateMachine) te).stationName = message.stationName;
                success = true;
            }

            // ④ 駅員端末（窓口精算機）の場合
            else if (te instanceof com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal) {
                ((com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal) te).stationName = message.stationName;
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
