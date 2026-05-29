package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData;
import com.SakuraMochi17.kaisatsumod.network.MessagePurchaseTicket;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GuiTicketMachine extends GuiContainer {

    // ★修正1：KaisatsuModMain.MODID を "kaisatsumod" に変更
    // ★修正：ディスペンサーの画像を借りる
    private static final ResourceLocation texture = new ResourceLocation("textures/gui/container/dispenser.png");
    private TileEntityTicketMachine tileEntity;
    private String currentStationName = "未設定";
    private List<Integer> availableFares = new ArrayList<>();

    public GuiTicketMachine(InventoryPlayer inventory, TileEntityTicketMachine te) {
        super(new ContainerTicketMachine(inventory, te));
        this.tileEntity = te;
        // ★修正：標準的なGUIのサイズに戻す
        this.xSize = 176;
        this.ySize = 166;
    }

    // ★修正2：unchecked警告を消すアノテーションを追加
    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        // 1. この券売機がある駅名を取得
        // ※ TileEntityTicketMachine に連携駅名(stationName)が保存されている前提です
        this.currentStationName = this.tileEntity.stationName;
        if (this.currentStationName == null || this.currentStationName.isEmpty()) {
            this.currentStationName = "未設定";
        }

        // 2. ダイクストラ法で全駅までの運賃を計算し、重複のない「金額リスト」を作る
        Set<Integer> fareSet = new TreeSet<>(); // TreeSetを使うと自動的に安い順(昇順)に並びます
        KaisatsuNetworkData data = KaisatsuNetworkData.get(this.mc.theWorld);

        if (data != null && data.companyLines != null && !this.currentStationName.equals("未設定")) {
            for (KaisatsuNetworkData.LineData line : data.companyLines.values()) {
                if (line.stationOrder != null) {
                    for (String targetStation : line.stationOrder) {
                        // 自分自身への運賃(0)や、直通していない駅(-1)は除外
                        int fare = FareManager.calculateFare(this.mc.theWorld, this.currentStationName, targetStation);
                        if (fare > 0) {
                            fareSet.add((int) Math.ceil(fare / 10.0) * 10); // 10円単位に切り上げ
                        }
                    }
                }
            }
        }
        this.availableFares = new ArrayList<>(fareSet);

        // 3. 計算された金額の数だけボタンを自動生成して並べる
        // ★修正：ボタンの配置位置を、小さくなったGUIに合わせて少し中心に寄せる
        int startX = (this.width - this.xSize) / 2 + 8;
        int startY = (this.height - this.ySize) / 2 + 20;
        int col = 0;
        int row = 0;

        for (int i = 0; i < availableFares.size(); i++) {
            int fare = availableFares.get(i);
            // 1行につき3個か4個並べる
            GuiButton btn = new GuiButton(i, startX + (col * 45), startY + (row * 25), 40, 20, fare + "円");
            this.buttonList.add(btn);

            col++;
            if (col >= 3) { // 3個並んだら次の行へ
                col = 0;
                row++;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // 金額ボタンが押されたら、その金額データをサーバーに送って切符を買う！
        if (button.id >= 0 && button.id < availableFares.size()) {
            int selectedFare = availableFares.get(button.id);

            // サーバー(裏側)へ「この券売機で、〇〇円の切符を買いたい」とパケットを飛ばす
            KaisatsuModMain.network.sendToServer(new MessagePurchaseTicket(
                    this.tileEntity.xCoord,
                    this.tileEntity.yCoord,
                    this.tileEntity.zCoord,
                    selectedFare,
                    this.currentStationName
            ));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString("スマート券売機 - " + this.currentStationName + "駅", 10, 10, 4210752);

        if (this.currentStationName.equals("未設定")) {
            this.fontRendererObj.drawString("§cリンクワンドで駅を登録してください§r", 10, 30, 4210752);
        } else if (this.availableFares.isEmpty()) {
            this.fontRendererObj.drawString("§c他の駅への路線が繋がっていません§r", 10, 30, 4210752);
        }
    }
}