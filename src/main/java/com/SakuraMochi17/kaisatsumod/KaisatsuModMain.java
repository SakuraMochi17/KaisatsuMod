package com.SakuraMochi17.kaisatsumod;

import com.SakuraMochi17.kaisatsumod.block.*;
import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.gui.*;
import com.SakuraMochi17.kaisatsumod.item.*;
import com.SakuraMochi17.kaisatsumod.network.*;
import com.SakuraMochi17.kaisatsumod.tileentity.*;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.SakuraMochi17.kaisatsumod.proxy.CommonProxy;
import cpw.mods.fml.common.SidedProxy;

@Mod(modid = "kaisatsumod", name = "Kaisatsu Mod", version = "1.0")
public class KaisatsuModMain {

    @Mod.Instance("kaisatsumod")
    public static KaisatsuModMain instance;

    // ==========================================
    // ★追加1: Forgeにプロキシクラスの場所を教える
    // ==========================================
    @SidedProxy(
            clientSide = "com.SakuraMochi17.kaisatsumod.proxy.ClientProxy",
            serverSide = "com.SakuraMochi17.kaisatsumod.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper network;

    // === ブロック ===
    public static Block transferGate;
    public static Block ticketGate;
    public static Block chargeMachine;
    public static Block ticketMachine;
    public static Block staffTerminal;
    public static Block stationManager;
    public static Block lineManager;
    public static Block oreAluminum;
    public static Block certificateMachine;


    // === アイテム ===
    public static Item icCard;
    public static Item ticket;
    public static Item settingTool;
    public static Item ingotAluminum;
    public static Item magicIcCard;
    public static Item certificate;

    // === 通貨アイテム ===
    public static Item coin1, coin5, coin10, coin50, coin100, coin500;
    public static Item bill1000, bill2000, bill5000, bill10000;

    // === RTM連携キャッシュ用フラグ ===
    private static Item cachedRtmMoney = null;
    private static boolean searchedRtm = false;

    // === クリエイティブタブの設定 ===
    public static final CreativeTabs tabKaisatsu = new CreativeTabs("tabKaisatsu") {
        @Override
        public Item getTabIconItem() {
            return icCard;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;

        // 1. インスタンスの生成（ブロック）
        transferGate   = new BlockTransferGate();
        staffTerminal  = new BlockStaffTerminal();
        ticketMachine  = new BlockTicketMachine();
        stationManager = new BlockStationManager();
        ticketGate     = new BlockTicketGate();
        chargeMachine  = new BlockChargeMachine();
        oreAluminum    = new BlockAluminumOre();
        lineManager  = new BlockLineManager();
        certificateMachine = new BlockCertificateMachine();

        // 2. インスタンスの生成（アイテム）
        ticket         = new ItemTicket();
        icCard         = new ItemICCard();
        settingTool    = new ItemSettingTool();
        ingotAluminum  = new ItemBasic("ingot_aluminum");
        magicIcCard    = new ItemMagicICCard();
        certificate      = new ItemCertificate();

        coin1          = new ItemBasic("coin_1");
        coin5          = new ItemBasic("coin_5");
        coin10         = new ItemBasic("coin_10");
        coin50         = new ItemBasic("coin_50");
        coin100        = new ItemBasic("coin_100");
        coin500        = new ItemBasic("coin_500");
        bill1000       = new ItemBasic("bill_1000");
        bill2000       = new ItemBasic("bill_2000");
        bill5000       = new ItemBasic("bill_5000");
        bill10000      = new ItemBasic("bill_10000");

        // 3. ブロックの登録
        GameRegistry.registerBlock(transferGate, "transferGate");
        GameRegistry.registerBlock(staffTerminal, "staffTerminal");
        GameRegistry.registerBlock(ticketMachine, "ticketMachine");
        GameRegistry.registerBlock(stationManager, "stationManager");
        GameRegistry.registerBlock(ticketGate, "ticketGate");
        GameRegistry.registerBlock(chargeMachine, "chargeMachine");
        GameRegistry.registerBlock(lineManager, "lineManager");
        GameRegistry.registerBlock(oreAluminum, "oreAluminum");
        GameRegistry.registerBlock(certificateMachine, "certificateMachine");

        // 4. TileEntityの登録
        GameRegistry.registerTileEntity(TileEntityTransferGate.class, "TileEntityTransferGate");
        GameRegistry.registerTileEntity(TileEntityChargeMachine.class, "TileEntityChargeMachine");
        GameRegistry.registerTileEntity(TileEntityStaffTerminal.class, "TileEntityStaffTerminal");
        GameRegistry.registerTileEntity(TileEntityTicketMachine.class, "TileEntityTicketMachine");
        GameRegistry.registerTileEntity(TileEntityStationManager.class, "TileEntityStationManager");
        GameRegistry.registerTileEntity(TileEntityTicketGate.class, "TileEntityTicketGate");
        GameRegistry.registerTileEntity(TileEntityLineManager.class, "TileEntityLineManager");
        GameRegistry.registerTileEntity(TileEntityCertificateMachine.class, "TileEntityCertificateMachine");

        // 5. アイテムの登録
        GameRegistry.registerItem(settingTool, "settingTool");
        GameRegistry.registerItem(icCard, "icCard");
        GameRegistry.registerItem(ticket, "ticket");
        GameRegistry.registerItem(ingotAluminum, "ingotAluminum");
        GameRegistry.registerItem(magicIcCard, "magicIcCard");
        GameRegistry.registerItem(certificate, "certificate");
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

        // 6. ネットワークパケットの登録
        network = NetworkRegistry.INSTANCE.newSimpleChannel("KaisatsuChannel");
        network.registerMessage(MessageStationUpdate.Handler.class, MessageStationUpdate.class, 0, Side.SERVER);
        network.registerMessage(MessagePurchaseTicket.Handler.class, MessagePurchaseTicket.class, 1, Side.SERVER);
        network.registerMessage(MessageStaffTerminal.Handler.class, MessageStaffTerminal.class, 2, Side.SERVER);
        // ★新設した「路線管理ブロック用」の通信パケットを追記
        network.registerMessage(MessageOpenLineGui.Handler.class, MessageOpenLineGui.class, 3, Side.CLIENT);
        network.registerMessage(MessageLineUpdate.Handler.class, MessageLineUpdate.class, 4, Side.SERVER);
        network.registerMessage(MessageOpenTicketMachine.Handler.class, MessageOpenTicketMachine.class, 5, Side.CLIENT);
        network.registerMessage(MessageOpenStationSelectGui.Handler.class, MessageOpenStationSelectGui.class, 6, Side.CLIENT);
        network.registerMessage(MessageSaveBlockStation.Handler.class, MessageSaveBlockStation.class, 7, Side.SERVER);
        network.registerMessage(MessageOpenTransferSelectGui.Handler.class, MessageOpenTransferSelectGui.class, 8, Side.CLIENT);
        network.registerMessage(MessageSaveTransferStation.Handler.class, MessageSaveTransferStation.class, 9, Side.SERVER);
        network.registerMessage(MessageStaffTerminalAdjust.Handler.class, MessageStaffTerminalAdjust.class, 10, Side.SERVER);

        // 7. 仮テクスチャ・アセットの一括流用
        ticketMachine.setBlockTextureName("minecraft:jukebox");
        oreAluminum.setBlockTextureName("minecraft:iron_ore");
        stationManager.setBlockTextureName("minecraft:command_block");
        ticketGate.setBlockTextureName("minecraft:iron_block");
        chargeMachine.setBlockTextureName("minecraft:dispenser");

        ingotAluminum.setTextureName("minecraft:iron_ingot");
        icCard.setTextureName("minecraft:name_tag");
        ticket.setTextureName("minecraft:paper");
        settingTool.setTextureName("minecraft:blaze_rod");

        coin1.setTextureName("minecraft:gold_nugget");
        coin5.setTextureName("minecraft:gold_nugget");
        coin10.setTextureName("minecraft:gold_nugget");
        coin50.setTextureName("minecraft:gold_nugget");
        coin100.setTextureName("minecraft:gold_nugget");
        coin500.setTextureName("minecraft:gold_nugget");
        bill1000.setTextureName("minecraft:map_empty");
        bill2000.setTextureName("minecraft:map_empty");
        bill5000.setTextureName("minecraft:map_empty");
        bill10000.setTextureName("minecraft:map_empty");

        // 8. レガシー設定の読み込み
        FareManager.loadAllLines(event.getModConfigurationDirectory());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // IGuiHandlerの登録
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new IGuiHandler() {
            @Override
            public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                TileEntity te = world.getTileEntity(x, y, z);
                if (ID == 1) return new ContainerStationManager();
                if (ID == 2 && te instanceof TileEntityChargeMachine) return new ContainerChargeMachine(player.inventory, (TileEntityChargeMachine) te);
                if (ID == 3 && te instanceof TileEntityTicketMachine) return new ContainerTicketMachine(player.inventory, (TileEntityTicketMachine) te);
                if (ID == 4 && te instanceof TileEntityStaffTerminal) return new ContainerStaffTerminal(player.inventory, (TileEntityStaffTerminal) te);
                return null;
            }

            @Override
            public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                TileEntity te = world.getTileEntity(x, y, z);
                if (ID == 1 && te instanceof TileEntityStationManager) return new GuiStationManager((TileEntityStationManager) te);
                if (ID == 2 && te instanceof TileEntityChargeMachine)  return new GuiChargeMachine(player.inventory, (TileEntityChargeMachine) te);
                if (ID == 3 && te instanceof TileEntityTicketMachine)  return new GuiTicketMachine(player.inventory, (TileEntityTicketMachine) te);
                if (ID == 4 && te instanceof TileEntityStaffTerminal)  return new GuiStaffTerminal(player.inventory, (TileEntityStaffTerminal) te);
                return null;
            }
        });

