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

import com.google.common.collect.*;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.api.subsets.SubsetsRegistry;
import me.shedaniel.rei.impl.subsets.SubsetsRegistryImpl;
import me.shedaniel.rei.utils.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class RecipeHelperImpl implements RecipeHelper {
    
    private static final Comparator<FocusedStackProvider> FOCUSED_STACK_PROVIDER_COMPARATOR = Comparator.comparingDouble(FocusedStackProvider::getPriority).reversed();
    private static final Comparator<DisplayVisibilityHandler> VISIBILITY_HANDLER_COMPARATOR = Comparator.comparingDouble(DisplayVisibilityHandler::getPriority).reversed();
    @SuppressWarnings("rawtypes")
    private static final Comparator<Recipe> RECIPE_COMPARATOR = Comparator.comparing((Recipe o) -> o.getId().getNamespace()).thenComparing(o -> o.getId().getPath());
    
    private final List<FocusedStackProvider> focusedStackProviders = Lists.newArrayList();
    private final List<AutoTransferHandler> autoTransferHandlers = Lists.newArrayList();
    private final List<RecipeFunction> recipeFunctions = Lists.newArrayList();
    private final List<ScreenClickArea> screenClickAreas = Lists.newArrayList();
    private final int[] recipeCount = {0};
    private final Map<Identifier, List<RecipeDisplay>> recipeDisplays = Maps.newHashMap();
    private final BiMap<RecipeCategory<?>, Identifier> categories = HashBiMap.create();
    private final Map<Identifier, ButtonAreaSupplier> autoCraftAreaSupplierMap = Maps.newHashMap();
    private final Map<Identifier, List<List<EntryStack>>> categoryWorkingStations = Maps.newHashMap();
    private final List<DisplayVisibilityHandler> displayVisibilityHandlers = Lists.newArrayList();
    private final List<LiveRecipeGenerator<RecipeDisplay>> liveRecipeGenerators = Lists.newArrayList();
    private RecipeManager recipeManager;
    private boolean arePluginsLoading = false;
    
    @Override
    public List<EntryStack> findCraftableEntriesByItems(List<EntryStack> inventoryItems) {
        List<EntryStack> craftables = new ArrayList<>();
        for (List<RecipeDisplay> value : recipeDisplays.values())
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
                    recipeDisplay.getResultingEntries().stream().flatMap(Collection::stream).collect(Collectors.toCollection(() -> craftables));
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
        recipeDisplays.put(category.getIdentifier(), Lists.newArrayList());
        categoryWorkingStations.put(category.getIdentifier(), Lists.newArrayList());
    }
    
    @SafeVarargs
    @Override
    public final void registerWorkingStations(Identifier category, List<EntryStack>... workingStations) {
        categoryWorkingStations.get(category).addAll(Arrays.asList(workingStations));
    }
    
    @Override
    public void registerWorkingStations(Identifier category, EntryStack... workingStations) {
        categoryWorkingStations.get(category).addAll(Stream.of(workingStations).map(Collections::singletonList).collect(Collectors.toList()));
    }
    
    @Override
    public List<List<EntryStack>> getWorkingStations(Identifier category) {
        return categoryWorkingStations.get(category);
    }
    
    @Override
    public void registerDisplay(RecipeDisplay display) {
        Identifier identifier = Objects.requireNonNull(display.getRecipeCategory());
        if (!recipeDisplays.containsKey(identifier))
            return;
        recipeCount[0]++;
        recipeDisplays.get(identifier).add(display);
    }
    
    private void registerDisplay(Identifier categoryIdentifier, RecipeDisplay display, int index) {
        if (!recipeDisplays.containsKey(categoryIdentifier))
            return;
        recipeCount[0]++;
        recipeDisplays.get(categoryIdentifier).add(index, display);
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> buildMapFor(ClientHelper.ViewSearchBuilder builder) {
        long start = Util.getMeasuringTimeNano();
        Set<Identifier> categories = builder.getCategories();
        List<EntryStack> recipesFor = builder.getRecipesFor();
        List<EntryStack> usagesFor = builder.getUsagesFor();
        
        Map<RecipeCategory<?>, List<RecipeDisplay>> result = Maps.newLinkedHashMap();
        for (Map.Entry<RecipeCategory<?>, Identifier> entry : this.categories.entrySet()) {
            RecipeCategory<?> category = entry.getKey();
            Identifier categoryId = entry.getValue();
            List<RecipeDisplay> allRecipesFromCategory = getAllRecipesFromCategory(category);
            
            Set<RecipeDisplay> set = Sets.newLinkedHashSet();
            if (categories.contains(categoryId)) {
                for (RecipeDisplay display : allRecipesFromCategory) {
                    if (isDisplayVisible(display)) {
                        set.add(display);
                    }
                }
                if (!set.isEmpty()) {
                    CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
                }
                continue;
            }
            for (RecipeDisplay display : allRecipesFromCategory) {
                if (!isDisplayVisible(display)) continue;
                if (!recipesFor.isEmpty()) {
                    back:
                    for (List<EntryStack> results : display.getResultingEntries()) {
                        for (EntryStack otherEntry : results) {
                            for (EntryStack stack : recipesFor) {
                                if (otherEntry.equals(stack)) {
                                    set.add(display);
                                    break back;
                                }
                            }
                        }
                    }
                }
                if (!usagesFor.isEmpty()) {
                    back:
                    for (List<EntryStack> input : display.getInputEntries()) {
                        for (EntryStack otherEntry : input) {
                            for (EntryStack stack : usagesFor) {
                                if (otherEntry.equals(stack)) {
                                    set.add(display);
                                    break back;
                                }
                            }
                        }
                    }
                }
            }
            for (EntryStack stack : usagesFor) {
                if (isStackWorkStationOfCategory(categoryId, stack)) {
                    set.addAll(allRecipesFromCategory);
                    break;
                }
            }
            if (!set.isEmpty()) {
                CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
            }
        }
        
        for (LiveRecipeGenerator<RecipeDisplay> liveRecipeGenerator : liveRecipeGenerators) {
            Set<RecipeDisplay> set = Sets.newLinkedHashSet();
            for (EntryStack stack : recipesFor) {
                Optional<List<RecipeDisplay>> recipeForDisplays = liveRecipeGenerator.getRecipeFor(stack);
                if (recipeForDisplays.isPresent()) {
                    for (RecipeDisplay display : recipeForDisplays.get()) {
                        if (isDisplayVisible(display))
                            set.add(display);
                    }
                }
            }
            for (EntryStack stack : usagesFor) {
                Optional<List<RecipeDisplay>> usageForDisplays = liveRecipeGenerator.getUsageFor(stack);
                if (usageForDisplays.isPresent()) {
                    for (RecipeDisplay display : usageForDisplays.get()) {
                        if (isDisplayVisible(display))
                            set.add(display);
                    }
                }
            }
            Optional<List<RecipeDisplay>> displaysGenerated = liveRecipeGenerator.getDisplaysGenerated(builder);
            if (displaysGenerated.isPresent()) {
                for (RecipeDisplay display : displaysGenerated.get()) {
                    if (isDisplayVisible(display))
                        set.add(display);
                }
            }
            if (!set.isEmpty()) {
                CollectionUtils.getOrPutEmptyList(result, getCategory(liveRecipeGenerator.getCategoryIdentifier())).addAll(set);
            }
        }
        
        long end = Util.getMeasuringTimeNano();
        String message = String.format("Built Recipe View in %dμs for %d categories, %d recipes for, %d usages for and %d live recipe generators.",
                (end - start) / 1000, categories.size(), recipesFor.size(), usagesFor.size(), liveRecipeGenerators.size());
        if (ConfigObject.getInstance().doDebugSearchTimeRequired()) {
            RoughlyEnoughItemsCore.LOGGER.info(message);
        } else {
            RoughlyEnoughItemsCore.LOGGER.trace(message);
        }
        return result;
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getRecipesFor(EntryStack stack) {
        return buildMapFor(ClientHelper.ViewSearchBuilder.builder().addRecipesFor(stack));
    }
    
    @Override
    public RecipeCategory<?> getCategory(Identifier identifier) {
        return categories.inverse().get(identifier);
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
        return buildMapFor(ClientHelper.ViewSearchBuilder.builder().addUsagesFor(stack));
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
    
    private void startSection(Object[] sectionData, String section) {
        sectionData[0] = Util.getMeasuringTimeNano();
        sectionData[2] = section;
        RoughlyEnoughItemsCore.LOGGER.debug("Reloading Section: \"%s\"", section);
    }
    
    private void endSection(Object[] sectionData) {
        sectionData[1] = Util.getMeasuringTimeNano();
        long time = (long) sectionData[1] - (long) sectionData[0];
        String section = (String) sectionData[2];
        if (time >= 1000000) {
            RoughlyEnoughItemsCore.LOGGER.debug("Reloading Section: \"%s\" done in %.2fms", section, time / 1000000.0F);
        } else {
            RoughlyEnoughItemsCore.LOGGER.debug("Reloading Section: \"%s\" done in %.2fμs", section, time / 1000.0F);
        }
    }
    
    private void pluginSection(Object[] sectionData, String sectionName, List<REIPluginV0> list, Consumer<REIPluginV0> consumer) {
        for (REIPluginV0 plugin : list) {
            try {
                startSection(sectionData, sectionName + " for " + plugin.getPluginIdentifier().toString());
                consumer.accept(plugin);
                endSection(sectionData);
            } catch (Throwable e) {
                RoughlyEnoughItemsCore.LOGGER.error(plugin.getPluginIdentifier().toString() + " plugin failed to " + sectionName + "!", e);
            }
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
        long startTime = Util.getMeasuringTimeMs();
        Object[] sectionData = {0L, 0L, ""};
        
        startSection(sectionData, "reset-data");
        arePluginsLoading = true;
        ScreenHelper.clearLastRecipeScreenData();
        recipeCount[0] = 0;
        this.recipeManager = recipeManager;
        this.recipeDisplays.clear();
        this.categories.clear();
        this.autoCraftAreaSupplierMap.clear();
        this.screenClickAreas.clear();
        this.categoryWorkingStations.clear();
        this.recipeFunctions.clear();
        this.displayVisibilityHandlers.clear();
        this.liveRecipeGenerators.clear();
        this.autoTransferHandlers.clear();
        this.focusedStackProviders.clear();
        
        DisplayHelperImpl displayHelper = (DisplayHelperImpl) DisplayHelper.getInstance();
        EntryRegistryImpl entryRegistry = (EntryRegistryImpl) EntryRegistry.getInstance();
        
        ((SubsetsRegistryImpl) SubsetsRegistry.getInstance()).reset();
        ((FluidSupportProviderImpl) FluidSupportProvider.getInstance()).reset();
        displayHelper.resetData();
        displayHelper.resetCache();
        BaseBoundsHandler baseBoundsHandler = new BaseBoundsHandlerImpl();
        displayHelper.registerHandler(baseBoundsHandler);
        displayHelper.setBaseBoundsHandler(baseBoundsHandler);
        List<REIPluginEntry> plugins = RoughlyEnoughItemsCore.getPlugins();
        plugins.sort(Comparator.comparingInt(REIPluginEntry::getPriority).reversed());
        RoughlyEnoughItemsCore.LOGGER.info("Reloading REI, registered %d plugins: %s", plugins.size(), plugins.stream().map(REIPluginEntry::getPluginIdentifier).map(Identifier::toString).collect(Collectors.joining(", ")));
        Collections.reverse(plugins);
        entryRegistry.reset();
        List<REIPluginV0> reiPluginV0s = new ArrayList<>();
        endSection(sectionData);
        for (REIPluginEntry plugin : plugins) {
            try {
                if (plugin instanceof REIPluginV0) {
                    startSection(sectionData, "pre-register for " + plugin.getPluginIdentifier().toString());
                    ((REIPluginV0) plugin).preRegister();
                    reiPluginV0s.add((REIPluginV0) plugin);
                    endSection(sectionData);
                }
            } catch (Throwable e) {
                RoughlyEnoughItemsCore.LOGGER.error(plugin.getPluginIdentifier().toString() + " plugin failed to pre register!", e);
            }
        }
        pluginSection(sectionData, "register-bounds", reiPluginV0s, plugin -> plugin.registerBounds(displayHelper));
        pluginSection(sectionData, "register-entries", reiPluginV0s, plugin -> plugin.registerEntries(entryRegistry));
        pluginSection(sectionData, "register-categories", reiPluginV0s, plugin -> plugin.registerPluginCategories(this));
        pluginSection(sectionData, "register-displays", reiPluginV0s, plugin -> plugin.registerRecipeDisplays(this));
        pluginSection(sectionData, "register-others", reiPluginV0s, plugin -> plugin.registerOthers(this));
        pluginSection(sectionData, "post-register", reiPluginV0s, REIPluginV0::postRegister);
        startSection(sectionData, "recipe-functions");
        if (!recipeFunctions.isEmpty()) {
            @SuppressWarnings("rawtypes") List<Recipe> allSortedRecipes = getAllSortedRecipes();
            Collections.reverse(allSortedRecipes);
            for (RecipeFunction recipeFunction : recipeFunctions) {
                try {
                    for (Recipe<?> recipe : CollectionUtils.filter(allSortedRecipes, recipe -> recipeFunction.recipeFilter.test(recipe))) {
                        registerDisplay(recipeFunction.category, (RecipeDisplay) recipeFunction.mappingFunction.apply(recipe), 0);
                    }
                } catch (Throwable e) {
                    RoughlyEnoughItemsCore.LOGGER.error("Failed to add recipes!", e);
                }
            }
        }
        endSection(sectionData);
        startSection(sectionData, "fill-handlers");
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
        registerFocusedStackProvider(new FocusedStackProvider() {
            @Override
            @NotNull
            public TypedActionResult<EntryStack> provide(Screen screen) {
                if (screen instanceof ContainerScreen) {
                    ContainerScreen<?> containerScreen = (ContainerScreen<?>) screen;
                    if (containerScreen.focusedSlot != null && !containerScreen.focusedSlot.getStack().isEmpty())
                        return TypedActionResult.success(EntryStack.create(containerScreen.focusedSlot.getStack()));
                }
                return TypedActionResult.pass(EntryStack.empty());
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
            public ActionResult shouldScreenBeOverlayed(Class<?> screen) {
                return ContainerScreen.class.isAssignableFrom(screen) ? ActionResult.SUCCESS : ActionResult.PASS;
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
        
        displayVisibilityHandlers.sort(VISIBILITY_HANDLER_COMPARATOR);
        endSection(sectionData);
        
        long usedTime = Util.getMeasuringTimeMs() - startTime;
        RoughlyEnoughItemsCore.LOGGER.info("Reloaded %d stack entries, %d recipes displays, %d exclusion zones suppliers, %d overlay deciders, %d visibility handlers and %d categories (%s) in %dms.",
                entryRegistry.getEntryStacks().count(), recipeCount[0], BaseBoundsHandler.getInstance().supplierSize(), displayHelper.getAllOverlayDeciders().size(), getDisplayVisibilityHandlers().size(), categories.size(), categories.keySet().stream().map(RecipeCategory::getCategoryName).collect(Collectors.joining(", ")), usedTime);
    }
    
    @Override
    public AutoTransferHandler registerAutoCraftingHandler(AutoTransferHandler handler) {
        autoTransferHandlers.add(handler);
        autoTransferHandlers.sort(Comparator.comparingDouble(AutoTransferHandler::getPriority).reversed());
        return handler;
    }
    
    @Override
    public void registerFocusedStackProvider(FocusedStackProvider provider) {
        focusedStackProviders.add(provider);
        focusedStackProviders.sort(FOCUSED_STACK_PROVIDER_COMPARATOR);
    }
    
    @Override
    @Nullable
    public EntryStack getScreenFocusedStack(Screen screen) {
        for (FocusedStackProvider provider : focusedStackProviders) {
            TypedActionResult<EntryStack> result = Objects.requireNonNull(provider.provide(screen));
            if (result.getResult() == ActionResult.SUCCESS) {
                if (!result.getValue().isEmpty())
                    return result.getValue();
                return null;
            } else if (result.getResult() == ActionResult.FAIL)
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
        return recipeCount[0];
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public List<Recipe> getAllSortedRecipes() {
        return getRecipeManager().values().parallelStream().sorted(RECIPE_COMPARATOR).collect(Collectors.toList());
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getAllRecipes() {
        return buildMapFor(ClientHelper.ViewSearchBuilder.builder().addAllCategories());
    }
    
    @Override
    public Map<RecipeCategory<?>, List<RecipeDisplay>> getAllRecipesNoHandlers() {
        Map<RecipeCategory<?>, List<RecipeDisplay>> result = Maps.newLinkedHashMap();
        for (Map.Entry<RecipeCategory<?>, Identifier> entry : categories.entrySet()) {
            RecipeCategory<?> category = entry.getKey();
            Identifier categoryId = entry.getValue();
            List<RecipeDisplay> displays = recipeDisplays.get(categoryId);
            if (displays != null && !displays.isEmpty()) {
                result.put(category, displays);
            }
        }
        return result;
    }
    
    @Override
    public List<RecipeDisplay> getAllRecipesFromCategory(RecipeCategory<?> category) {
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
            RoughlyEnoughItemsCore.LOGGER.error("Failed to check if the recipe is visible!", throwable);
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
        private Class<? extends ContainerScreen<?>> screenClass;
        private Rectangle rectangle;
        private Identifier[] categories;
        
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
        private Identifier category;
        private Predicate<Recipe> recipeFilter;
        private Function mappingFunction;
        
        public RecipeFunction(Identifier category, Predicate<Recipe> recipeFilter, Function<?, RecipeDisplay> mappingFunction) {
            this.category = category;
            this.recipeFilter = recipeFilter;
            this.mappingFunction = mappingFunction;
        }
    }
    
}
