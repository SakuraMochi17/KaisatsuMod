package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
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
        if (!world.isRemote) {
            // GUI ID「4」を開く
            player.openGui(KaisatsuModMain.instance, 4, world, x, y, z);
        }
        return true;
    }
}
