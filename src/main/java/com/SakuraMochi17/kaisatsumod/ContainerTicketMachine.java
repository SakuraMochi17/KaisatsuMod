package com.SakuraMochi17.kaisatsumod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTicketMachine extends Container {
    private TileEntityTicketMachine tileEntity;

    public ContainerTicketMachine(InventoryPlayer playerInv, TileEntityTicketMachine te) {
        this.tileEntity = te;

        // スロット1~9: 現金投入口 (左側 3x3)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlotToContainer(new Slot(te, 1 + j + i * 3, 12 + j * 18, 18 + i * 18));
            }
        }

        // スロット0: ICカード用 (右上)
        this.addSlotToContainer(new Slot(te, 0, 152, 14));

        // スロット10: 切符排出口 (右中央)
        this.addSlotToContainer(new Slot(te, 10, 152, 32) {
            @Override
            public boolean isItemValid(ItemStack stack) { return false; }
        });

        // スロット11~14: お釣り排出口 (右下 2x2)
        this.addSlotToContainer(new Slot(te, 11, 134, 50) { @Override public boolean isItemValid(ItemStack stack) { return false; } });
        this.addSlotToContainer(new Slot(te, 12, 152, 50) { @Override public boolean isItemValid(ItemStack stack) { return false; } });
        this.addSlotToContainer(new Slot(te, 13, 134, 68) { @Override public boolean isItemValid(ItemStack stack) { return false; } });
        this.addSlotToContainer(new Slot(te, 14, 152, 68) { @Override public boolean isItemValid(ItemStack stack) { return false; } });

        // プレイヤーのインベントリ
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tileEntity.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return null;
    }
}
