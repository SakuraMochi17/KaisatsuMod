package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.item.ItemMagicICCard;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class MessagePurchaseTicket implements IMessage {
    public int x, y, z;
    public int fare;

    public MessagePurchaseTicket() {}
    public MessagePurchaseTicket(int x, int y, int z, int fare) {
        this.x = x; this.y = y; this.z = z; this.fare = fare;
    }

    @Override public void fromBytes(ByteBuf buf) { this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt(); this.fare = buf.readInt(); }
    @Override public void toBytes(ByteBuf buf) { buf.writeInt(x); buf.writeInt(y); buf.writeInt(z); buf.writeInt(fare); }

    public static class Handler implements IMessageHandler<MessagePurchaseTicket, IMessage> {
        @Override
        public IMessage onMessage(MessagePurchaseTicket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);

            if (!(te instanceof TileEntityTicketMachine)) return null;
            TileEntityTicketMachine terminal = (TileEntityTicketMachine) te;

            // 排出枠(スロット10)に既に切符がある場合は買えない
            if (terminal.getStackInSlot(10) != null) return null;

            // 入場券フラグと必要運賃の決定
            boolean isNyujoken = (message.fare == -1);
            int requiredFare = isNyujoken ? 150 : message.fare;

            // 1. 資金の計算 (現金: 0~8, IC: 9)
            int cashTotal = 0;
            for (int i = 0; i <= 8; i++) {
                ItemStack c = terminal.getStackInSlot(i);
                if (c != null) cashTotal += KaisatsuModMain.getMoneyValue(c) * c.stackSize;
            }

            ItemStack payIc = terminal.getStackInSlot(9);
            boolean isMagic = payIc != null && payIc.getItem() instanceof ItemMagicICCard;
            int icBalance = 0;
            if (payIc != null && payIc.getItem() instanceof ItemICCard && !isMagic && payIc.stackTagCompound != null) {
                icBalance = payIc.stackTagCompound.getInteger("balance");
            }

            // 2. 残高チェック
            int totalAvailable = isMagic ? Integer.MAX_VALUE : (icBalance + cashTotal);
            if (totalAvailable < requiredFare) return null; // お金が足りない

            // 3. 引き落としとお釣り計算
            if (!isMagic) {
                int remainingFare = requiredFare;
                if (icBalance >= remainingFare) {
                    icBalance -= remainingFare;
                    remainingFare = 0;
                } else {
                    remainingFare -= icBalance;
                    icBalance = 0;
                }
                if (payIc != null) payIc.stackTagCompound.setInteger("balance", icBalance);

                int change = cashTotal - remainingFare;

                // 現金回収
                for (int i = 0; i <= 8; i++) terminal.setInventorySlotContents(i, null);

                // 釣銭払い出し (スロット 11~14)
                if (change > 0) {
                    int[] denoms = {10000, 5000, 1000, 500, 100, 50, 10, 5, 1};
                    int currentSlot = 11;

                    for (int d : denoms) {
                        if (change >= d) {
                            int amount = change / d;
                            change %= d;
                            while (amount > 0) {
                                int toGive = Math.min(amount, 64);
                                ItemStack moneyStack = KaisatsuModMain.getMoneyItemStack(d, toGive);
                                if (currentSlot <= 14) {
                                    terminal.setInventorySlotContents(currentSlot, moneyStack);
                                    currentSlot++;
                                } else {
                                    player.worldObj.spawnEntityInWorld(new EntityItem(player.worldObj, message.x + 0.5, message.y + 1, message.z + 0.5, moneyStack));
                                }
                                amount -= toGive;
                            }
                        }
                    }
                }
            }

            // 4. 切符アイテムの生成
            ItemStack ticket = new ItemStack(KaisatsuModMain.ticket);
            ticket.setTagCompound(new NBTTagCompound());
            ticket.stackTagCompound.setInteger("fare", requiredFare);
            ticket.stackTagCompound.setString("entryStation", terminal.stationName);
            ticket.stackTagCompound.setBoolean("isUsed", false);
            ticket.stackTagCompound.setBoolean("isNyujoken", isNyujoken); // ★入場券フラグを記録

            terminal.setInventorySlotContents(10, ticket);
            player.worldObj.playSoundEffect(message.x + 0.5, message.y + 0.5, message.z + 0.5, "random.orb", 1.0F, 1.0F);
            terminal.markDirty();

            return null;
        }
    }
}