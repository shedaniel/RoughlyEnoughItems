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
    
    @Override
    public void register() {
        List<VanillaCraftingRecipe> recipes = new LinkedList<>();
        List<VanillaFurnaceRecipe> furnaceRecipes = new LinkedList<>();
        REIRecipeManager.instance().addDisplayAdapter(new VanillaCraftingCategory());
        REIRecipeManager.instance().addDisplayAdapter(new VanillaFurnaceCategory());
        REIRecipeManager.instance().addDisplayAdapter(new VanillaPotionCategory());
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("a", new ItemStack(Blocks.ACACIA_BUTTON.asItem())));
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("b", new ItemStack(Blocks.ACACIA_LOG.asItem())));
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("c", new ItemStack(Blocks.ACACIA_LOG.asItem())));
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("d", new ItemStack(Blocks.ACACIA_LOG.asItem())));
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("e", new ItemStack(Blocks.ACACIA_LOG.asItem())));
        
        for(IRecipe recipe : REIRecipeManager.instance().recipeManager.getRecipes()) {
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
//        REIRecipeManager.instance().addPotionRecipe("a", new RandomRecipe("a"));
//        REIRecipeManager.instance().addPotionRecipe("b", new RandomRecipe("b"));
//        REIRecipeManager.instance().addPotionRecipe("c", new RandomRecipe("c"));
//        REIRecipeManager.instance().addPotionRecipe("d", new RandomRecipe("d"));
//        REIRecipeManager.instance().addPotionRecipe("e", new RandomRecipe("e"));
    }
    
    @Override
    public void addPotionRecipe(PotionType inputType, Item reagent, PotionType outputType) {
        potionRecipes.add(new VanillaPotionRecipe(new ItemStack[]{PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), inputType)},
                Ingredient.fromItems(reagent).getMatchingStacks(),
                new ItemStack[]{PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), outputType)}));
    }
}
