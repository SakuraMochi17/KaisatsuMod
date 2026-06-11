package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockTicketMachine extends BlockContainer {
    public BlockTicketMachine() {
        super(Material.iron);
        this.setBlockName("ticketMachine");
        this.setHardness(3.0F);
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTicketMachine();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) { // サーバー側でのみ処理
            ItemStack heldItem = player.getCurrentEquippedItem();

            TileEntity te = world.getTileEntity(x, y, z);
            TileEntityTicketMachine machine = null;
            if (te instanceof TileEntityTicketMachine) {
                machine = (TileEntityTicketMachine) te;
            }

            // ★分岐：設定ツールを持っている場合は駅選択GUIを開く
            if (heldItem != null && heldItem.getItem() == KaisatsuModMain.settingTool) {
                com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData data = com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.get(world);
                java.util.List<String> stationList = new java.util.ArrayList<>();
                if (data != null && data.globalStations != null) {
                    stationList.addAll(data.globalStations.keySet());
                }

                String currentStation = (machine != null && machine.stationName != null) ? machine.stationName : "未設定";

                KaisatsuModMain.network.sendTo(
                        new com.SakuraMochi17.kaisatsumod.network.MessageOpenStationSelectGui(x, y, z, currentStation, stationList),
                        (net.minecraft.entity.player.EntityPlayerMP) player
                );
            }
            // ★通常の右クリック（券売機を開く）
            else if (machine != null) {
                String currentStationName = machine.stationName;
                if (currentStationName == null) currentStationName = "未設定";

                java.util.Set<Integer> fareSet = new java.util.TreeSet<>();
                com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData data = com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.get(world);

                if (data != null && data.companyLines != null && !currentStationName.equals("未設定")) {
                    for (com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.LineData line : data.companyLines.values()) {
                        if (line.stationOrder != null) {
                            for (String targetStation : line.stationOrder) {
                                int fare = com.SakuraMochi17.kaisatsumod.core.FareManager.calculateFare(world, currentStationName, targetStation);
                                if (fare > 0) {
                                    fareSet.add((int) Math.ceil(fare / 10.0) * 10); // 10円単位切り上げ
                                }
                            }
                        }
                    }
                }
                java.util.List<Integer> availableFares = new java.util.ArrayList<>(fareSet);

                // 1. パケットを送信してクライアント（画面）に運賃リストを届ける
                KaisatsuModMain.network.sendTo(
                        new com.SakuraMochi17.kaisatsumod.network.MessageOpenTicketMachine(x, y, z, currentStationName, availableFares),
                        (net.minecraft.entity.player.EntityPlayerMP) player
                );

                // 2. その直後に、サーバー側で正式な手順でGUIを開く！
                player.openGui(KaisatsuModMain.instance, 3, world, x, y, z);
            }
        }
        return true;
    }

    // ブロックを壊した時に中身をぶちまける処理
    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityTicketMachine) {
            TileEntityTicketMachine terminal = (TileEntityTicketMachine) te;
            for (int i = 0; i < terminal.getSizeInventory(); i++) {
                ItemStack stack = terminal.getStackInSlot(i);
                if (stack != null) {
                    float f = world.rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
                    float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, stack.copy());
                    world.spawnEntityInWorld(entityitem);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
    // =========================================================
    // ★追加：3Dモデルを使うために、通常の四角いブロック描画をオフにする
    // =========================================================
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }
}
