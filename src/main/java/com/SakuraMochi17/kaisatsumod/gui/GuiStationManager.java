package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessageStationUpdate;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStationManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.util.ArrayList;
import java.util.List;

public class GuiStationManager extends GuiScreen {
    private final TileEntityStationManager tileEntity;

    private List<String> lineList;
    private List<String> stationList;
    private List<String> nextStationList; // ★隣接駅選択用のリスト（"未設定"を含む）

    private int lineIndex = 0;
    private int stationIndex = 0;
    private int next1Index = 0; // ★追加
    private int next2Index = 0; // ★追加

    public GuiStationManager(TileEntityStationManager te) {
        this.tileEntity = te;
    }

    @Override
    public void initGui() {
        super.initGui();
        int x = this.width / 2;
        int y = this.height / 2;

        this.lineList = FareManager.getAvailableLines();
        for (int i = 0; i < lineList.size(); i++) {
            if (lineList.get(i).equals(tileEntity.lineID)) {
                lineIndex = i; break;
            }
        }

        updateStationList();

        // 路線選択 [ < ] [ > ]
        this.buttonList.add(new GuiButton(1, x - 80, y - 55, 20, 20, "<"));
        this.buttonList.add(new GuiButton(2, x + 60, y - 55, 20, 20, ">"));
        // 駅名選択
        this.buttonList.add(new GuiButton(3, x - 80, y - 25, 20, 20, "<"));
        this.buttonList.add(new GuiButton(4, x + 60, y - 25, 20, 20, ">"));
        // ★隣接駅1選択
        this.buttonList.add(new GuiButton(5, x - 80, y + 5, 20, 20, "<"));
        this.buttonList.add(new GuiButton(6, x + 60, y + 5, 20, 20, ">"));
        // ★隣接駅2選択
        this.buttonList.add(new GuiButton(7, x - 80, y + 35, 20, 20, "<"));
        this.buttonList.add(new GuiButton(8, x + 60, y + 35, 20, 20, ">"));

        // 設定完了ボタン (位置を少し下げる)
        this.buttonList.add(new GuiButton(0, x - 50, y + 65, 100, 20, "設定完了"));
    }

    private void updateStationList() {
        String currentLine = lineList.get(lineIndex);
        this.stationList = FareManager.getStationsForLine(currentLine);

        // ★隣接駅用リストの作成（先頭に「未設定」を追加）
        this.nextStationList = new ArrayList<>();
        this.nextStationList.add("未設定");
        this.nextStationList.addAll(this.stationList);

        stationIndex = 0;
        next1Index = 0;
        next2Index = 0;

        for (int i = 0; i < stationList.size(); i++) {
            if (stationList.get(i).equals(tileEntity.stationName)) stationIndex = i;
        }
        for (int i = 0; i < nextStationList.size(); i++) {
            if (nextStationList.get(i).equals(tileEntity.nextStation1)) next1Index = i;
            if (nextStationList.get(i).equals(tileEntity.nextStation2)) next2Index = i;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            String finalLine = lineList.get(lineIndex);
            String finalStation = stationList.get(stationIndex);
            String finalNext1 = nextStationList.get(next1Index);
            String finalNext2 = nextStationList.get(next2Index);

            // ★パケットに隣接駅の情報を乗せて送る
            KaisatsuModMain.network.sendToServer(new MessageStationUpdate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, finalLine, finalStation, finalNext1, finalNext2));
            this.mc.displayGuiScreen(null);
        }

        if (button.id == 1) { lineIndex = (lineIndex - 1 + lineList.size()) % lineList.size(); updateStationList(); }
        if (button.id == 2) { lineIndex = (lineIndex + 1) % lineList.size(); updateStationList(); }
        if (button.id == 3) stationIndex = (stationIndex - 1 + stationList.size()) % stationList.size();
        if (button.id == 4) stationIndex = (stationIndex + 1) % stationList.size();
        // ★隣接駅の切り替え処理
        if (button.id == 5) next1Index = (next1Index - 1 + nextStationList.size()) % nextStationList.size();
        if (button.id == 6) next1Index = (next1Index + 1) % nextStationList.size();
        if (button.id == 7) next2Index = (next2Index - 1 + nextStationList.size()) % nextStationList.size();
        if (button.id == 8) next2Index = (next2Index + 1) % nextStationList.size();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int x = this.width / 2;
        int y = this.height / 2;

        this.drawCenteredString(this.fontRendererObj, "駅管理ブロック 設定", x, y - 90, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "§a" + FareManager.getCompanyID(lineList.get(lineIndex)) + "§r", x - 20, y - 75, 0xFFFFFF);

        this.drawString(this.fontRendererObj, "路線:", x - 120, y - 49, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "駅名:", x - 120, y - 19, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "隣接駅1:", x - 125, y + 11, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "隣接駅2:", x - 125, y + 41, 0xFFFFFF);

        this.drawCenteredString(this.fontRendererObj, lineList.get(lineIndex), x, y - 49, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, stationList.get(stationIndex), x, y - 19, 0xAAFFFF);

        // 未設定の場合はグレー、設定済みの場合は黄色で表示
        String next1 = nextStationList.get(next1Index);
        String next2 = nextStationList.get(next2Index);
        this.drawCenteredString(this.fontRendererObj, next1, x, y + 11, next1.equals("未設定") ? 0xAAAAAA : 0xFFFF55);
        this.drawCenteredString(this.fontRendererObj, next2, x, y + 41, next2.equals("未設定") ? 0xAAAAAA : 0xFFFF55);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
