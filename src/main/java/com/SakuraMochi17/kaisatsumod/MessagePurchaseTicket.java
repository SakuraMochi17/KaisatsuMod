package com.SakuraMochi17.kaisatsumod;

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

                // ★修正：周辺検索を廃止し、券売機自体が記憶している連携座標から駅情報をピンポイント取得
                if (!machine.isLinked) return null;

                int dimID = world.provider.dimensionId;
                StationRegistry.StationData currentStation = StationRegistry.findNearestStation(dimID, machine.linkedX, machine.linkedY, machine.linkedZ, 1.0);

                if (currentStation == null) return null;

                // 1. 投入金額の合計を計算
                int totalInserted = 0;
                for (int i = 1; i <= 9; i++) {
                    ItemStack slotItem = machine.getStackInSlot(i);
                    if (slotItem != null) {
                        totalInserted += KaisatsuModMain.getMoneyValue(slotItem) * slotItem.stackSize;
                    }
                }

                int requiredAmount = message.isNyujoken ? 150 : message.fare;

                if (totalInserted >= requiredAmount && machine.getStackInSlot(10) == null) {
                    for (int i = 1; i <= 9; i++) {
                        machine.setInventorySlotContents(i, null);
                    }

                    // 2. 切符に駅データをスタンプして発券
                    ItemStack ticketStack = new ItemStack(KaisatsuModMain.ticket);
                    ticketStack.setTagCompound(new NBTTagCompound());
                    ticketStack.stackTagCompound.setString("entryLine", currentStation.lineID);
                    ticketStack.stackTagCompound.setString("entryStation", currentStation.stationName);
                    ticketStack.stackTagCompound.setInteger("fare", requiredAmount);
                    ticketStack.stackTagCompound.setBoolean("isUsed", false);
                    ticketStack.stackTagCompound.setBoolean("isNyujoken", message.isNyujoken);
                    machine.setInventorySlotContents(10, ticketStack);

                    // 3. お釣りの自動払い戻し
                    int change = totalInserted - requiredAmount;
                    if (change > 0) {
                        if (change >= 1000) {
                            machine.setInventorySlotContents(11, new ItemStack(KaisatsuModMain.bill1000, change / 1000));
                            change %= 1000;
                        }
                        if (change >= 100) {
                            machine.setInventorySlotContents(12, new ItemStack(KaisatsuModMain.coin100, change / 100));
                            change %= 100;
                        }
                        if (change >= 10) {
                            machine.setInventorySlotContents(13, new ItemStack(KaisatsuModMain.coin10, change / 10));
                            change %= 10;
                        }
                        if (change >= 1) {
                            machine.setInventorySlotContents(14, new ItemStack(KaisatsuModMain.coin1, change));
                        }
                    }

                    world.playSoundEffect(message.x, message.y, message.z, "random.click", 1.0F, 1.2F);
                    machine.markDirty();
                }
            }
            return null;
        }
    }
}
