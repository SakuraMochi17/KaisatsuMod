package com.SakuraMochi17.kaisatsumod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import java.util.List;

public class ItemICCard extends Item {

    public ItemICCard() {
        super();
        this.setUnlocalizedName("icCard"); // 内部名
        this.setTextureName("yourmodid:ic_card"); // テクスチャのパス
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu); // クリエイティブタブ
        this.setMaxStackSize(1); // 1枚しか持てないようにする
    }

    // クラスの「中」にメソッドを配置します
    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        super.onCreated(stack, world, player);
        // アイテムが作成された時にNBTを初期化
        initNBT(stack);
    }

    // NBTを初期化するメソッド（独自定義）
    private void initNBT(ItemStack stack) {
        if (stack.stackTagCompound == null) {
            stack.setTagCompound(new NBTTagCompound());
            stack.stackTagCompound.setInteger("balance", 1000); // 初期残高を1000円に設定
        }
    }

    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("balance")) {
            int balance = stack.stackTagCompound.getInteger("balance");
            list.add("残高: " + balance + "円");
        } else {
            list.add("残高: 未設定");
        }
    }
} // <--- クラスの最後は必ずこの閉じ括弧で終わります