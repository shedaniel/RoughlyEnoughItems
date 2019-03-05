package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


public interface RecipeCategory<T extends RecipeDisplay> {
    
    Identifier getIdentifier();
    
    ItemStack getCategoryIcon();
    
    String getCategoryName();
    
    default List<IWidget> setupDisplay(Supplier<T> recipeDisplaySupplier, Rectangle bounds) {
        return Collections.singletonList(new RecipeBaseWidget(bounds));
    }
    
    default void drawCategoryBackground(Rectangle bounds, int mouseX, int mouseY, float delta) {
        new RecipeBaseWidget(bounds).draw(mouseX, mouseY, delta);
        DrawableHelper.drawRect(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, RecipeViewingScreen.SUB_COLOR.getRGB());
        DrawableHelper.drawRect(bounds.x + 17, bounds.y + 21, bounds.x + bounds.width - 17, bounds.y + 33, RecipeViewingScreen.SUB_COLOR.getRGB());
    }
    
    default DisplaySettings getDisplaySettings() {
        return new DisplaySettings<T>() {
            @Override
            public int getDisplayHeight(RecipeCategory category) {
                return 66;
            }
            
            @Override
            public int getDisplayWidth(RecipeCategory category, T display) {
                return 150;
            }
            
            @Override
            public int getMaximumRecipePerPage(RecipeCategory category) {
                return 99;
            }
        };
    }
    
    default int getDisplayHeight() {
        return getDisplaySettings().getDisplayHeight(this);
    }
    
    default int getDisplayWidth(T display) {
        return getDisplaySettings().getDisplayWidth(this, display);
    }
    
    default int getMaximumRecipePerPage() {
        return getDisplaySettings().getMaximumRecipePerPage(this);
    }
    
    default boolean checkTags() {
        return false;
    }
    
}
