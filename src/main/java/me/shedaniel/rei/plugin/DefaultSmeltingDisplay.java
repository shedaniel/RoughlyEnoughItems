package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IRecipeDisplay;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.recipe.smelting.SmeltingRecipe;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultSmeltingDisplay implements IRecipeDisplay<SmeltingRecipe> {
    
    private SmeltingRecipe display;
    private List<List<ItemStack>> input;
    private List<ItemStack> fuel, output;
    
    public DefaultSmeltingDisplay(SmeltingRecipe recipe) {
        this.display = recipe;
        this.fuel = Lists.newArrayList();
        this.input = Lists.newArrayList();
        fuel.addAll(FurnaceBlockEntity.createBurnableMap().keySet().stream().map(Item::getDefaultStack).collect(Collectors.toList()));
        recipe.getPreviewInputs().forEach(ingredient -> {
            input.add(Arrays.asList(ingredient.getStackArray()));
        });
        this.output = Arrays.asList(recipe.getOutput());
    }
    
    @Override
    public SmeltingRecipe getRecipe() {
        return display;
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return input;
    }
    
    public List<ItemStack> getFuel() {
        return fuel;
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return output;
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.SMELTING;
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return input;
    }
    
}
