package com.SakuraMochi17.kaisatsumod.item;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import net.minecraft.item.Item;

public class ItemSettingTool extends Item {
    public ItemSettingTool() {
        super();
        this.setUnlocalizedName("settingTool");
        this.setTextureName("minecraft:blaze_rod"); // 見た目はブレイズロッドなどを流用
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setMaxStackSize(1);
    }
}
