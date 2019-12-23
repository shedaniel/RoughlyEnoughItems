/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.annotations.Internal;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.utils.CollectionUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Deprecated
@Internal
public class RecipeHelperImpl implements RecipeHelper {
    
    private static final Comparator<DisplayVisibilityHandler> VISIBILITY_HANDLER_COMPARATOR;
    @SuppressWarnings("rawtypes") private static final Comparator<Recipe> RECIPE_COMPARATOR = (o1, o2) -> {
        int int_1 = o1.getId().getNamespace().compareTo(o2.getId().getNamespace());
        if (int_1 == 0)
            int_1 = o1.getId().getPath().compareTo(o2.getId().getPath());
        return int_1;
    };
    
    static {
        Comparator<DisplayVisibilityHandler> comparator = Comparator.comparingDouble(DisplayVisibilityHandler::getPriority);
        VISIBILITY_HANDLER_COMPARATOR = comparator.reversed();
    }
    
    private final List<AutoTransferHandler> autoTransferHandlers = Lists.newArrayList();
    private final List<RecipeFunction> recipeFunctions = Lists.newArrayList();
    private final List<ScreenClickArea> screenClickAreas = Lists.newArrayList();
    private final AtomicInteger recipeCount = new AtomicInteger();
    private final Map<Identifier, List<RecipeDisplay>> recipeCategoryListMap = Maps.newHashMap();
    private final List<RecipeCategory<?>> categories = Lists.newArrayList();
    private final Map<Identifier, ButtonAreaSupplier> autoCraftAreaSupplierMap = Maps.newHashMap();
    private final Map<Identifier, List<List<EntryStack>>> categoryWorkingStations = Maps.newHashMap();
    private final List<DisplayVisibilityHandler> displayVisibilityHandlers = Lists.newArrayList();
    private final List<LiveRecipeGenerator<RecipeDisplay>> liveRecipeGenerators = Lists.newArrayList();
    private RecipeManager recipeManager;
    private boolean arePluginsLoading = false;
    
    @Override
    public List<EntryStack> findCraftableEntriesByItems(List<EntryStack> inventoryItems) {
        List<EntryStack> craftables = new ArrayList<>();
        for (List<RecipeDisplay> value : recipeCategoryListMap.values())
            for (RecipeDisplay recipeDisplay : value) {
                int slotsCraftable = 0;
                List<List<EntryStack>> requiredInput = (List<List<EntryStack>>) recipeDisplay.getRequiredEntries();
                for (List<EntryStack> slot : requiredInput) {
                    if (slot.isEmpty()) {
                        slotsCraftable++;
                        continue;
                    }
                    boolean slotDone = false;
                    for (EntryStack possibleType : inventoryItems) {
                        for (EntryStack slotPossible : slot)
                            if (possibleType.equals(slotPossible)) {
                                slotsCraftable++;
                                slotDone = true;
                                break;
                            }
                        if (slotDone)
                            break;
                    }
                }
                if (slotsCraftable == recipeDisplay.getRequiredEntries().size())
                    craftables.addAll((List<EntryStack>) recipeDisplay.getOutputEntries());
            }
        return craftables.stream().distinct().collect(Collectors.toList());
    }
    
    @Override
    public boolean arePluginsLoading() {
        return arePluginsLoading;
    }
    
    @Override
    public void registerCategory(RecipeCategory<?> category) {
        categories.add(category);
        recipeCategoryListMap.put(category.getIdentifier(), Lists.newLinkedList());
        categoryWorkingStations.put(category.getIdentifier(), Lists.newLinkedList());
    }
    
    @Override
    public void registerWorkingStations(Identifier category, List<EntryStack>... workingStations) {
        categoryWorkingStations.get(category).addAll(Arrays.asList(workingStations));
    }
    
    @Override
    public void registerWorkingStations(Identifier category, EntryStack... workingStations) {
        categoryWorkingStations.get(category).addAll(Arrays.asList(workingStations).stream().map(Collections::singletonList).collect(Collectors.toList()));
    }
    
