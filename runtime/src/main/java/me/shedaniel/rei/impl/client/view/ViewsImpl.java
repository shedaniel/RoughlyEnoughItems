/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.client.view;

import com.google.common.base.Predicates;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.client.view.Views;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplayMerger;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.gui.craftable.CraftableFilterCalculator;
import me.shedaniel.rei.impl.client.gui.widget.AutoCraftingEvaluator;
import me.shedaniel.rei.impl.client.registry.display.DisplayCache;
import me.shedaniel.rei.impl.client.registry.display.DisplayRegistryImpl;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class ViewsImpl implements Views {
    private static final ThreadLocal<ViewSearchBuilder> BUILDER = new ThreadLocal<>();
    private static final Map<String, Long> MISSING_RECIPE_LOGS = new ConcurrentHashMap<>();
    private static final long MISSING_RECIPE_LOG_COOLDOWN_MS = 2000L;
    
    @Nullable
    @Override
    public ViewSearchBuilder getContext() {
        return BUILDER.get();
    }
    
    public static Map<DisplayCategory<?>, List<DisplaySpec>> buildMapFor(ViewSearchBuilder builder) {
        BUILDER.set(builder);
        
        try {
            return _buildMapFor(builder);
        } finally {
            BUILDER.remove();
        }
    }
    
    private static Map<DisplayCategory<?>, List<DisplaySpec>> _buildMapFor(ViewSearchBuilder builder) {
        if (PluginManager.areAnyReloading()) {
            InternalLogger.getInstance().info("Cancelled Views buildMap since plugins have not finished reloading.");
            return Maps.newLinkedHashMap();
        }
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        boolean processingVisibilityHandlers = builder.isProcessingVisibilityHandlers();
        Set<CategoryIdentifier<?>> categories = new HashSet<>(builder.getCategories());
        Set<CategoryIdentifier<?>> filteringCategories = builder.getFilteringCategories();
        List<EntryStack<?>> recipesForStacks = builder.getRecipesFor();
        List<EntryStack<?>> usagesForStacks = builder.getUsagesFor();
        Function<EntryStack<?>, Collection<EntryStack<?>>> wildcardFunction = stack -> {
            EntryStack<?> wildcard = stack.wildcard();
            if (EntryStacks.equalsFuzzy(wildcard, stack)) return Collections.emptyList();
            return Collections.singletonList(wildcard);
        };
        List<EntryStack<?>> recipesForStacksWildcard = CollectionUtils.flatMap(recipesForStacks, wildcardFunction);
        List<EntryStack<?>> usagesForStacksWildcard = CollectionUtils.flatMap(usagesForStacks, wildcardFunction);
        DisplayRegistry displayRegistry = DisplayRegistry.getInstance();
        if (!recipesForStacks.isEmpty() && !RoughlyEnoughItemsCoreClient.hasReceivedServerDisplaySync()) {
            RoughlyEnoughItemsCoreClient.ensureClientRecipeFallbackSyncedForSearch();
        }
        DisplayCache displayCache = ((DisplayRegistryImpl) displayRegistry).cache();
        
        Map<DisplayCategory<?>, Set<Display>> result = Maps.newHashMap();
        forCategories(processingVisibilityHandlers, filteringCategories, displayRegistry, result, (configuration, categoryId, displays, set) -> {
            if (categories.contains(categoryId)) { // If the category is in the search, add all displays
                for (Display display : displays) {
                    if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(configuration.getCategory(), display)) {
                        set.add(display);
                    }
                }
                if (!set.isEmpty()) {
                    getOrPutEmptyLinkedSet(result, configuration.getCategory()).addAll(set);
                }
                return;
            }
            for (Display display : displays) {
                if (processingVisibilityHandlers && !displayRegistry.isDisplayVisible(configuration.getCategory(), display)) continue;
                if (!recipesForStacks.isEmpty()) {
                    if (isRecipesFor(displayRegistry, displayCache, recipesForStacks, display)) {
                        set.add(display);
                        continue;
                    }
                }
                if (!usagesForStacks.isEmpty()) {
                    if (isUsagesFor(displayCache, usagesForStacks, display)) {
                        set.add(display);
                    }
                }
            }
        });
        
        // Generate live displays per category
        int generatorsCount = 0;
        
        for (Map.Entry<CategoryIdentifier<?>, List<DynamicDisplayGenerator<?>>> entry : displayRegistry.getCategoryDisplayGenerators().entrySet()) {
            CategoryIdentifier<?> categoryId = entry.getKey();
            DisplayCategory<?> category = CategoryRegistry.getInstance().get(categoryId).getCategory();
            if (processingVisibilityHandlers && CategoryRegistry.getInstance().isCategoryInvisible(category)) continue;
            if (!filteringCategories.isEmpty() && !filteringCategories.contains(categoryId)) continue;
            Set<Display> set = new LinkedHashSet<>();
            generatorsCount += entry.getValue().size();
            
            for (DynamicDisplayGenerator<Display> generator : (List<DynamicDisplayGenerator<Display>>) (List<? extends DynamicDisplayGenerator<?>>) entry.getValue()) {
                generateLiveDisplays(displayRegistry, wrapForError(generator), builder, set::add);
            }
            
            if (!set.isEmpty()) {
                getOrPutEmptyLinkedSet(result, category).addAll(set);
            }
        }
        
        Consumer<Display> displayConsumer = display -> {
            CategoryIdentifier<?> categoryIdentifier = display.getCategoryIdentifier();
            if (!filteringCategories.isEmpty() && !filteringCategories.contains(categoryIdentifier)) return;
            getOrPutEmptyLinkedSet(result, CategoryRegistry.getInstance().get(categoryIdentifier).getCategory()).add(display);
        };
        for (DynamicDisplayGenerator<Display> generator : (List<DynamicDisplayGenerator<Display>>) (List<? extends DynamicDisplayGenerator<?>>) displayRegistry.getGlobalDisplayGenerators()) {
            generatorsCount++;
            generateLiveDisplays(displayRegistry, wrapForError(generator), builder, displayConsumer);
        }
        
        if (CollectionUtils.allMatch(result.values(), Set::isEmpty) && (!recipesForStacksWildcard.isEmpty() || !usagesForStacksWildcard.isEmpty())) {
            // Run wildcard search because no displays were found
            forCategories(processingVisibilityHandlers, filteringCategories, displayRegistry, result, (configuration, categoryId, displays, set) -> {
                if (categories.contains(categoryId)) return;
                for (Display display : displays) {
                    if (processingVisibilityHandlers && !displayRegistry.isDisplayVisible(configuration.getCategory(), display)) continue;
                    if (!recipesForStacksWildcard.isEmpty()) {
                        if (isRecipesFor(displayRegistry, displayCache, recipesForStacksWildcard, display)) {
                            set.add(display);
                            continue;
                        }
                    }
                    if (!usagesForStacksWildcard.isEmpty()) {
                        if (isUsagesFor(displayCache, usagesForStacksWildcard, display)) {
                            set.add(display);
                        }
                    }
                }
            });
        }
        
        forCategories(processingVisibilityHandlers, filteringCategories, displayRegistry, result, (configuration, categoryId, displays, set) -> {
            if (categories.contains(categoryId)) return;
            for (EntryStack<?> usagesFor : Iterables.concat(usagesForStacks, usagesForStacksWildcard)) {
                if (isStackWorkStationOfCategory(configuration, usagesFor)) {
                    categories.add(categoryId);
                    if (processingVisibilityHandlers) {
                        set.addAll(CollectionUtils.filterToSet(displays, display -> displayRegistry.isDisplayVisible(configuration.getCategory(), display)));
                    } else {
                        set.addAll(displays);
                    }
                    break;
                }
            }
        });
        
        // Merging displays
        Stopwatch mergingStopwatch = Stopwatch.createStarted(), sortingStopwatch = Stopwatch.createUnstarted();
        Map<DisplayCategory<?>, List<DisplaySpec>> merged = (Map<DisplayCategory<?>, List<DisplaySpec>>) (Map) new LinkedHashMap<>();
        for (Map.Entry<DisplayCategory<?>, Set<Display>> entry : result.entrySet()) {
            merged.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        
        if (builder.isMergingDisplays() && ConfigObject.getInstance().doMergeDisplayUnderOne()) {
            mergeAndOptimize(result, merged);
        }
        
        mergingStopwatch.stop();
        // Sorting displays
        sortingStopwatch.start();
        Map<DisplayCategory<?>, List<DisplaySpec>> sorted = sortDisplays(merged);
        sortingStopwatch.stop();

        if (!recipesForStacks.isEmpty() && CollectionUtils.allMatch(sorted.values(), List::isEmpty)) {
            logMissingRecipeDiagnostics(builder, displayRegistry, displayCache, recipesForStacks);
        }
        
        String message = String.format("Built Recipe View in %s for %d categories, %d recipes for, %d usages for and %d live recipe generators.",
                stopwatch.stop(), categories.size(), recipesForStacks.size(), usagesForStacks.size(), generatorsCount);
        if (ConfigObject.getInstance().doDebugSearchTimeRequired()) {
            InternalLogger.getInstance().info(message);
        } else {
            InternalLogger.getInstance().trace(message);
        }
        return sorted;
    }
    
    private static Map<DisplayCategory<?>, List<DisplaySpec>> sortDisplays(Map<DisplayCategory<?>, List<DisplaySpec>> unsorted) {
        Object2IntMap<CategoryIdentifier<?>> categoryOrder = new Object2IntOpenHashMap<>();
        categoryOrder.defaultReturnValue(Integer.MAX_VALUE);
        int i = 100000;
        for (CategoryRegistry.CategoryConfiguration<?> configuration : CategoryRegistry.getInstance()) {
            categoryOrder.put(configuration.getCategoryIdentifier(), i++);
        }
        i = 0;
        for (CategoryIdentifier<?> identifier : ConfigObject.getInstance().getCategoryOrdering()) {
            categoryOrder.put(identifier, i++);
        }
        Map<DisplayCategory<?>, List<DisplaySpec>> result = new TreeMap<>(Comparator.comparingInt(category -> categoryOrder.getInt(category.getCategoryIdentifier())));
        result.putAll(unsorted);
        return result;
    }
    
    private static void forCategories(boolean processingVisibilityHandlers, Set<CategoryIdentifier<?>> filteringCategories, DisplayRegistry displayRegistry, Map<DisplayCategory<?>, Set<Display>> result, QuadConsumer<CategoryRegistry.CategoryConfiguration<?>, CategoryIdentifier<?>, List<Display>, Set<Display>> displayConsumer) {
        for (CategoryRegistry.CategoryConfiguration<?> configuration : CategoryRegistry.getInstance()) {
            if (processingVisibilityHandlers && CategoryRegistry.getInstance().isCategoryInvisible(configuration.getCategory())) continue;
            CategoryIdentifier<?> categoryId = configuration.getCategoryIdentifier();
            if (!filteringCategories.isEmpty() && !filteringCategories.contains(categoryId)) continue;
            List<Display> allRecipesFromCategory = displayRegistry.get((CategoryIdentifier<Display>) categoryId);
            Set<Display> set = new LinkedHashSet<>();
            displayConsumer.accept(configuration, categoryId, allRecipesFromCategory, set);
            if (!set.isEmpty()) {
                getOrPutEmptyLinkedSet(result, configuration.getCategory()).addAll(set);
            }
        }
    }
    
    public static <A, B> Set<B> getOrPutEmptyLinkedSet(Map<A, Set<B>> map, A key) {
        Set<B> b = map.get(key);
        if (b != null) {
            return b;
        }
        map.put(key, new ReferenceLinkedOpenHashSet<>());
        return map.get(key);
    }
    
    public static boolean isRecipesFor(DisplayRegistry displayRegistry, @Nullable DisplayCache displayCache, List<EntryStack<?>> stacks, Display display) {
        if (displayCache != null && displayCache.isCached(display)) {
            for (EntryStack<?> recipesFor : stacks) {
                if (displayCache.getDisplaysByOutput(recipesFor).contains(display)) {
                    return true;
                }
            }
        }

        if (checkUsages(stacks, display, display.getOutputEntries())) {
            return true;
        }

        Object origin = displayRegistry.getDisplayOrigin(display);
        if (origin instanceof DisplayRegistryImpl.ClientFallbackOrigin fallbackOrigin) {
            return checkFallbackRecipeOutputs(stacks, fallbackOrigin.entry());
        }

        return false;
    }

    public static boolean isRecipesFor(@Nullable DisplayCache displayCache, List<EntryStack<?>> stacks, Display display) {
        return isRecipesFor(DisplayRegistry.getInstance(), displayCache, stacks, display);
    }
    
    public static boolean isUsagesFor(@Nullable DisplayCache displayCache, List<EntryStack<?>> stacks, Display display) {
        if (displayCache != null && displayCache.isCached(display)) {
            for (EntryStack<?> recipesFor : stacks) {
                return displayCache.getDisplaysByInput(recipesFor).contains(display);
            }
        }
        
        return checkUsages(stacks, display, display.getInputEntries());
    }
    
    private static boolean checkUsages(List<EntryStack<?>> stacks, Display display, List<EntryIngredient> entries) {
        for (EntryIngredient results : entries) {
            for (EntryStack<?> otherEntry : results) {
                for (EntryStack<?> recipesFor : stacks) {
                    if (EntryStacks.equalsFuzzy(otherEntry, recipesFor)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private static boolean checkFallbackRecipeOutputs(List<EntryStack<?>> stacks, RecipeDisplayEntry entry) {
        try {
            ContextMap context = EntryIngredients.slotDisplayContext();
            for (EntryStack<?> result : EntryIngredients.ofItemStacks(entry.resultItems(context))) {
                for (EntryStack<?> stack : stacks) {
                    if (EntryStacks.equalsFuzzy(result, stack)) {
                        return true;
                    }
                }
            }
        } catch (Throwable throwable) {
            InternalLogger.getInstance().debug("Failed to match fallback recipe output for %s", entry.id(), throwable);
        }

        return false;
    }

    private static void logMissingRecipeDiagnostics(ViewSearchBuilder builder, DisplayRegistry displayRegistry, @Nullable DisplayCache displayCache, List<EntryStack<?>> stacks) {
        String query = stacks.stream()
                .map(ViewsImpl::describeStack)
                .collect(Collectors.joining(", "));
        long now = System.currentTimeMillis();
        Long previous = MISSING_RECIPE_LOGS.put(query, now);
        if (previous != null && now - previous < MISSING_RECIPE_LOG_COOLDOWN_MS) {
            return;
        }

        boolean processVisibility = builder.isProcessingVisibilityHandlers();
        Set<CategoryIdentifier<?>> filteringCategories = builder.getFilteringCategories();
        int totalDisplays = 0;
        int visibleDisplays = 0;
        int matchedDisplayOutputs = 0;
        int matchedFallbackOutputs = 0;
        int hiddenMatches = 0;
        int cacheMismatches = 0;
        int fallbackOutputMismatches = 0;
        List<String> examples = new ArrayList<>();

        for (CategoryRegistry.CategoryConfiguration<?> configuration : CategoryRegistry.getInstance()) {
            CategoryIdentifier<?> categoryId = configuration.getCategoryIdentifier();
            if (!filteringCategories.isEmpty() && !filteringCategories.contains(categoryId)) continue;
            List<Display> displays = displayRegistry.get((CategoryIdentifier<Display>) categoryId);
            for (Display display : displays) {
                totalDisplays++;
                boolean visible = !processVisibility || displayRegistry.isDisplayVisible(configuration.getCategory(), display);
                if (visible) {
                    visibleDisplays++;
                }

                boolean directMatch = checkUsages(stacks, display, display.getOutputEntries());
                if (directMatch) {
                    matchedDisplayOutputs++;
                    if (!visible) {
                        hiddenMatches++;
                    }
                    if (displayCache != null && displayCache.isCached(display) && !isCachedForAnyOutput(displayCache, stacks, display)) {
                        cacheMismatches++;
                    }
                }

                Object origin = displayRegistry.getDisplayOrigin(display);
                if (origin instanceof DisplayRegistryImpl.ClientFallbackOrigin fallbackOrigin) {
                    boolean fallbackMatch = checkFallbackRecipeOutputs(stacks, fallbackOrigin.entry());
                    if (fallbackMatch) {
                        matchedFallbackOutputs++;
                        if (!directMatch) {
                            fallbackOutputMismatches++;
                            if (examples.size() < 5) {
                                examples.add("fallback entry " + fallbackOrigin.entry().id()
                                        + " -> display " + display.getClass().getName()
                                        + " in " + display.getCategoryIdentifier()
                                        + " resolved outputs do not match display outputs");
                            }
                        }
                    }
                }
            }
        }

        InternalLogger.getInstance().warn("Recipe lookup for [%s] produced 0 categories. displays=%d visible=%d matchedOutputs=%d matchedFallbackOutputs=%d hiddenMatches=%d cacheMismatches=%d fallbackOutputMismatches=%d filteringCategories=%s clientFallbackPresent=%s clientFallbackState=%s",
                query, totalDisplays, visibleDisplays, matchedDisplayOutputs, matchedFallbackOutputs, hiddenMatches, cacheMismatches, fallbackOutputMismatches,
                filteringCategories.isEmpty() ? "<all>" : filteringCategories,
                ((DisplayRegistryImpl) displayRegistry).hasClientFallbackRecipes(),
                RoughlyEnoughItemsCoreClient.describeClientRecipeFallbackSyncState());
        for (String example : examples) {
            InternalLogger.getInstance().warn("Recipe lookup diagnostic: %s", example);
        }
        if (matchedDisplayOutputs == 0 && matchedFallbackOutputs == 0) {
            InternalLogger.getInstance().warn("Recipe lookup diagnostic: no registered display outputs matched [%s].", query);
        } else if (hiddenMatches > 0) {
            InternalLogger.getInstance().warn("Recipe lookup diagnostic: matched displays exist for [%s], but %d matching displays were hidden by visibility handlers.", query, hiddenMatches);
        }
    }

    private static boolean isCachedForAnyOutput(DisplayCache displayCache, List<EntryStack<?>> stacks, Display display) {
        for (EntryStack<?> stack : stacks) {
            if (displayCache.getDisplaysByOutput(stack).contains(display)) {
                return true;
            }
        }
        return false;
    }

    private static String describeStack(EntryStack<?> stack) {
        return stack.getIdentifier() + " (" + stack.asFormattedText().getString() + ")";
    }
    
    private static Iterable<Display> sortAutoCrafting(Iterable<Display> displays) {
        Set<Display> successfulDisplays = new LinkedHashSet<>();
        Set<Display> applicableDisplays = new LinkedHashSet<>();
        
        for (Display display : displays) {
            AutoCraftingEvaluator.AutoCraftingResult result = AutoCraftingEvaluator.evaluateAutoCrafting(false, false, display, null);
            
            if (result.successful) {
                successfulDisplays.add(display);
            } else if (result.hasApplicable) {
                applicableDisplays.add(display);
            }
        }
        
        return Iterables.concat(successfulDisplays, applicableDisplays,
                Iterables.filter(displays, display -> !successfulDisplays.contains(display) && !applicableDisplays.contains(display)));
    }
    
    private static <T extends Display> void generateLiveDisplays(DisplayRegistry displayRegistry, DynamicDisplayGenerator<T> generator, ViewSearchBuilder builder, Consumer<T> displayConsumer) {
        boolean processingVisibilityHandlers = builder.isProcessingVisibilityHandlers();
        
        for (EntryStack<?> stack : builder.getRecipesFor()) {
            Optional<List<T>> recipeForDisplays = generator.getRecipeFor(stack);
            if (recipeForDisplays.isPresent()) {
                for (T display : recipeForDisplays.get()) {
                    if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(display)) {
                        displayConsumer.accept(display);
                    }
                }
            }
        }
        
        for (EntryStack<?> stack : builder.getUsagesFor()) {
            Optional<List<T>> usageForDisplays = generator.getUsageFor(stack);
            if (usageForDisplays.isPresent()) {
                for (T display : usageForDisplays.get()) {
                    if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(display)) {
                        displayConsumer.accept(display);
                    }
                }
            }
        }
        
        Optional<List<T>> displaysGenerated = generator.generate(builder);
        if (displaysGenerated.isPresent()) {
            for (T display : displaysGenerated.get()) {
                if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(display)) {
                    displayConsumer.accept(display);
                }
            }
        }
    }
    
    private static <T extends Display> DynamicDisplayGenerator<T> wrapForError(DynamicDisplayGenerator<T> generator) {
        return new DynamicDisplayGenerator<>() {
            @Override
            public Optional<List<T>> getRecipeFor(EntryStack<?> entry) {
                try {
                    return generator.getRecipeFor(entry);
                } catch (Throwable throwable) {
                    CrashReport report = CrashReportUtils.essential(throwable, "Error while generating recipes for an entry stack");
                    CrashReportUtils.renderer(report, entry);
                    InternalLogger.getInstance().throwException(new ReportedException(report));
                    return Optional.empty();
                }
            }
            
            @Override
            public Optional<List<T>> getUsageFor(EntryStack<?> entry) {
                try {
                    return generator.getUsageFor(entry);
                } catch (Throwable throwable) {
                    CrashReport report = CrashReportUtils.essential(throwable, "Error while generating usages for an entry stack");
                    CrashReportUtils.renderer(report, entry);
                    InternalLogger.getInstance().throwException(new ReportedException(report));
                    return Optional.empty();
                }
            }
            
            @Override
            public Optional<List<T>> generate(ViewSearchBuilder builder) {
                try {
                    return generator.generate(builder);
                } catch (Throwable throwable) {
                    CrashReport report = CrashReportUtils.essential(throwable, "Error while generating recipes for a search");
                    InternalLogger.getInstance().throwException(new ReportedException(report));
                    return Optional.empty();
                }
            }
        };
    }
    
    public Predicate<HashedEntryStackWrapper> getCraftableEntriesPredicate() {
        if (PluginManager.areAnyReloading()) {
            return Predicates.alwaysTrue();
        }
        
        return new CraftableFilterCalculator();
    }
    
    private static <T> boolean isStackWorkStationOfCategory(CategoryRegistry.CategoryConfiguration<?> category, EntryStack<T> stack) {
        for (EntryIngredient ingredient : category.getWorkstations()) {
            if (EntryIngredients.testFuzzy(ingredient, stack)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void startReload() {
        
    }
    
    private static void mergeAndOptimize(Map<DisplayCategory<?>, Set<Display>> displays, Map<DisplayCategory<?>, List<DisplaySpec>> resultSpec) {
        for (Map.Entry<DisplayCategory<?>, Set<Display>> entry : displays.entrySet()) {
            DisplayMerger<Display> merger = (DisplayMerger<Display>) entry.getKey().getDisplayMerger();
            
            if (merger != null) {
                Map<WrappedDisplaySpec, WrappedDisplaySpec> wrappedSet = new LinkedHashMap<>();
                List<WrappedDisplaySpec> specs = new ArrayList<>();
                
                for (Display display : sortAutoCrafting(entry.getValue())) {
                    WrappedDisplaySpec wrapped = new WrappedDisplaySpec(merger, display);
                    if (wrappedSet.containsKey(wrapped)) {
                        wrappedSet.get(wrapped).add(display);
                    } else {
                        wrappedSet.put(wrapped, wrapped);
                        specs.add(wrapped);
                    }
                }
                
                resultSpec.put(entry.getKey(), (List<DisplaySpec>) (List) specs);
            }
        }
    }
    
    private static class WrappedDisplaySpec implements DisplaySpec {
        private final DisplayMerger<Display> merger;
        private final Display display;
        private List<Identifier> ids = null;
        private final int hash;
        
        public WrappedDisplaySpec(DisplayMerger<Display> merger, Display display) {
            this.merger = merger;
            this.display = display;
            this.hash = merger.hashOf(display);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WrappedDisplaySpec wrapped)) return false;
            return hash == wrapped.hash && merger.canMerge(display, wrapped.display);
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
        @Override
        public Display provideInternalDisplay() {
            return display;
        }
        
        @Override
        public Collection<Identifier> provideInternalDisplayIds() {
            if (ids == null) {
                ids = new ArrayList<>();
                Optional<Identifier> location = display.getDisplayLocation();
                if (location.isPresent()) {
                    ids.add(location.get());
                }
            }
            return ids;
        }
        
        public void add(Display display) {
            Optional<Identifier> location = display.getDisplayLocation();
            if (location.isPresent()) {
                provideInternalDisplayIds().add(location.get());
            }
        }
    }
    
    @FunctionalInterface
    private interface QuadConsumer<P1, P2, P3, P4> {
        void accept(P1 p1, P2 p2, P3 p3, P4 p4);
    }
}
