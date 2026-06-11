package com.SakuraMochi17.kaisatsumod.item;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
// ★追加
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.List;

public class ItemTicket extends Item {

    public ItemTicket() {
        super();
        this.setUnlocalizedName("ticket");
        this.setTextureName("minecraft:paper");
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setMaxStackSize(1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (stack.stackTagCompound != null) {
            tooltip.add("【 乗車券 】");

            String entryStation = stack.stackTagCompound.getString("entryStation");
            if (entryStation != null && !entryStation.isEmpty()) {
                // ★修正：「不明な路線」等の路線名の取得・表示を完全に削除し、駅名だけにします
                tooltip.add("発行: " + entryStation + "駅");
            }

            if (stack.stackTagCompound.getBoolean("isNyujoken")) {
                tooltip.add("§b入場券 (150円)§r");
            } else {
                tooltip.add("運賃: " + stack.stackTagCompound.getInteger("fare") + "円区間");
            }

            if (stack.stackTagCompound.getBoolean("isUsed")) {
                tooltip.add("§c[ 入場済み ]§r");
            }
        }
    }
}
