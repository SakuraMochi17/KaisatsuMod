package com.SakuraMochi17.kaisatsumod.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

public class RenderTicketGate extends TileEntitySpecialRenderer {

    private static final ResourceLocation MODEL_LOC = new ResourceLocation("yourmodid", "models/block/ticket_gate.obj");
    private static final ResourceLocation TEXTURE_LOC = new ResourceLocation("yourmodid", "textures/models/ticket_gate.png");

    private IModelCustom model;

    public RenderTicketGate() {
        this.model = AdvancedModelLoader.loadModel(MODEL_LOC);
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        GL11.glPushMatrix();

        // ブロックの中心へ移動
        GL11.glTranslated(x + 0.5, y, z + 0.5);

        // ==========================================
        // ★追加：Y軸（縦の軸）を中心に90度回転させる
        // （もし自分が思っていたのとは逆の90度だった場合は、
        // 　90.0F を -90.0F に変更してください）
        // ==========================================
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);

        // ★マイクラの標準的なブロックサイズ（1/16）に縮小します。
        // もしこれでも大きい/小さい場合は、0.05F や 0.08F などに微調整してください。
        float scale = 0.01F;
        GL11.glScalef(scale, scale, scale);

        // テクスチャをバインド
        this.bindTexture(TEXTURE_LOC);

        // ==========================================
        // ★修正：OBJファイル内の本当の名前で個別に呼び出す
        // ==========================================

        // 1. ボディパーツの描画 (obj2以外すべて)
        this.model.renderPart("obj1");
        this.model.renderPart("obj3");
        this.model.renderPart("obj4");
        this.model.renderPart("obj5");

        // 2. フラップパーツの描画 (アニメーションは後回しなので、そのまま定位置に描画)
        this.model.renderPart("obj2");

        GL11.glPopMatrix();
    }
}
