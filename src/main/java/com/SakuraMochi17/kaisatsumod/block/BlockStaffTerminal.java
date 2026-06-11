package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockStaffTerminal extends BlockContainer {
    public BlockStaffTerminal() {
        super(Material.iron);
        this.setBlockName("staffTerminal");
        this.setHardness(3.0F);
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setBlockTextureName("minecraft:anvil_base"); // 金床のテクスチャを仮当て
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityStaffTerminal();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityStaffTerminal)) return false;
        TileEntityStaffTerminal terminal = (TileEntityStaffTerminal) te;

        ItemStack heldItem = player.getCurrentEquippedItem();

        // ★分岐1：設定ツールを持っている場合は駅選択GUIを開く
        if (heldItem != null && heldItem.getItem() == KaisatsuModMain.settingTool) {
            if (!world.isRemote) {
                com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData data = com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.get(world);
                java.util.List<String> stationList = new java.util.ArrayList<>();
                if (data != null && data.globalStations != null) {
                    stationList.addAll(data.globalStations.keySet());
                }

                String currentStation = terminal.stationName;
                if (currentStation == null || currentStation.isEmpty()) currentStation = "未設定";

                KaisatsuModMain.network.sendTo(
                        new com.SakuraMochi17.kaisatsumod.network.MessageOpenStationSelectGui(x, y, z, currentStation, stationList),
                        (net.minecraft.entity.player.EntityPlayerMP) player
                );
            }
            return true;
        }

        // ★分岐2：設定ツール以外の場合は、精算機GUI（ID 4）を開く
        if (!world.isRemote) {
            player.openGui(KaisatsuModMain.instance, 4, world, x, y, z);
        }
        return true;
    }

    // ★追加：ブロックを壊した時に、中に入っているアイテムを外にドロップする処理
    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityStaffTerminal) {
            TileEntityStaffTerminal terminal = (TileEntityStaffTerminal) te;
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
}