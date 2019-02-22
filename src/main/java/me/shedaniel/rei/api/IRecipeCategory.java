package me.shedaniel.rei.api;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.RecipeViewingWidgetScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


public interface IRecipeCategory<T extends IRecipeDisplay> {
    
    public Identifier getIdentifier();
    
    public ItemStack getCategoryIcon();
    
    public String getCategoryName();
    
    default public boolean usesFullPage() {
        return false;
    }
    
    default public List<IWidget> setupDisplay(Supplier<T> recipeDisplaySupplier, Rectangle bounds) {
        return Arrays.asList(new RecipeBaseWidget(bounds));
    }
    
    default public void drawCategoryBackground(Rectangle bounds) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        MinecraftClient.getInstance().getTextureManager().bindTexture(RecipeViewingWidgetScreen.CHEST_GUI_TEXTURE);
        new DrawableHelper() {
        
        }.drawTexturedRect((int) bounds.getX(), (int) bounds.getY(), 0, 0, (int) bounds.getWidth(), (int) bounds.getHeight());
    }
    
    default public boolean checkTags() {
        return false;
    }
    
}
