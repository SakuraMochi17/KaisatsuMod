package com.SakuraMochi17.kaisatsumod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class BlockChargeMachine extends Block {
    public BlockChargeMachine() {
        super(Material.iron);
        this.setBlockName("chargeMachine");
        this.setBlockTextureName("yourmodid:charge_machine");
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getCurrentEquippedItem();

        if (heldItem != null && heldItem.getItem() instanceof ItemICCard) {
            if (!world.isRemote) {

                // NBTデータが存在しない場合は初期化
                if (heldItem.stackTagCompound == null) {
                    heldItem.setTagCompound(new NBTTagCompound());
                    heldItem.stackTagCompound.setInteger("balance", 0);
                }

                int currentBalance = heldItem.stackTagCompound.getInteger("balance");
                int chargeAmount = 2000;
                int maxBalance = 20000; // チャージ上限（2万円）

                // 上限チェック
                if (currentBalance + chargeAmount > maxBalance) {
                    player.addChatMessage(new ChatComponentText("これ以上チャージできません！（チャージ上限: " + maxBalance + "円）"));
                } else {
                    // モード問わず金インゴットを消費する
                    if (player.inventory.consumeInventoryItem(Items.gold_ingot)) {

                        // 残高を更新
                        int newBalance = currentBalance + chargeAmount;
                        heldItem.stackTagCompound.setInteger("balance", newBalance);

                        player.addChatMessage(new ChatComponentText(chargeAmount + "円チャージしました！ (現在: " + newBalance + "円)"));
                        world.playSoundAtEntity(player, "random.orb", 1.0F, 0.5F);

                        // クライアントのインベントリ表示を強制更新（見た目のズレ防止）
                        player.inventoryContainer.detectAndSendChanges();

                    } else {
                        player.addChatMessage(new ChatComponentText("チャージには金インゴットがインベントリに必要です。"));
                    }
                }
            }
            return true;
        }
        return false;
    }
}
