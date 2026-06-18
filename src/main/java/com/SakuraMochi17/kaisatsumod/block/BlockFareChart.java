package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityFareChart;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.List;

public class BlockFareChart extends BlockContainer {
    public BlockFareChart() {
        super(Material.iron);
        this.setBlockName("fareChart");
        this.setHardness(3.0F);
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityFareChart();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int dir = MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, dir, 2);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) { // サーバー側でのみ処理
            ItemStack heldItem = player.getCurrentEquippedItem();

            // ★修正：設定ツールを持っている場合は駅選択GUIを開く
            if (heldItem != null && heldItem.getItem() == KaisatsuModMain.settingTool) {
                TileEntity te = world.getTileEntity(x, y, z);
                String currentStation = "未設定";
                if (te instanceof TileEntityFareChart) {
                    if (((TileEntityFareChart) te).stationName != null && !((TileEntityFareChart) te).stationName.isEmpty()) {
                        currentStation = ((TileEntityFareChart) te).stationName;
                    }
                }

                com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData data = com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.get(world);
                java.util.List<String> stationList = new java.util.ArrayList<>();
                if (data != null && data.globalStations != null) {
                    stationList.addAll(data.globalStations.keySet());
                }

                // GUIを開くパケットをクライアントへ送信
                KaisatsuModMain.network.sendTo(
                        new com.SakuraMochi17.kaisatsumod.network.MessageOpenStationSelectGui(x, y, z, currentStation, stationList),
                        (net.minecraft.entity.player.EntityPlayerMP) player
                );
            }
        }
        return true;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, net.minecraft.entity.Entity entity) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == 0) this.setBlockBounds(0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
        else if (meta == 1) this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F);
        else if (meta == 2) this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
        else if (meta == 3) this.setBlockBounds(0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);

        if (meta == 0) this.setBlockBounds(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
        else if (meta == 1) this.setBlockBounds(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
        else if (meta == 2) this.setBlockBounds(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
        else if (meta == 3) this.setBlockBounds(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);

        this.setBlockBoundsBasedOnState(world, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean renderAsNormalBlock() { return false; }

    @Override
    public boolean isOpaqueCube() { return false; }

    @Override
    public int getRenderType() { return -1; }
}
