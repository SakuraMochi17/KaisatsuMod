package com.SakuraMochi17.kaisatsumod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mod(modid = "yourmodid", version = "1.0")
public class KaisatsuModMain {

    public static Item icCard;
    public static Block ticketGate;
    public static Block chargeMachine;

    public static Block oreAluminum;
    public static Item ingotAluminum;
    public static Item coin1, coin5, coin10, coin50, coin100, coin500;
    public static Item bill1000, bill2000, bill5000, bill10000;
    public static Item ticket;
    // --- ここに専用クリエイティブタブを定義 ---
    public static final CreativeTabs tabKaisatsu = new CreativeTabs("tabKaisatsu") {
        @Override
        public Item getTabIconItem() {
            // タブのアイコン（マーク）にするアイテムを指定します
            return icCard;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // インスタンス化
        icCard = new ItemICCard();
        ticketGate = new BlockTicketGate();
        chargeMachine = new BlockChargeMachine();

        // ゲームシステムへ登録
        GameRegistry.registerItem(icCard, "icCard");
        GameRegistry.registerBlock(ticketGate, "ticketGate");
        GameRegistry.registerBlock(chargeMachine, "chargeMachine");

        // --- フェーズ1：アイテムとブロックのインスタンス化 ---
        oreAluminum = new BlockAluminumOre();
        ingotAluminum = new ItemBasic("ingot_aluminum");
        ticket = new ItemBasic("ticket");

        coin1 = new ItemBasic("coin_1");
        coin5 = new ItemBasic("coin_5");
        coin10 = new ItemBasic("coin_10");
        coin50 = new ItemBasic("coin_50");
        coin100 = new ItemBasic("coin_100");
        coin500 = new ItemBasic("coin_500");

        bill1000 = new ItemBasic("bill_1000");
        bill2000 = new ItemBasic("bill_2000");
        bill5000 = new ItemBasic("bill_5000");
        bill10000 = new ItemBasic("bill_10000");

        // --- フェーズ1：ゲームシステムへの登録 ---
        GameRegistry.registerBlock(oreAluminum, "oreAluminum");
        GameRegistry.registerItem(ingotAluminum, "ingotAluminum");
        GameRegistry.registerItem(ticket, "ticket");
        GameRegistry.registerItem(coin1, "coin1");
        GameRegistry.registerItem(coin5, "coin5");
        GameRegistry.registerItem(coin10, "coin10");
        GameRegistry.registerItem(coin50, "coin50");
        GameRegistry.registerItem(coin100, "coin100");
        GameRegistry.registerItem(coin500, "coin500");
        GameRegistry.registerItem(bill1000, "bill1000");
        GameRegistry.registerItem(bill2000, "bill2000");
        GameRegistry.registerItem(bill5000, "bill5000");
        GameRegistry.registerItem(bill10000, "bill10000");
    }
    @EventHandler
    public void init(FMLInitializationEvent event) {
        // --- かまどレシピ（精錬） ---
        // 鉱石を焼くとインゴットが1つできる (0.5Fは経験値の量)
        GameRegistry.addSmelting(oreAluminum, new ItemStack(ingotAluminum), 0.5F);

        // --- 作業台レシピ（不定形レシピ＝どこに置いてもOK） ---
        // インゴット1つ → 1円玉64枚
        GameRegistry.addShapelessRecipe(new ItemStack(coin1, 64), ingotAluminum);

        // 逆両替（細かいお金 → 大きいお金）
        GameRegistry.addShapelessRecipe(new ItemStack(coin5, 1), coin1, coin1, coin1, coin1, coin1);
        GameRegistry.addShapelessRecipe(new ItemStack(coin10, 1), coin5, coin5);
        GameRegistry.addShapelessRecipe(new ItemStack(coin50, 1), coin10, coin10, coin10, coin10, coin10);
        GameRegistry.addShapelessRecipe(new ItemStack(coin100, 1), coin50, coin50);
        GameRegistry.addShapelessRecipe(new ItemStack(coin500, 1), coin100, coin100, coin100, coin100, coin100);
        GameRegistry.addShapelessRecipe(new ItemStack(bill1000, 1), coin500, coin500);
        GameRegistry.addShapelessRecipe(new ItemStack(bill2000, 1), bill1000, bill1000); // 2000円札！
        GameRegistry.addShapelessRecipe(new ItemStack(bill5000, 1), bill1000, bill1000, bill1000, bill1000, bill1000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill10000, 1), bill5000, bill5000);

        // 両替（大きいお金 → 細かいお金）
        GameRegistry.addShapelessRecipe(new ItemStack(coin1, 5), coin5);
        GameRegistry.addShapelessRecipe(new ItemStack(coin5, 2), coin10);
        GameRegistry.addShapelessRecipe(new ItemStack(coin10, 5), coin50);
        GameRegistry.addShapelessRecipe(new ItemStack(coin50, 2), coin100);
        GameRegistry.addShapelessRecipe(new ItemStack(coin100, 5), coin500);
        GameRegistry.addShapelessRecipe(new ItemStack(coin500, 2), bill1000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill1000, 2), bill2000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill1000, 5), bill5000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill5000, 2), bill10000);
        // ※ 5000円札を「1000円札5枚」にするなど、必要に応じて追加してください
    }
}