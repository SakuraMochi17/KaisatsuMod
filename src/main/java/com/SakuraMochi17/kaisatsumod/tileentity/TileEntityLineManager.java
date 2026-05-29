package com.SakuraMochi17.kaisatsumod.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityLineManager extends TileEntity {
    public String lineID = ""; // 例: "jr_banetsu"

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.lineID = nbt.getString("LineID");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("LineID", this.lineID);
    }
}
