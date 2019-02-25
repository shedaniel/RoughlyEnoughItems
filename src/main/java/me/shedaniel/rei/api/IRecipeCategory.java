package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.RecipeViewingGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


public interface IRecipeCategory<T extends IRecipeDisplay> {
    
    public Identifier getIdentifier();
    
    public ItemStack getCategoryIcon();
    
    public String getCategoryName();
    
    default public List<IWidget> setupDisplay(Supplier<T> recipeDisplaySupplier, Rectangle bounds) {
        return Arrays.asList(new RecipeBaseWidget(bounds));
    }
    
    default public void drawCategoryBackground(Rectangle bounds, int mouseX, int mouseY, float delta) {
        new RecipeBaseWidget(bounds).draw(mouseX, mouseY, delta);
        Gui.drawRect(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, RecipeViewingGui.SUB_COLOR.getRGB());
        Gui.drawRect(bounds.x + 17, bounds.y + 21, bounds.x + bounds.width - 17, bounds.y + 33, RecipeViewingGui.SUB_COLOR.getRGB());
    }
    
    default public IDisplaySettings getDisplaySettings() {
        return new IDisplaySettings<T>() {
            @Override
            public int getDisplayHeight(IRecipeCategory category) {
                return 66;
            }
            
            @Override
            public int getDisplayWidth(IRecipeCategory category, T display) {
                return 150;
            }
            
            @Override
            public int getMaximumRecipePerPage(IRecipeCategory category) {
                return 99;
            }
        };
    }
    
    default public int getDisplayHeight() {
        return getDisplaySettings().getDisplayHeight(this);
    }
    
    default public int getDisplayWidth(T display) {
        return getDisplaySettings().getDisplayWidth(this, display);
    }
    
    default public int getMaximumRecipePerPage() {
        return getDisplaySettings().getMaximumRecipePerPage(this);
    }
    
    default public boolean checkTags() {
        return false;
    }
    
}
