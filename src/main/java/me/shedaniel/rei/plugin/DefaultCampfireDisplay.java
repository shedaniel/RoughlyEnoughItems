package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IRecipeDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.cooking.CampfireCookingRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefaultCampfireDisplay implements IRecipeDisplay<CampfireCookingRecipe> {
    
    private List<ItemStack> inputs, output;
    private int cookTime;
    private CampfireCookingRecipe display;
    
    public DefaultCampfireDisplay(CampfireCookingRecipe recipe) {
        this(recipe.getPreviewInputs(), recipe.getOutput(), recipe.getCookTime());
        this.display = recipe;
    }
    
    public DefaultCampfireDisplay(DefaultedList<Ingredient> ingredients, ItemStack output, int cookTime) {
        this.inputs = Lists.newArrayList();
        ingredients.stream().map(ingredient -> ingredient.getStackArray()).forEach(itemStacks -> Collections.addAll(inputs, itemStacks));
        this.output = Arrays.asList(output);
        this.cookTime = cookTime;
    }
    
    public int getCookTime() {
        return cookTime;
    }
    
    @Override
    public Optional<CampfireCookingRecipe> getRecipe() {
        return Optional.ofNullable(display);
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return Arrays.asList(inputs);
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return this.output;
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.CAMPFIRE;
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return getInput();
    }
    
}
