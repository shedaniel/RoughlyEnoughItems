package me.shedaniel.rei.api;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public interface IRecipeCategory<T extends IRecipeDisplay> {
    
    public Identifier getIdentifier();
    
    public ItemStack getCategoryIcon();
    
    public String getCategoryName();
    
    default public boolean usesFullPage() {
        return false;
    }
    
    default public List<IWidget> setupDisplay(IMixinContainerGui containerGui, T recipeDisplay, Rectangle bounds) {
        return Arrays.asList(new RecipeBaseWidget(bounds));
    }
    
}
