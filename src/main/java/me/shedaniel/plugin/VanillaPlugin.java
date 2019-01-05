package me.shedaniel.plugin;

import me.shedaniel.api.IREIPlugin;
import me.shedaniel.impl.REIRecipeManager;
import me.shedaniel.listenerdefinitions.PotionCraftingAdder;
import me.shedaniel.plugin.blastfurnace.VanillaBlastFurnaceCategory;
import me.shedaniel.plugin.blastfurnace.VanillaBlastFurnaceRecipe;
import me.shedaniel.plugin.crafting.VanillaCraftingCategory;
import me.shedaniel.plugin.crafting.VanillaCraftingRecipe;
import me.shedaniel.plugin.crafting.VanillaShapedCraftingRecipe;
import me.shedaniel.plugin.crafting.VanillaShapelessCraftingRecipe;
import me.shedaniel.plugin.furnace.VanillaFurnaceCategory;
import me.shedaniel.plugin.furnace.VanillaFurnaceRecipe;
import me.shedaniel.plugin.potion.VanillaPotionCategory;
import me.shedaniel.plugin.potion.VanillaPotionRecipe;
import me.shedaniel.plugin.smoker.VanillaSmokerCategory;
import me.shedaniel.plugin.smoker.VanillaSmokerRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.recipe.smelting.BlastingRecipe;
import net.minecraft.recipe.smelting.SmeltingRecipe;
import net.minecraft.recipe.smelting.SmokingRecipe;
import net.minecraft.util.registry.Registry;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VanillaPlugin implements IREIPlugin, PotionCraftingAdder {
    
    private List<VanillaPotionRecipe> potionRecipes = new LinkedList<>();
    private List<VanillaCraftingRecipe> recipes = new LinkedList<>();
    private List<VanillaFurnaceRecipe> furnaceRecipes = new LinkedList<>();
    private List<VanillaSmokerRecipe> smokerRecipes = new LinkedList<>();
    private List<VanillaBlastFurnaceRecipe> blastFurnaceRecipes = new LinkedList<>();
    
    @Override
    public void registerCategories() {
        REIRecipeManager.instance().addDisplayAdapter(new VanillaCraftingCategory());
        REIRecipeManager.instance().addDisplayAdapter(new VanillaFurnaceCategory());
        REIRecipeManager.instance().addDisplayAdapter(new VanillaSmokerCategory());
        REIRecipeManager.instance().addDisplayAdapter(new VanillaBlastFurnaceCategory());
        REIRecipeManager.instance().addDisplayAdapter(new VanillaPotionCategory());
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("a", new ItemStack(Items.ITEM_FRAME)));
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("b", new ItemStack(Items.ITEM_FRAME)));
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("c", new ItemStack(Items.ITEM_FRAME)));
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("d", new ItemStack(Items.ITEM_FRAME)));
//        REIRecipeManager.instance().addDisplayAdapter(new TestRandomCategory("e", new ItemStack(Items.ITEM_FRAME)));
    }
    
    @Override
    public void registerRecipes() {
        for(Recipe recipe : REIRecipeManager.instance().recipeManager.values())
            if (recipe instanceof ShapelessRecipe)
                recipes.add(new VanillaShapelessCraftingRecipe((ShapelessRecipe) recipe));
            else if (recipe instanceof ShapedRecipe)
                recipes.add(new VanillaShapedCraftingRecipe((ShapedRecipe) recipe));
            else if (recipe instanceof SmeltingRecipe)
                furnaceRecipes.add(new VanillaFurnaceRecipe((SmeltingRecipe) recipe));
            else if (recipe instanceof SmokingRecipe)
                smokerRecipes.add(new VanillaSmokerRecipe((SmokingRecipe) recipe));
            else if (recipe instanceof BlastingRecipe)
                blastFurnaceRecipes.add(new VanillaBlastFurnaceRecipe((BlastingRecipe) recipe));
        Registry.POTION.stream().filter(potion -> !potion.equals(Potions.EMPTY)).forEach(potion -> {
            ItemStack basePotion = PotionUtil.setPotion(new ItemStack(Items.POTION), potion),
                    splashPotion = PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), potion),
                    lingeringPotion = PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), potion);
            potionRecipes.add(new VanillaPotionRecipe(new ItemStack[]{basePotion}, Ingredient.ofItems(Items.GUNPOWDER).getStackArray(),
                    new ItemStack[]{splashPotion}));
            potionRecipes.add(new VanillaPotionRecipe(new ItemStack[]{splashPotion}, Ingredient.ofItems(Items.DRAGON_BREATH).getStackArray(),
                    new ItemStack[]{lingeringPotion}));
        });
        
        REIRecipeManager.instance().addRecipe("vanilla", recipes);
        REIRecipeManager.instance().addRecipe("furnace", furnaceRecipes);
        REIRecipeManager.instance().addRecipe("smoker", smokerRecipes);
        REIRecipeManager.instance().addRecipe("potion", potionRecipes.stream().distinct().collect(Collectors.toList()));
        REIRecipeManager.instance().addRecipe("blastingfurnace", blastFurnaceRecipes);
//        REIRecipeManager.instance().addRecipe("a", Arrays.asList(new RandomRecipe("a")));
//        REIRecipeManager.instance().addRecipe("b", Arrays.asList(new RandomRecipe("b")));
//        REIRecipeManager.instance().addRecipe("c", Arrays.asList(new RandomRecipe("c")));
//        REIRecipeManager.instance().addRecipe("d", Arrays.asList(new RandomRecipe("d")));
//        REIRecipeManager.instance().addRecipe("e", Arrays.asList(new RandomRecipe("e")));
    }
    
    @Override
    public void registerSpecialGuiExclusion() {
    
    }
    
    @Override
    public void addPotionRecipe(Potion inputType, Item reagent, Potion outputType) {
        potionRecipes.add(new VanillaPotionRecipe(new ItemStack[]{PotionUtil.setPotion(new ItemStack(Items.POTION), inputType)},
                Ingredient.ofItems(reagent).getStackArray(),
                new ItemStack[]{PotionUtil.setPotion(new ItemStack(Items.POTION), outputType)}));
    }
}
