package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IRecipeDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefaultStoneCuttingDisplay implements IRecipeDisplay<StonecuttingRecipe> {
    
    private List<ItemStack> inputs, output;
    private StonecuttingRecipe display;
    
    public DefaultStoneCuttingDisplay(StonecuttingRecipe recipe) {
        this(recipe.getPreviewInputs(), recipe.getOutput());
        this.display = recipe;
    }
    
    public DefaultStoneCuttingDisplay(DefaultedList<Ingredient> ingredients, ItemStack output) {
        this.inputs = Lists.newArrayList();
        ingredients.stream().map(ingredient -> ingredient.getStackArray()).forEach(itemStacks -> Collections.addAll(inputs, itemStacks));
        this.output = Arrays.asList(output);
    }
    
    @Override
    public Optional<StonecuttingRecipe> getRecipe() {
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
        return DefaultPlugin.STONE_CUTTING;
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return getInput();
    }
    
}
