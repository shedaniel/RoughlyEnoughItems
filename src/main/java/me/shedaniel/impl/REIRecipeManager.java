package me.shedaniel.impl;

import me.shedaniel.Core;
import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.api.IREIPlugin;
import me.shedaniel.api.IRecipe;
import me.shedaniel.api.IRecipeManager;
import me.shedaniel.gui.RecipeGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.BooleanBiFunction;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Created by James on 8/7/2018.
 */
public class REIRecipeManager implements IRecipeManager {
    private Map<String, List<IRecipe>> recipeList;
    private List<IDisplayCategory> displayAdapters;
    public static RecipeManager recipeManager;
    private Map<Class<? extends Gui>, List<Function<Rectangle, Boolean>>> guiExcludeMap;
    
    private static REIRecipeManager myInstance;
    
    private REIRecipeManager() {
        recipeList = new HashMap<>();
        displayAdapters = new LinkedList<>();
        guiExcludeMap = new HashMap<>();
    }
    
    public List<IDisplayCategory> getDisplayAdapters() {
        return displayAdapters;
    }
    
    public static REIRecipeManager instance() {
        if (myInstance == null) {
            System.out.println("Newing me up.");
            myInstance = new REIRecipeManager();
        }
        return myInstance;
    }
    
    public void addExclusionOnGui(Class<? extends Gui> guiClass, Function<Rectangle, Boolean>... functions) {
        List<Function<Rectangle, Boolean>> list = guiExcludeMap.containsKey(guiClass) ? new LinkedList<>(guiExcludeMap.get(guiClass)) : new ArrayList<>();
        list.addAll(Arrays.asList(functions));
        guiExcludeMap.put(guiClass, list);
    }
    
    public boolean canAddSlot(Class<? extends Gui> guiClass, Rectangle slotRect) {
        if (!guiExcludeMap.containsKey(guiClass))
            return true;
        for(Function<Rectangle, Boolean> rectangleBooleanFunction : guiExcludeMap.get(guiClass)) {
            if (!rectangleBooleanFunction.apply(slotRect))
                return false;
        }
        return true;
    }
    
    @Override
    public void addRecipe(String id, IRecipe recipe) {
        if (recipeList.containsKey(id)) {
            recipeList.get(id).add(recipe);
        } else {
            List<IRecipe> recipes = new LinkedList<>();
            recipeList.put(id, recipes);
            recipes.add(recipe);
        }
    }
    
    @Override
    public void addRecipe(String id, List<? extends IRecipe> recipes) {
        if (recipeList.containsKey(id)) {
            recipeList.get(id).addAll(recipes);
        } else {
            List<IRecipe> newRecipeList = new LinkedList<>();
            recipeList.put(id, newRecipeList);
            newRecipeList.addAll(recipes);
        }
    }
    
    @Override
    public void addDisplayAdapter(IDisplayCategory adapter) {
        displayAdapters.add(adapter);
    }
    
    @Override
    public Map<IDisplayCategory, List<IRecipe>> getRecipesFor(ItemStack stack) {
        Map<IDisplayCategory, List<IRecipe>> categories = new HashMap<>();
        displayAdapters.forEach(f -> categories.put(f, new LinkedList<>()));
        for(List<IRecipe> value : recipeList.values()) {
            for(IRecipe iRecipe : value) {
                for(Object o : iRecipe.getOutput()) {
                    if (o instanceof ItemStack) {
                        if (ItemStack.areEqualIgnoreTags(stack, (ItemStack) o)) {
                            for(IDisplayCategory iDisplayCategory : categories.keySet()) {
                                if (iDisplayCategory.getId() == iRecipe.getId()) {
                                    categories.get(iDisplayCategory).add(iRecipe);
                                }
                            }
                        }
                    }
                }
            }
        }
        categories.keySet().removeIf(f -> categories.get(f).isEmpty());
        return categories;
    }
    
    public Map<IDisplayCategory, List<IRecipe>> getUsesFor(ItemStack stack) {
        Map<IDisplayCategory, List<IRecipe>> categories = new HashMap<>();
        displayAdapters.forEach(f -> categories.put(f, new LinkedList<>()));
        for(List<IRecipe> value : recipeList.values()) {
            for(IRecipe iRecipe : value) {
                boolean found = false;
                for(Object o : iRecipe.getInput()) {
                    List<ItemStack> input = (List<ItemStack>) o;
                    
                    for(ItemStack itemStack : input) {
                        if (ItemStack.areEqualIgnoreTags(itemStack, stack)) {
                            for(IDisplayCategory iDisplayCategory : categories.keySet()) {
                                if (iDisplayCategory.getId() == iRecipe.getId()) {
                                    categories.get(iDisplayCategory).add(iRecipe);
                                    found = true;
                                }
                            }
                            if (found)
                                break;
                        }
                    }
                    if (found)
                        break;
                }
            }
        }
        categories.keySet().removeIf(f -> categories.get(f).isEmpty());
        return categories;
    }
    
    
    public List<IDisplayCategory> getAdatapersForOutput(ItemStack stack) {
        return null;
    }
    
    public List<IDisplayCategory> getAdaptersForOutput(Item item) {
        return null;
    }
    
    public void RecipesLoaded(RecipeManager manager) {
        recipeList.clear();
        displayAdapters.clear();
        REIRecipeManager.instance().recipeManager = manager;
        Core.getListeners(IREIPlugin.class).forEach(IREIPlugin::registerCategories);
        Core.getListeners(IREIPlugin.class).forEach(IREIPlugin::registerRecipes);
    }
    
    public void displayRecipesFor(ItemStack stack) {
        Map<IDisplayCategory, List<IRecipe>> recipes = REIRecipeManager.instance().getRecipesFor(stack);
        if (recipes.isEmpty())
            return;
        RecipeGui gui = new RecipeGui(null, MinecraftClient.getInstance().currentGui, recipes);
        MinecraftClient.getInstance().openGui(gui);
    }
    
    public void displayUsesFor(ItemStack stack) {
        Map<IDisplayCategory, List<IRecipe>> recipes = REIRecipeManager.instance().getUsesFor(stack);
        if (recipes.isEmpty())
            return;
        RecipeGui gui = new RecipeGui(null, MinecraftClient.getInstance().currentGui, recipes);
        MinecraftClient.getInstance().openGui(gui);
    }
}
