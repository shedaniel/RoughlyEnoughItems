package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.client.RecipeHelper;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.recipe.smelting.BlastingRecipe;
import net.minecraft.recipe.smelting.SmeltingRecipe;
import net.minecraft.recipe.smelting.SmokingRecipe;
import net.minecraft.util.Identifier;

import java.util.List;

public class DefaultPlugin implements IRecipePlugin {
    
    static final Identifier CRAFTING = new Identifier("roughlyenoughitems", "plugins/crafting");
    static final Identifier SMELTING = new Identifier("roughlyenoughitems", "plugins/smelting");
    static final Identifier SMOKING = new Identifier("roughlyenoughitems", "plugins/smoking");
    static final Identifier BLASTING = new Identifier("roughlyenoughitems", "plugins/blasting");
    static final Identifier BREWING = new Identifier("roughlyenoughitems", "plugins/brewing");
    
    static final List<DefaultBrewingDisplay> BREWING_DISPLAYS = Lists.newArrayList();
    
    public static void registerBrewingDisplay(DefaultBrewingDisplay display) {
        BREWING_DISPLAYS.add(display);
    }
    
    @Override
    public void registerPluginCategories() {
        RecipeHelper.registerCategory(new DefaultCraftingCategory());
        RecipeHelper.registerCategory(new DefaultSmeltingCategory());
        RecipeHelper.registerCategory(new DefaultSmokingCategory());
        RecipeHelper.registerCategory(new DefaultBlastingCategory());
        RecipeHelper.registerCategory(new DefaultBrewingCategory());
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
            else if (value instanceof SmokingRecipe)
                RecipeHelper.registerRecipe(SMOKING, new DefaultSmokingDisplay((SmokingRecipe) value));
            else if (value instanceof BlastingRecipe)
                RecipeHelper.registerRecipe(BLASTING, new DefaultBlastingDisplay((BlastingRecipe) value));
        BREWING_DISPLAYS.forEach(display -> RecipeHelper.registerRecipe(BREWING, display));
    }
    
}
