package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.client.RecipeHelper;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.util.Identifier;

public class DefaultPlugin implements IRecipePlugin {
    
    static final Identifier CRAFTING = new Identifier("roughlyenoughitems", "plugin/crafting");
    
    @Override
    public void registerPluginCategories() {
        RecipeHelper.registerCategory(new DefaultCraftingCategory());
    }
    
    @Override
    public void registerRecipes() {
        for(Recipe value : RecipeHelper.getRecipeManager().values())
            if (value instanceof ShapelessRecipe)
                RecipeHelper.registerRecipe(CRAFTING, new DefaultShapelessDisplay((ShapelessRecipe) value));
            else if (value instanceof ShapedRecipe)
                RecipeHelper.registerRecipe(CRAFTING, new DefaultShapedDisplay((ShapedRecipe) value));
    }
    
}
