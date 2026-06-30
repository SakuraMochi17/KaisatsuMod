package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessageLineUpdate;
import com.SakuraMochi17.kaisatsumod.network.MessageOpenLineGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiLineManager extends GuiScreen {
    private final MessageOpenLineGui data;

    // 0: トップページ, 1: 路線編集ページ(設定/新規), 2: 会社名編集ページ
    private int guiPage = 0;

    // トップページ用変数
    private int selectedLineIndex = 0;

    // 会社名編集ページ用UI
    private GuiTextField compField;

    // 路線編集ページ用UI
    private GuiTextField idField, nameField, baseField, costField;
    private final List<String> globalStations;
    private List<String> editLineStations;
    private int globalIndex = 0;
    private int stationIndex = 0;
    private String currentOldLineID = "";

    public GuiLineManager(MessageOpenLineGui message) {
        this.data = message;
        this.globalStations = message.globalStations;
        if (this.globalStations.isEmpty()) this.globalStations.add("駅が見つかりません");
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        @SuppressWarnings("unchecked")
        java.util.List<GuiButton> buttons = this.buttonList;
        int x = this.width / 2;
        int y = this.height / 2;

        if (guiPage == 0) {
            // ===================================
            // 【 0: トップページ 】
            // ===================================
            buttons.add(new GuiButton(15, x + 60, y - 62, 50, 20, "編集"));

            buttons.add(new GuiButton(11, x - 100, y + 5, 20, 20, "<"));
            buttons.add(new GuiButton(12, x + 80, y + 5, 20, 20, ">"));

            GuiButton btnEdit = new GuiButton(13, x - 100, y + 40, 90, 20, "設定画面へ ->");
            if (data.companyLines.isEmpty()) btnEdit.enabled = false;
            buttons.add(btnEdit);

            buttons.add(new GuiButton(14, x + 10, y + 40, 90, 20, "+ 新規路線作成"));
        }
        else if (guiPage == 1) {
            // ===================================
            // 【 1: 路線編集ページ（設定/新規） 】
            // ===================================
            // 会社名の入力枠を削除し、レイアウトを中央に寄せて調整
            this.idField = new GuiTextField(this.fontRendererObj, x - 110, y - 80, 90, 15);
            this.nameField = new GuiTextField(this.fontRendererObj, x + 10, y - 80, 90, 15);

            this.baseField = new GuiTextField(this.fontRendererObj, x - 60, y - 45, 50, 15);
            this.costField = new GuiTextField(this.fontRendererObj, x + 50, y - 45, 50, 15);

            // 編集モードでのフィールド復元
            if (!currentOldLineID.isEmpty()) {
                this.idField.setText(currentOldLineID);
                for (MessageOpenLineGui.LineInfo info : data.companyLines) {
                    if (info.lineID.equals(currentOldLineID)) {
                        this.nameField.setText(info.lineName);
                        this.baseField.setText(String.valueOf(info.baseFare));
                        this.costField.setText(String.valueOf(info.costPerBlock));
                        break;
                    }
                }
            } else {
                this.baseField.setText("150");
                this.costField.setText("0.15");
            }

            buttons.add(new GuiButton(1, x - 115, y + 20, 20, 20, "<"));
            buttons.add(new GuiButton(2, x - 25, y + 20, 20, 20, ">"));
            buttons.add(new GuiButton(3, x - 100, y + 50, 80, 20, "路線に追加 ->"));

            buttons.add(new GuiButton(4, x + 95, y - 5, 20, 20, "∧"));
            buttons.add(new GuiButton(5, x + 95, y + 25, 20, 20, "∨"));
            buttons.add(new GuiButton(6, x + 120, y - 5, 30, 20, "上へ"));
            buttons.add(new GuiButton(7, x + 120, y + 25, 30, 20, "下へ"));
            buttons.add(new GuiButton(8, x + 95, y + 50, 55, 20, "削除"));

            buttons.add(new GuiButton(20, x - 130, y + 85, 70, 20, "<- トップへ"));
            buttons.add(new GuiButton(9, x - 50, y + 85, 60, 20, "路線を削除"));
            buttons.add(new GuiButton(0, x + 20, y + 85, 110, 20, "設定を保存して終了"));
        }
        else if (guiPage == 2) {
            // ===================================
            // 【 2: 会社名 編集ページ 】
            // ===================================
            this.compField = new GuiTextField(this.fontRendererObj, x - 50, y - 20, 100, 15);
            this.compField.setText(data.companyName);
            this.compField.setFocused(true);

            buttons.add(new GuiButton(10, x - 55, y + 10, 50, 20, "保存"));
            buttons.add(new GuiButton(16, x + 5, y + 10, 50, 20, "ｷｬﾝｾﾙ"));
        }
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (guiPage == 2 && compField.textboxKeyTyped(typedChar, keyCode)) return;
        if (guiPage == 1 && (idField.textboxKeyTyped(typedChar, keyCode) || nameField.textboxKeyTyped(typedChar, keyCode) ||
                baseField.textboxKeyTyped(typedChar, keyCode) || costField.textboxKeyTyped(typedChar, keyCode))) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (guiPage == 2) compField.mouseClicked(mouseX, mouseY, mouseButton);
        if (guiPage == 1) {
            idField.mouseClicked(mouseX, mouseY, mouseButton);
            nameField.mouseClicked(mouseX, mouseY, mouseButton);
            baseField.mouseClicked(mouseX, mouseY, mouseButton);
            costField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // --- トップページのボタン ---
        if (button.id == 15) { // 編集ボタン
            guiPage = 2;
            this.initGui();
        }
        if (button.id == 11 && !data.companyLines.isEmpty()) {
            selectedLineIndex = (selectedLineIndex - 1 + data.companyLines.size()) % data.companyLines.size();
        }
        if (button.id == 12 && !data.companyLines.isEmpty()) {
            selectedLineIndex = (selectedLineIndex + 1) % data.companyLines.size();
        }
        if (button.id == 13) {
            // 設定画面へ
            currentOldLineID = data.companyLines.get(selectedLineIndex).lineID;
            editLineStations = new ArrayList<>(data.companyLines.get(selectedLineIndex).stations);
            guiPage = 1;
            this.initGui();
        }
        if (button.id == 14) {
            // 新規作成画面へ
            currentOldLineID = "";
            editLineStations = new ArrayList<>();
            guiPage = 1;
            this.initGui();
        }

        // --- 会社名編集ページのボタン ---
        if (button.id == 10) { // 保存してトップへ戻る
            MessageLineUpdate msg = new MessageLineUpdate();
            msg.mode = 0; msg.x = data.x; msg.y = data.y; msg.z = data.z;
            msg.companyName = compField.getText();
            data.companyName = msg.companyName; // ローカルのデータも更新
            KaisatsuModMain.network.sendToServer(msg);
            guiPage = 0;
            this.initGui();
        }
        if (button.id == 16) { // キャンセルしてトップへ戻る
            guiPage = 0;
            this.initGui();
        }

        // --- 路線編集ページのボタン ---
        boolean isLoop = guiPage == 1 && editLineStations.size() > 1 && editLineStations.get(0).equals(editLineStations.get(editLineStations.size() - 1));

        if (button.id == 1) globalIndex = (globalIndex - 1 + globalStations.size()) % globalStations.size();
        if (button.id == 2) globalIndex = (globalIndex + 1) % globalStations.size();

        // =========================================================
        // ★制約1 & 2: 駅を路線に追加する際の厳格なチェック
        // =========================================================
        if (button.id == 3 && !globalStations.get(globalIndex).equals("駅が見つかりません")) {
            if (isLoop) return; // すでに環状線として閉じている場合は追加不可

            String target = globalStations.get(globalIndex);
            int count = Collections.frequency(editLineStations, target);

            if (count == 0) {
                // リストに存在しない駅は自由に追加可能（単純道の保証）
                editLineStations.add(target);
                stationIndex = editLineStations.size() - 1;
            }
            else if (count == 1 && editLineStations.get(0).equals(target)) {
                // リストの「始点」と同じ駅を追加しようとしている場合（環状線化の試み）
                if (editLineStations.size() >= 3) {
                    // ★制約2: すでに3駅以上ある場合のみ、環状線として閉じることを許可
                    editLineStations.add(target);
                    stationIndex = editLineStations.size() - 1;
                }
                // ※3駅未満（[A, B]の状態でAを追加しようとしたなど）の場合は何も起こらない
            }
        }

        if (guiPage == 1 && !editLineStations.isEmpty()) {
            if (button.id == 4) stationIndex = (stationIndex - 1 + editLineStations.size()) % editLineStations.size();
            if (button.id == 5) stationIndex = (stationIndex + 1) % editLineStations.size();
            if (button.id == 6 && stationIndex > 0) {
                if (isLoop && (stationIndex == editLineStations.size() - 1 || stationIndex == 1)) return;
                Collections.swap(editLineStations, stationIndex, stationIndex - 1);
                stationIndex--;
            }
            if (button.id == 7 && stationIndex < editLineStations.size() - 1) {
                if (isLoop && (stationIndex == 0 || stationIndex == editLineStations.size() - 2)) return;
                Collections.swap(editLineStations, stationIndex, stationIndex + 1);
                stationIndex++;
            }
            if (button.id == 8) {
                editLineStations.remove(stationIndex);
                if (stationIndex >= editLineStations.size()) stationIndex = Math.max(0, editLineStations.size() - 1);
            }
        }

        if (button.id == 20) { // トップへ戻る
            guiPage = 0;
            this.initGui();
        }

        // =========================================================
        // ★制約3: 路線データを保存する際のチェック
        // =========================================================
        if (button.id == 0 || button.id == 9) { // 路線データの保存・削除

            // 保存ボタン(id==0)が押された時、駅が2つ未満なら保存を拒否
            if (button.id == 0 && editLineStations.size() < 2) {
                return; // 処理を中断（何も起きない）
            }

            MessageLineUpdate msg = new MessageLineUpdate();
            msg.mode = (button.id == 0) ? 1 : 2;
            msg.x = data.x; msg.y = data.y; msg.z = data.z;
            msg.companyName = data.companyName;
            msg.oldLineID = currentOldLineID;
            msg.newLineID = idField.getText();
            msg.lineName = nameField.getText();
            try { msg.baseFare = Integer.parseInt(baseField.getText()); } catch(Exception e) { msg.baseFare = 150; }
            try { msg.costPerBlock = Double.parseDouble(costField.getText()); } catch(Exception e) { msg.costPerBlock = 0.15; }
            msg.lineStations = this.editLineStations;
            KaisatsuModMain.network.sendToServer(msg);
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int x = this.width / 2;
        int y = this.height / 2;

        if (guiPage == 0) {
            this.drawCenteredString(this.fontRendererObj, "路線管理管制塔 - 管轄トップ", x, y - 90, 0xFFFF55);
            this.drawCenteredString(this.fontRendererObj, "管理する会社名", x, y - 75, 0xAAAAAA);

            // テキストボックスの代わりにただの文字として表示
            String dispName = (data.companyName != null && !data.companyName.isEmpty()) ? data.companyName : "未設定";
            this.drawString(this.fontRendererObj, dispName, x - 50, y - 56, 0xFFFFFF);

            this.drawCenteredString(this.fontRendererObj, "[ 登録済みの路線 ]", x, y - 20, 0xAAFFFF);

            if (!data.companyLines.isEmpty()) {
                MessageOpenLineGui.LineInfo info = data.companyLines.get(selectedLineIndex);
                this.drawCenteredString(this.fontRendererObj, info.lineName + " (" + info.lineID + ")", x, y + 10, 0xFFFFFF);
                this.drawCenteredString(this.fontRendererObj, (selectedLineIndex + 1) + " / " + data.companyLines.size(), x, y + 25, 0x555555);
            } else {
                this.drawCenteredString(this.fontRendererObj, "路線がありません", x, y + 10, 0xAAAAAA);
            }

        } else if (guiPage == 1) {
            this.drawCenteredString(this.fontRendererObj, currentOldLineID.isEmpty() ? "路線管理管制塔 - 新規路線" : "路線管理管制塔 - 路線編集", x, y - 110, 0xFFFF55);

            // 会社名の項目が消えた分、少し見やすく寄せています
            this.drawString(this.fontRendererObj, "路線ID", x - 110, y - 95, 0xAAAAAA);
            this.drawString(this.fontRendererObj, "路線名", x + 10, y - 95, 0xAAAAAA);
            this.drawString(this.fontRendererObj, "初乗り運賃(円)", x - 60, y - 60, 0xAAAAAA);
            this.drawString(this.fontRendererObj, "1B単価(円)", x + 50, y - 60, 0xAAAAAA);

            idField.drawTextBox(); nameField.drawTextBox();
            baseField.drawTextBox(); costField.drawTextBox();

            this.drawCenteredString(this.fontRendererObj, "[ 登録可能な駅 ]", x - 60, y, 0xAAFFFF);
            this.drawCenteredString(this.fontRendererObj, globalStations.get(globalIndex), x - 60, y + 26, 0xFFFFFF);

            this.drawCenteredString(this.fontRendererObj, "[ 路線の駅順 ]", x + 50, y - 25, 0xAAFFFF);

            boolean isLoop = editLineStations.size() > 1 && editLineStations.get(0).equals(editLineStations.get(editLineStations.size() - 1));

            if (!editLineStations.isEmpty()) {
                int startIdx = Math.max(0, stationIndex - 2);
                int endIdx = Math.min(editLineStations.size() - 1, stationIndex + 2);

                if (endIdx - startIdx < 4) {
                    if (startIdx == 0) endIdx = Math.min(editLineStations.size() - 1, startIdx + 4);
                    else if (endIdx == editLineStations.size() - 1) startIdx = Math.max(0, endIdx - 4);
                }

                int drawY = y - 5;
                for (int i = startIdx; i <= endIdx; i++) {
                    String prefix = (i == stationIndex) ? "▶ " : "   ";
                    String text = prefix + (i + 1) + ". " + editLineStations.get(i);
                    if (isLoop && i == editLineStations.size() - 1) text += " (環状)";

                    int color = (i == stationIndex) ? 0xFFFF55 : 0xDDDDDD;
                    if (isLoop && (i == 0 || i == editLineStations.size() - 1)) color = (i == stationIndex) ? 0xFFFFAA : 0x55FFFF;

                    this.drawString(this.fontRendererObj, text, x + 10, drawY, color);
                    drawY += 12;
                }
            } else {
                this.drawCenteredString(this.fontRendererObj, "未登録", x + 50, y + 5, 0xAAAAAA);
            }
        } else if (guiPage == 2) {
            this.drawCenteredString(this.fontRendererObj, "路線管理管制塔 - 会社名編集", x, y - 50, 0xFFFF55);
            this.drawCenteredString(this.fontRendererObj, "新しい会社名を入力してください", x, y - 35, 0xAAAAAA);
            compField.drawTextBox();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
