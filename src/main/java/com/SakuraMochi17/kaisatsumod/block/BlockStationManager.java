package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
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
        // ★修正：危険なNBTコピペ機能を廃止し、シンプルにGUIを開くだけにする
        if (!world.isRemote) {
            player.openGui(KaisatsuModMain.instance, 1, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityStationManager) {
                String stationName = ((TileEntityStationManager) te).stationName;
                com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData data = com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.get(world);

                if (data != null && data.globalStations != null && stationName != null && !"未設定".equals(stationName)) {
                    data.globalStations.remove(stationName);
                    data.markDirty();
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
