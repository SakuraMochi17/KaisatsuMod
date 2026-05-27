package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockTicketMachine extends BlockContainer {
    public BlockTicketMachine() {
        super(Material.iron);
        this.setBlockName("ticketMachine");
        this.setBlockTextureName("minecraft:iron_block"); // 仮テクスチャ
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTicketMachine();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            // 切符発売機は GUI ID 3 とする
            player.openGui(KaisatsuModMain.instance, 3, world, x, y, z);
        }
        return true;
    }

    @Override
    public boolean renderAsNormalBlock() {
        // 通常の四角いブロックとしての描画を無効化
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        // 光を透過するように設定（これがないと周りのブロックが透けたり影がバグったりします）
        return false;
    }

    @Override
    public int getRenderType() {
        // -1 を返すことで「TileEntitySpecialRenderer (TESR) を使って描画する」という合図になります
        return -1;
    }
}

