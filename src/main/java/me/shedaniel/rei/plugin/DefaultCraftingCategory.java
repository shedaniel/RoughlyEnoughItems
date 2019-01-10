package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.IRecipeCategory;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class DefaultCraftingCategory implements IRecipeCategory<DefaultCraftingDisplay> {
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.CRAFTING;
    }
    
    @Override
    public ItemStack getCategoryIcon() {
        return new ItemStack(Blocks.CRAFTING_TABLE.getItem());
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.crafting");
    }
    
}
