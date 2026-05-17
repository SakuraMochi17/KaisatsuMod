package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.core.StationRegistry;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.item.ItemTicket;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTransferGate;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class BlockTransferGate extends BlockContainer {
    private static final int BIT_OPEN = 4;

    public BlockTransferGate() {
        super(Material.iron);
        this.setBlockName("transferGate");
        this.setHardness(3.0F);
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTransferGate();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int direction = MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, direction, 2);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if ((meta & BIT_OPEN) != 0) return null;
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1.5, z + 1);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityTransferGate)) return false;
        TileEntityTransferGate gateTE = (TileEntityTransferGate) te;

        ItemStack heldItem = player.getCurrentEquippedItem();

        if (heldItem == null && player.isSneaking()) {
            if (!world.isRemote) {
                gateTE.resetLinks();
                player.addChatMessage(new ChatComponentText("§e[乗り換え改札] 連携設定をリセットしました。"));
            }
            return true;
        }

        if (heldItem != null) {
            if (!world.isRemote) {
                if (!gateTE.isLinked1 || !gateTE.isLinked2) {
                    player.addChatMessage(new ChatComponentText("エラー: 2つの駅が完全に連携されていません。リンクワンドで降車駅と乗車駅を設定してください。"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return true;
                }

                if (heldItem.getItem() instanceof ItemTicket) {
                    player.addChatMessage(new ChatComponentText("エラー: この乗り換え改札機は『ICカード専用』です。切符はご利用になれません。"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return true;
                }

                if (heldItem.getItem() instanceof ItemICCard) {
                    processTransfer(world, x, y, z, player, heldItem, gateTE);
                }
            }
            return true;
        }
        return false;
    }

    private void processTransfer(World world, int x, int y, int z, EntityPlayer player, ItemStack card, TileEntityTransferGate gateTE) {
        if (!card.stackTagCompound.getBoolean("inGate")) {
            player.addChatMessage(new ChatComponentText("エラー: 入場記録がありません！"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        int dimID = world.provider.dimensionId;
        StationRegistry.StationData exitStation = StationRegistry.findNearestStation(dimID, gateTE.linked1X, gateTE.linked1Y, gateTE.linked1Z, 1.0);
        StationRegistry.StationData entryStation = StationRegistry.findNearestStation(dimID, gateTE.linked2X, gateTE.linked2Y, gateTE.linked2Z, 1.0);

        if (exitStation == null || entryStation == null) return;

        String entryLine = card.stackTagCompound.getString("entryLine");
        String entryStationName = card.stackTagCompound.getString("entryStation");

        // ★新規追加: 厳密な検札ロジック（すでに乗り換え先に入場済みの場合は弾く）
        if (entryLine.equals(entryStation.lineID) && entryStationName.equals(entryStation.stationName)) {
            player.addChatMessage(new ChatComponentText("エラー: 既に乗り換え処理は完了しています。このまま目的地へお向かいください。"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        int entryX = card.stackTagCompound.getInteger("entryX");
        int entryY = card.stackTagCompound.getInteger("entryY");
        int entryZ = card.stackTagCompound.getInteger("entryZ");

        // 1. 出場処理（これまでの運賃計算）
        String companyA = FareManager.getCompanyID(entryLine);
        String companyB = FareManager.getCompanyID(exitStation.lineID);
        int fare = (companyA.equals(companyB))
                ? FareManager.calculateFare(exitStation.lineID, entryX, entryY, entryZ, exitStation.x, exitStation.y, exitStation.z)
                : FareManager.calculateCrossCompanyFare(entryLine, exitStation.lineID, entryX, entryY, entryZ, exitStation.x, exitStation.y, exitStation.z);

        if (fare == -1) return;

        int balance = card.stackTagCompound.getInteger("balance");
        if (balance < fare) {
            player.addChatMessage(new ChatComponentText("残高不足です！のりこし精算機をご利用ください。(運賃: " + fare + "円)"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        // 2. 決済と同時に入場処理（乗車駅の書き換え）
        card.stackTagCompound.setInteger("balance", balance - fare);
        card.stackTagCompound.setString("entryLine", entryStation.lineID);
        card.stackTagCompound.setString("entryStation", entryStation.stationName);
        card.stackTagCompound.setInteger("entryX", entryStation.x);
        card.stackTagCompound.setInteger("entryY", entryStation.y);
        card.stackTagCompound.setInteger("entryZ", entryStation.z);

        player.addChatMessage(new ChatComponentText("§aピピッ！ 乗り換え完了§r"));
        player.addChatMessage(new ChatComponentText("精算: " + fare + "円 (残高: " + (balance - fare) + "円)"));
        player.addChatMessage(new ChatComponentText("入場: [" + FareManager.getLineName(entryStation.lineID) + " : " + entryStation.stationName + "]"));

        world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);

        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, meta | BIT_OPEN, 3);
        world.scheduleBlockUpdate(x, y, z, this, 60);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote) {
            int meta = world.getBlockMetadata(x, y, z);
            world.setBlockMetadataWithNotify(x, y, z, meta & ~BIT_OPEN, 3);
        }
    }
}
