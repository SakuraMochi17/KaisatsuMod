package com.SakuraMochi17.kaisatsumod.item;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.tileentity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemLinkWand extends Item {

    public ItemLinkWand() {
        super();
        this.setUnlocalizedName("settingTool");
        this.setTextureName("minecraft:blaze_rod");
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true; // サーバー側でのみ処理

        TileEntity te = world.getTileEntity(x, y, z);
        if (te == null) return false;

        // ワンドのNBT（記憶領域）を準備
        if (stack.stackTagCompound == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        // ====================================================
        // ① 駅管理ブロックをスニーク(Shift)＋右クリックで「座標を記憶」
        // ====================================================
        if (player.isSneaking() && te instanceof TileEntityStationManager) {
            TileEntityStationManager stationTE = (TileEntityStationManager) te;

            // ★追加：駅名がまだ保存されておらずnullだった場合の安全装置
            String sName = stationTE.stationName;
            if (sName == null || sName.isEmpty()) {
                sName = "未設定";
            }

            stack.stackTagCompound.setInteger("storedX", x);
            stack.stackTagCompound.setInteger("storedY", y);
            stack.stackTagCompound.setInteger("storedZ", z);
            stack.stackTagCompound.setString("storedName", sName); // ★安全な変数を書き込む

            player.addChatMessage(new ChatComponentText("§a[リンクワンド] 駅「" + sName + "」の座標データを記憶しました。§r"));
            world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
            return true;
        }

        // ====================================================
        // ② 改札機や券売機を右クリックで「座標をペースト（連携）」
        // ====================================================
        if (!player.isSneaking() && stack.stackTagCompound.hasKey("storedX")) {
            int storedX = stack.stackTagCompound.getInteger("storedX");
            int storedY = stack.stackTagCompound.getInteger("storedY");
            int storedZ = stack.stackTagCompound.getInteger("storedZ");
            String storedName = stack.stackTagCompound.getString("storedName");

            // --- 自動改札機への連携 ---
            if (te instanceof TileEntityTicketGate) {
                TileEntityTicketGate gateTE = (TileEntityTicketGate) te;
                gateTE.linkedX = storedX;
                gateTE.linkedY = storedY;
                gateTE.linkedZ = storedZ;
                gateTE.isLinked = true;
                gateTE.markDirty();
                world.markBlockForUpdate(x, y, z);

                player.addChatMessage(new ChatComponentText("§b[リンクワンド] 自動改札機を「" + storedName + "」駅として設定しました！§r"));
                world.playSoundAtEntity(player, "random.levelup", 1.0F, 1.0F);
                return true;
            }
            // --- 自動改札機への連携 ---
            // (既存のコード)

            // ★追加・修正：乗り換え改札機への二重連携 ---
            else if (te instanceof TileEntityTransferGate) {
                TileEntityTransferGate gateTE = (TileEntityTransferGate) te;

                // スニーク(Shift)しながら叩いた場合は「入場駅」として設定
                if (player.isSneaking()) {
                    gateTE.linked2X = storedX;
                    gateTE.linked2Y = storedY;
                    gateTE.linked2Z = storedZ;
                    gateTE.isLinked2 = true;
                    player.addChatMessage(new ChatComponentText("§b[リンクワンド] 乗り換え改札機の【入場駅(これから乗る駅)】を「" + storedName + "」駅として設定しました！§r"));
                }
                // 普通に叩いた場合は「出場駅」として設定
                else {
                    gateTE.linked1X = storedX;
                    gateTE.linked1Y = storedY;
                    gateTE.linked1Z = storedZ;
                    gateTE.isLinked1 = true;
                    player.addChatMessage(new ChatComponentText("§b[リンクワンド] 乗り換え改札機の【出場駅(ここまで乗ってきた駅)】を「" + storedName + "」駅として設定しました！§r"));
                }

                gateTE.markDirty();
                world.markBlockForUpdate(x, y, z);
                world.playSoundAtEntity(player, "random.levelup", 1.0F, 1.0F);
                return true;
            }

            // --- 券売機への連携 ---
            // (既存のコード)
            // --- 券売機への連携 ---
            else if (te instanceof TileEntityTicketMachine) {
                TileEntityTicketMachine machineTE = (TileEntityTicketMachine) te;
                machineTE.linkedX = storedX;
                machineTE.linkedY = storedY;
                machineTE.linkedZ = storedZ;
                // ★追加：駅名を確実にセットする
                machineTE.stationName = storedName;

                machineTE.isLinked = true;
                machineTE.markDirty();

                // ★追加：この1行で、修正1の「getDescriptionPacket」が呼び出され、画面に品川駅が同期されます！
                world.markBlockForUpdate(x, y, z);

                player.addChatMessage(new ChatComponentText("§b[リンクワンド] 券売機を「" + storedName + "」駅として設定しました！§r"));
                world.playSoundAtEntity(player, "random.levelup", 1.0F, 1.0F);
                return true;
            }
            // --- チャージ機への連携 ---
            else if (te instanceof TileEntityChargeMachine) {
                TileEntityChargeMachine chargeTE = (TileEntityChargeMachine) te;
                chargeTE.linkedX = storedX;
                chargeTE.linkedY = storedY;
                chargeTE.linkedZ = storedZ;
                chargeTE.isLinked = true;
                chargeTE.markDirty();
                world.markBlockForUpdate(x, y, z);

                player.addChatMessage(new ChatComponentText("§b[リンクワンド] 精算機・チャージ機を「" + storedName + "」駅として設定しました！§r"));
                world.playSoundAtEntity(player, "random.levelup", 1.0F, 1.0F);
                return true;
            }
        }

        return false;
    }
}
