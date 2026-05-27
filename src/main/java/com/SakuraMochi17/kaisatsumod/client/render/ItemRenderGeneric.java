package com.SakuraMochi17.kaisatsumod.client.render;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class ItemRenderGeneric implements IItemRenderer {

    // どんなTileEntity（改札機でも券売機でも）受け取れる変数
    private TileEntity dummyTE;

    // ★コンストラクタ：このクラスを呼び出す時に、どの機械のモデルを使いたいか指定する
    public ItemRenderGeneric(TileEntity te) {
        this.dummyTE = te;
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
        GL11.glPushMatrix();

        // アイテムとしての位置ズレ直し（共通の調整）
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        } else if (type == ItemRenderType.INVENTORY) {
            GL11.glTranslatef(0.0F, -0.1F, 0.0F);
        }

        // ★ここで、受け取ったTileEntity（dummyTE）のワールド用描画処理を呼び出す！
        TileEntityRendererDispatcher.instance.renderTileEntityAt(this.dummyTE, 0.0D, 0.0D, 0.0D, 0.0F);

        GL11.glPopMatrix();
    }
}
