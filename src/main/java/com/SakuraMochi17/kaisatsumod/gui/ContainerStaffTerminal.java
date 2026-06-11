package com.SakuraMochi17.kaisatsumod.gui;

import com.SakuraMochi17.kaisatsumod.core.FareManager;
import com.SakuraMochi17.kaisatsumod.item.ItemCertificate;
import com.SakuraMochi17.kaisatsumod.item.ItemICCard;
import com.SakuraMochi17.kaisatsumod.item.ItemTicket;
import com.SakuraMochi17.kaisatsumod.tileentity.TileEntityStaffTerminal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerStaffTerminal extends Container {
    private TileEntityStaffTerminal terminal;

    public int clientFare = 0;
    private int lastFare = 0;
    private String lastEntryStation = "";

    public ContainerStaffTerminal(IInventory playerInventory, TileEntityStaffTerminal te) {
        this.terminal = te;

        this.addSlotToContainer(new Slot(te, 0, 20, 25)); // 対象
        this.addSlotToContainer(new Slot(te, 1, 20, 75)); // 支払IC

        this.addSlotToContainer(new Slot(te, 2, 115, 25)); // 現金
        this.addSlotToContainer(new Slot(te, 3, 135, 25));
        this.addSlotToContainer(new Slot(te, 4, 115, 45));
        this.addSlotToContainer(new Slot(te, 5, 135, 45));

        this.addSlotToContainer(new Slot(te, 6, 115, 75)); // 釣銭
        this.addSlotToContainer(new Slot(te, 7, 135, 75));
        this.addSlotToContainer(new Slot(te, 8, 115, 95));
        this.addSlotToContainer(new Slot(te, 9, 135, 95));

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
    public void addCraftingToCrafters(ICrafting icrafting) {
        super.addCraftingToCrafters(icrafting);
        icrafting.sendProgressBarUpdate(this, 0, this.lastFare);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (!this.terminal.getWorldObj().isRemote) {
            int currentCalculatedFare = 0;
            String entryStation = "";

            ItemStack target = this.terminal.getStackInSlot(0);
            if (target != null && target.stackTagCompound != null) {
                if (target.getItem() instanceof ItemICCard && target.stackTagCompound.getBoolean("inGate")) {
                    entryStation = target.stackTagCompound.getString("entryStation");
                } else if (target.getItem() instanceof ItemCertificate) {
                    entryStation = target.stackTagCompound.getString("issueStation");
                } else if (target.getItem() instanceof ItemTicket) {
                    entryStation = target.stackTagCompound.getString("entryStation");
                }
            }

            if (!entryStation.equals(this.lastEntryStation)) {
                if (!entryStation.isEmpty() && terminal.stationName != null && !terminal.stationName.equals("未設定")) {
                    int rawFare = FareManager.calculateFare(terminal.getWorldObj(), entryStation, terminal.stationName);
                    // ★修正：運賃を10円単位に切り上げる (経路エラーの -1 はそのまま通す)
                    if (rawFare > 0) {
                        currentCalculatedFare = (int) Math.ceil(rawFare / 10.0) * 10;
                    } else {
                        currentCalculatedFare = rawFare;
                    }
                } else {
                    currentCalculatedFare = 0;
                }
                this.lastEntryStation = entryStation;
            } else {
                currentCalculatedFare = this.lastFare;
            }

            for (int i = 0; i < this.crafters.size(); ++i) {
                ICrafting icrafting = (ICrafting) this.crafters.get(i);
                if (this.lastFare != currentCalculatedFare) {
                    icrafting.sendProgressBarUpdate(this, 0, currentCalculatedFare);
                }
            }
            this.lastFare = currentCalculatedFare;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0) this.clientFare = value;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 10) {
                if (!this.mergeItemStack(itemstack1, 10, this.inventorySlots.size(), true)) return null;
            } else if (!this.mergeItemStack(itemstack1, 0, 6, false)) { return null; }
            if (itemstack1.stackSize == 0) slot.putStack(null);
            else slot.onSlotChanged();
        }
        return itemstack;
    }
}
