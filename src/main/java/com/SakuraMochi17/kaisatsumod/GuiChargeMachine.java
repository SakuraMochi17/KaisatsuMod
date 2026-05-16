package com.SakuraMochi17.kaisatsumod;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiChargeMachine extends GuiContainer {
    // バニラの金床のテクスチャをそのまま借用する
    private static final ResourceLocation anvilGuiTextures = new ResourceLocation("textures/gui/container/anvil.png");

    public GuiChargeMachine(InventoryPlayer playerInv, TileEntityChargeMachine te) {
        super(new ContainerChargeMachine(playerInv, te));
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString("ICカード チャージ機", 8, 6, 4210752);
        this.fontRendererObj.drawString("インベントリ", 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(anvilGuiTextures);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        // 金床の名前入力欄の部分（今回は使わない）をグレーの四角で隠す
        this.drawTexturedModalRect(k + 59, l + 20, 0, this.ySize + 16, 110, 16);
    }
}
