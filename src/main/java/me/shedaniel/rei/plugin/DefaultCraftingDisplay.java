package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.api.Identifier;
import net.minecraft.item.crafting.IRecipe;

public interface DefaultCraftingDisplay<T> extends IRecipeDisplay<IRecipe> {
    
    @Override
    default Identifier getRecipeCategory() {
        return DefaultPlugin.CRAFTING;
    }
    
    default public int getWidth() {
        return 2;
    }
    
    default public int getHeight() {
        return 2;
    }
    
}
