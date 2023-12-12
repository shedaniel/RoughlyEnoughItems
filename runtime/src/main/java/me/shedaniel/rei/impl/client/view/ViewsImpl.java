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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.client.view.Views;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplayMerger;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.gui.craftable.CraftableFilter;
import me.shedaniel.rei.impl.client.gui.widget.AutoCraftingEvaluator;
import me.shedaniel.rei.impl.client.registry.display.DisplayRegistryImpl;
import me.shedaniel.rei.impl.client.registry.display.DisplaysHolder;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.display.DisplaySpec;
import me.shedaniel.rei.plugin.autocrafting.DefaultCategoryHandler;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class ViewsImpl implements Views {
    private static final ThreadLocal<ViewSearchBuilder> BUILDER = new ThreadLocal<>();
    
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
        Set<CategoryIdentifier<?>> categories = builder.getCategories();
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
        DisplaysHolder displaysHolder = ((DisplayRegistryImpl) displayRegistry).displaysHolder();
        
        Map<DisplayCategory<?>, List<Display>> result = Maps.newHashMap();
        for (CategoryRegistry.CategoryConfiguration<?> categoryConfiguration : CategoryRegistry.getInstance()) {
            DisplayCategory<?> category = categoryConfiguration.getCategory();
            if (processingVisibilityHandlers && CategoryRegistry.getInstance().isCategoryInvisible(category)) continue;
            CategoryIdentifier<?> categoryId = categoryConfiguration.getCategoryIdentifier();
            if (!filteringCategories.isEmpty() && !filteringCategories.contains(categoryId)) continue;
            List<Display> allRecipesFromCategory = displayRegistry.get((CategoryIdentifier<Display>) categoryId);
            
            Set<Display> set = Sets.newLinkedHashSet();
            if (categories.contains(categoryId)) {
                for (Display display : allRecipesFromCategory) {
                    if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(display)) {
                        set.add(display);
                    }
                }
                if (!set.isEmpty()) {
                    CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
                }
                continue;
            }
            for (Display display : allRecipesFromCategory) {
                if (processingVisibilityHandlers && !displayRegistry.isDisplayVisible(display)) continue;
                if (!recipesForStacks.isEmpty()) {
                    if (isRecipesFor(displaysHolder, recipesForStacks, display)) {
                        set.add(display);
                        continue;
                    }
                }
                if (!usagesForStacks.isEmpty()) {
                    if (isUsagesFor(displaysHolder, usagesForStacks, display)) {
                        set.add(display);
                        continue;
                    }
                }
            }
            if (set.isEmpty() && (!recipesForStacksWildcard.isEmpty() || !usagesForStacksWildcard.isEmpty())) {
                for (Display display : allRecipesFromCategory) {
                    if (processingVisibilityHandlers && !displayRegistry.isDisplayVisible(display)) continue;
                    if (!recipesForStacksWildcard.isEmpty()) {
                        if (isRecipesFor(displaysHolder, recipesForStacksWildcard, display)) {
                            set.add(display);
                            continue;
                        }
                    }
                    if (!usagesForStacksWildcard.isEmpty()) {
                        if (isUsagesFor(displaysHolder, usagesForStacksWildcard, display)) {
                            set.add(display);
                            continue;
                        }
                    }
                }
            }
            for (EntryStack<?> usagesFor : Iterables.concat(usagesForStacks, usagesForStacksWildcard)) {
                if (isStackWorkStationOfCategory(categoryConfiguration, usagesFor)) {
                    if (processingVisibilityHandlers) {
                        set.addAll(CollectionUtils.filterToSet(allRecipesFromCategory, displayRegistry::isDisplayVisible));
                    } else {
                        set.addAll(allRecipesFromCategory);
                    }
                    break;
                }
            }
            if (!set.isEmpty()) {
                CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
            }
        }
        
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
                CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
            }
        }
        
        Consumer<Display> displayConsumer = display -> {
            CategoryIdentifier<?> categoryIdentifier = display.getCategoryIdentifier();
            if (!filteringCategories.isEmpty() && !filteringCategories.contains(categoryIdentifier)) return;
            CollectionUtils.getOrPutEmptyList(result, CategoryRegistry.getInstance().get(categoryIdentifier).getCategory()).add(display);
        };
        for (DynamicDisplayGenerator<Display> generator : (List<DynamicDisplayGenerator<Display>>) (List<? extends DynamicDisplayGenerator<?>>) displayRegistry.getGlobalDisplayGenerators()) {
            generatorsCount++;
            generateLiveDisplays(displayRegistry, wrapForError(generator), builder, displayConsumer);
        }
        
        Stopwatch mergingStopwatch = Stopwatch.createStarted(), sortingStopwatch = Stopwatch.createUnstarted();
        Map<DisplayCategory<?>, List<DisplaySpec>> merged = mergeDisplays(builder, result);
        mergingStopwatch.stop();
        sortingStopwatch.start();
        Map<DisplayCategory<?>, List<DisplaySpec>> sorted = sortDisplays(merged);
        sortingStopwatch.stop();
        
        String message = String.format("Built Recipe View in %s for %d categories, %d recipes for, %d usages for and %d live recipe generators." +
                                       " Merging took %s and sorting took %s.",
                stopwatch.stop(), categories.size(), recipesForStacks.size(), usagesForStacks.size(), generatorsCount, mergingStopwatch, sortingStopwatch);
        if (ConfigObject.getInstance().doDebugSearchTimeRequired()) {
            InternalLogger.getInstance().info(message);
        } else {
            InternalLogger.getInstance().trace(message);
        }
        
        return sorted;
    }
    
    private static Map<DisplayCategory<?>, List<DisplaySpec>> mergeDisplays(ViewSearchBuilder builder, Map<DisplayCategory<?>, List<Display>> displays) {
        Map<DisplayCategory<?>, List<DisplaySpec>> result = (Map<DisplayCategory<?>, List<DisplaySpec>>) (Map<?, ?>) new HashMap<>(displays);
        
        if (builder.isMergingDisplays() && ConfigObject.getInstance().doMergeDisplayUnderOne()) {
            for (Map.Entry<DisplayCategory<?>, List<Display>> entry : displays.entrySet()) {
                DisplayMerger<Display> merger = (DisplayMerger<Display>) entry.getKey().getDisplayMerger();
                
                if (merger != null) {
                    class Wrapped implements DisplaySpec {
                        private final Display display;
                        private List<ResourceLocation> ids = null;
                        private final int hash;
                        
                        public Wrapped(Display display) {
                            this.display = display;
                            this.hash = merger.hashOf(display);
                        }
                        
                        @Override
                        public boolean equals(Object o) {
                            if (this == o) return true;
                            if (!(o instanceof Wrapped)) return false;
                            Wrapped wrapped = (Wrapped) o;
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
                        public Collection<ResourceLocation> provideInternalDisplayIds() {
                            if (ids == null) {
                                ids = new ArrayList<>();
                                Optional<ResourceLocation> location = display.getDisplayLocation();
                                if (location.isPresent()) {
                                    ids.add(location.get());
                                }
                            }
                            return ids;
                        }
                        
                        public void add(Display display) {
                            Optional<ResourceLocation> location = display.getDisplayLocation();
                            if (location.isPresent()) {
                                provideInternalDisplayIds().add(location.get());
                            }
                        }
                    }
                    Map<Wrapped, Wrapped> wrappedSet = new HashMap<>();
                    List<Wrapped> specs = new ArrayList<>();
                    
                    for (Display display : sortAutoCrafting(entry.getValue())) {
                        Wrapped wrapped = new Wrapped(display);
                        if (wrappedSet.containsKey(wrapped)) {
                            wrappedSet.get(wrapped).add(display);
                        } else {
                            wrappedSet.put(wrapped, wrapped);
                            specs.add(wrapped);
                        }
                    }
                    
                    result.put(entry.getKey(), (List<DisplaySpec>) (List<?>) specs);
                }
            }
        }
        
        return result;
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
    
    public static boolean isRecipesFor(@Nullable DisplaysHolder displaysHolder, List<EntryStack<?>> stacks, Display display) {
        if (displaysHolder != null && displaysHolder.isCached(display)) {
            for (EntryStack<?> recipesFor : stacks) {
                return displaysHolder.getDisplaysByOutput(recipesFor).contains(display);
            }
        }
        
        return checkUsages(stacks, display, display.getOutputEntries());
    }
    
    public static boolean isUsagesFor(@Nullable DisplaysHolder displaysHolder, List<EntryStack<?>> stacks, Display display) {
        if (displaysHolder != null && displaysHolder.isCached(display)) {
            for (EntryStack<?> recipesFor : stacks) {
                return displaysHolder.getDisplaysByInput(recipesFor).contains(display);
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
    
    private static Iterable<Display> sortAutoCrafting(List<Display> displays) {
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
    
    @Override
    public Collection<EntryStack<?>> findCraftableEntriesByMaterials() {
        if (PluginManager.areAnyReloading()) {
            return Collections.emptySet();
        }
        
        AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
        Set<EntryStack<?>> craftables = new HashSet<>();
        AbstractContainerScreen<?> containerScreen = REIRuntime.getInstance().getPreviousContainerScreen();
        
        for (Map.Entry<CategoryIdentifier<?>, List<Display>> entry : DisplayRegistry.getInstance().getAll().entrySet()) {
            MenuSerializationContext<AbstractContainerMenu, LocalPlayer, Display> context = createLegacyContext(menu, entry.getKey().cast());
            
            List<Display> displays = entry.getValue();
            for (Display display : displays) {
                try {
                    TransferHandler.Context transferContext = TransferHandler.Context.create(false, false, containerScreen, display);
                    boolean successful = matchesLegacyRequirements(menu, context, display);
                    
                    if (!successful) {
                        for (TransferHandler handler : TransferHandlerRegistry.getInstance()) {
                            if (!(handler instanceof DefaultCategoryHandler) && handler.checkApplicable(transferContext).isSuccessful()) {
                                if (handler.handle(transferContext).isSuccessful()) {
                                    successful = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (!successful) continue;
                    
                    display.getOutputEntries().stream().flatMap(Collection::stream)
                            .collect(Collectors.toCollection(() -> craftables));
                } catch (Throwable t) {
                    InternalLogger.getInstance().warn("Error while checking if display is craftable", t);
                }
            }
        }
        return craftables;
    }
    
    private static MenuSerializationContext<AbstractContainerMenu, LocalPlayer, Display> createLegacyContext(AbstractContainerMenu menu, CategoryIdentifier<Display> categoryIdentifier) {
        class InfoSerializationContext implements MenuSerializationContext<AbstractContainerMenu, LocalPlayer, Display> {
            @Override
            public AbstractContainerMenu getMenu() {
                return menu;
            }
            
            @Override
            public LocalPlayer getPlayerEntity() {
                return Minecraft.getInstance().player;
            }
            
            @Override
            public CategoryIdentifier<Display> getCategoryIdentifier() {
                return categoryIdentifier;
            }
        }
        
        return new InfoSerializationContext();
    }
    
    private static boolean matchesLegacyRequirements(AbstractContainerMenu menu,
                                                     MenuSerializationContext<AbstractContainerMenu, LocalPlayer, Display> context,
                                                     Display display) {
        MenuInfo<AbstractContainerMenu, Display> info = menu != null ?
                MenuInfoRegistry.getInstance().getClient(display, context, menu)
                : null;
        
        if (menu != null && info == null) {
            return false;
        }
        
        Iterable<SlotAccessor> inputSlots = info != null ? Iterables.concat(info.getInputSlots(context.withDisplay(display)), info.getInventorySlots(context.withDisplay(display))) : Collections.emptySet();
        int slotsCraftable = 0;
        boolean containsNonEmpty = false;
        List<EntryIngredient> requiredInput = display.getRequiredEntries();
        Long2LongMap invCount = new Long2LongOpenHashMap(info == null ? CraftableFilter.INSTANCE.getInvStacks() : Long2LongMaps.EMPTY_MAP);
        for (SlotAccessor inputSlot : inputSlots) {
            ItemStack stack = inputSlot.getItemStack();
            
            EntryDefinition<ItemStack> definition;
            try {
                definition = VanillaEntryTypes.ITEM.getDefinition();
            } catch (NullPointerException e) {
                break;
            }
            
            if (!stack.isEmpty()) {
                long hash = definition.hash(null, stack, ComparisonContext.FUZZY);
                long newCount = invCount.get(hash) + Math.max(0, stack.getCount());
                invCount.put(hash, newCount);
            }
        }
        
        for (EntryIngredient slot : requiredInput) {
            if (slot.isEmpty()) {
                slotsCraftable++;
                continue;
            }
            for (EntryStack<?> slotPossible : slot) {
                if (slotPossible.getType() != VanillaEntryTypes.ITEM) continue;
                ItemStack stack = slotPossible.castValue();
                long hashFuzzy = EntryStacks.hashFuzzy(slotPossible);
                long availableAmount = invCount.get(hashFuzzy);
                if (availableAmount >= stack.getCount()) {
                    invCount.put(hashFuzzy, availableAmount - stack.getCount());
                    containsNonEmpty = true;
                    slotsCraftable++;
                    break;
                }
            }
        }
        
        return slotsCraftable == display.getRequiredEntries().size() && containsNonEmpty;
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
}
