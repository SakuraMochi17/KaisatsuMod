package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessageLineUpdate;
import com.SakuraMochi17.kaisatsumod.network.MessageOpenLineGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.List;

public class GuiLineManager extends GuiScreen {
    private final MessageOpenLineGui data;

    private GuiTextField idField, nameField, compField, baseField, costField;

    private List<String> globalStations;
    private List<String> lineStations;
    private int globalIndex = 0;
    private int lineIndex = 0;

    public GuiLineManager(MessageOpenLineGui message) {
        this.data = message;
        this.globalStations = message.globalStations;
        this.lineStations = message.lineStations;
        if (this.globalStations.isEmpty()) this.globalStations.add("駅が見つかりません");
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        int x = this.width / 2;
        int y = this.height / 2;

        this.idField = new GuiTextField(this.fontRendererObj, x - 130, y - 80, 80, 15);
        this.idField.setText(data.lineID);
        this.nameField = new GuiTextField(this.fontRendererObj, x - 40, y - 80, 80, 15);
        this.nameField.setText(data.lineName);
        this.compField = new GuiTextField(this.fontRendererObj, x + 50, y - 80, 80, 15);
        this.compField.setText(data.companyName);

        this.baseField = new GuiTextField(this.fontRendererObj, x - 60, y - 45, 50, 15);
        this.baseField.setText(String.valueOf(data.baseFare));
        this.costField = new GuiTextField(this.fontRendererObj, x + 50, y - 45, 50, 15);
        this.costField.setText(String.valueOf(data.costPerBlock));

        this.buttonList.add(new GuiButton(1, x - 115, y + 20, 20, 20, "<"));
        this.buttonList.add(new GuiButton(2, x - 25, y + 20, 20, 20, ">"));
        this.buttonList.add(new GuiButton(3, x - 100, y + 50, 80, 20, "路線に追加 ->"));

        this.buttonList.add(new GuiButton(4, x + 95, y - 5, 20, 20, "∧"));
        this.buttonList.add(new GuiButton(5, x + 95, y + 25, 20, 20, "∨"));
        this.buttonList.add(new GuiButton(6, x + 120, y - 5, 30, 20, "上へ"));
        this.buttonList.add(new GuiButton(7, x + 120, y + 25, 30, 20, "下へ"));
        this.buttonList.add(new GuiButton(8, x + 95, y + 50, 55, 20, "削除"));

        this.buttonList.add(new GuiButton(0, x - 50, y + 85, 100, 20, "設定を保存"));
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (idField.textboxKeyTyped(typedChar, keyCode) || nameField.textboxKeyTyped(typedChar, keyCode) ||
                compField.textboxKeyTyped(typedChar, keyCode) || baseField.textboxKeyTyped(typedChar, keyCode) ||
                costField.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        idField.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        compField.mouseClicked(mouseX, mouseY, mouseButton);
        baseField.mouseClicked(mouseX, mouseY, mouseButton);
        costField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            MessageLineUpdate msg = new MessageLineUpdate();
            msg.x = data.x; msg.y = data.y; msg.z = data.z;
            msg.oldLineID = data.lineID;
            msg.newLineID = idField.getText();
            msg.lineName = nameField.getText();
            msg.companyName = compField.getText();
            try { msg.baseFare = Integer.parseInt(baseField.getText()); } catch(Exception e) { msg.baseFare = 150; }
            try { msg.costPerBlock = Double.parseDouble(costField.getText()); } catch(Exception e) { msg.costPerBlock = 0.15; }
            msg.lineStations = this.lineStations;
            KaisatsuModMain.network.sendToServer(msg);
            this.mc.displayGuiScreen(null);
        }

        // ★現在のリストが「環状線（最初と最後が同じ）」かどうかを判定
        boolean isLoop = lineStations.size() > 1 && lineStations.get(0).equals(lineStations.get(lineStations.size() - 1));

        if (button.id == 1) globalIndex = (globalIndex - 1 + globalStations.size()) % globalStations.size();
        if (button.id == 2) globalIndex = (globalIndex + 1) % globalStations.size();

        // ★ 駅の追加ロジック（鉄壁のルール）
        if (button.id == 3 && !globalStations.get(globalIndex).equals("駅が見つかりません")) {
            if (isLoop) return; // 既に環状線として閉じている場合は追加不可

            String target = globalStations.get(globalIndex);
            int count = Collections.frequency(lineStations, target);

            if (count == 0) {
                // まだリストにない場合は普通に追加
                lineStations.add(target);
                lineIndex = lineStations.size() - 1;
            } else if (count == 1 && lineStations.get(0).equals(target)) {
                // 1個だけ存在し、かつそれが「始発駅」の場合のみ、環状線の終端として追加許可
                lineStations.add(target);
                lineIndex = lineStations.size() - 1;
            }
            // それ以外（中間の駅を重複させようとした等）は無視される
        }

        if (lineStations.isEmpty()) return;

        if (button.id == 4) lineIndex = (lineIndex - 1 + lineStations.size()) % lineStations.size();
        if (button.id == 5) lineIndex = (lineIndex + 1) % lineStations.size();

        // ★ 上へ移動（環状線破壊防止）
        if (button.id == 6 && lineIndex > 0) {
            // 環状線の場合、最後の駅を上へ移動させる、または2番目の駅を一番上にするのを防ぐ
            if (isLoop && (lineIndex == lineStations.size() - 1 || lineIndex == 1)) return;
            Collections.swap(lineStations, lineIndex, lineIndex - 1);
            lineIndex--;
        }
        // ★ 下へ移動（環状線破壊防止）
        if (button.id == 7 && lineIndex < lineStations.size() - 1) {
            // 環状線の場合、最初の駅を下へ移動させる、または最後から2番目の駅を一番下にするのを防ぐ
            if (isLoop && (lineIndex == 0 || lineIndex == lineStations.size() - 2)) return;
            Collections.swap(lineStations, lineIndex, lineIndex + 1);
            lineIndex++;
        }
        // 削除
        if (button.id == 8) {
            lineStations.remove(lineIndex);
            if (lineIndex >= lineStations.size()) lineIndex = Math.max(0, lineStations.size() - 1);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int x = this.width / 2;
        int y = this.height / 2;

        this.drawCenteredString(this.fontRendererObj, "路線管理管制塔", x, y - 115, 0xFFFF55);
        this.drawString(this.fontRendererObj, "路線ID", x - 130, y - 95, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "路線名", x - 40, y - 95, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "会社名", x + 50, y - 95, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "初乗り運賃(円)", x - 60, y - 60, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "1B単価(円)", x + 50, y - 60, 0xAAAAAA);

        idField.drawTextBox(); nameField.drawTextBox(); compField.drawTextBox();
        baseField.drawTextBox(); costField.drawTextBox();

        this.drawCenteredString(this.fontRendererObj, "[ 登録可能な駅 ]", x - 60, y + 0, 0xAAFFFF);
        this.drawCenteredString(this.fontRendererObj, globalStations.get(globalIndex), x - 60, y + 26, 0xFFFFFF);

        this.drawCenteredString(this.fontRendererObj, "[ 路線の駅順 ]", x + 50, y - 25, 0xAAFFFF);

        // ★環状線かどうかの判定（描画用）
        boolean isLoop = lineStations.size() > 1 && lineStations.get(0).equals(lineStations.get(lineStations.size() - 1));

        if (!lineStations.isEmpty()) {
            int startIdx = Math.max(0, lineIndex - 2);
            int endIdx = Math.min(lineStations.size() - 1, lineIndex + 2);

            if (endIdx - startIdx < 4) {
                if (startIdx == 0) endIdx = Math.min(lineStations.size() - 1, startIdx + 4);
                else if (endIdx == lineStations.size() - 1) startIdx = Math.max(0, endIdx - 4);
            }

            int drawY = y - 5;
            for (int i = startIdx; i <= endIdx; i++) {
                String prefix = (i == lineIndex) ? "▶ " : "   ";
                String text = prefix + (i + 1) + ". " + lineStations.get(i);

                // ★環状線の終端には (環状) の文字を付与
                if (isLoop && i == lineStations.size() - 1) {
                    text += " (環状)";
                }

                // 基本は白、選択中は黄色
                int color = (i == lineIndex) ? 0xFFFF55 : 0xDDDDDD;

                // ★環状線の両端（最初と最後）は青っぽく光らせる演出
                if (isLoop && (i == 0 || i == lineStations.size() - 1)) {
                    color = (i == lineIndex) ? 0xFFFFAA : 0x55FFFF;
                }

                this.drawString(this.fontRendererObj, text, x + 10, drawY, color);
                drawY += 12;
            }
        } else {
            this.drawCenteredString(this.fontRendererObj, "未登録", x + 50, y + 5, 0xAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
