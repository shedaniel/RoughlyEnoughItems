/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RecipeHelperImpl implements RecipeHelper {
    
    private static final Comparator<DisplayVisibilityHandler> VISIBILITY_HANDLER_COMPARATOR;
    private static final Comparator<Recipe> RECIPE_COMPARATOR = (o1, o2) -> {
        int int_1 = o1.getId().getNamespace().compareTo(o2.getId().getNamespace());
        if (int_1 == 0)
            int_1 = o1.getId().getPath().compareTo(o2.getId().getPath());
        return int_1;
    };
    
    static {
        Comparator<DisplayVisibilityHandler> comparator = Comparator.comparingDouble(DisplayVisibilityHandler::getPriority);
        VISIBILITY_HANDLER_COMPARATOR = comparator.reversed();
    }
    
    private final AtomicInteger recipeCount = new AtomicInteger();
    private final Map<Identifier, List<RecipeDisplay>> recipeCategoryListMap = Maps.newHashMap();
    private final Map<Identifier, DisplaySettings> categoryDisplaySettingsMap = Maps.newHashMap();
    private final List<RecipeCategory> categories = Lists.newArrayList();
    private final Map<Identifier, ButtonAreaSupplier> speedCraftAreaSupplierMap = Maps.newHashMap();
    private final Map<Identifier, List<SpeedCraftFunctional>> speedCraftFunctionalMap = Maps.newHashMap();
    private final List<DisplayVisibilityHandler> displayVisibilityHandlers = Lists.newArrayList();
    private RecipeManager recipeManager;
    
    @Override
    public List<ItemStack> findCraftableByItems(List<ItemStack> inventoryItems) {
        List<ItemStack> craftables = new ArrayList<>();
        for(List<RecipeDisplay> value : recipeCategoryListMap.values())
            for(RecipeDisplay recipeDisplay : value) {
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
    public void registerCategory(RecipeCategory category) {
        categories.add(category);
        categoryDisplaySettingsMap.put(category.getIdentifier(), category.getDisplaySettings());
        recipeCategoryListMap.put(category.getIdentifier(), Lists.newLinkedList());
    }
    
    @Override
    public void registerDisplay(Identifier categoryIdentifier, RecipeDisplay display) {
        if (!recipeCategoryListMap.containsKey(categoryIdentifier))
            return;
        recipeCount.incrementAndGet();
        recipeCategoryListMap.get(categoryIdentifier).add(display);
    }
    
    @Override
    public Map<RecipeCategory, List<RecipeDisplay>> getRecipesFor(ItemStack stack) {
        Map<Identifier, List<RecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getIdentifier(), Lists.newArrayList()));
        for(Map.Entry<Identifier, List<RecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            RecipeCategory category = getCategory(entry.getKey());
            for(RecipeDisplay recipeDisplay : entry.getValue())
                for(ItemStack outputStack : (List<ItemStack>) recipeDisplay.getOutput())
                    if (category.checkTags() ? ItemStack.areEqual(stack, outputStack) : ItemStack.areEqualIgnoreTags(stack, outputStack))
                        categoriesMap.get(recipeDisplay.getRecipeCategory()).add(recipeDisplay);
        }
        Map<RecipeCategory, List<RecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
        categories.forEach(category -> {
            if (categoriesMap.containsKey(category.getIdentifier()) && !categoriesMap.get(category.getIdentifier()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getIdentifier()).stream().filter(display -> isDisplayVisible(display, true)).collect(Collectors.toList()));
        });
        for(RecipeCategory category : Lists.newArrayList(recipeCategoryListMap.keySet()))
            if (recipeCategoryListMap.get(category).isEmpty())
                recipeCategoryListMap.remove(category);
        return recipeCategoryListMap;
    }
    
    private RecipeCategory getCategory(Identifier identifier) {
        return categories.stream().filter(category -> category.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
    
    @Override
    public Map<RecipeCategory, List<RecipeDisplay>> getUsagesFor(ItemStack stack) {
        Map<Identifier, List<RecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getIdentifier(), Lists.newArrayList()));
        for(Map.Entry<Identifier, List<RecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            RecipeCategory category = getCategory(entry.getKey());
            for(RecipeDisplay recipeDisplay : entry.getValue()) {
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
        Map<RecipeCategory, List<RecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
        categories.forEach(category -> {
            if (categoriesMap.containsKey(category.getIdentifier()) && !categoriesMap.get(category.getIdentifier()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getIdentifier()).stream().filter(display -> isDisplayVisible(display, true)).collect(Collectors.toList()));
        });
        for(RecipeCategory category : Lists.newArrayList(recipeCategoryListMap.keySet()))
            if (recipeCategoryListMap.get(category).isEmpty())
                recipeCategoryListMap.remove(category);
        return recipeCategoryListMap;
    }
    
    @Override
    public List<RecipeCategory> getAllCategories() {
        return new LinkedList<>(categories);
    }
    
    @Override
    public Optional<ButtonAreaSupplier> getSpeedCraftButtonArea(RecipeCategory category) {
        if (!speedCraftAreaSupplierMap.containsKey(category.getIdentifier()))
            return Optional.empty();
        return Optional.ofNullable(speedCraftAreaSupplierMap.get(category.getIdentifier()));
    }
    
    @Override
    public void registerSpeedCraftButtonArea(Identifier category, ButtonAreaSupplier rectangle) {
        speedCraftAreaSupplierMap.put(category, rectangle);
    }
    
    @Override
    public void registerDefaultSpeedCraftButtonArea(Identifier category) {
        registerSpeedCraftButtonArea(category, bounds -> new Rectangle((int) bounds.getMaxX() - 16, (int) bounds.getMaxY() - 16, 10, 10));
    }
    
    @Override
    public List<SpeedCraftFunctional> getSpeedCraftFunctional(RecipeCategory category) {
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
        this.categoryDisplaySettingsMap.clear();
        this.displayVisibilityHandlers.clear();
        ((DisplayHelperImpl) RoughlyEnoughItemsCore.getDisplayHelper()).resetCache();
        BaseBoundsHandler baseBoundsHandler = new BaseBoundsHandlerImpl();
        RoughlyEnoughItemsCore.getDisplayHelper().registerBoundsHandler(baseBoundsHandler);
        ((DisplayHelperImpl) RoughlyEnoughItemsCore.getDisplayHelper()).setBaseBoundsHandler(baseBoundsHandler);
        long startTime = System.currentTimeMillis();
        List<REIPluginEntry> plugins = new LinkedList<>(RoughlyEnoughItemsCore.getPlugins());
        plugins.sort((first, second) -> {
            return second.getPriority() - first.getPriority();
        });
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Loading %d plugins: %s", plugins.size(), plugins.stream().map(REIPluginEntry::getPluginIdentifier).map(Identifier::toString).collect(Collectors.joining(", ")));
        Collections.reverse(plugins);
        RoughlyEnoughItemsCore.getItemRegisterer().getModifiableItemList().clear();
        PluginDisabler pluginDisabler = RoughlyEnoughItemsCore.getPluginDisabler();
        plugins.forEach(plugin -> {
            Identifier identifier = plugin.getPluginIdentifier();
            try {
                if (pluginDisabler.isFunctionEnabled(identifier, PluginFunction.REGISTER_ITEMS))
                    plugin.registerItems(RoughlyEnoughItemsCore.getItemRegisterer());
                if (pluginDisabler.isFunctionEnabled(identifier, PluginFunction.REGISTER_CATEGORIES))
                    plugin.registerPluginCategories(this);
                if (pluginDisabler.isFunctionEnabled(identifier, PluginFunction.REGISTER_RECIPE_DISPLAYS))
                    plugin.registerRecipeDisplays(this);
                if (pluginDisabler.isFunctionEnabled(identifier, PluginFunction.REGISTER_BOUNDS))
                    plugin.registerBounds(RoughlyEnoughItemsCore.getDisplayHelper());
                if (pluginDisabler.isFunctionEnabled(identifier, PluginFunction.REGISTER_OTHERS))
                    plugin.registerOthers(this);
            } catch (Exception e) {
                RoughlyEnoughItemsCore.LOGGER.error("[REI] " + identifier.toString() + " plugin failed to load!", e);
            }
        });
        if (getDisplayVisibilityHandlers().isEmpty())
            registerRecipeVisibilityHandler(new DisplayVisibilityHandler() {
                @Override
                public DisplayVisibility handleDisplay(RecipeCategory category, RecipeDisplay display) {
                    return DisplayVisibility.ALWAYS_VISIBLE;
                }
                
                @Override
                public float getPriority() {
                    return -1f;
                }
            });
        long usedTime = System.currentTimeMillis() - startTime;
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Registered %d recipes displays, %d bounds handler, %d visibility " + "handlers and %d categories (%s) in %d ms.", recipeCount.get(), RoughlyEnoughItemsCore.getDisplayHelper().getAllBoundsHandlers().size(), getDisplayVisibilityHandlers().size(), categories.size(), String.join(", ", categories.stream().map(RecipeCategory::getCategoryName).collect(Collectors.toList())), usedTime);
    }
    
    @Override
    public int getRecipeCount() {
        return recipeCount.get();
    }
    
    @Override
    public List<Recipe> getAllSortedRecipes() {
        return getRecipeManager().values().stream().sorted(RECIPE_COMPARATOR).collect(Collectors.toList());
    }
    
    @Override
    public Map<RecipeCategory, List<RecipeDisplay>> getAllRecipes() {
        Map<RecipeCategory, List<RecipeDisplay>> map = Maps.newLinkedHashMap();
        categories.forEach(recipeCategory -> {
            if (recipeCategoryListMap.containsKey(recipeCategory.getIdentifier())) {
                List<RecipeDisplay> list = recipeCategoryListMap.get(recipeCategory.getIdentifier()).stream().filter(display -> isDisplayVisible(display, true)).collect(Collectors.toList());
                if (!list.isEmpty())
                    map.put(recipeCategory, list);
            }
        });
        return map;
    }
    
    @Override
    public void registerRecipeVisibilityHandler(DisplayVisibilityHandler visibilityHandler) {
        displayVisibilityHandlers.add(visibilityHandler);
    }
    
    @Override
    public void unregisterRecipeVisibilityHandler(DisplayVisibilityHandler visibilityHandler) {
        displayVisibilityHandlers.remove(visibilityHandler);
    }
    
    @Override
    public List<DisplayVisibilityHandler> getDisplayVisibilityHandlers() {
        return Collections.unmodifiableList(displayVisibilityHandlers);
    }
    
    @Override
    public boolean isDisplayVisible(RecipeDisplay display, boolean respectConfig) {
        RecipeCategory category = getCategory(display.getRecipeCategory());
        List<DisplayVisibilityHandler> list = getDisplayVisibilityHandlers().stream().sorted(VISIBILITY_HANDLER_COMPARATOR).collect(Collectors.toList());
        for(DisplayVisibilityHandler displayVisibilityHandler : list) {
            DisplayVisibility visibility = displayVisibilityHandler.handleDisplay(category, display);
            if (visibility != DisplayVisibility.PASS) {
                if (visibility == DisplayVisibility.CONFIG_OPTIONAL)
                    return RoughlyEnoughItemsCore.getConfigManager().getConfig().preferVisibleRecipes || !respectConfig;
                return visibility == DisplayVisibility.ALWAYS_VISIBLE;
            }
        }
        return true;
    }
    
    @Override
    public Optional<DisplaySettings> getCachedCategorySettings(Identifier category) {
        return categoryDisplaySettingsMap.entrySet().stream().filter(entry -> entry.getKey().equals(category)).map(Map.Entry::getValue).findAny();
    }
    
}
