package com.SakuraMochi17.kaisatsumod;

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
        this.setTextureName("minecraft:blaze_rod"); // メインクラスでの指定に合わせる
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        Block clickedBlock = world.getBlock(x, y, z);

        if (stack.stackTagCompound == null) {
            stack.setTagCompound(new NBTTagCompound());
            stack.stackTagCompound.setBoolean("hasData", false);
        }

        // ① 駅管理ブロックをクリック：座標をステッキに記憶
        if (clickedBlock instanceof BlockStationManager) {
            stack.stackTagCompound.setBoolean("hasData", true);
            stack.stackTagCompound.setInteger("savedX", x);
            stack.stackTagCompound.setInteger("savedY", y);
            stack.stackTagCompound.setInteger("savedZ", z);

            player.addChatMessage(new ChatComponentText("§a[リンクワンド] 駅管理ブロックの座標を記憶しました！ (" + x + ", " + y + ", " + z + ")"));
            world.playSoundAtEntity(player, "random.levelup", 1.0F, 2.0F);
            return true;
        }

        // 記憶した座標データの取得
        boolean hasData = stack.stackTagCompound.getBoolean("hasData");
        int savedX = stack.stackTagCompound.getInteger("savedX");
        int savedY = stack.stackTagCompound.getInteger("savedY");
        int savedZ = stack.stackTagCompound.getInteger("savedZ");

        // ② 改札機をクリック：座標を書き込み
        if (clickedBlock instanceof BlockTicketGate) {
            if (!hasData) {
                player.addChatMessage(new ChatComponentText("§c[リンクワンド] 先に駅管理ブロックを右クリックして座標を記憶させてください。"));
                return true;
            }

            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityTicketGate) {
                ((TileEntityTicketGate) te).setLinkedStation(savedX, savedY, savedZ);
                player.addChatMessage(new ChatComponentText("§b[リンクワンド] 改札機を駅と連携させました！"));
                world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
            }
            return true;
        }

        // ③ ★追加：自動券売機をクリック：座標を書き込み
        if (clickedBlock instanceof BlockTicketMachine) {
            if (!hasData) {
                player.addChatMessage(new ChatComponentText("§c[リンクワンド] 先に駅管理ブロックを右クリックして座標を記憶させてください。"));
                return true;
            }

            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityTicketMachine) {
                ((TileEntityTicketMachine) te).setLinkedStation(savedX, savedY, savedZ);
                player.addChatMessage(new ChatComponentText("§b[リンクワンド] 自動券売機を駅と連携させました！"));
                world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
            }
            return true;
        }

        return false;
    }
}
