package com.SakuraMochi17.kaisatsumod.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import com.SakuraMochi17.kaisatsumod.item.ItemMagicICCard;

public class ItemRenderModel implements IItemRenderer {

    private IModelCustom model;
    private ResourceLocation texture;

    public ItemRenderModel(String modelPath, String texturePath) {
        this.model = AdvancedModelLoader.loadModel(new ResourceLocation("kaisatsumod", modelPath));
        this.texture = new ResourceLocation("kaisatsumod", texturePath);
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix(); // ★位置・サイズの変更スタート

        // アイテム状態ごとの位置の微調整
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        } else if (type == ItemRenderType.INVENTORY) {
            GL11.glTranslatef(0.0F, -0.1F, 0.0F);
        } else if (type == ItemRenderType.ENTITY) {
            GL11.glTranslatef(0.0F, 0.5F, 0.0F);
        }

        // マイクラサイズに縮小
        float scale = 0.0625F;
        GL11.glScalef(scale, scale, scale);

        // 1. まず通常のモデルを描画する
        Minecraft.getMinecraft().renderEngine.bindTexture(this.texture);
        this.model.renderAll();

        // =======================================================
        // 2. 魔法のICカードなら、同じ位置・サイズのまま輝きを重ねがけする
        // =======================================================
        if (item != null && item.getItem() instanceof ItemMagicICCard) {

            ResourceLocation glintTexture = new ResourceLocation("textures/misc/enchanted_item_glint.png");
            Minecraft.getMinecraft().renderEngine.bindTexture(glintTexture);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glDisable(GL11.GL_LIGHTING);

            float tick = Minecraft.getMinecraft().thePlayer.ticksExisted;
            GL11.glMatrixMode(GL11.GL_TEXTURE);

            // --- 1層目の光のオーラ ---
            GL11.glPushMatrix();
            GL11.glScalef(8.0F, 8.0F, 8.0F);
            GL11.glTranslatef((tick * 0.01F) % 1.0F, (tick * 0.01F) % 1.0F, 0.0F);
            GL11.glRotatef(30.0F, 0.0F, 0.0F, 1.0F);
            GL11.glColor4f(0.38F, 0.19F, 0.6F, 1.0F);
            this.model.renderAll();
            GL11.glPopMatrix();

            // --- 2層目の光のオーラ ---
            GL11.glPushMatrix();
            GL11.glScalef(8.0F, 8.0F, 8.0F);
            GL11.glTranslatef(-(tick * 0.02F) % 1.0F, (tick * 0.02F) % 1.0F, 0.0F);
            GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
            GL11.glColor4f(0.38F, 0.19F, 0.6F, 1.0F);
            this.model.renderAll();
            GL11.glPopMatrix();

            // 描画設定を元に戻す
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        // =======================================================

        GL11.glPopMatrix(); // ★位置・サイズの変更おわり（必ず最後に来る！）
    }
}