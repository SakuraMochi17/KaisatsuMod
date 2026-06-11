package com.SakuraMochi17.kaisatsumod.item;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemCertificate extends Item {
    public ItemCertificate() {
        super();
        this.setUnlocalizedName("boardingCertificate");
        this.setTextureName("minecraft:paper"); // 見た目はとりあえず紙にしておきます
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setMaxStackSize(1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("issueStation")) {
            tooltip.add("§7乗車駅: §f" + stack.stackTagCompound.getString("issueStation"));
            tooltip.add("§c※降車駅で精算してください§r");
        } else {
            tooltip.add("§7乗車駅: 不明");
        }
    }
}
