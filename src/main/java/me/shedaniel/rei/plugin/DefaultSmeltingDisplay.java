package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.tileentity.TileEntityFurnace;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultSmeltingDisplay implements RecipeDisplay<FurnaceRecipe> {
    
    private FurnaceRecipe display;
    private List<List<ItemStack>> input;
    private List<ItemStack> output;
    
    public DefaultSmeltingDisplay(FurnaceRecipe recipe) {
        this.display = recipe;
        List<ItemStack> fuel = Lists.newArrayList();
        this.input = Lists.newArrayList();
        fuel.addAll(TileEntityFurnace.getBurnTimes().keySet().stream().map(Item::getDefaultInstance).collect(Collectors.toList()));
        recipe.getIngredients().forEach(ingredient -> {
            input.add(Arrays.asList(ingredient.getMatchingStacks()));
        });
        input.add(fuel);
        this.output = Arrays.asList(recipe.getRecipeOutput());
    }
    
    @Override
    public Optional<FurnaceRecipe> getRecipe() {
        return Optional.ofNullable(display);
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return input;
    }
    
    public List<ItemStack> getFuel() {
        return input.get(1);
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
