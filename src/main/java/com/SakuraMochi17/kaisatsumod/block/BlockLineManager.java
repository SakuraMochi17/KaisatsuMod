package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData;
import com.SakuraMochi17.kaisatsumod.network.MessageOpenLineGui;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityLineManager;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockLineManager extends BlockContainer {
    public BlockLineManager() {
        super(Material.iron);
        this.setBlockName("lineManager");
        this.setHardness(3.0F);
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLineManager();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityLineManager) {
                String compName = ((TileEntityLineManager) te).companyName;
                KaisatsuNetworkData data = KaisatsuNetworkData.get(world);

                MessageOpenLineGui msg = new MessageOpenLineGui();
                msg.x = x; msg.y = y; msg.z = z;
                msg.companyName = compName;

                if (data != null && data.globalStations != null) {
                    msg.globalStations.addAll(data.globalStations.keySet()); // 全駅リスト

                    if (data.companyLines != null) {
                        for (KaisatsuNetworkData.LineData ld : data.companyLines.values()) {
                            // ★この会社に所属している路線だけを抽出
                            if (compName != null && !compName.isEmpty() && compName.equals(ld.companyName)) {
                                MessageOpenLineGui.LineInfo info = new MessageOpenLineGui.LineInfo();
                                info.lineID = ld.lineID;
                                info.lineName = ld.lineName;
                                info.baseFare = ld.baseFare;
                                info.costPerBlock = ld.costPerBlock;
                                if (ld.stationOrder != null) info.stations.addAll(ld.stationOrder);
                                msg.companyLines.add(info);
                            }
                        }
                    }
                }
                KaisatsuModMain.network.sendTo(msg, (net.minecraft.entity.player.EntityPlayerMP) player);
            }
        }
        return true;
    }
}
