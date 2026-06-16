package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.*;
import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkManager;
import com.SakuraMochi17.kaisatsumod.item.ItemMagicICCard;
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
    public boolean onBlockActivated(net.minecraft.world.World world, int x, int y, int z, net.minecraft.entity.player.EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        net.minecraft.tileentity.TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketGate)) return false;
        com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketGate gateTE = (com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketGate) te;

        net.minecraft.item.ItemStack heldItem = player.getCurrentEquippedItem();

        // =========================================================
        // ★新規追加：設定ツールを持っている場合は駅選択GUIを開く
        // =========================================================
        if (heldItem != null && heldItem.getItem() == KaisatsuModMain.settingTool) {
            // ★修正：この1行を追加！ サーバー側でのみパケット処理を実行させ、クラッシュを防ぎます
            if (!world.isRemote) {
                com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData data = com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.get(world);
                java.util.List<String> stationList = new java.util.ArrayList<>();
                if (data != null && data.globalStations != null) {
                    stationList.addAll(data.globalStations.keySet());
                }

                // ★修正：新しく te を定義せず、一番上で定義済みの gateTE を使って駅名を取得する
                String currentStation = gateTE.stationName;
                if (currentStation == null || currentStation.isEmpty()) {
                    currentStation = "未設定";
                }

                KaisatsuModMain.network.sendTo(
                        new com.SakuraMochi17.kaisatsumod.network.MessageOpenStationSelectGui(x, y, z, currentStation, stationList),
                        (net.minecraft.entity.player.EntityPlayerMP) player
                );
            }
            return true; // クライアント側はここで処理を終了する（腕を振るアニメーションだけ出る）
        }

        // =========================================================
        // 既存：スニーク＋素手でモード変更
        // =========================================================
        if (heldItem == null && player.isSneaking()) {
            if (!world.isRemote) {
                gateTE.gateMode = (gateTE.gateMode + 1) % 3;
                gateTE.markDirty();
                String[] modes = {"双方向", "入場専用", "出場専用"};
                player.addChatMessage(new net.minecraft.util.ChatComponentText("改札モード: " + modes[gateTE.gateMode]));
            }
            return true;
        }

        // =========================================================
        // 既存：ICカードや切符を持っていた場合の処理
        // =========================================================
        if (heldItem != null && (heldItem.getItem() instanceof com.SakuraMochi17.kaisatsumod.item.ItemICCard || heldItem.getItem() instanceof com.SakuraMochi17.kaisatsumod.item.ItemTicket)) {
            if (!world.isRemote) {

                // ★修正：新システム（設定ツール）の駅名のみを取得（リンクワンド処理を除外）
                String currentStationName = gateTE.stationName;

                // 駅名が設定されていない場合はエラー
                if (currentStationName == null || currentStationName.equals("未設定")) {
                    player.addChatMessage(new net.minecraft.util.ChatComponentText("エラー: 駅が設定されていません。設定ツールを使用してください。"));
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return true;
                }

                // 会社データ自体がまだ存在しない（Null）場合のクラッシュを防ぐ鉄壁ガード
                com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData data = com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.get(world);
                boolean isRegistered = false;
                if (data != null && data.companyLines != null) {
                    for (com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData.LineData line : data.companyLines.values()) {
                        if (line != null && line.stationOrder != null && line.stationOrder.contains(currentStationName)) {
                            isRegistered = true;
                            break;
                        }
                    }
                }

                // 路線管理ブロックにこの駅名が登録されていない場合は弾く
                if (!isRegistered) {
                    net.minecraft.util.ChatComponentText errorMsg = new net.minecraft.util.ChatComponentText("§cピンポーン♪ エラー: この駅(");
                    errorMsg.appendText("§c" + currentStationName);
                    errorMsg.appendText("§c)はまだどの路線にも登録されていません！§r");

                    player.addChatMessage(errorMsg);
                    world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                    return true;
                }

                // --- ここを通過できた＝いつでもリンクOKかつ路線登録も完了している安全な状態 ---
                if (heldItem.getItem() instanceof com.SakuraMochi17.kaisatsumod.item.ItemICCard) {
                    if (heldItem.stackTagCompound == null) {
                        heldItem.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
                        heldItem.stackTagCompound.setInteger("balance", 1000);
                        heldItem.stackTagCompound.setBoolean("inGate", false);
                        heldItem.stackTagCompound.setString("entryLine", "");
                        heldItem.stackTagCompound.setString("entryStation", "");
                    }
                    boolean inGate = heldItem.stackTagCompound.getBoolean("inGate");
                    boolean attemptEntry = gateTE.gateMode == 1 || (gateTE.gateMode != 2 && !inGate);

                    if (attemptEntry) processEntry(world, x, y, z, player, heldItem, currentStationName);
                    else processExit(world, x, y, z, player, heldItem, currentStationName);
                }
                else if (heldItem.getItem() instanceof com.SakuraMochi17.kaisatsumod.item.ItemTicket) {
                    if (heldItem.stackTagCompound == null) {
                        player.addChatMessage(new net.minecraft.util.ChatComponentText("エラー: 不正な切符です。"));
                        return true;
                    }
                    boolean isUsed = heldItem.stackTagCompound.getBoolean("isUsed");
                    boolean attemptEntry = gateTE.gateMode == 1 || (gateTE.gateMode != 2 && !isUsed);

                    if (attemptEntry) processTicketEntry(world, x, y, z, player, heldItem, currentStationName);
                    else processTicketExit(world, x, y, z, player, heldItem, currentStationName);
                }
            }
            return true;
        }
        return false;
    }

    // --- ICカード入場処理 ---
    private void processEntry(World world, int x, int y, int z, EntityPlayer player, ItemStack card, String currentStationName) {
        if (card.stackTagCompound == null) card.setTagCompound(new NBTTagCompound());

        if (card.stackTagCompound.getBoolean("inGate")) {
            player.addChatMessage(new ChatComponentText("エラー: 既に入場状態です！"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        // ★修正: 魔法のカードではない場合のみ残高チェックを行う
        boolean isMagic = card.getItem() instanceof ItemMagicICCard;
        if (!isMagic && card.stackTagCompound.getInteger("balance") < 150) {
            player.addChatMessage(new ChatComponentText("残高不足 (150円以上必要)"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        // ... (以降の処理はそのまま)

        // 駅の属する路線IDと名前を引き出す
        // --- 修正前（削除またはコメントアウトしてください） ---
        // --------------------------------------------------

        card.stackTagCompound.setBoolean("inGate", true);
        card.stackTagCompound.setString("entryStation", currentStationName);

        // ★修正：チャットのメッセージから路線名を消し、駅名だけにします
        player.addChatMessage(new ChatComponentText("ピッ！ 入場 [" + currentStationName + "]"));
        world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
        openGate(world, x, y, z);
    }

    // --- ICカード出場処理 ---
    private void processExit(World world, int x, int y, int z, EntityPlayer player, ItemStack card, String currentStationName) {
        if (!card.stackTagCompound.getBoolean("inGate")) {
            player.addChatMessage(new ChatComponentText("エラー: 入場記録がありません！"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        String entryStation = card.stackTagCompound.getString("entryStation");

        // 新FareManagerによる距離連動運賃計算
        int fare = KaisatsuNetworkManager.calculateFare(world, entryStation, currentStationName);

        if (fare == -1) {
            // ★修正：システムエラーではなく、正規の「経路（直通）エラー」として弾く
            ChatComponentText errorMsg = new ChatComponentText("§cピンポーン♪ エラー: 乗車駅(");
            errorMsg.appendText("§c" + entryStation);
            errorMsg.appendText("§c)からこの駅までは直通していません！乗り換え改札をご利用ください。§r");

            player.addChatMessage(errorMsg);
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        // ★修正: 魔法のカード判定と、引き落としのスキップ
        boolean isMagic = card.getItem() instanceof ItemMagicICCard;
        int balance = card.stackTagCompound.getInteger("balance");

        if (!isMagic) {
            if (balance < fare) {
                player.addChatMessage(new ChatComponentText("残高不足です！ (運賃: " + fare + "円)"));
                world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
                return;
            }
            card.stackTagCompound.setInteger("balance", balance - fare);
        }

        card.stackTagCompound.setBoolean("inGate", false);

        // ★表示も魔法のカード用に少し変えると分かりやすいです
        String fareText = isMagic ? "§d魔法パス§r" : fare + "円";
        String balanceText = isMagic ? "§d∞§r" : (balance - fare) + "円";

        player.addChatMessage(new ChatComponentText("ピッ！ 出場 (" + entryStation + " → " + currentStationName + " / 運賃: " + fareText + " / 残高: " + balanceText + ")"));
        world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
        openGate(world, x, y, z);
    }

    // --- 切符入場処理 ---
    private void processTicketEntry(World world, int x, int y, int z, EntityPlayer player, ItemStack ticket, String currentStationName) {
        if (ticket.stackTagCompound.getBoolean("isUsed")) {
            player.addChatMessage(new ChatComponentText("エラー: 既に入場済みの切符です。"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        String buyStation = ticket.stackTagCompound.getString("entryStation");

        if (!buyStation.equals(currentStationName)) {
            player.addChatMessage(new ChatComponentText("エラー: 購入駅以外の改札口からは入場できません！ (購入駅: " + buyStation + ")"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        ticket.stackTagCompound.setBoolean("isUsed", true);

        player.addChatMessage(new ChatComponentText("§aガチャン！ 入場 [切符 : " + currentStationName + "]"));
        world.playSoundAtEntity(player, "random.click", 1.0F, 1.5F);
        openGate(world, x, y, z);
    }

    // --- 切符出場処理 ---
    private void processTicketExit(World world, int x, int y, int z, EntityPlayer player, ItemStack ticket, String currentStationName) {
        if (!ticket.stackTagCompound.getBoolean("isUsed")) {
            player.addChatMessage(new ChatComponentText("エラー: 入場記録がありません！"));
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        String buyStation = ticket.stackTagCompound.getString("entryStation");
        boolean isNyujoken = ticket.stackTagCompound.getBoolean("isNyujoken");

        if (isNyujoken) {
            if (buyStation.equals(currentStationName)) {
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

        // 新FareManagerによる運賃計算
        int requiredFare = KaisatsuNetworkManager.calculateFare(world, buyStation, currentStationName);

        // ★追加：切符でも直通していない駅で降りようとしたら弾くガード
        if (requiredFare == -1) {
            ChatComponentText errorMsg = new ChatComponentText("§cピンポーン♪ エラー: 乗車駅(");
            errorMsg.appendText("§c" + buyStation);
            errorMsg.appendText("§c)からこの駅までは直通していません！乗り換え改札をご利用ください。§r");

            player.addChatMessage(errorMsg);
            world.playSoundAtEntity(player, "note.bassattack", 1.0F, 0.5F);
            return;
        }

        int roundedRequiredFare = (int) Math.ceil(requiredFare / 10.0) * 10;
        int ticketValue = ticket.stackTagCompound.getInteger("fare");

        if (roundedRequiredFare > ticketValue) {
            player.addChatMessage(new ChatComponentText("運賃が不足しています！ (不足: " + (roundedRequiredFare - ticketValue) + "円)"));
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
            ticket.stackTagCompound.setString("entryStation", currentStationName);
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

    @Override
    public boolean renderAsNormalBlock() { return false; }
    @Override
    public boolean isOpaqueCube() { return false; }
    @Override
    public int getRenderType() { return -1; }
}
