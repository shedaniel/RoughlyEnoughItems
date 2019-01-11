package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.client.RecipeHelper;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.recipe.smelting.SmeltingRecipe;
import net.minecraft.util.Identifier;

public class DefaultPlugin implements IRecipePlugin {
    
    static final Identifier CRAFTING = new Identifier("roughlyenoughitems", "plugins/crafting");
    static final Identifier SMELTING = new Identifier("roughlyenoughitems", "plugins/smelting");
    
    @Override
    public void registerPluginCategories() {
        RecipeHelper.registerCategory(new DefaultCraftingCategory());
        RecipeHelper.registerCategory(new DefaultSmeltingCategory());
    }
    
    @Override
    public void registerRecipes() {
        for(Recipe value : RecipeHelper.getRecipeManager().values())
            if (value instanceof ShapelessRecipe)
                RecipeHelper.registerRecipe(CRAFTING, new DefaultShapelessDisplay((ShapelessRecipe) value));
            else if (value instanceof ShapedRecipe)
                RecipeHelper.registerRecipe(CRAFTING, new DefaultShapedDisplay((ShapedRecipe) value));
            else if (value instanceof SmeltingRecipe)
                RecipeHelper.registerRecipe(SMELTING, new DefaultSmeltingDisplay((SmeltingRecipe) value));
    }
    
}
