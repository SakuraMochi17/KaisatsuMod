package com.SakuraMochi17.kaisatsumod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockStationManager extends BlockContainer {
    public BlockStationManager() {
        super(Material.iron);
        this.setBlockName("stationManager");
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setHardness(3.0F);
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

    // ★追加：ブロックが破壊された時に中のTileEntityのinvalidate（登録解除）を確実に呼ぶ処理
    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
    }
}
