package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessageSaveBlockStation;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

public class GuiStationSelect extends GuiScreen {
    private final int targetX, targetY, targetZ;
    private final String currentStation; // ★追加
    private final List<String> stationList;

    // ★追加：ページネーション用変数
    private int currentPage = 0;
    private final int STATIONS_PER_PAGE = 5;

    public GuiStationSelect(int x, int y, int z, String currentStation, List<String> stations) {
        this.targetX = x; this.targetY = y; this.targetZ = z;
        this.currentStation = currentStation;
        this.stationList = stations;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        this.buttonList.clear();

        int startX = this.width / 2 - 75;
        int startY = 60; // 文字と被らないように少し下に移動

        if (stationList.isEmpty()) {
            this.buttonList.add(new GuiButton(-1, startX, startY, 150, 20, "駅が登録されていません"));
            return;
        }

        // ページ計算
        int maxPage = (stationList.size() - 1) / STATIONS_PER_PAGE;
        if (currentPage < 0) currentPage = 0;
        if (currentPage > maxPage) currentPage = maxPage;

        int startIndex = currentPage * STATIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + STATIONS_PER_PAGE, stationList.size());

        // 現在のページの駅ボタンだけを生成
        for (int i = startIndex; i < endIndex; i++) {
            int btnIndex = i - startIndex;
            this.buttonList.add(new GuiButton(i, startX, startY + (btnIndex * 24), 150, 20, stationList.get(i)));
        }

        // ページ切り替えボタン
        int pageBtnY = startY + (STATIONS_PER_PAGE * 24) + 5;
        GuiButton prevBtn = new GuiButton(100, startX, pageBtnY, 70, 20, "<- 前へ");
        prevBtn.enabled = (currentPage > 0);
        this.buttonList.add(prevBtn);

        GuiButton nextBtn = new GuiButton(101, startX + 80, pageBtnY, 70, 20, "次へ ->");
        nextBtn.enabled = (currentPage < maxPage);
        this.buttonList.add(nextBtn);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == -1) {
            this.mc.displayGuiScreen(null);
        } else if (button.id == 100) {
            currentPage--;
            this.initGui(); // 画面再描画
        } else if (button.id == 101) {
            currentPage++;
            this.initGui(); // 画面再描画
        } else if (button.id >= 0 && button.id < stationList.size()) {
            String selectedStation = stationList.get(button.id);
            KaisatsuModMain.network.sendToServer(new MessageSaveBlockStation(targetX, targetY, targetZ, selectedStation));
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "所属する駅を選択してください", this.width / 2, 15, 16777215);

        // ★追加：現在の設定を黄色い文字で表示
        this.drawCenteredString(this.fontRendererObj, "現在の設定: §e" + this.currentStation + "§r", this.width / 2, 35, 16777215);

        // ページ番号の表示
        if (!stationList.isEmpty()) {
            int maxPage = (stationList.size() - 1) / STATIONS_PER_PAGE;
            this.drawCenteredString(this.fontRendererObj, (currentPage + 1) + " / " + (maxPage + 1) + " ページ", this.width / 2, 60 + (STATIONS_PER_PAGE * 24) + 30, 10526880);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
