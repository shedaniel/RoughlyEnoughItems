package net.minecraft.recipe;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.plugin.BrewingRecipe;
import me.shedaniel.rei.plugin.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

import java.util.Arrays;
import java.util.List;

public class REIBrewingRecipeRegistry {
    
    public static void registerDisplays(RecipeHelper recipeHelper) {
        List<Potion> registeredPotionTypes = Lists.newArrayList();
        List<BrewingRecipe> potionItemConversions = Lists.newArrayList();
        List<Ingredient> potionItems = REIPotionRecipeUtils.getPotionTypes();
        REIPotionRecipeUtils.getItemRecipes().forEach(o -> {
            try {
                Item input = (Item) REIPotionRecipeUtils.getInputFromRecipe(o);
                Item output = (Item) REIPotionRecipeUtils.getOutputFromRecipe(o);
                Ingredient reagent = REIPotionRecipeUtils.getIngredientFromRecipe(o);
                potionItemConversions.add(new BrewingRecipe(input, reagent, output));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        REIPotionRecipeUtils.getPotionRecipes().forEach(o -> {
            try {
                Potion input = (Potion) REIPotionRecipeUtils.getInputFromRecipe(o);
                Potion output = (Potion) REIPotionRecipeUtils.getOutputFromRecipe(o);
                Ingredient ingredient = REIPotionRecipeUtils.getIngredientFromRecipe(o);
                if (!registeredPotionTypes.contains(input))
                    registerPotionType(recipeHelper, registeredPotionTypes, potionItemConversions, input);
                if (!registeredPotionTypes.contains(output))
                    registerPotionType(recipeHelper, registeredPotionTypes, potionItemConversions, output);
                potionItems.stream().map(Ingredient::getStackArray).forEach(itemStacks -> Arrays.stream(itemStacks).forEach(stack -> {
                    recipeHelper.registerDisplay(DefaultPlugin.BREWING, new DefaultBrewingDisplay(PotionUtil.setPotion(stack.copy(), input), ingredient, PotionUtil.setPotion(stack.copy(), output)));
                }));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
    
    private static void registerPotionType(RecipeHelper recipeHelper, List<Potion> list, List<BrewingRecipe> potionItemConversions, Potion potion) {
        list.add(potion);
        potionItemConversions.forEach(recipe -> {
            recipeHelper.registerDisplay(DefaultPlugin.BREWING, new DefaultBrewingDisplay(PotionUtil.setPotion(recipe.input.getDefaultStack(), potion), recipe.ingredient, PotionUtil.setPotion(recipe.output.getDefaultStack(), potion)));
        });
    }
    
}
