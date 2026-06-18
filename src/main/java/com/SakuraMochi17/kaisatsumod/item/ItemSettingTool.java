package com.SakuraMochi17.kaisatsumod.item;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemSettingTool extends Item {
    public ItemSettingTool() {
        super();
        this.setUnlocalizedName("settingTool");
        this.setTextureName("minecraft:blaze_rod");
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setMaxStackSize(1);
    }

    // ★追加: スニーク中（しゃがみ中）でもブロックの右クリック処理を実行させる魔法のメソッド
    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
        return true;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("copiedStation")) {
            String stationName = stack.getTagCompound().getString("copiedStation");
            if (stationName != null && !stationName.isEmpty()) {
                tooltip.add("§bコピー中の駅: " + stationName);
            } else {
                tooltip.add("§7コピー中の駅: なし");
            }
        } else {
            tooltip.add("§7コピー中の駅: なし");
        }
    }
}
