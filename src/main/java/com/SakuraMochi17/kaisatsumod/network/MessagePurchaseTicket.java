package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.core.StationRegistry;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MessagePurchaseTicket implements IMessage {
    private int x, y, z;
    private int fare;
    private boolean isNyujoken;

    public MessagePurchaseTicket() {}

    public MessagePurchaseTicket(int x, int y, int z, int fare, boolean isNyujoken) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fare = fare;
        this.isNyujoken = isNyujoken;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.fare = buf.readInt();
        this.isNyujoken = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.fare);
        buf.writeBoolean(this.isNyujoken);
    }

    public static class Handler implements IMessageHandler<MessagePurchaseTicket, IMessage> {
        @Override
        public IMessage onMessage(MessagePurchaseTicket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);

            if (te instanceof TileEntityTicketMachine) {
                TileEntityTicketMachine machine = (TileEntityTicketMachine) te;

                if (!machine.isLinked) return null;

                int dimID = world.provider.dimensionId;
                StationRegistry.StationData currentStation = StationRegistry.findNearestStation(dimID, machine.linkedX, machine.linkedY, machine.linkedZ, 1.0);

                if (currentStation == null) return null;

                int requiredAmount = message.isNyujoken ? 150 : message.fare;

                // 1. 現金の合計を計算 (スロット1〜9)
                int totalInserted = 0;
                for (int i = 1; i <= 9; i++) {
                    ItemStack slotItem = machine.getStackInSlot(i);
                    if (slotItem != null) {
                        totalInserted += KaisatsuModMain.getMoneyValue(slotItem) * slotItem.stackSize;
                    }
                }

                // 2. ICカードの残高を確認 (スロット0)
                ItemStack icCardStack = machine.getStackInSlot(0);
                boolean hasICCard = icCardStack != null && icCardStack.getItem() instanceof ItemICCard;
                int icBalance = 0;
                if (hasICCard) {
                    if (icCardStack.stackTagCompound == null) {
                        icCardStack.setTagCompound(new NBTTagCompound());
                        icCardStack.stackTagCompound.setInteger("balance", 0);
                    }
                    icBalance = icCardStack.stackTagCompound.getInteger("balance");
                }

                // 排出口(スロット10)が空いているかチェック
                if (machine.getStackInSlot(10) != null) return null;

                boolean paymentSuccess = false;

                // 3. 決済ロジック（現金優先、足りなければICカード）
                if (totalInserted >= requiredAmount) {
                    // 現金で決済
                    // 現金で決済
                    for (int i = 1; i <= 9; i++) {
                        machine.setInventorySlotContents(i, null);
                    }
                    int change = totalInserted - requiredAmount;
                    if (change > 0) {
                        // ★修正：お釣りとして、RTMのお金（未導入なら自作のお金）を返すように変更
                        if (change >= 1000) { machine.setInventorySlotContents(11, KaisatsuModMain.getMoneyItemStack(1000, change / 1000)); change %= 1000; }
                        if (change >= 100) { machine.setInventorySlotContents(12, KaisatsuModMain.getMoneyItemStack(100, change / 100)); change %= 100; }
                        if (change >= 10) { machine.setInventorySlotContents(13, KaisatsuModMain.getMoneyItemStack(10, change / 10)); change %= 10; }
                        if (change >= 1) { machine.setInventorySlotContents(14, KaisatsuModMain.getMoneyItemStack(1, change)); }
                    }
                    paymentSuccess = true;
                } else if (hasICCard && icBalance >= requiredAmount) {
                    // ICカードで決済
                    icCardStack.stackTagCompound.setInteger("balance", icBalance - requiredAmount);
                    paymentSuccess = true;
                }

                if (paymentSuccess) {
                    // 発券処理
                    ItemStack ticketStack = new ItemStack(KaisatsuModMain.ticket);
                    ticketStack.setTagCompound(new NBTTagCompound());
                    ticketStack.stackTagCompound.setString("entryLine", currentStation.lineID);
                    ticketStack.stackTagCompound.setString("entryStation", currentStation.stationName);
                    ticketStack.stackTagCompound.setInteger("fare", requiredAmount);
                    ticketStack.stackTagCompound.setBoolean("isUsed", false);
                    ticketStack.stackTagCompound.setBoolean("isNyujoken", message.isNyujoken);
                    machine.setInventorySlotContents(10, ticketStack);

                    world.playSoundEffect(message.x, message.y, message.z, "random.click", 1.0F, 1.2F);
                    machine.markDirty();
                }
            }
            return null;
        }
    }
}
