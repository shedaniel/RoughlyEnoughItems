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

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.api.registry.CategoryRegistry;
import me.shedaniel.rei.api.registry.ParentReloadable;
import me.shedaniel.rei.api.registry.Reloadable;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.util.CollectionUtils;
import me.shedaniel.rei.impl.subsets.SubsetsRegistryImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class RecipeRegistryImpl implements RecipeRegistry, ParentReloadable {
    private static final Comparator<Recipe<?>> RECIPE_COMPARATOR = Comparator.comparing((Recipe<?> o) -> o.getId().getNamespace()).thenComparing(o -> o.getId().getPath());
    
    private final List<Reloadable> reloadables = new ArrayList<>();
    private final List<FocusedStackProvider> focusedStackProviders = Lists.newArrayList();
    private final List<AutoTransferHandler> autoTransferHandlers = Lists.newArrayList();
    private final List<RecipeFunction<?>> recipeFunctions = Lists.newArrayList();
    private final Multimap<Class<? extends Screen>, ClickAreaHandler<?>> screenClickAreas = HashMultimap.create();
    private final MutableInt recipeCount = new MutableInt(0);
    private final Map<ResourceLocation, List<Display>> recipeDisplays = Maps.newHashMap();
    private final BiMap<DisplayCategory<?>, ResourceLocation> categories = HashBiMap.create();
    private final Map<ResourceLocation, ButtonAreaSupplier> autoCraftAreaSupplierMap = Maps.newHashMap();
    private final List<DisplayVisibilityHandler> displayVisibilityHandlers = Lists.newArrayList();
    private final List<LiveRecipeGenerator<Display>> liveRecipeGenerators = Lists.newArrayList();
    private RecipeManager recipeManager;
    private boolean arePluginsLoading = false;
    
    public RecipeRegistryImpl() {
        reloadables.add(CategoryRegistry.getInstance());
    }
    
    @Override
    public List<EntryStack<?>> findCraftableEntriesByItems(Iterable<? extends EntryStack<?>> inventoryItems) {
        List<EntryStack<?>> craftables = new ArrayList<>();
        for (List<Display> value : recipeDisplays.values())
            for (Display display : Lists.newArrayList(value)) {
                int slotsCraftable = 0;
                List<EntryIngredient> requiredInput = display.getRequiredEntries();
                for (EntryIngredient slot : requiredInput) {
                    if (slot.isEmpty()) {
                        slotsCraftable++;
                        continue;
                    }
                    back:
                    for (EntryStack<?> possibleType : inventoryItems) {
                        for (EntryStack<?> slotPossible : slot)
                            if (EntryStacks.equalsIgnoreCount(possibleType, slotPossible)) {
                                slotsCraftable++;
                                break back;
                            }
                    }
                }
                if (slotsCraftable == display.getRequiredEntries().size())
                    display.getResultingEntries().stream().flatMap(Collection::stream).collect(Collectors.toCollection(() -> craftables));
            }
        return craftables.stream().distinct().collect(Collectors.toList());
    }
    
    @Override
    public boolean arePluginsLoading() {
        return arePluginsLoading;
    }
    
    @Override
    public void registerDisplay(Display display) {
        ResourceLocation identifier = Objects.requireNonNull(display.getRecipeCategory());
        if (!recipeDisplays.containsKey(identifier))
            throw new IllegalArgumentException("Unable to identify category: " + identifier.toString());
        recipeCount.increment();
        recipeDisplays.get(identifier).add(display);
    }
    
    private void registerDisplay(ResourceLocation categoryIdentifier, Display display, int index) {
        if (!recipeDisplays.containsKey(categoryIdentifier))
            throw new IllegalArgumentException("Unable to identify category: " + categoryIdentifier.toString());
        recipeCount.increment();
        recipeDisplays.get(categoryIdentifier).add(index, display);
    }
    
    @Override
    public Map<DisplayCategory<?>, List<Display>> buildMapFor(ClientHelper.ViewSearchBuilder builder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Set<ResourceLocation> categories = builder.getCategories();
        List<EntryStack<?>> recipesFor = builder.getRecipesFor();
        List<EntryStack<?>> usagesFor = builder.getUsagesFor();
        
        Map<DisplayCategory<?>, List<Display>> result = Maps.newLinkedHashMap();
        for (Map.Entry<DisplayCategory<?>, ResourceLocation> entry : this.categories.entrySet()) {
            DisplayCategory<?> category = entry.getKey();
            ResourceLocation categoryId = entry.getValue();
            List<Display> allRecipesFromCategory = getAllRecipesFromCategory(category);
            
            Set<Display> set = Sets.newLinkedHashSet();
            if (categories.contains(categoryId)) {
                for (Display display : allRecipesFromCategory) {
                    if (isDisplayVisible(display)) {
                        set.add(display);
                    }
                }
                if (!set.isEmpty()) {
                    CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
                }
                continue;
            }
            for (Display display : allRecipesFromCategory) {
                if (!isDisplayVisible(display)) continue;
                if (!recipesFor.isEmpty()) {
                    back:
                    for (List<? extends EntryStack<?>> results : display.getResultingEntries()) {
                        for (EntryStack<?> otherEntry : results) {
                            for (EntryStack<?> stack : recipesFor) {
                                if (EntryStacks.equalsIgnoreCount(otherEntry, stack)) {
                                    set.add(display);
                                    break back;
                                }
                            }
                        }
                    }
                }
                if (!usagesFor.isEmpty()) {
                    back:
                    for (List<? extends EntryStack<?>> input : display.getInputEntries()) {
                        for (EntryStack<?> otherEntry : input) {
                            for (EntryStack<?> stack : usagesFor) {
                                if (EntryStacks.equalsIgnoreCount(otherEntry, stack)) {
                                    set.add(display);
                                    break back;
                                }
                            }
                        }
                    }
                }
            }
            for (EntryStack<?> stack : usagesFor) {
                if (isStackWorkStationOfCategory(categoryId, stack)) {
                    set.addAll(CollectionUtils.filter(allRecipesFromCategory, this::isDisplayVisible));
                    break;
                }
            }
            if (!set.isEmpty()) {
                CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
            }
        }
        
        for (LiveRecipeGenerator<Display> liveRecipeGenerator : liveRecipeGenerators) {
            Set<Display> set = Sets.newLinkedHashSet();
            for (EntryStack<?> stack : recipesFor) {
                Optional<List<Display>> recipeForDisplays = liveRecipeGenerator.getRecipeFor(stack);
                if (recipeForDisplays.isPresent()) {
                    for (Display display : recipeForDisplays.get()) {
                        if (isDisplayVisible(display))
                            set.add(display);
                    }
                }
            }
            for (EntryStack<?> stack : usagesFor) {
                Optional<List<Display>> usageForDisplays = liveRecipeGenerator.getUsageFor(stack);
                if (usageForDisplays.isPresent()) {
                    for (Display display : usageForDisplays.get()) {
                        if (isDisplayVisible(display))
                            set.add(display);
                    }
                }
            }
            Optional<List<Display>> displaysGenerated = liveRecipeGenerator.getDisplaysGenerated(builder);
            if (displaysGenerated.isPresent()) {
                for (Display display : displaysGenerated.get()) {
                    if (isDisplayVisible(display))
                        set.add(display);
                }
            }
            if (!set.isEmpty()) {
                CollectionUtils.getOrPutEmptyList(result, getCategory(liveRecipeGenerator.getCategoryIdentifier())).addAll(set);
            }
        }
        
        String message = String.format("Built Recipe View in %s for %d categories, %d recipes for, %d usages for and %d live recipe generators.",
                stopwatch.stop().toString(), categories.size(), recipesFor.size(), usagesFor.size(), liveRecipeGenerators.size());
        if (ConfigObject.getInstance().doDebugSearchTimeRequired()) {
            RoughlyEnoughItemsCore.LOGGER.info(message);
        } else {
            RoughlyEnoughItemsCore.LOGGER.trace(message);
        }
        return result;
    }
    
    @Override
    public Map<DisplayCategory<?>, List<Display>> getRecipesFor(EntryStack<?> stack) {
        return buildMapFor(ClientHelper.ViewSearchBuilder.builder().addRecipesFor(stack));
    }
    
    @Override
    public DisplayCategory<?> getCategory(ResourceLocation identifier) {
        return categories.inverse().get(identifier);
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
    
    private boolean isStackWorkStationOfCategory(ResourceLocation category, EntryStack<?> stack) {
        for (List<? extends EntryStack<?>> stacks : getWorkingStations(category)) {
            for (EntryStack<?> entryStack : stacks) {
                if (EntryStacks.equalsFuzzy(entryStack, stack))
                    return true;
            }
        }
        return false;
    }
    
    @Override
    public Map<DisplayCategory<?>, List<Display>> getUsagesFor(EntryStack<?> stack) {
        return buildMapFor(ClientHelper.ViewSearchBuilder.builder().addUsagesFor(stack));
    }
    
    @Override
    public List<DisplayCategory<?>> getAllCategories() {
        return Lists.newArrayList(categories.keySet());
    }
    
    @Override
    public Optional<ButtonAreaSupplier> getAutoCraftButtonArea(DisplayCategory<?> category) {
        if (!autoCraftAreaSupplierMap.containsKey(category.getIdentifier()))
            return Optional.ofNullable(bounds -> new Rectangle(bounds.getMaxX() - 16, bounds.getMaxY() - 16, 10, 10));
        return Optional.ofNullable(autoCraftAreaSupplierMap.get(category.getIdentifier()));
    }
    
    @Override
    public void registerAutoCraftButtonArea(ResourceLocation category, ButtonAreaSupplier rectangle) {
        if (rectangle == null) {
            autoCraftAreaSupplierMap.remove(category);
        } else
            autoCraftAreaSupplierMap.put(category, rectangle);
    }
    
    private void startSection(MutablePair<Stopwatch, String> sectionData, String section) {
        sectionData.setRight(section);
        RoughlyEnoughItemsCore.LOGGER.debug("Reloading Section: \"%s\"", section);
        sectionData.getLeft().reset().start();
    }
    
    private void endSection(MutablePair<Stopwatch, String> sectionData) {
        sectionData.getLeft().stop();
        String section = sectionData.getRight();
        RoughlyEnoughItemsCore.LOGGER.debug("Reloading Section: \"%s\" done in %s", section, sectionData.getLeft().toString());
        sectionData.getLeft().reset();
    }
    
    private void pluginSection(MutablePair<Stopwatch, String> sectionData, String sectionName, List<REIPluginV0> list, Consumer<REIPluginV0> consumer) {
        for (REIPluginV0 plugin : list) {
            startSection(sectionData, sectionName + " for " + plugin.getPluginName());
            try {
                consumer.accept(plugin);
            } catch (Throwable e) {
                RoughlyEnoughItemsCore.LOGGER.error(plugin.getPluginName() + " plugin failed to " + sectionName + "!", e);
            }
            endSection(sectionData);
        }
    }
    
    public void tryRecipesLoaded(RecipeManager recipeManager) {
        try {
            recipesLoaded(recipeManager);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        arePluginsLoading = false;
    }
    
    public void recipesLoaded(RecipeManager recipeManager) {
        long startTime = Util.getMillis();
        MutablePair<Stopwatch, String> sectionData = new MutablePair<>(Stopwatch.createUnstarted(), "");
        
        startSection(sectionData, "reset-data");
        for (Reloadable reloadable : reloadables) {
            reloadable.resetData();
        }
        arePluginsLoading = true;
        ScreenHelper.clearLastRecipeScreenData();
        recipeCount.setValue(0);
        this.recipeManager = recipeManager;
        this.recipeDisplays.clear();
        this.categories.clear();
        this.autoCraftAreaSupplierMap.clear();
        this.screenClickAreas.clear();
        this.recipeFunctions.clear();
        this.displayVisibilityHandlers.clear();
        this.liveRecipeGenerators.clear();
        this.autoTransferHandlers.clear();
        this.focusedStackProviders.clear();
        
        DisplayBoundsRegistryImpl displayHelper = (DisplayBoundsRegistryImpl) DisplayBoundsRegistry.getInstance();
        EntryRegistryImpl entryRegistry = (EntryRegistryImpl) EntryRegistry.getInstance();
        EntryTypeRegistryImpl entryTypeRegistry = EntryTypeRegistryImpl.getInstance();
        
        entryTypeRegistry.reset();
        FavoriteEntryTypeRegistryImpl.getInstance().clear();
        ((SubsetsRegistryImpl) SubsetsRegistry.getInstance()).reset();
        ((FluidSupportProviderImpl) FluidSupportProvider.getInstance()).reset();
        displayHelper.resetData();
        displayHelper.resetCache();
        ExclusionZones exclusionZones = new ExclusionZonesImpl();
        displayHelper.registerHandler(exclusionZones);
        displayHelper.setExclusionZones(exclusionZones);
        List<REIPlugin> plugins = RoughlyEnoughItemsCore.getPlugins();
        plugins.sort(Comparator.comparingInt(REIPlugin::getPriority).reversed());
        RoughlyEnoughItemsCore.LOGGER.info("Reloading REI, registered %d plugins: %s", plugins.size(), plugins.stream().map(REIPlugin::getPluginName).collect(Collectors.joining(", ")));
        Collections.reverse(plugins);
        entryRegistry.resetToReloadStart();
        List<REIPluginV0> reiPluginV0s = new ArrayList<>();
        endSection(sectionData);
        for (REIPlugin plugin : plugins) {
            startSection(sectionData, "pre-register for " + plugin.getPluginName());
            try {
                if (plugin instanceof REIPluginV0) {
                    ((REIPluginV0) plugin).preRegister();
                    reiPluginV0s.add((REIPluginV0) plugin);
                }
            } catch (Throwable e) {
                RoughlyEnoughItemsCore.LOGGER.error(plugin.getPluginName() + " plugin failed to pre register!", e);
            }
            endSection(sectionData);
        }
        pluginSection(sectionData, "register-entry-types", reiPluginV0s, plugin -> plugin.registerEntryTypes(entryTypeRegistry));
        pluginSection(sectionData, "register-bounds", reiPluginV0s, plugin -> plugin.registerBounds(displayHelper));
        pluginSection(sectionData, "register-entries", reiPluginV0s, plugin -> plugin.registerEntries(entryRegistry));
        pluginSection(sectionData, "register-categories", reiPluginV0s, plugin -> plugin.registerCategories(this));
        pluginSection(sectionData, "register-displays", reiPluginV0s, plugin -> plugin.registerDisplays(this));
        pluginSection(sectionData, "register-others", reiPluginV0s, plugin -> plugin.registerOthers(this));
        pluginSection(sectionData, "post-register", reiPluginV0s, REIPluginV0::postRegister);
        startSection(sectionData, "recipe-functions");
        if (!recipeFunctions.isEmpty()) {
            List<Recipe<?>> allSortedRecipes = getAllSortedRecipes();
            for (int i = allSortedRecipes.size() - 1; i >= 0; i--) {
                Recipe<?> recipe = allSortedRecipes.get(i);
                for (RecipeFunction<?> recipeFunction : recipeFunctions) {
                    try {
                        if (recipeFunction.recipeFilter.test(recipe)) {
                            registerDisplay(recipeFunction.category, recipeFunction.get(recipe), 0);
                        }
                    } catch (Throwable e) {
                        RoughlyEnoughItemsCore.LOGGER.error("Failed to add recipes!", e);
                    }
                }
            }
        }
        endSection(sectionData);
        startSection(sectionData, "fill-handlers");
        if (getDisplayVisibilityHandlers().isEmpty())
            registerRecipeVisibilityHandler(new DisplayVisibilityHandler() {
                @Override
                public InteractionResult handleDisplay(DisplayCategory<?> category, Display display) {
                    return InteractionResult.SUCCESS;
                }
                
                @Override
                public float getPriority() {
                    return -1f;
                }
            });
        registerFocusedStackProvider(new FocusedStackProvider() {
            @Override
            @NotNull
            public InteractionResultHolder<EntryStack<?>> provide(Screen screen) {
                if (screen instanceof AbstractContainerScreen) {
                    AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
                    if (containerScreen.hoveredSlot != null && !containerScreen.hoveredSlot.getItem().isEmpty())
                        return InteractionResultHolder.success(EntryStacks.of(containerScreen.hoveredSlot.getItem()));
                }
                return InteractionResultHolder.pass(EntryStack.empty());
            }
            
            @Override
            public double getPriority() {
                return -1.0;
            }
        });
        displayHelper.registerHandler(new OverlayDecider() {
            @Override
            public boolean isHandingScreen(Class<?> screen) {
                return true;
            }
            
            @Override
            public InteractionResult shouldScreenBeOverlaid(Class<?> screen) {
                return AbstractContainerScreen.class.isAssignableFrom(screen) ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            
            @Override
            public float getPriority() {
                return -10;
            }
        });
        endSection(sectionData);
        
        // Clear Cache
        displayHelper.resetCache();
        REIHelper.getInstance().getOverlay().ifPresent(REIOverlay::queueReloadOverlay);
        
        startSection(sectionData, "entry-registry-finalise");
        
        // Finish Reload
        entryRegistry.finishReload();
        
        endSection(sectionData);
        startSection(sectionData, "entry-registry-refilter");
        
        arePluginsLoading = false;
        entryRegistry.refilter();
        
        endSection(sectionData);
        startSection(sectionData, "finalizing");
        
        // Clear Cache Again!
        displayHelper.resetCache();
        REIHelper.getInstance().getOverlay().ifPresent(REIOverlay::queueReloadOverlay);
        
        displayVisibilityHandlers.sort(Comparator.reverseOrder());
        endSection(sectionData);
        
        long usedTime = Util.getMillis() - startTime;
        RoughlyEnoughItemsCore.LOGGER.info("Reloaded %d stack entries, %d recipes displays, %d exclusion zones suppliers, %d overlay deciders, %d visibility handlers and %d categories (%s) in %dms.",
                entryRegistry.getEntryStacks().count(), recipeCount.getValue(), ExclusionZones.getInstance().getZonesCount(), displayHelper.getAllOverlayDeciders().size(), getDisplayVisibilityHandlers().size(), categories.size(), categories.keySet().stream().map(DisplayCategory::getCategoryName).collect(Collectors.joining(", ")), usedTime);
    }
    
    @Override
    public AutoTransferHandler registerAutoCraftingHandler(AutoTransferHandler handler) {
        autoTransferHandlers.add(handler);
        autoTransferHandlers.sort(Comparator.reverseOrder());
        return handler;
    }
    
    @Override
    public void registerFocusedStackProvider(FocusedStackProvider provider) {
        focusedStackProviders.add(provider);
        focusedStackProviders.sort(Comparator.reverseOrder());
    }
    
    @Override
    @Nullable
    public EntryStack<?> getScreenFocusedStack(Screen screen) {
        for (FocusedStackProvider provider : focusedStackProviders) {
            InteractionResultHolder<EntryStack<?>> result = Objects.requireNonNull(provider.provide(screen));
            if (result.getResult() == InteractionResult.SUCCESS) {
                if (!result.getObject().isEmpty())
                    return result.getObject();
                return null;
            } else if (result.getResult() == InteractionResult.FAIL)
                return null;
        }
        return null;
    }
    
    @Override
    public List<AutoTransferHandler> getSortedAutoCraftingHandler() {
        return autoTransferHandlers;
    }
    
    @Override
    public int getRecipeCount() {
        return recipeCount.getValue();
    }
    
    @Override
    public List<Recipe<?>> getAllSortedRecipes() {
        return getRecipeManager().getRecipes().parallelStream().sorted(RECIPE_COMPARATOR).collect(Collectors.toList());
    }
    
    @Override
    public Map<DisplayCategory<?>, List<Display>> getAllRecipes() {
        return buildMapFor(ClientHelper.ViewSearchBuilder.builder().addAllCategories());
    }
    
    @Override
    public Map<DisplayCategory<?>, List<Display>> getAllRecipesNoHandlers() {
        Map<DisplayCategory<?>, List<Display>> result = Maps.newLinkedHashMap();
        for (Map.Entry<DisplayCategory<?>, ResourceLocation> entry : categories.entrySet()) {
            DisplayCategory<?> category = entry.getKey();
            ResourceLocation categoryId = entry.getValue();
            List<Display> displays = recipeDisplays.get(categoryId);
            if (displays != null && !displays.isEmpty()) {
                result.put(category, displays);
            }
        }
        return result;
    }
    
    @Override
    public List<Display> getAllRecipesFromCategory(DisplayCategory<?> category) {
        return recipeDisplays.get(category.getIdentifier());
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
    public boolean isDisplayNotVisible(Display display) {
        return !isDisplayVisible(display);
    }
    
    @Override
    public boolean isDisplayVisible(Display display) {
        DisplayCategory<?> category = getCategory(display.getRecipeCategory());
        try {
            for (DisplayVisibilityHandler displayVisibilityHandler : displayVisibilityHandlers) {
                InteractionResult visibility = displayVisibilityHandler.handleDisplay(category, display);
                if (visibility != InteractionResult.PASS)
                    return visibility == InteractionResult.SUCCESS;
            }
        } catch (Throwable throwable) {
            RoughlyEnoughItemsCore.LOGGER.error("Failed to check if the recipe is visible!", throwable);
        }
        return true;
    }
    
    @Override
    public <T extends AbstractContainerScreen<?>> void registerContainerClickArea(ScreenClickAreaProvider<T> rectangleSupplier, Class<T> screenClass, ResourceLocation... categories) {
        registerClickArea(screen -> {
            Rectangle rectangle = rectangleSupplier.provide(screen).clone();
            rectangle.translate(screen.leftPos, screen.topPos);
            return rectangle;
        }, screenClass, categories);
    }
    
    @Override
    public <T extends Screen> void registerClickArea(ScreenClickAreaProvider<T> rectangleSupplier, Class<T> screenClass, ResourceLocation... categories) {
        registerClickArea(screenClass, rectangleSupplier.toHandler(() -> categories));
    }
    
    @Override
    public <T extends Screen> void registerClickArea(Class<T> screenClass, ClickAreaHandler<T> handler) {
        this.screenClickAreas.put(screenClass, handler);
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(ResourceLocation category, Predicate<Recipe<?>> recipeFilter, Function<T, Display> mappingFunction) {
        recipeFunctions.add(new RecipeFunction<>(category, recipeFilter, mappingFunction));
    }
    
    @Override
    public void registerLiveRecipeGenerator(LiveRecipeGenerator<?> liveRecipeGenerator) {
        liveRecipeGenerators.add((LiveRecipeGenerator<Display>) liveRecipeGenerator);
    }
    
    public Multimap<Class<? extends Screen>, ClickAreaHandler<?>> getClickAreas() {
        return screenClickAreas;
    }
    
    private static class RecipeFunction<T extends Recipe<?>> {
        private ResourceLocation category;
        private Predicate<Recipe<?>> recipeFilter;
        private Function<T, Display> mappingFunction;
        
        public RecipeFunction(ResourceLocation category, Predicate<Recipe<?>> recipeFilter, Function<T, Display> mappingFunction) {
            this.category = category;
            this.recipeFilter = recipeFilter;
            this.mappingFunction = mappingFunction;
        }
        
        private <A> Display get(A screen) {
            return mappingFunction.apply((T) screen);
        }
    }
}
