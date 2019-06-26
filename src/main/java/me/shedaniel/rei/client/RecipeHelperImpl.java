/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
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
    
    private final List<AutoCraftingHandler> autoCraftingHandlers = Lists.newArrayList();
    private final List<RecipeFunction> recipeFunctions = Lists.newArrayList();
    private final List<ScreenClickArea> screenClickAreas = Lists.newArrayList();
    private final AtomicInteger recipeCount = new AtomicInteger();
    private final Map<Identifier, List<RecipeDisplay>> recipeCategoryListMap = Maps.newHashMap();
    private final List<RecipeCategory> categories = Lists.newArrayList();
    private final Map<Identifier, ButtonAreaSupplier> speedCraftAreaSupplierMap = Maps.newHashMap();
    private final Map<Identifier, List<List<ItemStack>>> categoryWorkingStations = Maps.newHashMap();
    private final List<DisplayVisibilityHandler> displayVisibilityHandlers = Lists.newArrayList();
    private final List<LiveRecipeGenerator<?>> liveRecipeGenerators = Lists.newArrayList();
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
                            if (ItemStack.areItemsEqualIgnoreDamage(slotPossible, possibleType)) {
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
        recipeCategoryListMap.put(category.getIdentifier(), Lists.newLinkedList());
        categoryWorkingStations.put(category.getIdentifier(), Lists.newLinkedList());
    }
    
    @Override
    public void registerWorkingStations(Identifier category, List<ItemStack>... workingStations) {
        categoryWorkingStations.get(category).addAll(Arrays.asList(workingStations));
    }
    
    @Override
    public void registerWorkingStations(Identifier category, ItemStack... workingStations) {
        categoryWorkingStations.get(category).addAll(Arrays.asList(workingStations).stream().map(Collections::singletonList).collect(Collectors.toList()));
    }
    
    @Override
    public List<List<ItemStack>> getWorkingStations(Identifier category) {
        return categoryWorkingStations.get(category);
    }
    
    @Override
    public void registerDisplay(Identifier categoryIdentifier, RecipeDisplay display) {
        if (!recipeCategoryListMap.containsKey(categoryIdentifier))
            return;
        recipeCount.incrementAndGet();
        recipeCategoryListMap.get(categoryIdentifier).add(display);
    }
    
    private void registerDisplay(Identifier categoryIdentifier, RecipeDisplay display, int index) {
        if (!recipeCategoryListMap.containsKey(categoryIdentifier))
            return;
        recipeCount.incrementAndGet();
        recipeCategoryListMap.get(categoryIdentifier).add(index, display);
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getRecipesFor(ItemStack stack) {
        Map<Identifier, List<RecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getIdentifier(), Lists.newArrayList()));
        for(Map.Entry<Identifier, List<RecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            RecipeCategory category = getCategory(entry.getKey());
            for(RecipeDisplay recipeDisplay : entry.getValue())
                for(ItemStack outputStack : (List<ItemStack>) recipeDisplay.getOutput())
                    if (category.checkTags() ? ItemStack.areEqualIgnoreDamage(stack, outputStack) : ItemStack.areItemsEqualIgnoreDamage(stack, outputStack))
                        categoriesMap.get(recipeDisplay.getRecipeCategory()).add(recipeDisplay);
        }
        for(LiveRecipeGenerator liveRecipeGenerator : liveRecipeGenerators)
            ((Optional<List>) liveRecipeGenerator.getRecipeFor(stack)).ifPresent(o -> categoriesMap.get(liveRecipeGenerator.getCategoryIdentifier()).addAll(o));
        Map<RecipeCategory<?>, List<RecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
        categories.forEach(category -> {
            if (categoriesMap.containsKey(category.getIdentifier()) && !categoriesMap.get(category.getIdentifier()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getIdentifier()).stream().filter(display -> isDisplayVisible(display)).collect(Collectors.toList()));
        });
        for(RecipeCategory<?> category : Lists.newArrayList(recipeCategoryListMap.keySet()))
            if (recipeCategoryListMap.get(category).isEmpty())
                recipeCategoryListMap.remove(category);
        return recipeCategoryListMap;
    }
    
    @Override
    public RecipeCategory getCategory(Identifier identifier) {
        return categories.stream().filter(category -> category.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getUsagesFor(ItemStack stack) {
        Map<Identifier, List<RecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getIdentifier(), Lists.newArrayList()));
        for(Map.Entry<Identifier, List<RecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            RecipeCategory category = getCategory(entry.getKey());
            for(RecipeDisplay recipeDisplay : entry.getValue()) {
                boolean found = false;
                for(List<ItemStack> input : (List<List<ItemStack>>) recipeDisplay.getInput()) {
                    for(ItemStack itemStack : input) {
                        if (category.checkTags() ? ItemStack.areEqualIgnoreDamage(itemStack, stack) : ItemStack.areItemsEqualIgnoreDamage(itemStack, stack)) {
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
        for(LiveRecipeGenerator liveRecipeGenerator : liveRecipeGenerators)
            ((Optional<List>) liveRecipeGenerator.getUsageFor(stack)).ifPresent(o -> categoriesMap.get(liveRecipeGenerator.getCategoryIdentifier()).addAll(o));
        Map<RecipeCategory<?>, List<RecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
        categories.forEach(category -> {
            if (categoriesMap.containsKey(category.getIdentifier()) && !categoriesMap.get(category.getIdentifier()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getIdentifier()).stream().filter(display -> isDisplayVisible(display)).collect(Collectors.toList()));
        });
        for(RecipeCategory<?> category : Lists.newArrayList(recipeCategoryListMap.keySet()))
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
            return Optional.ofNullable(bounds -> new Rectangle((int) bounds.getMaxX() - 16, (int) bounds.getMaxY() - 16, 10, 10));
        return Optional.ofNullable(speedCraftAreaSupplierMap.get(category.getIdentifier()));
    }
    
    @Override
    public void registerSpeedCraftButtonArea(Identifier category, ButtonAreaSupplier rectangle) {
        if (rectangle == null) {
            if (speedCraftAreaSupplierMap.containsKey(category))
                speedCraftAreaSupplierMap.remove(category);
        } else
            speedCraftAreaSupplierMap.put(category, rectangle);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void registerDefaultSpeedCraftButtonArea(Identifier category) {
        registerSpeedCraftButtonArea(category, bounds -> new Rectangle((int) bounds.getMaxX() - 16, (int) bounds.getMaxY() - 16, 10, 10));
    }
    
    @SuppressWarnings("deprecation")
    public void recipesLoaded(RecipeManager recipeManager) {
        this.recipeCount.set(0);
        this.recipeManager = recipeManager;
        this.recipeCategoryListMap.clear();
        this.categories.clear();
        this.speedCraftAreaSupplierMap.clear();
        this.screenClickAreas.clear();
        this.categoryWorkingStations.clear();
        this.recipeFunctions.clear();
        this.displayVisibilityHandlers.clear();
        this.liveRecipeGenerators.clear();
        this.autoCraftingHandlers.clear();
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
        if (!recipeFunctions.isEmpty()) {
            List<Recipe> allSortedRecipes = getAllSortedRecipes();
            Collections.reverse(allSortedRecipes);
            recipeFunctions.forEach(recipeFunction -> {
                try {
                    allSortedRecipes.stream().filter(recipe -> recipeFunction.recipeFilter.test(recipe)).forEach(t -> registerDisplay(recipeFunction.category, (RecipeDisplay) recipeFunction.mappingFunction.apply(t), 0));
                } catch (Exception e) {
                    RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to add recipes!", e);
                }
            });
        }
        if (getDisplayVisibilityHandlers().isEmpty())
            registerRecipeVisibilityHandler(new DisplayVisibilityHandler() {
                @Override
                public ActionResult handleDisplay(RecipeCategory<?> category, RecipeDisplay display) {
                    return ActionResult.SUCCESS;
                }
                
                @Override
                public float getPriority() {
                    return -1f;
                }
            });
        // Clear Cache
        ((DisplayHelperImpl) RoughlyEnoughItemsCore.getDisplayHelper()).resetCache();
        ScreenHelper.getOptionalOverlay().ifPresent(overlay -> overlay.shouldReInit = true);
        
        long usedTime = System.currentTimeMillis() - startTime;
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Registered %d stack entries, %d recipes displays, %d bounds handler, %d visibility handlers and %d categories (%s) in %d ms.", RoughlyEnoughItemsCore.getItemRegisterer().getItemList().size(), recipeCount.get(), RoughlyEnoughItemsCore.getDisplayHelper().getAllBoundsHandlers().size(), getDisplayVisibilityHandlers().size(), categories.size(), String.join(", ", categories.stream().map(RecipeCategory::getCategoryName).collect(Collectors.toList())), usedTime);
    }
    
    @Override
    public AutoCraftingHandler registerAutoCraftingHandler(AutoCraftingHandler handler) {
        autoCraftingHandlers.add(handler);
        return handler;
    }
    
    @Override
    public List<AutoCraftingHandler> getSortedAutoCraftingHandler() {
        return autoCraftingHandlers.stream().sorted(Comparator.comparingDouble(AutoCraftingHandler::getPriority).reversed()).collect(Collectors.toList());
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
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getAllRecipes() {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = Maps.newLinkedHashMap();
        categories.forEach(recipeCategory -> {
            if (recipeCategoryListMap.containsKey(recipeCategory.getIdentifier())) {
                List<RecipeDisplay> list = recipeCategoryListMap.get(recipeCategory.getIdentifier()).stream().filter(display -> isDisplayVisible(display)).collect(Collectors.toList());
                if (!list.isEmpty())
                    map.put(recipeCategory, list);
            }
        });
        return map;
    }
    
    @Override
    public List<RecipeDisplay> getAllRecipesFromCategory(RecipeCategory category) {
        return recipeCategoryListMap.get(category.getIdentifier());
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
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean isDisplayVisible(RecipeDisplay display, boolean respectConfig) {
        return isDisplayVisible(display);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean isDisplayVisible(RecipeDisplay display) {
        RecipeCategory category = getCategory(display.getRecipeCategory());
        List<DisplayVisibilityHandler> list = getDisplayVisibilityHandlers().stream().sorted(VISIBILITY_HANDLER_COMPARATOR).collect(Collectors.toList());
        for(DisplayVisibilityHandler displayVisibilityHandler : list) {
            try {
                ActionResult visibility = displayVisibilityHandler.handleDisplay(category, display);
                if (visibility != ActionResult.PASS)
                    return visibility == ActionResult.SUCCESS;
            } catch (Throwable throwable) {
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to check if the recipe is visible!", throwable);
            }
        }
        return true;
    }
    
    @Override
    public void registerScreenClickArea(Rectangle rectangle, Class<? extends AbstractContainerScreen> screenClass, Identifier... categories) {
        this.screenClickAreas.add(new ScreenClickArea(screenClass, rectangle, categories));
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(Identifier category, Class<T> recipeClass, Function<T, RecipeDisplay> mappingFunction) {
        recipeFunctions.add(new RecipeFunction(category, recipe -> recipeClass.isAssignableFrom(recipe.getClass()), mappingFunction));
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(Identifier category, Function<Recipe, Boolean> recipeFilter, Function<T, RecipeDisplay> mappingFunction) {
        recipeFunctions.add(new RecipeFunction(category, recipeFilter::apply, mappingFunction));
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(Identifier category, Predicate<Recipe> recipeFilter, Function<T, RecipeDisplay> mappingFunction) {
        recipeFunctions.add(new RecipeFunction(category, recipeFilter, mappingFunction));
    }
    
    @Override
    public void registerLiveRecipeGenerator(LiveRecipeGenerator<?> liveRecipeGenerator) {
        liveRecipeGenerators.add(liveRecipeGenerator);
    }
    
    @Override
    public List<ScreenClickArea> getScreenClickAreas() {
        return screenClickAreas;
    }
    
    public class ScreenClickArea {
        Class<? extends AbstractContainerScreen> screenClass;
        Rectangle rectangle;
        Identifier[] categories;
        
        private ScreenClickArea(Class<? extends AbstractContainerScreen> screenClass, Rectangle rectangle, Identifier[] categories) {
            this.screenClass = screenClass;
            this.rectangle = rectangle;
            this.categories = categories;
        }
        
        public Class<? extends AbstractContainerScreen> getScreenClass() {
            return screenClass;
        }
        
        public Rectangle getRectangle() {
            return rectangle;
        }
        
        public Identifier[] getCategories() {
            return categories;
        }
    }
    
    private class RecipeFunction {
        Identifier category;
        Predicate<Recipe> recipeFilter;
        Function mappingFunction;
        
        public RecipeFunction(Identifier category, Predicate<Recipe> recipeFilter, Function<?, RecipeDisplay> mappingFunction) {
            this.category = category;
            this.recipeFilter = recipeFilter;
            this.mappingFunction = mappingFunction;
        }
    }
    
}
