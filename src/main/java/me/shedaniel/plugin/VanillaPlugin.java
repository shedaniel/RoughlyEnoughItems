package me.shedaniel.plugin;

import me.shedaniel.api.IAEIPlugin;
import me.shedaniel.impl.AEIRecipeManager;
import me.shedaniel.plugin.crafting.VanillaCraftingCategory;
import me.shedaniel.plugin.crafting.VanillaCraftingRecipe;
import me.shedaniel.plugin.crafting.VanillaShapedCraftingRecipe;
import me.shedaniel.plugin.crafting.VanillaShapelessCraftingRecipe;
import me.shedaniel.plugin.furnace.VanillaFurnaceCategory;
import me.shedaniel.plugin.furnace.VanillaFurnaceRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class VanillaPlugin implements IAEIPlugin {
    @Override
    public void register() {
        List<VanillaCraftingRecipe> recipes = new LinkedList<>();
        List<VanillaFurnaceRecipe> furnaceRecipes = new LinkedList<>();
        AEIRecipeManager.instance().addDisplayAdapter(new VanillaCraftingCategory());
        AEIRecipeManager.instance().addDisplayAdapter(new VanillaFurnaceCategory());
        
        for(IRecipe recipe : AEIRecipeManager.instance().recipeManager.getRecipes()) {
            if (recipe instanceof ShapelessRecipe) {
                recipes.add(new VanillaShapelessCraftingRecipe((ShapelessRecipe) recipe));
            }
            if (recipe instanceof ShapedRecipe) {
                recipes.add(new VanillaShapedCraftingRecipe((ShapedRecipe) recipe));
            }
            if (recipe instanceof FurnaceRecipe) {
                furnaceRecipes.add(new VanillaFurnaceRecipe((FurnaceRecipe) recipe));
            }
        }
        
        AEIRecipeManager.instance().addRecipe("vanilla", recipes);
        AEIRecipeManager.instance().addRecipe("furnace", furnaceRecipes);
    }
}
