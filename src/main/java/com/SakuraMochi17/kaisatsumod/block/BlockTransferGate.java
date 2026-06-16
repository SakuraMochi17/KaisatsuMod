package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkManager;
import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.item.ItemMagicICCard;
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

        // =========================================================
        // ★設定ツールを持っている場合は 乗り換え設定GUI を開く
        // =========================================================
        if (heldItem != null && heldItem.getItem() == KaisatsuModMain.settingTool) {
            if (!world.isRemote) {
                KaisatsuNetworkData data = KaisatsuNetworkData.get(world);
                java.util.List<String> stationList = new java.util.ArrayList<>();
                if (data != null && data.globalStations != null) {
                    stationList.addAll(data.globalStations.keySet());
                }

                String curExit = gateTE.exitStationName;
                String curEntry = gateTE.entryStationName;

                KaisatsuModMain.network.sendTo(
                        new com.SakuraMochi17.kaisatsumod.network.MessageOpenTransferSelectGui(x, y, z, curExit, curEntry, stationList),
                        (net.minecraft.entity.player.EntityPlayerMP) player
                );
            }
            return true;
        }

        // =========================================================
        // ICカードを持っていた場合の処理（リンクワンド処理は完全排除）
        // =========================================================
        if (heldItem != null && heldItem.getItem() instanceof ItemICCard) {
            if (!world.isRemote) {
                String exitStationName = gateTE.exitStationName;
                String entryStationName = gateTE.entryStationName;

                // エラー判定
                if (exitStationName == null || exitStationName.equals("未設定") || entryStationName == null || entryStationName.equals("未設定")) {
                    player.addChatMessage(new ChatComponentText("エラー: 出場駅と入場駅の2つが設定されていません。設定ツールを使用してください。"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return true;
                }

                // 路線登録チェック
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

                processTransfer(world, x, y, z, player, heldItem, exitStationName, entryStationName);
            }
            return true;
        }

        return false;
    }

    private void processTransfer(World world, int x, int y, int z, EntityPlayer player, ItemStack card, String exitStationName, String entryStationName) {

        // =========================================================
        // ★追加：新品のICカード（NBTデータが空）だった場合のクラッシュ防止
        // =========================================================
        if (card.stackTagCompound == null) {
            card.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
            card.stackTagCompound.setInteger("balance", 1000); // 初期残高
            card.stackTagCompound.setBoolean("inGate", false);
            card.stackTagCompound.setString("entryLine", "");
            card.stackTagCompound.setString("entryStation", "");
        }

        // 入場記録がない（通常改札を通っていない）場合はエラーを出して弾く
        if (!card.stackTagCompound.getBoolean("inGate")) {
            player.addChatMessage(new ChatComponentText("エラー: 入場記録がありません！まずは普通の改札機から入場してください。"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        String oldEntryStation = card.stackTagCompound.getString("entryStation");

        // 既に乗換先に入場済みの場合は弾く（ループ防止）
        if (oldEntryStation.equals(entryStationName)) {
            player.addChatMessage(new ChatComponentText("エラー: 既に乗り換え処理は完了しています。このまま目的地へお向かいください。"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        // 1. 出場処理（A線の運賃計算）
        int fare = KaisatsuNetworkManager.calculateFare(world, oldEntryStation, exitStationName);
        if (fare == -1) {
            player.addChatMessage(new ChatComponentText("§cエラー: 経路が見つかりません。§r"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        boolean isMagic = card.getItem() instanceof ItemMagicICCard;
        int balance = card.stackTagCompound.getInteger("balance");

        // 通常ICカードの残高確認と引き落とし
        if (!isMagic) {
            if (balance < fare) {
                player.addChatMessage(new ChatComponentText("残高不足です！精算機をご利用ください。(運賃: " + fare + "円 / 残高: " + balance + "円)"));
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

        card.stackTagCompound.setString("entryLine", newLineID);
        card.stackTagCompound.setString("entryStation", entryStationName);
        // ※inGate は true のまま維持されます

        // 3. 処理完了のメッセージと音
        player.addChatMessage(new ChatComponentText("§aピピッ！ 乗り換え完了§r"));
        if (!isMagic) {
            player.addChatMessage(new ChatComponentText("精算: " + fare + "円 (残高: " + (balance - fare) + "円)"));
        }
        player.addChatMessage(new ChatComponentText("入場: [" + newLineName + " : " + entryStationName + "]"));

        world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);

        // ゲートを開ける処理
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