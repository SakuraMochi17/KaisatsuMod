package com.SakuraMochi17.kaisatsumod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import java.util.Random;

public class BlockTicketGate extends Block {
    public BlockTicketGate() {
        super(Material.iron);
        this.setBlockName("ticketGate");
        this.setBlockTextureName("yourmodid:ticket_gate");
        this.setHardness(3.0F);
        this.setCreativeTab(CreativeTabs.tabTransport);
    }

    // 当たり判定の計算（フェンスのように飛び越えられなくする）
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        // メタデータが1（開状態）なら当たり判定を無くす（通れるようになる）
        if (meta == 1) {
            return null;
        }
        // 通常（閉状態）は高さを1.5にして飛び越えを防止
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1.5, z + 1);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getCurrentEquippedItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemICCard) {
            if (!world.isRemote) {
                // NBT初期化
                if (heldItem.stackTagCompound == null) {
                    heldItem.setTagCompound(new NBTTagCompound());
                    heldItem.stackTagCompound.setInteger("balance", 1000);
                }

                int balance = heldItem.stackTagCompound.getInteger("balance");
                int fare = 200;

                if (balance >= fare) {
                    // 残高を減らす
                    int newBalance = balance - fare;
                    heldItem.stackTagCompound.setInteger("balance", newBalance);

                    // 【修正箇所】お知らせメッセージを復活
                    player.addChatMessage(new ChatComponentText("ピッ！ 通行許可 (残高: " + newBalance + "円)"));
                    world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);

                    // 改札を開く（メタデータを1にする）
                    world.setBlockMetadataWithNotify(x, y, z, 1, 3);
                    // 3秒後（60tick後）に閉じるように予約
                    world.scheduleBlockUpdate(x, y, z, this, 60);
                } else {
                    player.addChatMessage(new ChatComponentText("残高不足！ (現在の残高: " + balance + "円)"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                }
            }
            return true;
        }
        return false;
    }

    // 予約された時間（3秒後）に実行される処理
    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote) {
            // 改札を閉じる（メタデータを0に戻す）
            world.setBlockMetadataWithNotify(x, y, z, 0, 3);
        }
    }
}
