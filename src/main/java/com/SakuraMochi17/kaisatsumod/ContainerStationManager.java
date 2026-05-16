package com.SakuraMochi17.kaisatsumod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerStationManager extends Container {
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
