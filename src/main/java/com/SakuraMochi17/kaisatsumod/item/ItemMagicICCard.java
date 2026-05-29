package com.SakuraMochi17.kaisatsumod.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMagicICCard extends ItemICCard {

    public ItemMagicICCard() {
        super(); // 親クラス(ItemICCard)の設定を引き継ぐ
        this.setUnlocalizedName("magicICCard");

        // ★修正: setTextureName を消すことで、自動的に普通のICカードと同じテクスチャになります
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add("§d【魔法のICカード (検証用)】§r");
        list.add("§7残高を消費せずに改札を通過できます§r");
        super.addInformation(stack, player, list, advanced); // 通常のICカードの表示（残高など）も下に表示
    }

    // ★追加: このアイテムがエンチャント特有の紫色の輝き（オーラ）を放つようにする魔法のメソッド
    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack, int pass) {
        return true;
    }
}
