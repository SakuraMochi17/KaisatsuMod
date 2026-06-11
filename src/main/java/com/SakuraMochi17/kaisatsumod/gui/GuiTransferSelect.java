package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessageSaveTransferStation;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

public class GuiTransferSelect extends GuiScreen {
    private final int targetX, targetY, targetZ;
    private final List<String> stationList;
    private String selectedExit;
    private String selectedEntry;
    private boolean isSelectingExit = true;

    // ★追加：ページネーション用変数
    private int currentPage = 0;
    private final int STATIONS_PER_PAGE = 5;

    public GuiTransferSelect(int x, int y, int z, String currentExit, String currentEntry, List<String> stations) {
        this.targetX = x; this.targetY = y; this.targetZ = z;
        this.selectedExit = currentExit != null ? currentExit : "未設定";
        this.selectedEntry = currentEntry != null ? currentEntry : "未設定";
        this.stationList = stations;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;

        String modeText = isSelectingExit ? "§e[選択中] 乗り換え元 (出場) を選ぶ§r" : "§a[選択中] 乗り換え先 (入場) を選ぶ§r";
        this.buttonList.add(new GuiButton(1000, centerX - 100, 25, 200, 20, modeText));
        this.buttonList.add(new GuiButton(1001, centerX - 100, this.height - 30, 200, 20, "確定して閉じる"));

        int startX = centerX - 75;
        int startY = 85;

        if (stationList.isEmpty()) return;

        // ページ計算
        int maxPage = (stationList.size() - 1) / STATIONS_PER_PAGE;
        if (currentPage < 0) currentPage = 0;
        if (currentPage > maxPage) currentPage = maxPage;

        int startIndex = currentPage * STATIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + STATIONS_PER_PAGE, stationList.size());

        for (int i = startIndex; i < endIndex; i++) {
            int btnIndex = i - startIndex;
            this.buttonList.add(new GuiButton(i, startX, startY + (btnIndex * 22), 150, 20, stationList.get(i)));
        }

        // ページ切り替えボタン
        int pageBtnY = startY + (STATIONS_PER_PAGE * 22) + 5;
        GuiButton prevBtn = new GuiButton(1002, startX, pageBtnY, 70, 20, "<- 前へ");
        prevBtn.enabled = (currentPage > 0);
        this.buttonList.add(prevBtn);

        GuiButton nextBtn = new GuiButton(1003, startX + 80, pageBtnY, 70, 20, "次へ ->");
        nextBtn.enabled = (currentPage < maxPage);
        this.buttonList.add(nextBtn);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1000) {
            this.isSelectingExit = !this.isSelectingExit;
            this.initGui();
        } else if (button.id == 1001) {
            KaisatsuModMain.network.sendToServer(new MessageSaveTransferStation(targetX, targetY, targetZ, selectedExit, selectedEntry));
            this.mc.displayGuiScreen(null);
        } else if (button.id == 1002) {
            currentPage--;
            this.initGui();
        } else if (button.id == 1003) {
            currentPage++;
            this.initGui();
        } else if (button.id >= 0 && button.id < stationList.size()) {
            if (isSelectingExit) selectedExit = stationList.get(button.id);
            else selectedEntry = stationList.get(button.id);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "乗り換え改札機 連携設定", this.width / 2, 10, 16777215);

        this.drawString(this.fontRendererObj, "乗り換え元 (出場): " + selectedExit, this.width / 2 - 100, 55, 0xFFFF55);
        this.drawString(this.fontRendererObj, "乗り換え先 (入場): " + selectedEntry, this.width / 2 - 100, 70, 0x55FF55);

        if (!stationList.isEmpty()) {
            int maxPage = (stationList.size() - 1) / STATIONS_PER_PAGE;
            this.drawCenteredString(this.fontRendererObj, (currentPage + 1) + " / " + (maxPage + 1) + " ページ", this.width / 2, 85 + (STATIONS_PER_PAGE * 22) + 30, 10526880);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}