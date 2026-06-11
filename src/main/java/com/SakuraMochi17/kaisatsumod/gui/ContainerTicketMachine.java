package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityTicketMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTicketMachine extends Container {
    private TileEntityTicketMachine terminal;

    public ContainerTicketMachine(IInventory playerInventory, TileEntityTicketMachine te) {
        this.terminal = te;

        // --- 左側：現金投入口 (3x3 = スロット 0~8) ---
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(new Slot(te, x + y * 3, 12 + x * 18, 24 + y * 18));
            }
        }

        // --- 右側：IC・切符・釣銭 ---
        this.addSlotToContainer(new Slot(te, 9, 148, 20));  // スロット9: ICカード
        this.addSlotToContainer(new Slot(te, 10, 148, 60)); // スロット10: 発券された切符

        // 釣銭口 (2x2 = スロット 11~14)
        this.addSlotToContainer(new Slot(te, 11, 139, 95));
        this.addSlotToContainer(new Slot(te, 12, 157, 95));
        this.addSlotToContainer(new Slot(te, 13, 139, 113));
        this.addSlotToContainer(new Slot(te, 14, 157, 113));

        // --- プレイヤーインベントリ ---
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 198));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) { return this.terminal.isUseableByPlayer(player); }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 15) {
                if (!this.mergeItemStack(itemstack1, 15, this.inventorySlots.size(), true)) return null;
            } else if (!this.mergeItemStack(itemstack1, 0, 10, false)) { return null; }
            if (itemstack1.stackSize == 0) slot.putStack(null);
            else slot.onSlotChanged();
        }
        return itemstack;
    }
}
