package com.SakuraMochi17.kaisatsumod.item;

import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import com.SakuraMochi17.kaisatsumod.core.KaisatsuNetworkData; // ★追加
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.List;

public class ItemTicket extends Item {

    public ItemTicket() {
        super();
        this.setUnlocalizedName("ticket");
        this.setTextureName("minecraft:paper");
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("entryStation")) {
            String line = stack.stackTagCompound.getString("entryLine");
            String station = stack.stackTagCompound.getString("entryStation");
            boolean isNyujoken = stack.stackTagCompound.getBoolean("isNyujoken");

            // ★修正：グローバルデータから路線名を取得
            String lineName = "不明な路線";
            KaisatsuNetworkData data = KaisatsuNetworkData.get(player.worldObj);
            if (data != null && data.companyLines.containsKey(line)) {
                lineName = data.companyLines.get(line).lineName;
            }

            if (isNyujoken) {
                list.add("§b【入場券】§r");
                list.add("発行: " + lineName + " " + station + "駅");
                list.add("§7※入場駅でのみ出場可能§r");
            } else {
                int fare = stack.stackTagCompound.getInteger("fare");
                list.add("§e【乗車券】§r");
                list.add("発行: " + lineName + " " + station + "駅");
                list.add("運賃: " + fare + "円区間");
            }
        } else {
            list.add("無効なきっぷ（未発券）");
        }
    }
}
