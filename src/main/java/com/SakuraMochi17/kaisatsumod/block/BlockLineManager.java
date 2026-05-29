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
        this.setBlockName("lineManager"); // ★修正: stationManagerと被らない名前に変更
        this.setHardness(3.0F);
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    // ★追加: ブロック設置時にTileEntity（セーブデータ）を生成する必須メソッド
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLineManager();
    }

    // BlockLineManager.java の onBlockActivated 内
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) { // サーバー側でのみ処理
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityLineManager) {
                String lineID = ((TileEntityLineManager) te).lineID;
                KaisatsuNetworkData data = KaisatsuNetworkData.get(world);

                MessageOpenLineGui msg = new MessageOpenLineGui();
                msg.x = x; msg.y = y; msg.z = z;

                // ★修正: data が null ではなく、かつ globalStations が初期化されているか確認
                if (data != null && data.globalStations != null) {
                    msg.globalStations.addAll(data.globalStations.keySet()); // 全駅リスト

                    // ★修正: lineID が null や空文字ではないかを必ずチェックする
                    if (lineID != null && !lineID.isEmpty() && data.companyLines != null) {
                        if (data.companyLines.containsKey(lineID)) {
                            KaisatsuNetworkData.LineData ld = data.companyLines.get(lineID);
                            msg.lineID = ld.lineID;
                            msg.lineName = ld.lineName;
                            msg.companyName = ld.companyName;
                            msg.baseFare = ld.baseFare;
                            msg.costPerBlock = ld.costPerBlock;
                            if (ld.stationOrder != null) {
                                msg.lineStations.addAll(ld.stationOrder);
                            }
                        }
                    }
                }

                // パケットをGUIを開くプレイヤーに送信
                KaisatsuModMain.network.sendTo(msg, (net.minecraft.entity.player.EntityPlayerMP) player);
            }
        }
        return true;
    }
}