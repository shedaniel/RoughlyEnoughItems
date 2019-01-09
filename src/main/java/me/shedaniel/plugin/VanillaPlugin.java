package me.shedaniel.plugin;

import me.shedaniel.api.IREIPlugin;
import me.shedaniel.impl.REIRecipeManager;
import me.shedaniel.listenerdefinitions.PotionCraftingAdder;
import me.shedaniel.plugin.crafting.VanillaCraftingCategory;
import me.shedaniel.plugin.crafting.VanillaCraftingRecipe;
import me.shedaniel.plugin.crafting.VanillaShapedCraftingRecipe;
import me.shedaniel.plugin.crafting.VanillaShapelessCraftingRecipe;
import me.shedaniel.plugin.furnace.VanillaFurnaceCategory;
import me.shedaniel.plugin.furnace.VanillaFurnaceRecipe;
import me.shedaniel.plugin.potion.VanillaPotionCategory;
import me.shedaniel.plugin.potion.VanillaPotionRecipe;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.registry.IRegistry;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VanillaPlugin implements IREIPlugin, PotionCraftingAdder {
    
    private List<VanillaPotionRecipe> potionRecipes = new LinkedList<>();
    private List<VanillaCraftingRecipe> recipes = new LinkedList<>();
    private List<VanillaFurnaceRecipe> furnaceRecipes = new LinkedList<>();
    
    @Override
    public void registerCategories() {
        REIRecipeManager.instance().addDisplayAdapter(new VanillaCraftingCategory());
        REIRecipeManager.instance().addDisplayAdapter(new VanillaFurnaceCategory());
        REIRecipeManager.instance().addDisplayAdapter(new VanillaPotionCategory());
    }
    
    @Override
    public void registerRecipes() {
        for(IRecipe recipe : REIRecipeManager.instance().recipeManager.getRecipes()) {
            if (recipe instanceof ShapelessRecipe)
                recipes.add(new VanillaShapelessCraftingRecipe((ShapelessRecipe) recipe));
            else if (recipe instanceof ShapedRecipe)
                recipes.add(new VanillaShapedCraftingRecipe((ShapedRecipe) recipe));
            else if (recipe instanceof FurnaceRecipe)
                furnaceRecipes.add(new VanillaFurnaceRecipe((FurnaceRecipe) recipe));
        }
        IRegistry.POTION.stream().filter(potionType -> !potionType.equals(PotionTypes.EMPTY)).forEach(potionType -> {
            ItemStack basePotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), potionType),
                    splashPotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potionType),
                    lingeringPotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), potionType);
            potionRecipes.add(new VanillaPotionRecipe(new ItemStack[]{basePotion}, Ingredient.fromItems(Items.GUNPOWDER).getMatchingStacks(),
                    new ItemStack[]{splashPotion}));
            potionRecipes.add(new VanillaPotionRecipe(new ItemStack[]{splashPotion}, Ingredient.fromItems(Items.DRAGON_BREATH).getMatchingStacks(),
                    new ItemStack[]{lingeringPotion}));
        });
        REIRecipeManager.instance().addRecipe("vanilla", recipes);
        REIRecipeManager.instance().addRecipe("furnace", furnaceRecipes);
        REIRecipeManager.instance().addRecipe("potion", potionRecipes.stream().collect(Collectors.toList()));
    }
    
    @Override
    public void registerSpecialGuiExclusion() {
    
    }
    
    @Override
    public void addPotionRecipe(PotionType inputType, Item reagent, PotionType outputType) {
        potionRecipes.add(new VanillaPotionRecipe(new ItemStack[]{PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), inputType)},
                Ingredient.fromItems(reagent).getMatchingStacks(),
                new ItemStack[]{PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), outputType)}));
    }
}
