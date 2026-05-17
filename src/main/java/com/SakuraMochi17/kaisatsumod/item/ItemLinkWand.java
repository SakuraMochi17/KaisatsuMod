package com.SakuraMochi17.kaisatsumod.item;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.block.BlockStationManager;
import com.SakuraMochi17.kaisatsumod.block.BlockTicketGate;
import com.SakuraMochi17.kaisatsumod.block.BlockTicketMachine;
import com.SakuraMochi17.kaisatsumod.block.BlockTransferGate;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketGate;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTransferGate;
import net.minecraft.block.Block;
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
        this.setUnlocalizedName("linkWand");
        this.setTextureName("minecraft:blaze_rod");
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        Block clickedBlock = world.getBlock(x, y, z);

        if (!(clickedBlock instanceof BlockStationManager || clickedBlock instanceof BlockTicketGate ||
                clickedBlock instanceof BlockTicketMachine || clickedBlock instanceof BlockTransferGate)) {
            return false;
        }

        if (world.isRemote) {
            return false;
        }

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        if (clickedBlock instanceof BlockStationManager) {
            stack.getTagCompound().setBoolean("hasData", true);
            stack.getTagCompound().setInteger("savedX", x);
            stack.getTagCompound().setInteger("savedY", y);
            stack.getTagCompound().setInteger("savedZ", z);
            player.addChatMessage(new ChatComponentText("§a[リンクワンド] 駅管理ブロックの座標を記憶しました！ (" + x + ", " + y + ", " + z + ")"));
            world.playSoundAtEntity(player, "random.levelup", 1.0F, 2.0F);
            return true;
        }

        boolean hasData = stack.getTagCompound().getBoolean("hasData");
        if (!hasData) {
            player.addChatMessage(new ChatComponentText("§c[リンクワンド] 先に駅管理ブロックを右クリックして座標を記憶させてください。"));
            return true;
        }

        int savedX = stack.getTagCompound().getInteger("savedX");
        int savedY = stack.getTagCompound().getInteger("savedY");
        int savedZ = stack.getTagCompound().getInteger("savedZ");
        TileEntity te = world.getTileEntity(x, y, z);

        if (clickedBlock instanceof BlockTicketGate && te instanceof TileEntityTicketGate) {
            ((TileEntityTicketGate) te).setLinkedStation(savedX, savedY, savedZ);
            player.addChatMessage(new ChatComponentText("§b[リンクワンド] 改札機を駅と連携させました！"));
            world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
            return true;
        }

        if (clickedBlock instanceof BlockTicketMachine && te instanceof TileEntityTicketMachine) {
            ((TileEntityTicketMachine) te).setLinkedStation(savedX, savedY, savedZ);
            player.addChatMessage(new ChatComponentText("§b[リンクワンド] 自動券売機を駅と連携させました！"));
            world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
            return true;
        }

        if (clickedBlock instanceof BlockTransferGate && te instanceof TileEntityTransferGate) {
            TileEntityTransferGate transferTE = (TileEntityTransferGate) te;
            if (!transferTE.isLinked1) {
                transferTE.setLink1(savedX, savedY, savedZ);
                // ★修正：メッセージを2行に分割して表示崩れを防止
                player.addChatMessage(new ChatComponentText("§d[リンクワンド] 1/2: 乗り換え元（降車駅）を設定しました。"));
                player.addChatMessage(new ChatComponentText("§d続けて別の駅を記憶し、再度タッチしてください。"));
                world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
            } else if (!transferTE.isLinked2) {
                transferTE.setLink2(savedX, savedY, savedZ);
                player.addChatMessage(new ChatComponentText("§d[リンクワンド] 2/2: 乗り換え先（乗車駅）を設定しました。セットアップ完了！"));
                world.playSoundAtEntity(player, "random.levelup", 1.0F, 1.0F);
            } else {
                player.addChatMessage(new ChatComponentText("§c[リンクワンド] すでに2つの駅が設定されています。リセットする場合はスニーク＋素手で右クリックしてください。"));
            }
            return true;
        }

        return true;
    }
}
