package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.cooking.BlastingRecipe;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultBlastingDisplay implements RecipeDisplay<BlastingRecipe> {
    
    private BlastingRecipe display;
    private List<List<ItemStack>> input;
    private List<ItemStack> output;
    
    public DefaultBlastingDisplay(BlastingRecipe recipe) {
        this.display = recipe;
        List<ItemStack> fuel = Lists.newArrayList();
        this.input = Lists.newArrayList();
        fuel.addAll(FurnaceBlockEntity.createFuelTimeMap().keySet().stream().map(Item::getDefaultStack).collect(Collectors.toList()));
        recipe.getPreviewInputs().forEach(ingredient -> {
            input.add(Arrays.asList(ingredient.getStackArray()));
        });
        input.add(fuel);
        this.output = Arrays.asList(recipe.getOutput());
    }
    
    @Override
    public Optional<BlastingRecipe> getRecipe() {
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
        return DefaultPlugin.BLASTING;
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return input;
    }
    
}
