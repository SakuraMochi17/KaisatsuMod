package com.SakuraMochi17.kaisatsumod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.List;

public class ItemTicket extends Item {

    public ItemTicket() {
        super();
        this.setUnlocalizedName("ticket");
        this.setTextureName("minecraft:paper"); // バニラの紙の見た目を流用
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        // NBTデータを持っている（発券済みである）かチェック
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("entryStation")) {
            String line = stack.stackTagCompound.getString("entryLine");
            String station = stack.stackTagCompound.getString("entryStation");
            boolean isNyujoken = stack.stackTagCompound.getBoolean("isNyujoken");

            String lineName = FareManager.getLineName(line);

            // ★入場券か乗車券かで色と表記を変える
            if (isNyujoken) {
                list.add("§b【入場券】§r"); // 水色
                list.add("発行: " + lineName + " " + station + "駅");
                list.add("§7※入場駅でのみ出場可能§r"); // 灰色
            } else {
                int fare = stack.stackTagCompound.getInteger("fare");
                list.add("§e【乗車券】§r"); // 黄色
                list.add("発行: " + lineName + " " + station + "駅");
                list.add("運賃: " + fare + "円区間");
            }
        } else {
            list.add("無効なきっぷ（未発券）");
        }
    }
}
