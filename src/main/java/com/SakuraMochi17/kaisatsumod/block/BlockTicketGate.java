package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.*;
import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.core.StationRegistry;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.item.ItemTicket;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketGate;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import java.util.Random;

public class BlockTicketGate extends BlockContainer {

    private static final int BIT_OPEN = 4;

    public BlockTicketGate() {
        super(Material.iron);
        this.setBlockName("ticketGate");
        this.setHardness(3.0F);
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityTicketGate(); }

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
        if (!(te instanceof TileEntityTicketGate)) return false;
        TileEntityTicketGate gateTE = (TileEntityTicketGate) te;

        ItemStack heldItem = player.getCurrentEquippedItem();

        // モード切替
        if (heldItem == null && player.isSneaking()) {
            if (!world.isRemote) {
                gateTE.gateMode = (gateTE.gateMode + 1) % 3;
                gateTE.markDirty();
                String[] modes = {"双方向", "入場専用", "出場専用"};
                player.addChatMessage(new ChatComponentText("改札モード: " + modes[gateTE.gateMode]));
            }
            return true;
        }

        // 未連携チェック
        if (heldItem != null && (heldItem.getItem() instanceof ItemICCard || heldItem.getItem() instanceof ItemTicket)) {
            if (!world.isRemote) {
                if (!gateTE.isLinked) {
                    player.addChatMessage(new ChatComponentText("エラー: 駅と連携されていません。リンクワンドを使用してください♡"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return true;
                }

                // --- ICカードの処理 ---
                if (heldItem.getItem() instanceof ItemICCard) {
                    if (heldItem.stackTagCompound == null) {
                        heldItem.setTagCompound(new NBTTagCompound());
                        heldItem.stackTagCompound.setInteger("balance", 1000);
                        heldItem.stackTagCompound.setBoolean("inGate", false);
                        heldItem.stackTagCompound.setString("entryLine", "");
                        heldItem.stackTagCompound.setString("entryStation", "");
                    }
                    boolean inGate = heldItem.stackTagCompound.getBoolean("inGate");

                    // ★警告修正：論理式をシンプルな形に最適化
                    boolean attemptEntry = gateTE.gateMode == 1 || (gateTE.gateMode != 2 && !inGate);

                    if (attemptEntry) processEntry(world, x, y, z, player, heldItem, gateTE);
                    else processExit(world, x, y, z, player, heldItem, gateTE);
                }
                // --- 切符の処理 ---
                else if (heldItem.getItem() instanceof ItemTicket) {
                    if (heldItem.stackTagCompound == null) {
                        player.addChatMessage(new ChatComponentText("エラー: 不正な切符です。"));
                        return true;
                    }
                    boolean isUsed = heldItem.stackTagCompound.getBoolean("isUsed");

                    // ★警告修正：論理式をシンプルな形に最適化
                    boolean attemptEntry = gateTE.gateMode == 1 || (gateTE.gateMode != 2 && !isUsed);

                    if (attemptEntry) processTicketEntry(world, x, y, z, player, heldItem, gateTE);
                    else processTicketExit(world, x, y, z, player, heldItem, gateTE);
                }
            }
            return true;
        }
        return false;
    }

    private void processEntry(World world, int x, int y, int z, EntityPlayer player, ItemStack card, TileEntityTicketGate gateTE) {
        if (card.stackTagCompound.getBoolean("inGate")) {
            player.addChatMessage(new ChatComponentText("エラー: 既に入場状態です！"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }
        if (card.stackTagCompound.getInteger("balance") < 150) {
            player.addChatMessage(new ChatComponentText("残高不足 (150円以上必要)"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        StationRegistry.StationData station = StationRegistry.findNearestStation(world.provider.dimensionId, gateTE.linkedX, gateTE.linkedY, gateTE.linkedZ, 1.0);
        if (station == null) {
            player.addChatMessage(new ChatComponentText("エラー: 連携先の駅データが見つかりません。再連携してください。"));
            return;
        }

        card.stackTagCompound.setBoolean("inGate", true);
        card.stackTagCompound.setString("entryLine", station.lineID);
        card.stackTagCompound.setString("entryStation", station.stationName);
        card.stackTagCompound.setInteger("entryX", station.x);
        card.stackTagCompound.setInteger("entryY", station.y);
        card.stackTagCompound.setInteger("entryZ", station.z);

        player.addChatMessage(new ChatComponentText("ピッ！ 入場 [" + FareManager.getLineName(station.lineID) + " : " + station.stationName + "]"));
        world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
        openGate(world, x, y, z);
    }

    // ==========================================
    // ICカード出場処理 (会社またぎ対応)
    // ==========================================
    private void processExit(World world, int x, int y, int z, EntityPlayer player, ItemStack card, TileEntityTicketGate gateTE) {
        if (!card.stackTagCompound.getBoolean("inGate")) {
            player.addChatMessage(new ChatComponentText("エラー: 入場記録がありません！"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        StationRegistry.StationData station = StationRegistry.findNearestStation(world.provider.dimensionId, gateTE.linkedX, gateTE.linkedY, gateTE.linkedZ, 1.0);
        if (station == null) return;

        String entryLine = card.stackTagCompound.getString("entryLine");
        String entryStation = card.stackTagCompound.getString("entryStation");
        int entryX = card.stackTagCompound.getInteger("entryX");
        int entryY = card.stackTagCompound.getInteger("entryY");
        int entryZ = card.stackTagCompound.getInteger("entryZ");

        String entryCompany = FareManager.getCompanyID(entryLine);
        String exitCompany = FareManager.getCompanyID(station.lineID);

        int fare;
        // ★ 会社が同じなら通常計算、違うなら平均単価で計算！
        if (entryCompany.equals(exitCompany)) {
            fare = FareManager.calculateFare(station.lineID, entryX, entryY, entryZ, station.x, station.y, station.z);
        } else {
            fare = FareManager.calculateCrossCompanyFare(entryLine, station.lineID, entryX, entryY, entryZ, station.x, station.y, station.z);
        }

        // ★修正: 計算不能時に無言で終わらず、エラーで知らせる
        if (fare == -1) {
            player.addChatMessage(new net.minecraft.util.ChatComponentText("§cシステムエラー: 運賃計算に失敗しました。(路線データ '" + entryLine + "' または '" + station.lineID + "' が存在しません)"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        int balance = card.stackTagCompound.getInteger("balance");
        if (balance < fare) {
            player.addChatMessage(new ChatComponentText("残高不足です！精算機でチャージしてください。(運賃: " + fare + "円)"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        card.stackTagCompound.setInteger("balance", balance - fare);
        card.stackTagCompound.setBoolean("inGate", false);

        // 他社線の場合はメッセージに特別感を出す
        String companyText = entryCompany.equals(exitCompany) ? "" : " §e[他社線直通]§r";

        player.addChatMessage(new ChatComponentText("ピッ！ 出場 (" + FareManager.getLineName(station.lineID) + " " + entryStation + " → " + station.stationName + companyText + " / 運賃: " + fare + "円 / 残高: " + (balance - fare) + "円)"));
        world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
        openGate(world, x, y, z);
    }

    private void processTicketEntry(World world, int x, int y, int z, EntityPlayer player, ItemStack ticket, TileEntityTicketGate gateTE) {
        if (ticket.stackTagCompound.getBoolean("isUsed")) {
            player.addChatMessage(new ChatComponentText("エラー: 既に入場済みの切符です。"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        StationRegistry.StationData station = StationRegistry.findNearestStation(world.provider.dimensionId, gateTE.linkedX, gateTE.linkedY, gateTE.linkedZ, 1.0);
        if (station == null) return;

        String buyStation = ticket.stackTagCompound.getString("entryStation");

        if (!buyStation.equals(station.stationName)) {
            player.addChatMessage(new ChatComponentText("エラー: 購入駅以外の改札口からは入場できません！ (購入駅: " + buyStation + ")"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        ticket.stackTagCompound.setBoolean("isUsed", true);
        ticket.stackTagCompound.setInteger("entryX", station.x);
        ticket.stackTagCompound.setInteger("entryY", station.y);
        ticket.stackTagCompound.setInteger("entryZ", station.z);

        player.addChatMessage(new ChatComponentText("§aガチャン！ 入場 [切符 : " + station.stationName + "]"));
        world.playSoundAtEntity(player, "random.click", 1.0F, 1.5F);
        openGate(world, x, y, z);
    }

    // ==========================================
    // 切符出場処理 (同社線のみに制限)
    // ==========================================
    private void processTicketExit(World world, int x, int y, int z, EntityPlayer player, ItemStack ticket, TileEntityTicketGate gateTE) {
        if (!ticket.stackTagCompound.getBoolean("isUsed")) {
            player.addChatMessage(new ChatComponentText("エラー: 入場記録がありません！"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        StationRegistry.StationData station = StationRegistry.findNearestStation(world.provider.dimensionId, gateTE.linkedX, gateTE.linkedY, gateTE.linkedZ, 1.0);
        if (station == null) return;

        String buyStation = ticket.stackTagCompound.getString("entryStation");
        boolean isNyujoken = ticket.stackTagCompound.getBoolean("isNyujoken");

        if (isNyujoken) {
            if (buyStation.equals(station.stationName)) {
                player.addChatMessage(new ChatComponentText("§bガチャン！ 出場 (入場券回収)"));
                world.playSoundAtEntity(player, "random.click", 1.0F, 1.0F);
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                openGate(world, x, y, z);
            } else {
                player.addChatMessage(new ChatComponentText("エラー: 入場券は他の駅では出場できません。元の駅へお戻りください。"));
                world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            }
            return;
        }

        String entryLine = ticket.stackTagCompound.getString("entryLine");
        String entryCompany = FareManager.getCompanyID(entryLine);
        String exitCompany = FareManager.getCompanyID(station.lineID);

        // ★ 切符は他社線の改札では絶対に降りられないようにブロックする
        if (!entryCompany.equals(exitCompany)) {
            player.addChatMessage(new ChatComponentText("エラー: この切符は他社線では精算できません。乗り越し精算機か窓口をご利用ください。"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        int entryX = ticket.stackTagCompound.getInteger("entryX");
        int entryY = ticket.stackTagCompound.getInteger("entryY");
        int entryZ = ticket.stackTagCompound.getInteger("entryZ");

        int requiredFare = FareManager.calculateFare(entryLine, entryX, entryY, entryZ, station.x, station.y, station.z);
        int roundedRequiredFare = (int) Math.ceil(requiredFare / 10.0) * 10;
        int ticketValue = ticket.stackTagCompound.getInteger("fare");

        if (roundedRequiredFare > ticketValue) {
            player.addChatMessage(new ChatComponentText("運賃が不足しています！のりこし精算をしてください。 (不足: " + (roundedRequiredFare - ticketValue) + "円)"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
        } else if (roundedRequiredFare == ticketValue) {
            player.addChatMessage(new ChatComponentText("§bガチャン！ 出場 (切符が回収されました)"));
            world.playSoundAtEntity(player, "random.click", 1.0F, 1.0F);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            openGate(world, x, y, z);
        } else {
            int remainValue = ticketValue - roundedRequiredFare;
            ticket.stackTagCompound.setInteger("fare", remainValue);
            ticket.stackTagCompound.setBoolean("isUsed", false);
            ticket.stackTagCompound.setString("entryStation", station.stationName);
            player.addChatMessage(new ChatComponentText("§eピッ！ 途中下車 (区間運賃 " + roundedRequiredFare + "円を差し引きました。 残高: " + remainValue + "円)"));
            world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
            openGate(world, x, y, z);
        }
    }

    private void openGate(World world, int x, int y, int z) {
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
    // --- 既存のコードの下に以下を追記 ---

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
