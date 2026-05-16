package com.SakuraMochi17.kaisatsumod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ContainerChargeMachine extends Container {
    private TileEntityChargeMachine tileEntity;

    public ContainerChargeMachine(InventoryPlayer playerInv, TileEntityChargeMachine te) {
        this.tileEntity = te;

        // スロット0: ICカード入力 (X: 27, Y: 47)
        this.addSlotToContainer(new Slot(te, 0, 27, 47) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemICCard;
            }
            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                updateChargeResult();
            }
        });

        // スロット1: お金入力 (X: 76, Y: 47)
        this.addSlotToContainer(new Slot(te, 1, 76, 47) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return KaisatsuModMain.getMoneyValue(stack) > 0;
            }
            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                updateChargeResult();
            }
        });

        // スロット2: 出力 (X: 134, Y: 47) - 取り出し専用スロット
        this.addSlotToContainer(new Slot(te, 2, 134, 47) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false; // プレイヤーはここへアイテムを置けない
            }
            @Override
            public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
                // 取り出されたら、入力スロットのカードとお金を消費する
                tileEntity.setInventorySlotContents(0, null);
                tileEntity.setInventorySlotContents(1, null);
                super.onPickupFromSlot(player, stack);
            }
        });

        // プレイヤーのインベントリ(下部)を紐付け
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    // 左と中央のスロットが変更されたときに呼ばれ、右の完成品を計算する
    private void updateChargeResult() {
        ItemStack cardStack = tileEntity.getStackInSlot(0);
        ItemStack moneyStack = tileEntity.getStackInSlot(1);

        if (cardStack != null && cardStack.getItem() instanceof ItemICCard && moneyStack != null) {
            int moneyValue = KaisatsuModMain.getMoneyValue(moneyStack);

            if (moneyValue > 0) {
                int chargeAmount = moneyValue * moneyStack.stackSize;

                // カードのNBTを取得（なければ初期化）
                NBTTagCompound nbt = cardStack.stackTagCompound;
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                    nbt.setInteger("balance", 0);
                }

                int currentBalance = nbt.getInteger("balance");
                int newBalance = currentBalance + chargeAmount;

                // 上限(例: 20000円)を超えない場合のみ出力スロットに結果を表示
                if (newBalance <= 20000) {
                    ItemStack resultCard = cardStack.copy(); // 元のカードをコピー
                    resultCard.setTagCompound((NBTTagCompound) nbt.copy());
                    resultCard.stackTagCompound.setInteger("balance", newBalance);
                    tileEntity.setInventorySlotContents(2, resultCard);
                    return;
                }
            }
        }
        // 条件を満たさない場合は出力スロットを空にする
        tileEntity.setInventorySlotContents(2, null);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tileEntity.isUseableByPlayer(player);
    }
}
