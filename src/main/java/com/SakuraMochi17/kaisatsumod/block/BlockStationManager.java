package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.core.StationRegistry;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStationManager;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockStationManager extends BlockContainer {
    public BlockStationManager() {
        super(Material.iron);
        this.setBlockName("stationManager");
        this.setHardness(3.0F);
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityStationManager();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(KaisatsuModMain.instance, 1, world, x, y, z);
        }
        return true;
    }

    // ==========================================
    // ★追加：ブロックが破壊されたら駅データを消す
    // ==========================================
    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        if (!world.isRemote) {
            int dimID = world.provider.dimensionId;
            // レジストリからこの座標の駅データを確実に削除
            com.SakuraMochi17.kaisatsumod.core.StationRegistry.removeStation(dimID, x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
