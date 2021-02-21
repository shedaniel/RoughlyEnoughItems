package me.shedaniel.rei.impl.registry;

import me.shedaniel.rei.api.registry.RecipeManagerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeManagerContextImpl implements RecipeManagerContext {
    private static final Comparator<Recipe<?>> RECIPE_COMPARATOR = Comparator.comparing((Recipe<?> o) -> o.getId().getNamespace()).thenComparing(o -> o.getId().getPath());
    private List<Recipe<?>> sortedRecipes = null;
    
    @Override
    public List<Recipe<?>> getAllSortedRecipes() {
        if (sortedRecipes == null) {
            this.sortedRecipes = getRecipeManager().getRecipes().parallelStream().sorted(RECIPE_COMPARATOR).collect(Collectors.toList());
        }
        
        return Collections.unmodifiableList(sortedRecipes);
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        return Minecraft.getInstance().getConnection().getRecipeManager();
    }
    
    @Override
    public void resetData() {
        this.sortedRecipes = null;
    }
}
