package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTransferGate;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

public class MessageSaveTransferStation implements IMessage {
    public int x, y, z;
    public String exitStation, entryStation;

    public MessageSaveTransferStation() {}

    public MessageSaveTransferStation(int x, int y, int z, String ex, String en) {
        this.x = x; this.y = y; this.z = z;
        this.exitStation = ex; this.entryStation = en;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.exitStation = ByteBufUtils.readUTF8String(buf);
        this.entryStation = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buf, this.exitStation);
        ByteBufUtils.writeUTF8String(buf, this.entryStation);
    }

    public static class Handler implements IMessageHandler<MessageSaveTransferStation, IMessage> {
        @Override
        public IMessage onMessage(MessageSaveTransferStation message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);

            if (te instanceof TileEntityTransferGate) {
                TileEntityTransferGate gate = (TileEntityTransferGate) te;
                gate.exitStationName = message.exitStation;
                gate.entryStationName = message.entryStation;
                gate.markDirty();
                player.worldObj.markBlockForUpdate(message.x, message.y, message.z);
                player.addChatMessage(new ChatComponentText("§a[設定ツール] 乗り換え設定を更新しました。（元: " + message.exitStation + " ➔ 先: " + message.entryStation + "）§r"));
            }
            return null;
        }
    }
}
