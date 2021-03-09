package me.shedaniel.rei.jeicompat;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class JEIGuiIngredientGroup<T> implements IGuiIngredientGroup<T> {
    private final IIngredientType<T> type;
    
    public JEIGuiIngredientGroup(IIngredientType<T> type) {
        this.type = type;
    }
    
    @Override
    public void set(@NotNull IIngredients ingredients) {
        
    }
    
    @Override
    public void set(int slotIndex, @Nullable List<T> ingredients) {
        
    }
    
    @Override
    public void set(int slotIndex, @Nullable T ingredient) {
        
    }
    
    @Override
    public void setBackground(int slotIndex, @NotNull IDrawable background) {
        
    }
    
    @Override
    public void addTooltipCallback(@NotNull ITooltipCallback<T> tooltipCallback) {
        
    }
    
    @Override
    @NotNull
    public Map<Integer, ? extends IGuiIngredient<T>> getGuiIngredients() {
        return null;
    }
    
    @Override
    public void init(int slotIndex, boolean input, int xPosition, int yPosition) {
        
    }
    
    @Override
    public void init(int slotIndex, boolean input, @NotNull IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xPadding, int yPadding) {
        
    }
    
    @Override
    public void setOverrideDisplayFocus(@Nullable IFocus<T> focus) {
        
    }
}
