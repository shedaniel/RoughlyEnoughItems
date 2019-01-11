package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.IRecipeCategory;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class DefaultSmeltingCategory implements IRecipeCategory<DefaultSmeltingDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.SMELTING;
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.FURNACE.getItem());
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.smelting");
    }
    
}