    @Override
    public List<List<EntryStack>> getWorkingStations(Identifier category) {
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
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getRecipesFor(EntryStack stack) {
        Map<Identifier, List<RecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getIdentifier(), Lists.newArrayList()));
        for (Map.Entry<Identifier, List<RecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            RecipeCategory<?> category = getCategory(entry.getKey());
            for (RecipeDisplay recipeDisplay : entry.getValue())
                for (EntryStack outputStack : recipeDisplay.getOutputEntries())
                    if (stack.equals(outputStack))
                        categoriesMap.get(recipeDisplay.getRecipeCategory()).add(recipeDisplay);
        }
        for (LiveRecipeGenerator<RecipeDisplay> liveRecipeGenerator : liveRecipeGenerators) {
            liveRecipeGenerator.getRecipeFor(stack).ifPresent(o -> categoriesMap.get(liveRecipeGenerator.getCategoryIdentifier()).addAll(o));
        }
        Map<RecipeCategory<?>, List<RecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
        categories.forEach(category -> {
            if (categoriesMap.containsKey(category.getIdentifier()) && !categoriesMap.get(category.getIdentifier()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getIdentifier()).stream().filter(display -> isDisplayVisible(display)).collect(Collectors.toList()));
        });
        for (RecipeCategory<?> category : Lists.newArrayList(recipeCategoryListMap.keySet()))
            if (recipeCategoryListMap.get(category).isEmpty())
                recipeCategoryListMap.remove(category);
        return recipeCategoryListMap;
    }
    
    @Override
    public RecipeCategory<?> getCategory(Identifier identifier) {
        return CollectionUtils.findFirstOrNull(categories, category -> category.getIdentifier().equals(identifier));
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getUsagesFor(EntryStack stack) {
        Map<Identifier, List<RecipeDisplay>> categoriesMap = new HashMap<>();
        categories.forEach(f -> categoriesMap.put(f.getIdentifier(), Lists.newArrayList()));
        for (Map.Entry<Identifier, List<RecipeDisplay>> entry : recipeCategoryListMap.entrySet()) {
            boolean isWorkstationCategory = getWorkingStations(entry.getKey()).stream().anyMatch(ws -> ws.contains(stack));
            for (RecipeDisplay recipeDisplay : entry.getValue()) {
                if (isWorkstationCategory) {
                    categoriesMap.get(recipeDisplay.getRecipeCategory()).add(recipeDisplay);
                } else {
                    boolean found = false;
                    for (List<EntryStack> input : recipeDisplay.getInputEntries()) {
                        for (EntryStack otherEntry : input) {
                            if (otherEntry.equals(stack)) {
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
        }
        for (LiveRecipeGenerator<RecipeDisplay> liveRecipeGenerator : liveRecipeGenerators) {
            liveRecipeGenerator.getUsageFor(stack).ifPresent(o -> categoriesMap.get(liveRecipeGenerator.getCategoryIdentifier()).addAll(o));
        }
        Map<RecipeCategory<?>, List<RecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
        for (RecipeCategory<?> category : categories) {
            if (categoriesMap.containsKey(category.getIdentifier()) && !categoriesMap.get(category.getIdentifier()).isEmpty())
                recipeCategoryListMap.put(category, categoriesMap.get(category.getIdentifier()).stream().filter(display -> isDisplayVisible(display)).collect(Collectors.toList()));
        }
        for (RecipeCategory<?> category : Lists.newArrayList(recipeCategoryListMap.keySet()))
            if (recipeCategoryListMap.get(category).isEmpty())
                recipeCategoryListMap.remove(category);
        return recipeCategoryListMap;
    }
    
    @Override
    public List<RecipeCategory<?>> getAllCategories() {
        return Collections.unmodifiableList(categories);
    }
    
    @Override
    public Optional<ButtonAreaSupplier> getAutoCraftButtonArea(RecipeCategory<?> category) {
        if (!autoCraftAreaSupplierMap.containsKey(category.getIdentifier()))
            return Optional.ofNullable(bounds -> new Rectangle(bounds.getMaxX() - 16, bounds.getMaxY() - 16, 10, 10));
        return Optional.ofNullable(autoCraftAreaSupplierMap.get(category.getIdentifier()));
    }
    
    @Override
    public void registerAutoCraftButtonArea(Identifier category, ButtonAreaSupplier rectangle) {
        if (rectangle == null) {
            if (autoCraftAreaSupplierMap.containsKey(category))
                autoCraftAreaSupplierMap.remove(category);
        } else
            autoCraftAreaSupplierMap.put(category, rectangle);
    }
    
    @SuppressWarnings("deprecation")
    public void recipesLoaded(RecipeManager recipeManager) {
        long startTime = System.currentTimeMillis();
        arePluginsLoading = true;
        ScreenHelper.clearData();
        this.recipeCount.set(0);
        this.recipeManager = recipeManager;
        this.recipeCategoryListMap.clear();
        this.categories.clear();
        this.autoCraftAreaSupplierMap.clear();
        this.screenClickAreas.clear();
        this.categoryWorkingStations.clear();
        this.recipeFunctions.clear();
        this.displayVisibilityHandlers.clear();
        this.liveRecipeGenerators.clear();
        this.autoTransferHandlers.clear();
        ((DisplayHelperImpl) DisplayHelper.getInstance()).resetData();
        ((DisplayHelperImpl) DisplayHelper.getInstance()).resetCache();
        BaseBoundsHandler baseBoundsHandler = new BaseBoundsHandlerImpl();
        DisplayHelper.getInstance().registerBoundsHandler(baseBoundsHandler);
        ((DisplayHelperImpl) DisplayHelper.getInstance()).setBaseBoundsHandler(baseBoundsHandler);
        List<REIPluginEntry> plugins = Lists.newLinkedList(RoughlyEnoughItemsCore.getPlugins());
        plugins.sort(Comparator.comparingInt(REIPluginEntry::getPriority).reversed());
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Loading %d plugins: %s", plugins.size(), plugins.stream().map(REIPluginEntry::getPluginIdentifier).map(Identifier::toString).collect(Collectors.joining(", ")));
        Collections.reverse(plugins);
        EntryRegistry.getInstance().getStacksList().clear();
        Version reiVersion = FabricLoader.getInstance().getModContainer("roughlyenoughitems").get().getMetadata().getVersion();
        if (!(reiVersion instanceof SemanticVersion))
            RoughlyEnoughItemsCore.LOGGER.warn("[REI] Roughly Enough Items is not using semantic versioning, will be ignoring plugins' minimum versions!");
        List<REIPluginV0> reiPluginV0s = new ArrayList<>();
        for (REIPluginEntry plugin : plugins) {
            try {
                if (reiVersion instanceof SemanticVersion)
                    if (plugin.getMinimumVersion().compareTo((SemanticVersion) reiVersion) > 0) {
                        throw new IllegalStateException("Requires " + plugin.getMinimumVersion().getFriendlyString() + " version of REI!");
                    }
                if (plugin instanceof REIPluginV0) {
                    ((REIPluginV0) plugin).preRegister();
                    reiPluginV0s.add((REIPluginV0) plugin);
                }
            } catch (Throwable e) {
                RoughlyEnoughItemsCore.LOGGER.error("[REI] " + plugin.getPluginIdentifier().toString() + " plugin failed to pre register!", e);
            }
        }
        for (REIPluginV0 plugin : reiPluginV0s) {
            Identifier identifier = plugin.getPluginIdentifier();
            try {
                plugin.registerBounds(DisplayHelper.getInstance());
                plugin.registerEntries(EntryRegistry.getInstance());
                plugin.registerPluginCategories(this);
                plugin.registerRecipeDisplays(this);
                plugin.registerOthers(this);
            } catch (Throwable e) {
                RoughlyEnoughItemsCore.LOGGER.error("[REI] " + identifier.toString() + " plugin failed to load!", e);
            }
        }
        for (REIPluginV0 plugin : reiPluginV0s) {
            Identifier identifier = plugin.getPluginIdentifier();
            try {
                plugin.postRegister();
            } catch (Throwable e) {
                RoughlyEnoughItemsCore.LOGGER.error("[REI] " + identifier.toString() + " plugin failed to post register!", e);
            }
        }
        if (!recipeFunctions.isEmpty()) {
            @SuppressWarnings("rawtypes") List<Recipe> allSortedRecipes = getAllSortedRecipes();
            Collections.reverse(allSortedRecipes);
            for (RecipeFunction recipeFunction : recipeFunctions) {
                try {
                    for (Recipe<?> recipe : CollectionUtils.filter(allSortedRecipes, recipe -> recipeFunction.recipeFilter.test(recipe))) {
                        registerDisplay(recipeFunction.category, (RecipeDisplay) recipeFunction.mappingFunction.apply(recipe), 0);
                    }
                } catch (Exception e) {
                    RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to add recipes!", e);
                }
            }
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
        ((DisplayHelperImpl) DisplayHelper.getInstance()).resetCache();
        ScreenHelper.getOptionalOverlay().ifPresent(overlay -> overlay.shouldReInit = true);
        
        long usedTime = System.currentTimeMillis() - startTime;
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Registered %d stack entries, %d recipes displays, %d exclusion zones suppliers, %d bounds handler, %d visibility handlers and %d categories (%s) in %d ms.", EntryRegistry.getInstance().getStacksList().size(), recipeCount.get(), DisplayHelper.getInstance().getBaseBoundsHandler().supplierSize(), DisplayHelper.getInstance().getAllBoundsHandlers().size(), getDisplayVisibilityHandlers().size(), categories.size(), String.join(", ", categories.stream().map(RecipeCategory::getCategoryName).collect(Collectors.toList())), usedTime);
        arePluginsLoading = false;
    }
    
    @Override
    public AutoTransferHandler registerAutoCraftingHandler(AutoTransferHandler handler) {
        autoTransferHandlers.add(handler);
        return handler;
    }
    
    @Override
    public List<AutoTransferHandler> getSortedAutoCraftingHandler() {
        return autoTransferHandlers.stream().sorted(Comparator.comparingDouble(AutoTransferHandler::getPriority).reversed()).collect(Collectors.toList());
    }
    
    @Override
    public int getRecipeCount() {
        return recipeCount.get();
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public List<Recipe> getAllSortedRecipes() {
        return getRecipeManager().values().stream().sorted(RECIPE_COMPARATOR).collect(Collectors.toList());
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getAllRecipes() {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = Maps.newLinkedHashMap();
        for (RecipeCategory<?> recipeCategory : categories) {
            if (recipeCategoryListMap.containsKey(recipeCategory.getIdentifier())) {
                List<RecipeDisplay> list = CollectionUtils.filter(recipeCategoryListMap.get(recipeCategory.getIdentifier()), this::isDisplayVisible);
                if (!list.isEmpty())
                    map.put(recipeCategory, list);
            }
        }
        return map;
    }
    
    @Override
    public List<RecipeDisplay> getAllRecipesFromCategory(RecipeCategory<?> category) {
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
        RecipeCategory<?> category = getCategory(display.getRecipeCategory());
        List<DisplayVisibilityHandler> list = getDisplayVisibilityHandlers().stream().sorted(VISIBILITY_HANDLER_COMPARATOR).collect(Collectors.toList());
        for (DisplayVisibilityHandler displayVisibilityHandler : list) {
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
    public void registerScreenClickArea(Rectangle rectangle, Class<? extends AbstractContainerScreen<?>> screenClass, Identifier... categories) {
        this.screenClickAreas.add(new ScreenClickAreaImpl(screenClass, rectangle, categories));
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(Identifier category, Class<T> recipeClass, Function<T, RecipeDisplay> mappingFunction) {
        recipeFunctions.add(new RecipeFunction(category, recipe -> recipeClass.isAssignableFrom(recipe.getClass()), mappingFunction));
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(Identifier category, @SuppressWarnings("rawtypes")
            Function<Recipe, Boolean> recipeFilter, Function<T, RecipeDisplay> mappingFunction) {
        recipeFunctions.add(new RecipeFunction(category, recipeFilter::apply, mappingFunction));
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(Identifier category,
            @SuppressWarnings("rawtypes") Predicate<Recipe> recipeFilter, Function<T, RecipeDisplay> mappingFunction) {
        recipeFunctions.add(new RecipeFunction(category, recipeFilter, mappingFunction));
    }
    
    @Override
    public void registerLiveRecipeGenerator(LiveRecipeGenerator<?> liveRecipeGenerator) {
        liveRecipeGenerators.add((LiveRecipeGenerator<RecipeDisplay>) liveRecipeGenerator);
    }
    
    @Override
    public List<ScreenClickArea> getScreenClickAreas() {
        return screenClickAreas;
    }
    
    private class ScreenClickAreaImpl implements ScreenClickArea {
        Class<? extends AbstractContainerScreen<?>> screenClass;
        Rectangle rectangle;
        Identifier[] categories;
        
        private ScreenClickAreaImpl(Class<? extends AbstractContainerScreen<?>> screenClass, Rectangle rectangle, Identifier[] categories) {
            this.screenClass = screenClass;
            this.rectangle = rectangle;
            this.categories = categories;
        }
        
        public Class<? extends AbstractContainerScreen<?>> getScreenClass() {
            return screenClass;
        }
        
        public Rectangle getRectangle() {
            return rectangle;
        }
        
        public Identifier[] getCategories() {
            return categories;
        }
    }
    
    @SuppressWarnings("rawtypes")
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
