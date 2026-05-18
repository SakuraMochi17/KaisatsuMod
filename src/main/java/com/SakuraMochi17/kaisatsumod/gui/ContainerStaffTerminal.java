package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerStaffTerminal extends Container {
    private TileEntityStaffTerminal tileEntity;

    public ContainerStaffTerminal(InventoryPlayer playerInv, TileEntityStaffTerminal te) {
        this.tileEntity = te;

        // 左上：ICカード投入(IN)スロット
        this.addSlotToContainer(new Slot(te, 0, 26, 24) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemICCard;
            }
        });

        // 左下：ICカード排出(OUT)スロット
        this.addSlotToContainer(new Slot(te, 1, 26, 54) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        // プレイヤーインベントリ (そのまま)
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(playerInv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }
        for (int x = 0; x < 9; ++x) {
            this.addSlotToContainer(new Slot(playerInv, x, 8 + x * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tileEntity.isUseableByPlayer(player);
    }
}
