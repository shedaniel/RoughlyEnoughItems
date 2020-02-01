/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.stripping;

import net.minecraft.block.Block;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ToolMaterial;

import java.util.Map;

public class DummyAxeItem extends AxeItem {
    protected DummyAxeItem(ToolMaterial toolMaterial_1, float float_1, float float_2, Settings item$Settings_1) {
        super(toolMaterial_1, float_1, float_2, item$Settings_1);
    }
    
    public static Map<Block, Block> getStrippedBlocksMap() {
        return STRIPPED_BLOCKS;
    }
}
