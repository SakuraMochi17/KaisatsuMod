package com.SakuraMochi17.kaisatsumod.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

public class ItemRenderModel implements IItemRenderer {

    private IModelCustom model;
    private ResourceLocation texture;

    // ★コンストラクタ：呼び出す時に、読み込むOBJとPNGのファイル名を指定する
    public ItemRenderModel(String modelPath, String texturePath) {
        this.model = AdvancedModelLoader.loadModel(new ResourceLocation("kaisatsumod", modelPath));
        this.texture = new ResourceLocation("kaisatsumod", texturePath);
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        // ドロップ時、手持ち時、インベントリ内のすべてで3D表示する
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        // これをtrueにすると、インベントリ内でブロックのようにクルクル回って立体的に表示されます
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();

        // アイテム状態ごとの位置の微調整
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glTranslatef(0.5F, 0.5F, 0.5F); // 手に持った時の位置
            // GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F); // 向きがおかしい場合はここで回転させます
        } else if (type == ItemRenderType.INVENTORY) {
            GL11.glTranslatef(0.0F, -0.1F, 0.0F); // インベントリ内の位置
        } else if (type == ItemRenderType.ENTITY) {
            GL11.glTranslatef(0.0F, 0.5F, 0.0F); // 地面にドロップした時の位置
        }

        // ★マイクラサイズに縮小（※モデリング時のサイズに合わせて微調整してください）
        float scale = 0.0625F; // 1/16サイズ
        GL11.glScalef(scale, scale, scale);

        // テクスチャを貼って、モデル全体を描画！
        Minecraft.getMinecraft().renderEngine.bindTexture(this.texture);
        this.model.renderAll();

        GL11.glPopMatrix();
    }
}
