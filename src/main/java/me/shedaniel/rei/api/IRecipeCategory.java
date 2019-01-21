package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.RecipeViewingWidget;
import me.shedaniel.rei.listeners.IMixinGuiContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
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
    
    default public void drawCategoryBackground(Rectangle bounds) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getInstance().getTextureManager().bindTexture(RecipeViewingWidget.CHEST_GUI_TEXTURE);
        new Gui() {
        
        }.drawTexturedModalRect((int) bounds.getX(), (int) bounds.getY(), 0, 0, (int) bounds.getWidth(), (int) bounds.getHeight());
    }
    
}
