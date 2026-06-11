package com.SakuraMochi17.kaisatsumod.proxy;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketGate;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTransferGate;
import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        // 3Dモデル描画（ワールド内）
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTicketMachine.class, new com.SakuraMochi17.kaisatsumod.client.render.RenderTicketMachine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTicketGate.class, new com.SakuraMochi17.kaisatsumod.client.render.RenderTicketGate());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransferGate.class, new com.SakuraMochi17.kaisatsumod.client.render.RenderTicketGate());

        // 3Dモデル描画（インベントリ内ブロック）
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(KaisatsuModMain.ticketMachine), new com.SakuraMochi17.kaisatsumod.client.render.ItemRenderGeneric(new TileEntityTicketMachine()));
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(KaisatsuModMain.ticketGate), new com.SakuraMochi17.kaisatsumod.client.render.ItemRenderGeneric(new TileEntityTicketGate()));
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(KaisatsuModMain.transferGate), new com.SakuraMochi17.kaisatsumod.client.render.ItemRenderGeneric(new TileEntityTransferGate()));

        // ★標準ICカードの3Dモデル登録
        MinecraftForgeClient.registerItemRenderer(KaisatsuModMain.icCard, new com.SakuraMochi17.kaisatsumod.client.render.ItemRenderModel("models/item/ic_card.obj", "textures/item/ic_card.png"));

        // ★追加：魔法のICカードにも全く同じ3Dモデルを登録！
        MinecraftForgeClient.registerItemRenderer(KaisatsuModMain.magicIcCard, new com.SakuraMochi17.kaisatsumod.client.render.ItemRenderModel("models/item/ic_card.obj", "textures/item/ic_card.png"));
    }
}
