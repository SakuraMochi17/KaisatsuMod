package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessageStationUpdate;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStationManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("unchecked")
public class GuiStationManager extends GuiScreen {
    private final TileEntityStationManager tileEntity;
    private GuiTextField nameField;
    private final String originalName;

    public GuiStationManager(TileEntityStationManager te) {
        this.tileEntity = te;
        this.originalName = te.stationName;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true); // キーの長押し入力を許可
        int x = this.width / 2;
        int y = this.height / 2;

        // テキストボックスの作成
        this.nameField = new GuiTextField(this.fontRendererObj, x - 75, y - 20, 150, 20);
        this.nameField.setMaxStringLength(30);
        this.nameField.setFocused(true);
        this.nameField.setText(this.originalName.equals("未設定") ? "" : this.originalName);

        // 登録ボタン
        this.buttonList.add(new GuiButton(0, x - 50, y + 20, 100, 20, "ネットワークに登録"));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // テキストボックスに文字を入力させる処理
        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            String newName = this.nameField.getText().trim();
            if (newName.isEmpty()) {
                newName = "未設定";
            }
            KaisatsuModMain.network.sendToServer(new MessageStationUpdate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, originalName, newName));
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int x = this.width / 2;
        int y = this.height / 2;

        this.drawCenteredString(this.fontRendererObj, "駅管理ブロック 設定", x, y - 50, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "駅名を入力:", x - 75, y - 35, 0xAAAAAA);

        this.nameField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
