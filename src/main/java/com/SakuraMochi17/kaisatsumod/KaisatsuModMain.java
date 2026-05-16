package com.SakuraMochi17.kaisatsumod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.world.World;

@Mod(modid = "yourmodid", version = "1.0")
public class KaisatsuModMain {

    @Mod.Instance("yourmodid")
    public static KaisatsuModMain instance;

    public static SimpleNetworkWrapper network;

    public static Block stationManager;
    public static Item icCard;
    public static Block ticketGate;
    public static Block chargeMachine;
    public static Block ticketMachine; // これを追加

    // ★リンクワンド（ステッキ）の変数を追加
    public static Item linkWand;

    public static Block oreAluminum;
    public static Item ingotAluminum;
    public static Item coin1, coin5, coin10, coin50, coin100, coin500;
    public static Item bill1000, bill2000, bill5000, bill10000;
    public static Item ticket;

    public static final CreativeTabs tabKaisatsu = new CreativeTabs("tabKaisatsu") {
        @Override
        public Item getTabIconItem() {
            return icCard;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;

        GameRegistry.registerTileEntity(TileEntityChargeMachine.class, "TileEntityChargeMachine");

        // ▼ これを追加・修正
        ticketMachine = new BlockTicketMachine();
        GameRegistry.registerBlock(ticketMachine, "ticketMachine");
        GameRegistry.registerTileEntity(TileEntityTicketMachine.class, "TileEntityTicketMachine");
        ticketMachine.setBlockTextureName("minecraft:jukebox"); // 券売機の仮テクスチャ

// ▼ 既存の ItemBasic だった ticket を ItemTicket に書き換え
// 修正前: ticket = new ItemBasic("ticket");
        ticket = new ItemTicket();

        network = NetworkRegistry.INSTANCE.newSimpleChannel("KaisatsuChannel");
        network.registerMessage(MessageStationUpdate.Handler.class, MessageStationUpdate.class, 0, Side.SERVER);
        // network.registerMessageのすぐ下あたりに追加
        network.registerMessage(MessagePurchaseTicket.Handler.class, MessagePurchaseTicket.class, 1, Side.SERVER);

        FareManager.loadAllLines(event.getModConfigurationDirectory());

        stationManager = new BlockStationManager();
        GameRegistry.registerBlock(stationManager, "stationManager");
        GameRegistry.registerTileEntity(TileEntityStationManager.class, "TileEntityStationManager");

        icCard = new ItemICCard();
        ticketGate = new BlockTicketGate();
        chargeMachine = new BlockChargeMachine();

        // ★リンクワンドのインスタンス化と登録
        linkWand = new ItemLinkWand();
        GameRegistry.registerItem(linkWand, "linkWand");

        GameRegistry.registerItem(icCard, "icCard");
        GameRegistry.registerBlock(ticketGate, "ticketGate");
        GameRegistry.registerBlock(chargeMachine, "chargeMachine");
        GameRegistry.registerTileEntity(TileEntityTicketGate.class, "TileEntityTicketGate");

        oreAluminum = new BlockAluminumOre();
        ingotAluminum = new ItemBasic("ingot_aluminum");

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

        // ========== ★ 簡易テクスチャの一括割り当て（バニラアセットの流用） ==========
        // ブロック関係
        oreAluminum.setBlockTextureName("minecraft:iron_ore");          // 鉄鉱石の見た目
        stationManager.setBlockTextureName("minecraft:command_block");  // コマンドブロックの見た目
        ticketGate.setBlockTextureName("minecraft:iron_block");          // 鉄ブロックの見た目
        chargeMachine.setBlockTextureName("minecraft:dispenser");       // ディスペンサーの見た目

        // アイテム関係
        ingotAluminum.setTextureName("minecraft:iron_ingot");           // 鉄インゴット
        icCard.setTextureName("minecraft:name_tag");                    // 名札（ICカードの代用）
        ticket.setTextureName("minecraft:paper");                       // 紙（きっぷの代用）
        linkWand.setTextureName("minecraft:blaze_rod");                 // ブレイズロッド（魔法のステッキ）

        // 硬貨・紙幣（バニラの丸いアイテムや紙を流用）
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
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new IGuiHandler() {
            // ▼ サーバー側（裏側のアイテム管理）: 必ず Container を返す！
            @Override
            public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                if (ID == 1) return new ContainerStationManager();
                if (ID == 2) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te instanceof TileEntityChargeMachine) return new ContainerChargeMachine(player.inventory, (TileEntityChargeMachine) te);
                }
                if (ID == 3) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te instanceof TileEntityTicketMachine) return new ContainerTicketMachine(player.inventory, (TileEntityTicketMachine) te);
                }
                return null;
            }

            // ▼ クライアント側（プレイヤーが見る画面）: 必ず Gui を返す！
            @Override
            public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                if (ID == 1) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te instanceof TileEntityStationManager) return new GuiStationManager((TileEntityStationManager) te);
                }
                if (ID == 2) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te instanceof TileEntityChargeMachine) return new GuiChargeMachine(player.inventory, (TileEntityChargeMachine) te);
                }
                if (ID == 3) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te instanceof TileEntityTicketMachine) return new GuiTicketMachine(player.inventory, (TileEntityTicketMachine) te);
                }
                return null;
            }
        });


        GameRegistry.addSmelting(oreAluminum, new ItemStack(ingotAluminum), 0.5F);
        GameRegistry.addShapelessRecipe(new ItemStack(coin1, 64), ingotAluminum);

        GameRegistry.addShapelessRecipe(new ItemStack(coin5, 1), coin1, coin1, coin1, coin1, coin1);
        GameRegistry.addShapelessRecipe(new ItemStack(coin10, 1), coin5, coin5);
        GameRegistry.addShapelessRecipe(new ItemStack(coin50, 1), coin10, coin10, coin10, coin10, coin10);
        GameRegistry.addShapelessRecipe(new ItemStack(coin100, 1), coin50, coin50);
        GameRegistry.addShapelessRecipe(new ItemStack(coin500, 1), coin100, coin100, coin100, coin100, coin100);
        GameRegistry.addShapelessRecipe(new ItemStack(bill1000, 1), coin500, coin500);
        GameRegistry.addShapelessRecipe(new ItemStack(bill2000, 1), bill1000, bill1000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill5000, 1), bill1000, bill1000, bill1000, bill1000, bill1000);
        GameRegistry.addShapelessRecipe(new ItemStack(bill10000, 1), bill5000, bill5000);

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
    // アイテムがお金かどうか、いくらかを判定するメソッド
    public static int getMoneyValue(ItemStack stack) {
        if (stack == null) return 0;
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
}
