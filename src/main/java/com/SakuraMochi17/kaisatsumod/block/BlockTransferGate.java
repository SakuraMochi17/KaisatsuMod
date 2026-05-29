package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.item.ItemMagicICCard;
import com.SakuraMochi17.kaisatsumod.item.ItemTicket;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStationManager;
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
    private long lastClickTime = 0; // 連打ガード

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
        if (System.currentTimeMillis() - lastClickTime < 500) return true;
        lastClickTime = System.currentTimeMillis();

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityTransferGate)) return false;
        TileEntityTransferGate gateTE = (TileEntityTransferGate) te;

        ItemStack heldItem = player.getCurrentEquippedItem();

        // リンクワンドを持っている場合はブロック側の処理を無視
        if (heldItem != null && heldItem.getItem() instanceof com.SakuraMochi17.kaisatsumod.item.ItemLinkWand) {
            return false;
        }

        // スニーク＋素手で連携リセット
        if (heldItem == null && player.isSneaking()) {
            if (!world.isRemote) {
                gateTE.resetLinks();
                player.addChatMessage(new ChatComponentText("§e[乗り換え改札] 連携設定をリセットしました。§r"));
            }
            return true;
        }

        if (heldItem != null && (heldItem.getItem() instanceof ItemICCard || heldItem.getItem() instanceof ItemTicket)) {
            if (!world.isRemote) {
                if (!gateTE.isLinked1 || !gateTE.isLinked2) {
                    player.addChatMessage(new ChatComponentText("エラー: 出場駅と入場駅の2つが連携されていません。リンクワンドで設定してください。"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return true;
                }

                if (heldItem.getItem() instanceof ItemTicket) {
                    player.addChatMessage(new ChatComponentText("エラー: この乗り換え改札機は『ICカード専用』です。"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return true;
                }

                // 2つの駅名を取得
                TileEntity te1 = world.getTileEntity(gateTE.linked1X, gateTE.linked1Y, gateTE.linked1Z);
                TileEntity te2 = world.getTileEntity(gateTE.linked2X, gateTE.linked2Y, gateTE.linked2Z);
                if (!(te1 instanceof TileEntityStationManager) || !(te2 instanceof TileEntityStationManager)) {
                    player.addChatMessage(new ChatComponentText("エラー: 連携先の駅ブロックが見つかりません。"));
                    return true;
                }

                String exitStationName = ((TileEntityStationManager) te1).stationName;
                String entryStationName = ((TileEntityStationManager) te2).stationName;

                // 路線登録チェック（2つの駅が両方とも路線に登録されているか）
                KaisatsuNetworkData data = KaisatsuNetworkData.get(world);
                boolean exitRegistered = false;
                boolean entryRegistered = false;
                if (data != null && data.companyLines != null) {
                    for (KaisatsuNetworkData.LineData line : data.companyLines.values()) {
                        if (line.stationOrder.contains(exitStationName)) exitRegistered = true;
                        if (line.stationOrder.contains(entryStationName)) entryRegistered = true;
                    }
                }

                if (!exitRegistered || !entryRegistered) {
                    player.addChatMessage(new ChatComponentText("§cエラー: 連携先の駅が路線に登録されていません。§r"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return true;
                }

                if (heldItem.getItem() instanceof ItemICCard) {
                    processTransfer(world, x, y, z, player, heldItem, exitStationName, entryStationName);
                }
            }
            return true;
        }

        return false;
    }

    private void processTransfer(World world, int x, int y, int z, EntityPlayer player, ItemStack card, String exitStationName, String entryStationName) {
        if (!card.stackTagCompound.getBoolean("inGate")) {
            player.addChatMessage(new ChatComponentText("エラー: 入場記録がありません！"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        String oldEntryStation = card.stackTagCompound.getString("entryStation");

        // ★既に乗換先に入場済みの場合は弾く（ループ防止）
        if (oldEntryStation.equals(entryStationName)) {
            player.addChatMessage(new ChatComponentText("エラー: 既に乗り換え処理は完了しています。このまま目的地へお向かいください。"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        // 1. 出場処理（A線の運賃計算）
        int fare = FareManager.calculateFare(world, oldEntryStation, exitStationName);

        if (fare == -1) {
            // (経路エラー処理)
            return;
        }

        // ★修正: 魔法のカード判定と引き落としスキップ
        boolean isMagic = card.getItem() instanceof ItemMagicICCard;
        int balance = card.stackTagCompound.getInteger("balance");

        if (!isMagic) {
            if (balance < fare) {
                player.addChatMessage(new ChatComponentText("残高不足です！精算機をご利用ください。(運賃: " + fare + "円)"));
                world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                return;
            }
            card.stackTagCompound.setInteger("balance", balance - fare);
        }

        // 2. 決済と同時に「新しい駅からの入場処理」を上書き
        KaisatsuNetworkData data = KaisatsuNetworkData.get(world);
        String newLineID = "";
        String newLineName = "不明な路線";
        if (data != null && data.companyLines != null) {
            for (KaisatsuNetworkData.LineData line : data.companyLines.values()) {
                if (line.stationOrder.contains(entryStationName)) {
                    newLineID = line.lineID;
                    newLineName = line.lineName;
                    break;
                }
            }
        }

        card.stackTagCompound.setInteger("balance", balance - fare);
        card.stackTagCompound.setString("entryLine", newLineID);
        card.stackTagCompound.setString("entryStation", entryStationName);
        // ※inGateはtrueのまま維持される

        player.addChatMessage(new ChatComponentText("§aピピッ！ 乗り換え完了§r"));
        player.addChatMessage(new ChatComponentText("精算: " + fare + "円 (残高: " + (balance - fare) + "円)"));
        player.addChatMessage(new ChatComponentText("入場: [" + newLineName + " : " + entryStationName + "]"));

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

    @Override
    public boolean renderAsNormalBlock() { return false; }
    @Override
    public boolean isOpaqueCube() { return false; }
    @Override
    public int getRenderType() { return -1; }
}
