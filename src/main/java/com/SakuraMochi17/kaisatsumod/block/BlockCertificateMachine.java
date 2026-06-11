package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityCertificateMachine;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class BlockCertificateMachine extends BlockContainer {

    public BlockCertificateMachine() {
        super(Material.iron);
        this.setBlockName("certificateMachine");
        this.setBlockTextureName("minecraft:iron_block"); // 仮のテクスチャ
        this.setHardness(3.0F);
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCertificateMachine();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityCertificateMachine)) return false;
        TileEntityCertificateMachine machine = (TileEntityCertificateMachine) te;

        ItemStack heldItem = player.getCurrentEquippedItem();

        // =========================================================
        // 1. 設定ツールでの駅設定（共通GUIを開く）
        // =========================================================
        if (heldItem != null && heldItem.getItem() == KaisatsuModMain.settingTool) {
            if (!world.isRemote) {
                com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData data = com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.get(world);
                java.util.List<String> stationList = new java.util.ArrayList<>();
                if (data != null && data.globalStations != null) {
                    stationList.addAll(data.globalStations.keySet());
                }

                String currentStation = machine.stationName;
                if (currentStation == null || currentStation.isEmpty()) currentStation = "未設定";

                KaisatsuModMain.network.sendTo(
                        new com.SakuraMochi17.kaisatsumod.network.MessageOpenStationSelectGui(x, y, z, currentStation, stationList),
                        (net.minecraft.entity.player.EntityPlayerMP) player
                );
            }
            return true;
        }

        // =========================================================
        // 2. 通常右クリック：乗車駅証明書を発行する
        // =========================================================
        if (!world.isRemote) {
            if (machine.stationName == null || machine.stationName.equals("未設定")) {
                player.addChatMessage(new ChatComponentText("§cエラー: 駅が設定されていません。設定ツールを使用してください。§r"));
                world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                return true;
            }

            // 証明書アイテムを作成し、NBTに駅名を書き込む
            ItemStack cert = new ItemStack(KaisatsuModMain.certificate);
            cert.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
            cert.stackTagCompound.setString("issueStation", machine.stationName);

            // プレイヤーのインベントリに入れる
            if (!player.inventory.addItemStackToInventory(cert)) {
                // インベントリが一杯の場合は足元にドロップ
                player.dropPlayerItemWithRandomChoice(cert, false);
            } else {
                // ★修正：インベントリの「見た目」をサーバーと強制同期させる処理を追加
                if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
                    ((net.minecraft.entity.player.EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
                }
            }

            player.addChatMessage(new ChatComponentText("§a「" + machine.stationName + "」の乗車駅証明書を発行しました。§r"));
            world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F); // 発行音
        }
        return true;
    }
}
