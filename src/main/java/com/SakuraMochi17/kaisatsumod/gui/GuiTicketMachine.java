package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessageOpenTicketMachine;
import com.SakuraMochi17.kaisatsumod.network.MessagePurchaseTicket;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiTicketMachine extends GuiContainer {
    private static final ResourceLocation texture = new ResourceLocation("textures/gui/container/generic_54.png");
    private TileEntityTicketMachine terminal;
    private int targetX, targetY, targetZ;
    public List<Integer> availableFares;

    public GuiTicketMachine(InventoryPlayer playerInv, TileEntityTicketMachine te) {
        super(new ContainerTicketMachine(playerInv, te));
        this.terminal = te;
        this.targetX = te.xCoord;
        this.targetY = te.yCoord;
        this.targetZ = te.zCoord;
        this.availableFares = new ArrayList<Integer>();
        this.xSize = 176;
        this.ySize = 222;
    }

    // src/main/java/com/SakuraMochi17/kaisatsumod/gui/GuiTicketMachine.java の initGui を修正
    @SuppressWarnings({"unchecked", "unchecked"})
    @Override
    public void initGui() {
        this.availableFares = MessageOpenTicketMachine.latestFares;
        if (this.availableFares == null) this.availableFares = new ArrayList<Integer>();

        // ★運賃リストを昇順にソートし、重複を排除
        List<Integer> sortedUniqueFares = new ArrayList<>();
        for (Integer f : this.availableFares) {
            if (!sortedUniqueFares.contains(f) && f > 0) {
                sortedUniqueFares.add(f);
            }
        }
        Collections.sort(sortedUniqueFares);

        super.initGui();
        this.buttonList.clear();
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // 最大9個まで運賃ボタンを表示（高額運賃が多い場合は上位を優先するか、UI拡張が必要）
        int maxBtn = Math.min(9, sortedUniqueFares.size());
        for (int i = 0; i < maxBtn; i++) {
            int col = i % 2;
            int row = i / 2;
            // 運賃が1000円を超える場合は文字サイズや表記を調整する等の工夫が可能
            String fareStr = sortedUniqueFares.get(i) + "";
            this.buttonList.add(new GuiButton(i, x + 72 + (col * 35), y + 20 + (row * 21), 34, 20, fareStr));
        }

        // 入場券ボタンを最後に追加 (ボタンID 100)
        int nIdx = maxBtn;
        int nCol = nIdx % 2;
        int nRow = nIdx / 2;
        this.buttonList.add(new GuiButton(100, x + 72 + (nCol * 35), y + 20 + (nRow * 21), 34, 20, "入場券"));

        // 置き換えたリストを保持
        this.availableFares = sortedUniqueFares;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 100) {
            KaisatsuModMain.network.sendToServer(new MessagePurchaseTicket(targetX, targetY, targetZ, -1));
        } else if (button.id >= 0 && button.id < availableFares.size()) {
            int fare = availableFares.get(button.id);
            KaisatsuModMain.network.sendToServer(new MessagePurchaseTicket(targetX, targetY, targetZ, fare));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = "券売機 - " + (terminal.stationName != null ? terminal.stationName : "未設定");
        this.fontRendererObj.drawString(title, 8, 5, 4210752);

        this.fontRendererObj.drawString("現金", 25, 14, 4210752);
        this.fontRendererObj.drawString("ICｶｰﾄﾞ", 140, 10, 4210752);
        this.fontRendererObj.drawString("切符", 146, 50, 4210752);
        this.fontRendererObj.drawString("釣銭", 146, 84, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
        net.minecraft.client.gui.Gui.drawRect(x + 7, y + 13, x + 169, y + 135, 0xFFC6C6C6);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);

        // 現金マス (3x3)
        for (int ry = 0; ry < 3; ry++) {
            for (int rx = 0; rx < 3; rx++) {
                this.drawTexturedModalRect(x + 11 + (rx * 18), y + 23 + (ry * 18), 7, 17, 18, 18);
            }
        }

        this.drawTexturedModalRect(x + 147, y + 19, 7, 17, 18, 18); // IC
        this.drawTexturedModalRect(x + 147, y + 59, 7, 17, 18, 18); // 切符

        // 釣銭マス (2x2)
        this.drawTexturedModalRect(x + 138, y + 94, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 156, y + 94, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 138, y + 112, 7, 17, 18, 18);
        this.drawTexturedModalRect(x + 156, y + 112, 7, 17, 18, 18);
    }
}
