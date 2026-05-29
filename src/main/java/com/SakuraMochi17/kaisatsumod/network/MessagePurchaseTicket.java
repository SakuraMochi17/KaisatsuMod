package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.item.ItemTicket;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

import java.nio.charset.StandardCharsets;

public class MessagePurchaseTicket implements IMessage {
    public int x, y, z;
    public int fare;
    public String stationName;

    public MessagePurchaseTicket() {}

    public MessagePurchaseTicket(int x, int y, int z, int fare, String stationName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fare = fare;
        this.stationName = stationName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.fare = buf.readInt();
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        this.stationName = new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.fare);
        byte[] bytes = this.stationName.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static class Handler implements IMessageHandler<MessagePurchaseTicket, IMessage> {
        @Override
        public IMessage onMessage(MessagePurchaseTicket message, MessageContext ctx) {
            // ★修正：1.7.10の仕様に合わせて直接処理を書く
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);

            if (te instanceof TileEntityTicketMachine) {
                TileEntityTicketMachine machine = (TileEntityTicketMachine) te;

                // TODO: お金の消費処理は後でここに書きます

                // お金が足りていると仮定して、切符を生成
                ItemStack ticketStack = new ItemStack(com.SakuraMochi17.kaisatsumod.KaisatsuModMain.ticket);
                ticketStack.setTagCompound(new NBTTagCompound());
                ticketStack.stackTagCompound.setInteger("fare", message.fare);
                ticketStack.stackTagCompound.setString("buyStation", message.stationName);
                ticketStack.stackTagCompound.setBoolean("used", false);

                // プレイヤーのインベントリに直接突っ込む（一杯なら足元にドロップ）
                if (!player.inventory.addItemStackToInventory(ticketStack)) {
                    player.dropPlayerItemWithRandomChoice(ticketStack, false);
                }

                player.addChatMessage(new ChatComponentText("§a券売機: " + message.stationName + "からの " + message.fare + "円区間きっぷを発行しました。§r"));
                player.worldObj.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
            }
            return null; // 必ずnullを返す
        }
    }
}