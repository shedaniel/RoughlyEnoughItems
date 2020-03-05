/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class RecipeHelperImpl implements RecipeHelper {
    
    private static final Comparator<DisplayVisibilityHandler> VISIBILITY_HANDLER_COMPARATOR;
    @SuppressWarnings("rawtypes")
    private static final Comparator<Recipe> RECIPE_COMPARATOR = Comparator.comparing((Recipe o) -> o.getId().getNamespace()).thenComparing(o -> o.getId().getPath());
    
    static {
        Comparator<DisplayVisibilityHandler> comparator = Comparator.comparingDouble(DisplayVisibilityHandler::getPriority);
        VISIBILITY_HANDLER_COMPARATOR = comparator.reversed();
    }
    
    private final List<AutoTransferHandler> autoTransferHandlers = Lists.newLinkedList();
    private final List<RecipeFunction> recipeFunctions = Lists.newLinkedList();
    private final List<ScreenClickArea> screenClickAreas = Lists.newLinkedList();
    private final int[] recipeCount = {0};
    private final Map<Identifier, List<RecipeDisplay>> recipeCategoryListMap = Maps.newLinkedHashMap();
    private final Map<RecipeCategory<?>, Identifier> categories = Maps.newLinkedHashMap();
    private final Map<Identifier, RecipeCategory<?>> reversedCategories = Maps.newHashMap();
    private final Map<Identifier, ButtonAreaSupplier> autoCraftAreaSupplierMap = Maps.newLinkedHashMap();
    private final Map<Identifier, List<List<EntryStack>>> categoryWorkingStations = Maps.newLinkedHashMap();
    private final List<DisplayVisibilityHandler> displayVisibilityHandlers = Lists.newLinkedList();
    private final List<LiveRecipeGenerator<RecipeDisplay>> liveRecipeGenerators = Lists.newLinkedList();
    private RecipeManager recipeManager;
    private boolean arePluginsLoading = false;
    
    @Override
    public List<EntryStack> findCraftableEntriesByItems(List<EntryStack> inventoryItems) {
        List<EntryStack> craftables = new ArrayList<>();
        for (List<RecipeDisplay> value : recipeCategoryListMap.values())
            for (RecipeDisplay recipeDisplay : Lists.newArrayList(value)) {
                int slotsCraftable = 0;
                List<List<EntryStack>> requiredInput = recipeDisplay.getRequiredEntries();
                for (List<EntryStack> slot : requiredInput) {
                    if (slot.isEmpty()) {
                        slotsCraftable++;
                        continue;
                    }
                    back:
                    for (EntryStack possibleType : inventoryItems) {
                        for (EntryStack slotPossible : slot)
                            if (possibleType.equals(slotPossible)) {
                                slotsCraftable++;
                                break back;
                            }
                    }
                }
                if (slotsCraftable == recipeDisplay.getRequiredEntries().size())
                    craftables.addAll(recipeDisplay.getOutputEntries());
            }
        return craftables.stream().distinct().collect(Collectors.toList());
    }
    
    @Override
    public boolean arePluginsLoading() {
        return arePluginsLoading;
    }
    
    @Override
    public void registerCategory(RecipeCategory<?> category) {
        categories.put(category, category.getIdentifier());
        reversedCategories.put(category.getIdentifier(), category);
        recipeCategoryListMap.put(category.getIdentifier(), Lists.newArrayList());
        categoryWorkingStations.put(category.getIdentifier(), Lists.newArrayList());
    }
    
    @SafeVarargs
    @Override
    public final void registerWorkingStations(Identifier category, List<EntryStack>... workingStations) {
        categoryWorkingStations.get(category).addAll(Arrays.asList(workingStations));
    }
    
    @Override
    public void registerWorkingStations(Identifier category, EntryStack... workingStations) {
        categoryWorkingStations.get(category).addAll(Arrays.stream(workingStations).map(Collections::singletonList).collect(Collectors.toList()));
    }
    
    @Override
    public List<List<EntryStack>> getWorkingStations(Identifier category) {
        return categoryWorkingStations.get(category);
    }
    
    @Deprecated
    @Override
    public void registerDisplay(Identifier categoryIdentifier, RecipeDisplay display) {
        if (!recipeCategoryListMap.containsKey(categoryIdentifier))
            return;
        recipeCount[0]++;
        recipeCategoryListMap.get(categoryIdentifier).add(display);
    }
    
    @Override
    public void registerDisplay(RecipeDisplay display) {
        Identifier identifier = Objects.requireNonNull(display.getRecipeCategory());
        if (!recipeCategoryListMap.containsKey(identifier))
            return;
        recipeCount[0]++;
        recipeCategoryListMap.get(identifier).add(display);
    }
    
    private void registerDisplay(Identifier categoryIdentifier, RecipeDisplay display, int index) {
        if (!recipeCategoryListMap.containsKey(categoryIdentifier))
            return;
        recipeCount[0]++;
        recipeCategoryListMap.get(categoryIdentifier).add(index, display);
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getRecipesFor(EntryStack stack) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> result = Maps.newLinkedHashMap();
        for (Map.Entry<RecipeCategory<?>, Identifier> entry : categories.entrySet()) {
            RecipeCategory<?> category = entry.getKey();
            Identifier categoryId = entry.getValue();
            Set<RecipeDisplay> set = Sets.newLinkedHashSet();
            for (RecipeDisplay display : Lists.newArrayList(recipeCategoryListMap.get(categoryId))) {
                for (EntryStack outputStack : display.getOutputEntries())
                    if (stack.equals(outputStack) && isDisplayVisible(display)) {
                        set.add(display);
                        break;
                    }
            }
            if (!set.isEmpty())
                CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
        }
        for (LiveRecipeGenerator<RecipeDisplay> liveRecipeGenerator : liveRecipeGenerators) {
            RecipeCategory<?> category = getCategory(liveRecipeGenerator.getCategoryIdentifier());
            Optional<List<RecipeDisplay>> recipeFor = liveRecipeGenerator.getRecipeFor(stack);
            if (recipeFor.isPresent()) {
                Set<RecipeDisplay> set = Sets.newLinkedHashSet();
                for (RecipeDisplay display : recipeFor.get()) {
                    if (isDisplayVisible(display))
                        set.add(display);
                }
                if (!set.isEmpty())
                    CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
            }
        }
        return result;
    }
    
    @Override
    public RecipeCategory<?> getCategory(Identifier identifier) {
        return reversedCategories.get(identifier);
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
    
    private boolean isStackWorkStationOfCategory(Identifier category, EntryStack stack) {
        for (List<EntryStack> stacks : getWorkingStations(category)) {
            for (EntryStack entryStack : stacks) {
                if (entryStack.equalsIgnoreTagsAndAmount(stack))
                    return true;
            }
        }
        return false;
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getUsagesFor(EntryStack stack) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> result = Maps.newLinkedHashMap();
        for (Map.Entry<RecipeCategory<?>, Identifier> entry : categories.entrySet()) {
            Set<RecipeDisplay> set = Sets.newLinkedHashSet();
            RecipeCategory<?> category = entry.getKey();
            Identifier categoryId = entry.getValue();
            for (RecipeDisplay display : Lists.newArrayList(recipeCategoryListMap.get(categoryId))) {
                back:
                for (List<EntryStack> input : display.getInputEntries()) {
                    for (EntryStack otherEntry : input) {
                        if (otherEntry.equals(stack)) {
                            if (isDisplayVisible(display))
                                set.add(display);
                            break back;
                        }
                    }
                }
            }
            if (isStackWorkStationOfCategory(categoryId, stack)) {
                set.addAll(Lists.newArrayList(recipeCategoryListMap.get(categoryId)));
            }
            if (!set.isEmpty())
                CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
        }
        for (LiveRecipeGenerator<RecipeDisplay> liveRecipeGenerator : liveRecipeGenerators) {
            RecipeCategory<?> category = getCategory(liveRecipeGenerator.getCategoryIdentifier());
            Optional<List<RecipeDisplay>> recipeFor = liveRecipeGenerator.getUsageFor(stack);
            if (recipeFor.isPresent()) {
                Set<RecipeDisplay> set = Sets.newLinkedHashSet();
                for (RecipeDisplay display : recipeFor.get()) {
                    if (isDisplayVisible(display))
                        set.add(display);
                }
                if (!set.isEmpty())
                    CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
            }
        }
        return result;
    }
    
    @Override
    public List<RecipeCategory<?>> getAllCategories() {
        return Lists.newArrayList(categories.keySet());
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
            autoCraftAreaSupplierMap.remove(category);
        } else
            autoCraftAreaSupplierMap.put(category, rectangle);
    }
    
    public void recipesLoaded(RecipeManager recipeManager) {
        long startTime = System.currentTimeMillis();
        arePluginsLoading = true;
        ScreenHelper.clearLastRecipeScreenData();
        recipeCount[0] = 0;
        this.recipeManager = recipeManager;
        this.recipeCategoryListMap.clear();
        this.categories.clear();
        this.reversedCategories.clear();
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
        DisplayHelper.getInstance().registerHandler(baseBoundsHandler);
        ((DisplayHelperImpl) DisplayHelper.getInstance()).setBaseBoundsHandler(baseBoundsHandler);
        List<REIPluginEntry> plugins = RoughlyEnoughItemsCore.getPlugins();
        plugins.sort(Comparator.comparingInt(REIPluginEntry::getPriority).reversed());
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Loading %d plugins: %s", plugins.size(), plugins.stream().map(REIPluginEntry::getPluginIdentifier).map(Identifier::toString).collect(Collectors.joining(", ")));
        Collections.reverse(plugins);
        ((EntryRegistryImpl) EntryRegistry.getInstance()).reset();
        List<REIPluginV0> reiPluginV0s = new ArrayList<>();
        for (REIPluginEntry plugin : plugins) {
            try {
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
        DisplayHelper.getInstance().registerHandler(new OverlayDecider() {
            @Override
            public boolean isHandingScreen(Class<?> screen) {
                return true;
            }
            
            @Override
            public ActionResult shouldScreenBeOverlayed(Class<?> screen) {
                return ContainerScreen.class.isAssignableFrom(screen) ? ActionResult.SUCCESS : ActionResult.PASS;
            }
            
            @Override
            public float getPriority() {
                return -10;
            }
        });
        
        // Clear Cache
        ((DisplayHelperImpl) DisplayHelper.getInstance()).resetCache();
        ScreenHelper.getOptionalOverlay().ifPresent(overlay -> overlay.shouldReInit = true);
        
        // Remove duplicate entries
        ((EntryRegistryImpl) EntryRegistry.getInstance()).distinct();
        arePluginsLoading = false;
        ((EntryRegistryImpl) EntryRegistry.getInstance()).refilter();
        
        // Clear Cache Again!
        ((DisplayHelperImpl) DisplayHelper.getInstance()).resetCache();
        ScreenHelper.getOptionalOverlay().ifPresent(overlay -> overlay.shouldReInit = true);
        
        displayVisibilityHandlers.sort(VISIBILITY_HANDLER_COMPARATOR);
        long usedTime = System.currentTimeMillis() - startTime;
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Registered %d stack entries, %d recipes displays, %d exclusion zones suppliers, %d overlay decider, %d visibility handlers and %d categories (%s) in %d ms.", EntryRegistry.getInstance().getStacksList().size(), recipeCount[0], BaseBoundsHandler.getInstance().supplierSize(), DisplayHelper.getInstance().getAllOverlayDeciders().size(), getDisplayVisibilityHandlers().size(), categories.size(), categories.keySet().stream().map(RecipeCategory::getCategoryName).collect(Collectors.joining(", ")), usedTime);
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
        return recipeCount[0];
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public List<Recipe> getAllSortedRecipes() {
        return getRecipeManager().values().stream().sorted(RECIPE_COMPARATOR).collect(Collectors.toList());
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getAllRecipes() {
        Map<RecipeCategory<?>, List<RecipeDisplay>> result = Maps.newLinkedHashMap();
        for (Map.Entry<RecipeCategory<?>, Identifier> entry : categories.entrySet()) {
            RecipeCategory<?> category = entry.getKey();
            Identifier categoryId = entry.getValue();
            List<RecipeDisplay> displays = Lists.newArrayList(recipeCategoryListMap.get(categoryId));
            if (displays != null) {
                displays.removeIf(this::isDisplayNotVisible);
                if (!displays.isEmpty())
                    result.put(category, displays);
            }
        }
        return result;
    }
    
    @Override
    public List<RecipeDisplay> getAllRecipesFromCategory(RecipeCategory<?> category) {
        return Lists.newArrayList(recipeCategoryListMap.get(category.getIdentifier()));
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
    public boolean isDisplayNotVisible(RecipeDisplay display) {
        return !isDisplayVisible(display);
    }
    
    @Override
    public boolean isDisplayVisible(RecipeDisplay display) {
        RecipeCategory<?> category = getCategory(display.getRecipeCategory());
        try {
            for (DisplayVisibilityHandler displayVisibilityHandler : displayVisibilityHandlers) {
                ActionResult visibility = displayVisibilityHandler.handleDisplay(category, display);
                if (visibility != ActionResult.PASS)
                    return visibility == ActionResult.SUCCESS;
            }
        } catch (Throwable throwable) {
            RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to check if the recipe is visible!", throwable);
        }
        return true;
    }
    
    @Override
    public void registerScreenClickArea(Rectangle rectangle, Class<? extends ContainerScreen<?>> screenClass, Identifier... categories) {
        this.screenClickAreas.add(new ScreenClickAreaImpl(screenClass, rectangle, categories));
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(Identifier category, Class<T> recipeClass, Function<T, RecipeDisplay> mappingFunction) {
        recipeFunctions.add(new RecipeFunction(category, recipe -> recipeClass.isAssignableFrom(recipe.getClass()), mappingFunction));
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(Identifier category,
            @SuppressWarnings("rawtypes") Function<Recipe, Boolean> recipeFilter, Function<T, RecipeDisplay> mappingFunction) {
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
    
    private static class ScreenClickAreaImpl implements ScreenClickArea {
        Class<? extends ContainerScreen<?>> screenClass;
        Rectangle rectangle;
        Identifier[] categories;
        
        private ScreenClickAreaImpl(Class<? extends ContainerScreen<?>> screenClass, Rectangle rectangle, Identifier[] categories) {
            this.screenClass = screenClass;
            this.rectangle = rectangle;
            this.categories = categories;
        }
        
        public Class<? extends ContainerScreen<?>> getScreenClass() {
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
    private static class RecipeFunction {
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
