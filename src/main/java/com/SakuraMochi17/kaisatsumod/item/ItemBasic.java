package com.SakuraMochi17.kaisatsumod.item;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import net.minecraft.item.Item;

public class ItemBasic extends Item {
    // コンストラクタで名前を受け取るようにする
    public ItemBasic(String name) {
        super();
        this.setUnlocalizedName(name);
        this.setTextureName("yourmodid:" + name); // 名前がそのままテクスチャ名になる
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }
}
