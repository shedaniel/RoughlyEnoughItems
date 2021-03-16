package me.shedaniel.rei.jeicompat.unwrap;

import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;

public class JEIUnwrappedCategory<T extends Display> implements IRecipeCategory<T> {
    private final DisplayCategory<T> backingCategory;
    
    public JEIUnwrappedCategory(DisplayCategory<T> backingCategory) {
        this.backingCategory = backingCategory;
    }
    
    @Override
    @NotNull
    public ResourceLocation getUid() {
        return backingCategory.getIdentifier();
    }
    
    @Override
    @NotNull
    public Class<? extends T> getRecipeClass() {
        return (Class<? extends T>) Display.class;
    }
    
    @Override
    @NotNull
    public String getTitle() {
        return backingCategory.getTitle().getString();
    }
    
    @Override
    @NotNull
    public IDrawable getBackground() {
        throw TODO();
    }
    
    @Override
    @NotNull
    public IDrawable getIcon() {
        throw TODO();
    }
    
    @Override
    public void setIngredients(@NotNull T recipe, @NotNull IIngredients ingredients) {
        throw TODO();
    }
    
    @Override
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull T recipe, @NotNull IIngredients ingredients) {
        throw TODO();
    }
}
