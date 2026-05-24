package com.SakuraMochi17.kaisatsumod.block;

import com.SakuraMochi17.kaisatsumod.KaisatsuModMain;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockAluminumOre extends Block {
    public BlockAluminumOre() {
        super(Material.rock); // 石と同じ材質
        this.setBlockName("oreAluminum");
        this.setBlockTextureName("yourmodid:ore_aluminum");
        this.setCreativeTab(KaisatsuModMain.tabKaisatsu);
        this.setHardness(3.0F); // 石炭鉱石と同じくらいの硬さ
        this.setResistance(5.0F); // 爆破耐性
        this.setHarvestLevel("pickaxe", 1); // 石のツルハシ以上で回収可能
    }
}
