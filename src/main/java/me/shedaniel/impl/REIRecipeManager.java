package me.shedaniel.impl;

import me.shedaniel.Core;
import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.api.IREIPlugin;
import me.shedaniel.api.IRecipe;
import me.shedaniel.api.IRecipeManager;
import me.shedaniel.gui.RecipeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import org.dimdev.riftloader.RiftLoader;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by James on 8/7/2018.
 */
public class REIRecipeManager implements IRecipeManager {
    
    private Map<String, List<IRecipe>> recipeList;
    private List<IDisplayCategory> displayAdapters;
    public static RecipeManager recipeManager;
    private Map<Class<? extends GuiScreen>, List<Function<Rectangle, Boolean>>> guiExcludeMap;
    
    private static REIRecipeManager myInstance;
    
    private REIRecipeManager() {
        recipeList = new HashMap<>();
        displayAdapters = new LinkedList<>();
        guiExcludeMap = new HashMap<>();
    }
    
    public Map<String, List<IRecipe>> getRecipeList() {
        return recipeList;
    }
    
    public List<IDisplayCategory> getDisplayAdapters() {
        return displayAdapters;
    }
    
    public static REIRecipeManager instance() {
        if (myInstance == null) {
            Core.LOGGER.info("REI: Newing me up.");
            myInstance = new REIRecipeManager();
        }
        return myInstance;
    }
    
    public void addExclusionOnGui(Class<? extends GuiScreen> guiClass, Function<Rectangle, Boolean>... functions) {
        List<Function<Rectangle, Boolean>> list = guiExcludeMap.containsKey(guiClass) ? new LinkedList<>(guiExcludeMap.get(guiClass)) : new ArrayList<>();
        list.addAll(Arrays.asList(functions));
        guiExcludeMap.put(guiClass, list);
    }
    
    public boolean canAddSlot(Class<? extends GuiScreen> guiClass, Rectangle slotRect) {
        if (!guiExcludeMap.containsKey(guiClass))
            return true;
        for(Function<Rectangle, Boolean> rectangleBooleanFunction : guiExcludeMap.get(guiClass))
            if (!rectangleBooleanFunction.apply(slotRect))
                return false;
        return true;
    }
    
    @Override
    public void addRecipe(String id, IRecipe recipe) {
        if (recipeList.containsKey(id))
            recipeList.get(id).add(recipe);
        else {
            List<IRecipe> recipes = new LinkedList<>();
            recipeList.put(id, recipes);
            recipes.add(recipe);
        }
    }
    
    @Override
    public void addRecipe(String id, List<? extends IRecipe> recipes) {
        if (recipeList.containsKey(id))
            recipeList.get(id).addAll(recipes);
        else {
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
        for(List<IRecipe> value : recipeList.values())
            for(IRecipe iRecipe : value)
                for(Object o : iRecipe.getOutput())
                    if (o instanceof ItemStack)
                        if (ItemStack.areItemsEqual(stack, (ItemStack) o))
                            for(IDisplayCategory iDisplayCategory : categories.keySet())
                                if (iDisplayCategory.getId() == iRecipe.getId())
                                    categories.get(iDisplayCategory).add(iRecipe);
        categories.keySet().removeIf(f -> categories.get(f).isEmpty());
        return categories;
    }
    
    public Map<IDisplayCategory, List<IRecipe>> getUsesFor(ItemStack stack) {
        Map<IDisplayCategory, List<IRecipe>> categories = new HashMap<>();
        displayAdapters.forEach(f -> categories.put(f, new LinkedList<>()));
        for(List<IRecipe> value : recipeList.values())
            for(IRecipe iRecipe : value) {
                boolean found = false;
                for(Object o : iRecipe.getInput()) {
                    List<ItemStack> input = (List<ItemStack>) o;
                    for(ItemStack itemStack : input) {
                        if (ItemStack.areItemsEqual(itemStack, stack)) {
                            for(IDisplayCategory iDisplayCategory : categories.keySet())
                                if (iDisplayCategory.getId() == iRecipe.getId()) {
                                    categories.get(iDisplayCategory).add(iRecipe);
                                    found = true;
                                }
                            if (found)
                                break;
                        }
                    }
                    if (found)
                        break;
                }
            }
        categories.keySet().removeIf(f -> categories.get(f).isEmpty());
        return categories;
    }
    
    @Deprecated
    public List<IRecipe> findUsageForItems(List<ItemStack> types) {
        List<IRecipe> recipes = new ArrayList<>();
        types.forEach(item -> {
            Map<IDisplayCategory, List<IRecipe>> itemUsages = getUsesFor(item);
            itemUsages.values().forEach(iRecipes -> recipes.addAll(iRecipes));
        });
        return recipes;
    }
    
    public List<ItemStack> findCraftableByItems(List<ItemStack> types) {
        List<ItemStack> craftables = new ArrayList<>();
        for(List<IRecipe> value : recipeList.values())
            for(IRecipe iRecipe : value) {
                int slotsCraftable = 0;
                List<List<ItemStack>> requiredInput = (List<List<ItemStack>>) iRecipe.getRecipeRequiredInput();
                for(List<ItemStack> slot : requiredInput) {
                    if (slot.isEmpty()) {
                        slotsCraftable++;
                        continue;
                    }
                    boolean slotDone = false;
                    for(ItemStack possibleType : types) {
                        for(ItemStack slotPossible : slot)
                            if (ItemStack.areItemsEqual(slotPossible, possibleType)) {
                                slotsCraftable++;
                                slotDone = true;
                                break;
                            }
                        if (slotDone)
                            break;
                    }
                }
                if (slotsCraftable == iRecipe.getRecipeRequiredInput().size())
                    craftables.addAll((List<ItemStack>) iRecipe.getOutput());
            }
        return craftables.stream().distinct().collect(Collectors.toList());
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
        RiftLoader.instance.getListeners(IREIPlugin.class).forEach(IREIPlugin::registerCategories);
        RiftLoader.instance.getListeners(IREIPlugin.class).forEach(IREIPlugin::registerRecipes);
    }
    
    public void displayRecipesFor(ItemStack stack) {
        Map<IDisplayCategory, List<IRecipe>> recipes = REIRecipeManager.instance().getRecipesFor(stack);
        if (recipes.isEmpty())
            return;
        RecipeGui gui;
        if (Minecraft.getInstance().currentScreen instanceof RecipeGui)
            gui = new RecipeGui(null, ((RecipeGui) Minecraft.getInstance().currentScreen).getPrevScreen(), recipes);
        else gui = new RecipeGui(null, Minecraft.getInstance().currentScreen, recipes);
        Minecraft.getInstance().displayGuiScreen(gui);
    }
    
    public void displayUsesFor(ItemStack stack) {
        Map<IDisplayCategory, List<IRecipe>> recipes = REIRecipeManager.instance().getUsesFor(stack);
        if (recipes.isEmpty())
            return;
        RecipeGui gui;
        if (Minecraft.getInstance().currentScreen instanceof RecipeGui)
            gui = new RecipeGui(null, ((RecipeGui) Minecraft.getInstance().currentScreen).getPrevScreen(), recipes);
        else gui = new RecipeGui(null, Minecraft.getInstance().currentScreen, recipes);
        Minecraft.getInstance().displayGuiScreen(gui);
    }
}
