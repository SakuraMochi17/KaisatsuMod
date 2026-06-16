package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkManager;
import com.SakuraMochi17.kaisatsumod.item.ItemCertificate;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.item.ItemMagicICCard;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class MessageStaffTerminalAdjust implements IMessage {
    public int x, y, z;

    public MessageStaffTerminalAdjust() {}
    public MessageStaffTerminalAdjust(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }

    @Override public void fromBytes(ByteBuf buf) { this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt(); }
    @Override public void toBytes(ByteBuf buf) { buf.writeInt(x); buf.writeInt(y); buf.writeInt(z); }

    public static class Handler implements IMessageHandler<MessageStaffTerminalAdjust, IMessage> {
        @Override
        public IMessage onMessage(MessageStaffTerminalAdjust message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);

            if (!(te instanceof TileEntityStaffTerminal)) return null;
            TileEntityStaffTerminal terminal = (TileEntityStaffTerminal) te;

            // 1. 証明書の確認と運賃計算
            ItemStack target = terminal.getStackInSlot(0);
            if (target == null || !(target.getItem() instanceof ItemCertificate) || target.stackTagCompound == null) return null;

            String entryStation = target.stackTagCompound.getString("issueStation");
            int rawFare = KaisatsuNetworkManager.calculateFare(player.worldObj, entryStation, terminal.stationName);

            // 経路エラーの場合は弾く
            if (rawFare < 0) return null;

            // ★修正：実際の引き落とし時も10円単位に切り上げる
            int fare = rawFare > 0 ? (int) Math.ceil(rawFare / 10.0) * 10 : 0;

            // 2. 投入資金の計算
            int cashTotal = 0;
            for (int i = 2; i <= 5; i++) {
                ItemStack c = terminal.getStackInSlot(i);
                if (c != null) cashTotal += KaisatsuModMain.getMoneyValue(c) * c.stackSize;
            }

            ItemStack payIc = terminal.getStackInSlot(1);
            boolean isMagic = payIc != null && payIc.getItem() instanceof ItemMagicICCard;
            int icBalance = 0;
            if (payIc != null && payIc.getItem() instanceof ItemICCard && !isMagic && payIc.stackTagCompound != null) {
                icBalance = payIc.stackTagCompound.getInteger("balance");
            }

            // 3. 残高チェック
            int totalAvailable = isMagic ? Integer.MAX_VALUE : (icBalance + cashTotal);
            if (totalAvailable < fare) return null; // お金が足りない場合は何もせず弾く

            // 4. 引き落とし処理（ICから優先）
            if (!isMagic) {
                int remainingFare = fare;

                // ICから引けるだけ引く
                if (icBalance >= remainingFare) {
                    icBalance -= remainingFare;
                    remainingFare = 0;
                } else {
                    remainingFare -= icBalance;
                    icBalance = 0;
                }
                if (payIc != null) payIc.stackTagCompound.setInteger("balance", icBalance);

                // 残りの運賃は現金から引き、釣銭を計算する
                int change = cashTotal - remainingFare;

                // 現金スロット(2~5)を全て空にする（回収）
                for (int i = 2; i <= 5; i++) terminal.setInventorySlotContents(i, null);

                // 釣銭の払い出し（スロット6~9へ）
                if (change > 0) {
                    int[] denoms = {10000, 5000, 1000, 500, 100, 50, 10, 5, 1};
                    int currentSlot = 6;

                    for (int d : denoms) {
                        if (change >= d) {
                            int amount = change / d;
                            change %= d;

                            while (amount > 0) {
                                int toGive = Math.min(amount, 64);
                                ItemStack moneyStack = KaisatsuModMain.getMoneyItemStack(d, toGive);

                                if (currentSlot <= 9) {
                                    terminal.setInventorySlotContents(currentSlot, moneyStack);
                                    currentSlot++;
                                } else {
                                    // 枠から溢れたお釣りは足元にドロップ
                                    player.worldObj.spawnEntityInWorld(new EntityItem(player.worldObj, message.x + 0.5, message.y + 1, message.z + 0.5, moneyStack));
                                }
                                amount -= toGive;
                            }
                        }
                    }
                }
            }

            // 5. 決済完了処理（証明書を消す ＆ 音を鳴らす）
            terminal.setInventorySlotContents(0, null); // 回収
            player.worldObj.playSoundEffect(message.x + 0.5, message.y + 0.5, message.z + 0.5, "random.orb", 1.0F, 1.0F);
            terminal.markDirty();

            return null;
        }
    }
}
