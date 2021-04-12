package me.shedaniel.rei.jeicompat.wrap;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.unwrap.JEIUnwrappedCategory;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.*;

public enum JEIRecipeManager implements IRecipeManager {
    INSTANCE;
    
    @Override
    public List<IRecipeCategory<?>> getRecipeCategories() {
        return CollectionUtils.map(CategoryRegistry.getInstance(), config -> new JEIUnwrappedCategory<>(config.getCategory()));
    }
    
    @Override
    public List<IRecipeCategory<?>> getRecipeCategories(List<ResourceLocation> recipeCategoryUids) {
        return CollectionUtils.map(recipeCategoryUids, this::getRecipeCategory);
    }
    
    @Override
    public @Nullable IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid) {
        try {
            return new JEIUnwrappedCategory<>(CategoryRegistry.getInstance().get(CategoryIdentifier.of(recipeCategoryUid)).getCategory());
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    @Override
    public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
        return new JEIFocus<>(mode, ingredient);
    }
    
    @Override
    public <V> List<IRecipeCategory<?>> getRecipeCategories(IFocus<V> focus) {
        throw TODO();
    }
    
    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        throw TODO();
    }
    
    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        return wrapRecipes(CategoryIdentifier.of(recipeCategory.getUid()));
    }
    
    @Override
    public List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory) {
        List<Object> objects = new ArrayList<>();
        for (EntryIngredient stacks : CategoryRegistry.getInstance().get(CategoryIdentifier.of(recipeCategory.getUid())).getWorkstations()) {
            objects.addAll(CollectionUtils.map(stacks, JEIPluginDetector::unwrap));
        }
        return objects;
    }
    
    @Override
    public @Nullable <T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocus<?> focus) {
        throw TODO();
    }
    
    @Override
    public <T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
        throw TODO();
    }
    
    @Override
    public <T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
        throw TODO();
    }
    
    @Override
    public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
        throw TODO();
    }
    
    @Override
    public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
        throw TODO();
    }
    
    @Override
    public <T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid) {
        Collection<Display> display = createDisplayFrom(recipe);
        for (Display d : display) {
            if (Objects.equals(d.getCategoryIdentifier().getIdentifier(), recipeCategoryUid)) {
                DisplayRegistry.getInstance().add(d);
            }
        }
    }
}
