package com.SakuraMochi17.kaisatsumod;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import java.util.List;

public class GuiStationManager extends GuiContainer {
    private final TileEntityStationManager tileEntity;

    private List<String> availableLines;
    private List<String> availableStations;
    private int lineIndex = 0;
    private int stationIndex = 0;

    private GuiButton buttonLine;
    private GuiButton buttonStation;

    public GuiStationManager(TileEntityStationManager tile) {
        super(new ContainerStationManager());
        this.tileEntity = tile;
        this.xSize = 176;
        this.ySize = 96;
    }

    @Override
    public void initGui() {
        super.initGui();

        availableLines = FareManager.getAvailableLines();
        if (availableLines.isEmpty()) availableLines.add("データなし");

        // 現在保存されている路線IDのインデックスを取得
        lineIndex = Math.max(0, availableLines.indexOf(tileEntity.lineID));

        // ★改善：初期化時は駅選択を0にリセットせず、保存されている駅名から位置を取得する
        String currentLine = availableLines.get(lineIndex);
        availableStations = FareManager.getStationsForLine(currentLine);
        if (availableStations.isEmpty()) availableStations.add("駅データなし");

        stationIndex = Math.max(0, availableStations.indexOf(tileEntity.stationName));

        // 切替ボタンの配置
        buttonLine = new GuiButton(1, this.guiLeft + 50, this.guiTop + 18, 110, 20, getLineDisplay());
        buttonStation = new GuiButton(2, this.guiLeft + 50, this.guiTop + 42, 110, 20, getStationDisplay());

        this.buttonList.add(buttonLine);
        this.buttonList.add(buttonStation);
        this.buttonList.add(new GuiButton(0, this.guiLeft + 50, this.guiTop + 68, 80, 20, "設定完了"));
    }

    // ★改善：路線ボタンを自分でクリックした「時だけ」駅の選択を先頭（0）にリセットする
    private void onLineChanged() {
        String currentLine = availableLines.get(lineIndex);
        availableStations = FareManager.getStationsForLine(currentLine);
        if (availableStations.isEmpty()) availableStations.add("駅データなし");
        stationIndex = 0;
    }

    private String getLineDisplay() {
        String lineID = availableLines.get(lineIndex);
        return FareManager.getLineName(lineID);
    }

    private String getStationDisplay() {
        return availableStations.get(stationIndex);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            lineIndex = (lineIndex + 1) % availableLines.size();
            onLineChanged();
            buttonLine.displayString = getLineDisplay();
            buttonStation.displayString = getStationDisplay();
        }
        else if (button.id == 2) {
            stationIndex = (stationIndex + 1) % availableStations.size();
            buttonStation.displayString = getStationDisplay();
        }
        else if (button.id == 0) {
            String selectedLine = availableLines.get(lineIndex);
            String selectedStation = availableStations.get(stationIndex);

            KaisatsuModMain.network.sendToServer(new MessageStationUpdate(
                    tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord,
                    selectedLine, selectedStation
            ));
            this.mc.thePlayer.closeScreen();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        drawRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, 0xCC000000);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString("路線:", 15, 24, 0xFFFFFF);
        this.fontRendererObj.drawString("駅名:", 15, 48, 0xFFFFFF);
    }
}