        // レシピ関係の登録
        registerRecipes();

        // ==========================================
        // ★追加2: 初期化のタイミングで描画登録を呼び出す
        // ==========================================
        proxy.registerRenderers();
    }

    private void registerRecipes() {
        // 精錬レシピ
        GameRegistry.addSmelting(oreAluminum, new ItemStack(ingotAluminum), 0.5F);

        // 圧縮レシピ（インゴット ➔ 1円玉）
        GameRegistry.addShapelessRecipe(new ItemStack(coin1, 64), ingotAluminum);

        // 両替レシピ（小銭 ➔ 紙幣）
        GameRegistry.addShapelessRecipe(new ItemStack(coin5, 1), coin1, coin1, coin1, coin1, coin1);
        GameRegistry.addShapelessRecipe(new ItemStack(coin10, 1), coin5, coin5);
        GameRegistry.addShapelessRecipe(new ItemStack(coin50, 1), coin10, coin10, coin10, coin10, coin10);
        GameRegistry.addShapelessRecipe(new ItemStack(coin100, 1), coin50, coin50);
        GameRegistry.addShapelessRecipe(new ItemStack(coin500, 1), coin100, coin100, coin100, coin100, coin100);
        GameRegistry.addShapelessRecipe(new ItemStack(bill1000, 1), coin500, coin500);
        GameRegistry.addShapelessRecipe(new ItemStack(bill2000, 1), bill1000, bill1000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill5000, 1), bill1000, bill1000, bill1000, bill1000, bill1000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill10000, 1), bill5000, bill5000);

        // 逆両替レシピ（紙幣 ➔ 小銭）
        GameRegistry.addShapelessRecipe(new ItemStack(coin1, 5), coin5);
        GameRegistry.addShapelessRecipe(new ItemStack(coin5, 2), coin10);
        GameRegistry.addShapelessRecipe(new ItemStack(coin10, 5), coin50);
        GameRegistry.addShapelessRecipe(new ItemStack(coin50, 2), coin100);
        GameRegistry.addShapelessRecipe(new ItemStack(coin100, 5), coin500);
        GameRegistry.addShapelessRecipe(new ItemStack(coin500, 2), bill1000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill1000, 2), bill2000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill1000, 5), bill5000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill5000, 2), bill10000);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new com.SakuraMochi17.kaisatsumod.command.CommandResetStations());
    }

    // ====================================================
    // 🪙 RTM連携および通貨価値計算メソッド群
    // ====================================================

    public static Item getRtmMoneyItem() {
        if (searchedRtm) return cachedRtmMoney;
        searchedRtm = true;
        for (Object obj : Item.itemRegistry) {
            Item item = (Item) obj;
            if (item != null && item.getUnlocalizedName() != null && item.getUnlocalizedName().startsWith("item.rtm:money")) {
                cachedRtmMoney = item;
                System.out.println("[KaisatsuMod] RTMの通貨アイテムを検出しました！完全連携モードを有効化します。");
                break;
            }
        }
        return cachedRtmMoney;
    }

    public static int getMoneyValue(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return 0;

        // ① RTM通貨の判定
        String unlocName = stack.getItem().getUnlocalizedName(stack);
        if (unlocName != null && unlocName.startsWith("item.rtm:money")) {
            switch (stack.getItemDamage()) {
                case 0: return 1;
                case 1: return 5;
                case 2: return 10;
                case 3: return 50;
                case 4: return 100;
                case 5: return 500;
                case 6: return 1000;
                case 7: return 5000;
                case 8: return 10000;
                default: return 0;
            }
        }

        // ② 自作MOD通貨の判定
        Item item = stack.getItem();
        if (item == coin1) return 1;
        if (item == coin5) return 5;
        if (item == coin10) return 10;
        if (item == coin50) return 50;
        if (item == coin100) return 100;
        if (item == coin500) return 500;
        if (item == bill1000) return 1000;
        if (item == bill2000) return 2000;
        if (item == bill5000) return 5000;
        if (item == bill10000) return 10000;

        return 0;
    }

    public static ItemStack getMoneyItemStack(int value, int amount) {
        if (amount <= 0) return null;

        // RTMが導入されていればRTMの硬貨・紙幣を優先出力
        Item rtmItem = getRtmMoneyItem();
        if (rtmItem != null) {
            int meta = -1;
            switch (value) {
                case 1: meta = 0; break;
                case 5: meta = 1; break;
                case 10: meta = 2; break;
                case 50: meta = 3; break;
                case 100: meta = 4; break;
                case 500: meta = 5; break;
                case 1000: meta = 6; break;
                case 5000: meta = 7; break;
                case 10000: meta = 8; break;
            }
            if (meta != -1) return new ItemStack(rtmItem, amount, meta);
        }

        // RTM未導入、または2000円札の場合は自作アイテムを生成
        Item ourItem = null;
        switch (value) {
            case 1: ourItem = coin1; break;
            case 5: ourItem = coin5; break;
            case 10: ourItem = coin10; break;
            case 50: ourItem = coin50; break;
            case 100: ourItem = coin100; break;
            case 500: ourItem = coin500; break;
            case 1000: ourItem = bill1000; break;
            case 2000: ourItem = bill2000; break;
            case 5000: ourItem = bill5000; break;
            case 10000: ourItem = bill10000; break;
        }
        return ourItem != null ? new ItemStack(ourItem, amount) : null;
    }
}
