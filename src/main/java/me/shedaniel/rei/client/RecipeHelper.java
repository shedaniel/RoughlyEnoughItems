package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RecipeHelper implements IRecipeHelper {
    
    private final AtomicInteger recipeCount = new AtomicInteger();
    private final Map<Identifier, List<IRecipeDisplay>> recipeCategoryListMap = Maps.newHashMap();
    private final List<IRecipeCategory> categories = Lists.newArrayList();
    private final Map<Identifier, SpeedCraftAreaSupplier> speedCraftAreaSupplierMap = Maps.newHashMap();
    private final Map<Identifier, List<SpeedCraftFunctional>> speedCraftFunctionalMap = Maps.newHashMap();
    private RecipeManager recipeManager;
    
    @Override
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
                            if (ItemStack.areEqualIgnoreTags(slotPossible, possibleType)) {
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
    
    @Override
    public void registerCategory(IRecipeCategory category) {
        categories.add(category);
        recipeCategoryListMap.put(category.getIdentifier(), Lists.newLinkedList());
    }
    
    @Override
    public void registerDisplay(Identifier categoryIdentifier, IRecipeDisplay display) {
        if (!recipeCategoryListMap.containsKey(categoryIdentifier))
            return;
        recipeCount.incrementAndGet();
        recipeCategoryListMap.get(categoryIdentifier).add(display);
    }
    
    @Override
    public Map<IRecipeCategory, List<IRecipeDisplay>> getRecipesFor(ItemStack stack) {
        Map<Identifier, List<IRecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getIdentifier(), Lists.newArrayList()));
        for(Map.Entry<Identifier, List<IRecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            IRecipeCategory category = getCategory(entry.getKey());
            for(IRecipeDisplay recipeDisplay : entry.getValue())
                for(ItemStack outputStack : (List<ItemStack>) recipeDisplay.getOutput())
                    if (category.checkTags() ? ItemStack.areEqual(stack, outputStack) : ItemStack.areEqualIgnoreTags(stack, outputStack))
                        categoriesMap.get(recipeDisplay.getRecipeCategory()).add(recipeDisplay);
        }
        Map<IRecipeCategory, List<IRecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
        categories.forEach(category -> {
            if (categoriesMap.containsKey(category.getIdentifier()) && !categoriesMap.get(category.getIdentifier()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getIdentifier()));
        });
        return recipeCategoryListMap;
    }
    
    private IRecipeCategory getCategory(Identifier identifier) {
        return categories.stream().filter(category -> category.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
    
    @Override
    public Map<IRecipeCategory, List<IRecipeDisplay>> getUsagesFor(ItemStack stack) {
        Map<Identifier, List<IRecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getIdentifier(), Lists.newArrayList()));
        for(Map.Entry<Identifier, List<IRecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            IRecipeCategory category = getCategory(entry.getKey());
            for(IRecipeDisplay recipeDisplay : entry.getValue()) {
                boolean found = false;
                for(List<ItemStack> input : (List<List<ItemStack>>) recipeDisplay.getInput()) {
                    for(ItemStack itemStack : input) {
                        if (category.checkTags() ? ItemStack.areEqual(itemStack, stack) : ItemStack.areEqualIgnoreTags(itemStack, stack)) {
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
            if (categoriesMap.containsKey(category.getIdentifier()) && !categoriesMap.get(category.getIdentifier()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getIdentifier()));
        });
        return recipeCategoryListMap;
    }
    
    @Override
    public List<IRecipeCategory> getAllCategories() {
        return new LinkedList<>(categories);
    }
    
    @Override
    public SpeedCraftAreaSupplier getSpeedCraftButtonArea(IRecipeCategory category) {
        if (!speedCraftAreaSupplierMap.containsKey(category.getIdentifier()))
            return bounds -> {
                return new Rectangle((int) bounds.getMaxX() - 16, (int) bounds.getMaxY() - 16, 10, 10);
            };
        return speedCraftAreaSupplierMap.get(category.getIdentifier());
    }
    
    @Override
    public void registerSpeedCraftButtonArea(Identifier category, SpeedCraftAreaSupplier rectangle) {
        speedCraftAreaSupplierMap.put(category, rectangle);
    }
    
    @Override
    public List<SpeedCraftFunctional> getSpeedCraftFunctional(IRecipeCategory category) {
        if (speedCraftFunctionalMap.get(category.getIdentifier()) == null)
            return Lists.newArrayList();
        return speedCraftFunctionalMap.get(category.getIdentifier());
    }
    
    @Override
    public void registerSpeedCraftFunctional(Identifier category, SpeedCraftFunctional functional) {
        List<SpeedCraftFunctional> list = speedCraftFunctionalMap.containsKey(category) ? new LinkedList<>(speedCraftFunctionalMap.get(category)) : Lists.newLinkedList();
        list.add(functional);
        speedCraftFunctionalMap.put(category, list);
    }
    
    @SuppressWarnings("deprecation")
    public void recipesLoaded(RecipeManager recipeManager) {
        this.recipeCount.set(0);
        this.recipeManager = recipeManager;
        this.recipeCategoryListMap.clear();
        this.categories.clear();
        this.speedCraftAreaSupplierMap.clear();
        this.speedCraftFunctionalMap.clear();
        List<IRecipePlugin> plugins = new LinkedList<>(RoughlyEnoughItemsCore.getPlugins());
        plugins.sort((first, second) -> {
            return second.getPriority() - first.getPriority();
        });
        RoughlyEnoughItemsCore.LOGGER.info("Loading %d REI plugins: %s", plugins.size(), String.join(", ", plugins.stream().map(plugin -> {
            return RoughlyEnoughItemsCore.getPluginIdentifier(plugin).map(Identifier::toString).orElseGet(() -> "null");
        }).collect(Collectors.toList())));
        Collections.reverse(plugins);
        RoughlyEnoughItemsCore.getItemRegisterer().getModifiableItemList().clear();
        IPluginDisabler pluginDisabler = RoughlyEnoughItemsCore.getPluginDisabler();
        plugins.forEach(plugin -> {
            Identifier identifier = RoughlyEnoughItemsCore.getPluginIdentifier(plugin).orElseGet(() -> new Identifier("null"));
            if (pluginDisabler.isFunctionEnabled(identifier, PluginFunction.REGISTER_ITEMS))
                plugin.registerItems(RoughlyEnoughItemsCore.getItemRegisterer());
            if (pluginDisabler.isFunctionEnabled(identifier, PluginFunction.REGISTER_CATEGORIES))
                plugin.registerPluginCategories(this);
            if (pluginDisabler.isFunctionEnabled(identifier, PluginFunction.REGISTER_RECIPE_DISPLAYS))
                plugin.registerRecipeDisplays(this);
            if (pluginDisabler.isFunctionEnabled(identifier, PluginFunction.REGISTER_SPEED_CRAFT))
                plugin.registerSpeedCraft(this);
        });
        RoughlyEnoughItemsCore.LOGGER.info("Registered REI Categories: " + String.join(", ", categories.stream().map(IRecipeCategory::getCategoryName).collect(Collectors.toList())));
        RoughlyEnoughItemsCore.LOGGER.info("Registered %d recipes for REI.", recipeCount.get());
    }
    
    @Override
    public int getRecipeCount() {
        return recipeCount.get();
    }
    
    @Override
    public Map<IRecipeCategory, List<IRecipeDisplay>> getAllRecipes() {
        Map<IRecipeCategory, List<IRecipeDisplay>> map = Maps.newLinkedHashMap();
        Map<Identifier, List<IRecipeDisplay>> tempMap = Maps.newLinkedHashMap();
        recipeCategoryListMap.forEach((identifier, recipeDisplays) -> tempMap.put(identifier, new LinkedList<>(recipeDisplays)));
        categories.forEach(category -> {
            if (tempMap.containsKey(category.getIdentifier()))
                map.put(category, tempMap.get(category.getIdentifier()));
        });
        return map;
    }
    
}
