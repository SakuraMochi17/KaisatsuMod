package com.SakuraMochi17.kaisatsumod.network;

import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class MessageStaffTerminal implements IMessage {
    private int x, y, z;
    private int amount;

    public MessageStaffTerminal() {}

    public MessageStaffTerminal(int x, int y, int z, int amount) {
        this.x = x; this.y = y; this.z = z;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt(); this.y = buf.readInt(); this.z = buf.readInt();
        this.amount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x); buf.writeInt(this.y); buf.writeInt(this.z);
        buf.writeInt(this.amount);
    }

    public static class Handler implements IMessageHandler<MessageStaffTerminal, IMessage> {
        @Override
        public IMessage onMessage(MessageStaffTerminal message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);

            if (te instanceof TileEntityStaffTerminal) {
                TileEntityStaffTerminal terminal = (TileEntityStaffTerminal) te;
                ItemStack inputSlot = terminal.getStackInSlot(0); // 左スロット

                if (inputSlot != null && inputSlot.getItem() instanceof ItemICCard) {
                    // 右スロットが空いているかチェック
                    if (terminal.getStackInSlot(1) != null) {
                        player.addChatMessage(new ChatComponentText("§cエラー: 排出スロットにカードが残っています。"));
                        return null;
                    }

                    if (!inputSlot.hasTagCompound()) return null;

                    int currentBalance = inputSlot.stackTagCompound.getInteger("balance");
                    if (currentBalance < message.amount) {
                        player.addChatMessage(new ChatComponentText("§cエラー: ICカードの残高が不足しています。(残高: " + currentBalance + "円)"));
                        world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                        return null;
                    }

                    // 精算処理（引き落とし ＆ 入場記録の強制消去）
                    inputSlot.stackTagCompound.setInteger("balance", currentBalance - message.amount);
                    inputSlot.stackTagCompound.setBoolean("inGate", false);

                    // 右スロットへ移動
                    terminal.setInventorySlotContents(1, inputSlot);
                    terminal.setInventorySlotContents(0, null);

                    player.addChatMessage(new ChatComponentText("§a[窓口端末] " + message.amount + "円の精算と入場記録の消去が完了しました。"));
                    world.playSoundEffect(message.x, message.y, message.z, "random.levelup", 1.0F, 2.0F);
                    terminal.markDirty();
                }
            }
            return null;
        }
    }
}
