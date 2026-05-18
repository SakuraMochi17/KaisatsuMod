package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.network.MessageStaffTerminal;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiStaffTerminal extends GuiContainer {
    private static final ResourceLocation texture = new ResourceLocation("textures/gui/container/dispenser.png");
    private final TileEntityStaffTerminal tileEntity;
    private String inputAmount = "";

    public GuiStaffTerminal(InventoryPlayer playerInv, TileEntityStaffTerminal te) {
        super(new ContainerStaffTerminal(playerInv, te));
        this.tileEntity = te;
    }

    @Override
    public void initGui() {
        super.initGui();
        int cx = (this.width - this.xSize) / 2;
        int cy = (this.height - this.ySize) / 2;

        // ★修正: ボタンの高さを15に縮め、アイテム欄に干渉しないように右側にギュッと配置
        int btnW = 20; int btnH = 15;
        int startX = cx + 100; int startY = cy + 15;

        // 1段目 (7, 8, 9)
        this.buttonList.add(new GuiButton(7, startX, startY, btnW, btnH, "7"));
        this.buttonList.add(new GuiButton(8, startX + 22, startY, btnW, btnH, "8"));
        this.buttonList.add(new GuiButton(9, startX + 44, startY, btnW, btnH, "9"));

        // 2段目 (4, 5, 6)
        this.buttonList.add(new GuiButton(4, startX, startY + 16, btnW, btnH, "4"));
        this.buttonList.add(new GuiButton(5, startX + 22, startY + 16, btnW, btnH, "5"));
        this.buttonList.add(new GuiButton(6, startX + 44, startY + 16, btnW, btnH, "6"));

        // 3段目 (1, 2, 3)
        this.buttonList.add(new GuiButton(1, startX, startY + 32, btnW, btnH, "1"));
        this.buttonList.add(new GuiButton(2, startX + 22, startY + 32, btnW, btnH, "2"));
        this.buttonList.add(new GuiButton(3, startX + 44, startY + 32, btnW, btnH, "3"));

        // 4段目 (C, 0, E)
        this.buttonList.add(new GuiButton(10, startX, startY + 48, btnW, btnH, "C"));
        this.buttonList.add(new GuiButton(0, startX + 22, startY + 48, btnW, btnH, "0"));
        this.buttonList.add(new GuiButton(11, startX + 44, startY + 48, btnW, btnH, "E"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= 0 && button.id <= 9) {
            if (inputAmount.length() < 6) {
                inputAmount += button.id;
            }
        } else if (button.id == 10) { // C (Clear)
            inputAmount = "";
        } else if (button.id == 11) { // E (Enter)
            if (!inputAmount.isEmpty()) {
                int amount = Integer.parseInt(inputAmount);
                KaisatsuModMain.network.sendToServer(new MessageStaffTerminal(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, amount));
                inputAmount = "";
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString("窓口精算端末", 8, 4, 4210752);

        // スロットの位置に合わせてラベルを調整
        this.fontRendererObj.drawString("IN", 28, 12, 4210752);
        this.fontRendererObj.drawString("OUT", 25, 42, 4210752);

        // 金額表示を、隠したグリッドの上の綺麗なスペースに配置
        this.fontRendererObj.drawString("引去額:", 52, 28, 4210752);
        String display = inputAmount.isEmpty() ? "0" : inputAmount;
        this.fontRendererObj.drawString(display + " 円", 52, 42, 0xFF0000);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int cx = (this.width - this.xSize) / 2;
        int cy = (this.height - this.ySize) / 2;

        // 1. ベース（ディスペンサー）の描画
        this.drawTexturedModalRect(cx, cy, 0, 0, this.xSize, this.ySize);

        // 2. ★裏技: 邪魔な3x3グリッドの上に、画像の端っこの無地グレー部分を上書きして隠す
        this.drawTexturedModalRect(cx + 60, cy + 15, 120, 15, 56, 56);

        // 3. ★追加: スロットの「凹み枠」をプレイヤーインベントリ部分からコピーして描画する
        this.drawTexturedModalRect(cx + 25, cy + 23, 7, 83, 18, 18); // INスロット用枠
        this.drawTexturedModalRect(cx + 25, cy + 53, 7, 83, 18, 18); // OUTスロット用枠
    }
}
