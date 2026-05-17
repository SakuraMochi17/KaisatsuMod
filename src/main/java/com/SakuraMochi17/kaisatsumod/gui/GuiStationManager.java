package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessageStationUpdate;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStationManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.util.List;

public class GuiStationManager extends GuiScreen {
    private final TileEntityStationManager tileEntity;

    private List<String> lineList;
    private List<String> stationList;
    private int lineIndex = 0;
    private int stationIndex = 0;

    public GuiStationManager(TileEntityStationManager te) {
        this.tileEntity = te;
    }

    @Override
    public void initGui() {
        super.initGui();
        int x = this.width / 2;
        int y = this.height / 2;

        // 1. 利用可能な路線のリストを取得
        this.lineList = FareManager.getAvailableLines();

        // 現在設定されている路線がリストのどこにあるか探す
        for (int i = 0; i < lineList.size(); i++) {
            if (lineList.get(i).equals(tileEntity.lineID)) {
                lineIndex = i;
                break;
            }
        }

        // 2. 選択中の路線の駅名リストを取得
        updateStationList();

        // 3. ボタンの配置
        // 路線選択ボタン [ < ] [ ID ] [ > ]
        this.buttonList.add(new GuiButton(1, x - 80, y - 50, 20, 20, "<"));
        this.buttonList.add(new GuiButton(2, x + 60, y - 50, 20, 20, ">"));

        // 駅名選択ボタン [ < ] [ NAME ] [ > ]
        this.buttonList.add(new GuiButton(3, x - 80, y - 20, 20, 20, "<"));
        this.buttonList.add(new GuiButton(4, x + 60, y - 20, 20, 20, ">"));

        // 設定完了ボタン
        this.buttonList.add(new GuiButton(0, x - 50, y + 40, 100, 20, "設定完了"));
    }

    private void updateStationList() {
        String currentLine = lineList.get(lineIndex);
        this.stationList = FareManager.getStationsForLine(currentLine);

        // 駅名のインデックスを初期化（現在の設定と一致するものがあれば合わせる）
        stationIndex = 0;
        for (int i = 0; i < stationList.size(); i++) {
            if (stationList.get(i).equals(tileEntity.stationName)) {
                stationIndex = i;
                break;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            // 設定完了：パケット送信
            String finalLine = lineList.get(lineIndex);
            String finalStation = stationList.get(stationIndex);
            KaisatsuModMain.network.sendToServer(new MessageStationUpdate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, finalLine, finalStation));
            this.mc.displayGuiScreen(null);
        }

        // 路線切り替え
        if (button.id == 1) { // 前の路線
            lineIndex = (lineIndex - 1 + lineList.size()) % lineList.size();
            updateStationList();
        }
        if (button.id == 2) { // 次の路線
            lineIndex = (lineIndex + 1) % lineList.size();
            updateStationList();
        }

        // 駅名切り替え
        if (button.id == 3) { // 前の駅
            stationIndex = (stationIndex - 1 + stationList.size()) % stationList.size();
        }
        if (button.id == 4) { // 次の駅
            stationIndex = (stationIndex + 1) % stationList.size();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int x = this.width / 2;
        int y = this.height / 2;

        this.drawCenteredString(this.fontRendererObj, "駅管理ブロック 設定", x, y - 80, 0xFFFFFF);

        // ラベル描画
        this.drawString(this.fontRendererObj, "路線:", x - 120, y - 44, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "駅名:", x - 120, y - 14, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "会社:", x - 120, y + 10, 0xAAAAAA);

        // 選択中の値を描画（ボタンの間のスペース）
        String currentLine = lineList.get(lineIndex);
        String currentStation = stationList.get(stationIndex);
        String currentCompany = FareManager.getCompanyID(currentLine);

        this.drawCenteredString(this.fontRendererObj, currentLine, x + 5, y - 44, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, currentStation, x + 5, y - 14, 0xAAFFFF);
        this.drawString(this.fontRendererObj, "§a" + currentCompany + "§r", x - 55, y + 10, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
