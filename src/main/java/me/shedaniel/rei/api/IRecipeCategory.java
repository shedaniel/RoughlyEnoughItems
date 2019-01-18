package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.listeners.IMixinGuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Arrays;
import java.util.List;


public interface IRecipeCategory<T extends IRecipeDisplay> {
    
    public ResourceLocation getResourceLocation();
    
    public ItemStack getCategoryIcon();
    
    public String getCategoryName();
    
    default public boolean usesFullPage() {
        return false;
    }
    
    default public List<IWidget> setupDisplay(IMixinGuiContainer containerGui, T recipeDisplay, Rectangle bounds) {
        return Arrays.asList(new RecipeBaseWidget(bounds));
    }
    
}
