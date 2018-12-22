package me.shedaniel.impl;

import me.shedaniel.api.IAEIPlugin;
import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.api.IRecipe;
import me.shedaniel.api.IRecipeManager;
import me.shedaniel.gui.RecipeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.ResourceLocation;
import org.dimdev.riftloader.RiftLoader;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by James on 8/7/2018.
 */
public class AEIRecipeManager implements IRecipeManager {
    private Map<String, List<IRecipe>> recipeList;
    private List<IDisplayCategory> displayAdapters;
    public static RecipeManager recipeManager;
    
    private static AEIRecipeManager myInstance;
    
    private AEIRecipeManager() {
        recipeList = new HashMap<>();
        displayAdapters = new LinkedList<>();
    }
    
    public static AEIRecipeManager instance() {
        if (myInstance == null) {
            System.out.println("Newing me up.");
            myInstance = new AEIRecipeManager();
        }
        return myInstance;
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
                        if (ItemStack.areItemsEqual(stack, (ItemStack) o)) {
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
                        if (ItemStack.areItemsEqual(itemStack, stack)) {
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
        AEIRecipeManager.instance().recipeManager = manager;
        RiftLoader.instance.getListeners(IAEIPlugin.class).forEach(IAEIPlugin::register);
    }
    
    public void displayRecipesFor(ItemStack stack) {
        Map<IDisplayCategory, List<IRecipe>> recipes = AEIRecipeManager.instance().getRecipesFor(stack);
        if (recipes.isEmpty())
            return;
        RecipeGui gui = new RecipeGui(null, Minecraft.getInstance().currentScreen, recipes);
        Minecraft.getInstance().displayGuiScreen(gui);
    }
    
    public void displayUsesFor(ItemStack stack) {
        Map<IDisplayCategory, List<IRecipe>> recipes = AEIRecipeManager.instance().getUsesFor(stack);
        if (recipes.isEmpty())
            return;
        RecipeGui gui = new RecipeGui(null, Minecraft.getInstance().currentScreen, recipes);
        Minecraft.getInstance().displayGuiScreen(gui);
    }
}
