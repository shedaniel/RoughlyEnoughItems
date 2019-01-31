package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.RoughlyEnoughItemsPlugin;
import me.shedaniel.rei.api.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeHelper {
    
    private final Map<ResourceLocation, List<IRecipeDisplay>> recipeCategoryListMap = Maps.newHashMap();
    private final List<IRecipeCategory> categories = Lists.newArrayList();
    private RecipeManager recipeManager;
    private final Map<ResourceLocation, SpeedCraftAreaSupplier> speedCraftAreaSupplierMap = Maps.newHashMap();
    private final Map<ResourceLocation, List<SpeedCraftFunctional>> speedCraftFunctionalMap = Maps.newHashMap();
    
    public static RecipeHelper getInstance() {
        return RoughlyEnoughItemsCore.getRecipeHelper();
    }
    
    public List<ItemStack> findCraftableByItems(List<ItemStack> inventoryItems) {
        List<ItemStack> craftables = new ArrayList<>();
        for(List<IRecipeDisplay> value : recipeCategoryListMap.values())
            for(IRecipeDisplay recipeDisplay : value) {
                int slotsCraftable = 0;
                List<List<ItemStack>> requiredInput = (List<List<ItemStack>>) recipeDisplay.getRequiredItems();
                for(List<ItemStack> slot : requiredInput) {
                    if (slot.isEmpty()) {
                        slotsCraftable++;
                        continue;
                    }
                    boolean slotDone = false;
                    for(ItemStack possibleType : inventoryItems) {
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
                if (slotsCraftable == recipeDisplay.getRequiredItems().size())
                    craftables.addAll((List<ItemStack>) recipeDisplay.getOutput());
            }
        return craftables.stream().distinct().collect(Collectors.toList());
    }
    
    public void registerCategory(IRecipeCategory category) {
        categories.add(category);
        recipeCategoryListMap.put(category.getResourceLocation(), Lists.newLinkedList());
    }
    
    public void registerRecipe(ResourceLocation categoryIdentifier, IRecipeDisplay display) {
        if (!recipeCategoryListMap.containsKey(categoryIdentifier))
            return;
        recipeCategoryListMap.get(categoryIdentifier).add(display);
    }
    
    public Map<IRecipeCategory, List<IRecipeDisplay>> getRecipesFor(ItemStack stack) {
        Map<ResourceLocation, List<IRecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getResourceLocation(), Lists.newArrayList()));
        for(Map.Entry<ResourceLocation, List<IRecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            IRecipeCategory category = getCategory(entry.getKey());
            for(IRecipeDisplay recipeDisplay : entry.getValue())
                for(ItemStack outputStack : (List<ItemStack>) recipeDisplay.getOutput())
                    if (category.checkTags() ? ItemStack.areItemStacksEqual(stack, outputStack) : ItemStack.areItemsEqual(stack, outputStack))
                        categoriesMap.get(recipeDisplay.getRecipeCategory()).add(recipeDisplay);
        }
        Map<IRecipeCategory, List<IRecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
        categories.forEach(category -> {
            if (categoriesMap.containsKey(category.getResourceLocation()) && !categoriesMap.get(category.getResourceLocation()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getResourceLocation()));
        });
        return recipeCategoryListMap;
    }
    
    private IRecipeCategory getCategory(ResourceLocation resourceLocation) {
        return categories.stream().filter(category -> category.getResourceLocation().equals(resourceLocation)).findFirst().orElse(null);
    }
    
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
    
    public Map<IRecipeCategory, List<IRecipeDisplay>> getUsagesFor(ItemStack stack) {
        Map<ResourceLocation, List<IRecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getResourceLocation(), Lists.newArrayList()));
        for(Map.Entry<ResourceLocation, List<IRecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            IRecipeCategory category = getCategory(entry.getKey());
            for(IRecipeDisplay recipeDisplay : entry.getValue()) {
                boolean found = false;
                for(List<ItemStack> input : (List<List<ItemStack>>) recipeDisplay.getInput()) {
                    for(ItemStack itemStack : input) {
                        if (category.checkTags() ? ItemStack.areItemStacksEqual(itemStack, stack) : ItemStack.areItemsEqual(itemStack, stack)) {
                            categoriesMap.get(recipeDisplay.getRecipeCategory()).add(recipeDisplay);
                            found = true;
                            break;
                        }
                    }
                    if (found)
                        break;
                }
            }
        }
        Map<IRecipeCategory, List<IRecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
        categories.forEach(category -> {
            if (categoriesMap.containsKey(category.getResourceLocation()) && !categoriesMap.get(category.getResourceLocation()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getResourceLocation()));
        });
        return recipeCategoryListMap;
    }
    
    public List<IRecipeCategory> getCategories() {
        return new LinkedList<>(categories);
    }
    
    public SpeedCraftAreaSupplier getSpeedCraftButtonArea(IRecipeCategory category) {
        if (!speedCraftAreaSupplierMap.containsKey(category.getResourceLocation()))
            return bounds -> {
                return new Rectangle((int) bounds.getMaxX() - 16, (int) bounds.getMaxY() - 16, 10, 10);
            };
        return speedCraftAreaSupplierMap.get(category.getResourceLocation());
    }
    
    public void registerSpeedCraftButtonArea(ResourceLocation category, SpeedCraftAreaSupplier rectangle) {
        speedCraftAreaSupplierMap.put(category, rectangle);
    }
    
    public List<SpeedCraftFunctional> getSpeedCraftFunctional(IRecipeCategory category) {
        if (speedCraftFunctionalMap.get(category.getResourceLocation()) == null)
            return Lists.newArrayList();
        return speedCraftFunctionalMap.get(category.getResourceLocation());
    }
    
    public void registerSpeedCraftFunctional(ResourceLocation category, SpeedCraftFunctional functional) {
        List<SpeedCraftFunctional> list = speedCraftFunctionalMap.containsKey(category) ? new LinkedList<>(speedCraftFunctionalMap.get(category)) : Lists.newLinkedList();
        list.add(functional);
        speedCraftFunctionalMap.put(category, list);
    }
    
    public void recipesLoaded(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
        this.recipeCategoryListMap.clear();
        this.categories.clear();
        this.speedCraftAreaSupplierMap.clear();
        this.speedCraftFunctionalMap.clear();
        List<IRecipePlugin> plugins = new LinkedList<>(RoughlyEnoughItemsPlugin.getPlugins());
        plugins.sort((first, second) -> {
            return second.getPriority() - first.getPriority();
        });
        RoughlyEnoughItemsCore.LOGGER.info("Loading %d REI plugins: %s", plugins.size(), String.join(", ", plugins.stream().map(plugin -> {
            String resourceLocation = RoughlyEnoughItemsPlugin.getPluginResourceLocation(plugin);
            return resourceLocation == null ? "NULL" : resourceLocation;
        }).collect(Collectors.toList())));
        Collections.reverse(plugins);
        plugins.forEach(plugin -> {
            plugin.registerPluginCategories();
            plugin.registerRecipes();
            plugin.registerSpeedCraft();
        });
        RoughlyEnoughItemsCore.LOGGER.info("Registered REI Categories: " + String.join(", ", categories.stream().map(category -> {
            return category.getCategoryName();
        }).collect(Collectors.toList())));
    }
    
}
