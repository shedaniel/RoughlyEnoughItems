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

package me.shedaniel.rei.impl.view;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.LiveDisplayGenerator;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.util.EntryIngredients;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.api.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.util.CollectionUtils;
import me.shedaniel.rei.api.view.ViewSearchBuilder;
import me.shedaniel.rei.api.view.Views;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class ViewsImpl implements Views {
    @Override
    public Map<DisplayCategory<?>, List<Display>> buildMapFor(ViewSearchBuilder builder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Set<ResourceLocation> categories = builder.getCategories();
        List<EntryStack<?>> recipesFor = builder.getRecipesFor();
        List<EntryStack<?>> usagesFor = builder.getUsagesFor();
        
        Map<DisplayCategory<?>, List<Display>> result = Maps.newLinkedHashMap();
        for (CategoryRegistry.CategoryConfiguration<?> categoryConfiguration : CategoryRegistry.getInstance()) {
            DisplayCategory<?> category = categoryConfiguration.getCategory();
            ResourceLocation categoryId = categoryConfiguration.getIdentifier();
            List<Display> allRecipesFromCategory = DisplayRegistry.getInstance().getDisplays(categoryId);
            
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
                if (isStackWorkStationOfCategory(categoryConfiguration, stack)) {
                    set.addAll(CollectionUtils.filterToSet(allRecipesFromCategory, this::isDisplayVisible));
                    break;
                }
            }
            if (!set.isEmpty()) {
                CollectionUtils.getOrPutEmptyList(result, category).addAll(set);
            }
        }
        
        int generatorsCount = 0;
        
        for (Map.Entry<ResourceLocation, List<LiveDisplayGenerator<?>>> entry : DisplayRegistry.getInstance().getCategoryDisplayGenerators().entrySet()) {
            ResourceLocation categoryId = entry.getKey();
            Set<Display> set = new LinkedHashSet<>();
            generatorsCount += entry.getValue().size();
            
            for (LiveDisplayGenerator<Display> generator : (List<LiveDisplayGenerator<Display>>) (List) entry.getValue()) {
                generateLiveDisplays(generator, builder, set::add);
            }
            
            if (!set.isEmpty()) {
                CollectionUtils.getOrPutEmptyList(result, CategoryRegistry.getInstance().get(categoryId).getCategory()).addAll(set);
            }
        }
        
        Consumer<Display> displayConsumer = display -> {
            CollectionUtils.getOrPutEmptyList(result, CategoryRegistry.getInstance().get(display.getCategoryIdentifier()).getCategory()).add(display);
        };
        for (LiveDisplayGenerator<Display> generator : (List<LiveDisplayGenerator<Display>>) (List) DisplayRegistry.getInstance().getGlobalDisplayGenerators()) {
            generatorsCount++;
            generateLiveDisplays(generator, builder, displayConsumer);
        }
        
        String message = String.format("Built Recipe View in %s for %d categories, %d recipes for, %d usages for and %d live recipe generators.",
                stopwatch.stop().toString(), categories.size(), recipesFor.size(), usagesFor.size(), generatorsCount);
        if (ConfigObject.getInstance().doDebugSearchTimeRequired()) {
            RoughlyEnoughItemsCore.LOGGER.info(message);
        } else {
            RoughlyEnoughItemsCore.LOGGER.trace(message);
        }
        return result;
    }
    
    private <T extends Display> void generateLiveDisplays(LiveDisplayGenerator<T> generator, ViewSearchBuilder builder, Consumer<T> displayConsumer) {
        for (EntryStack<?> stack : builder.getRecipesFor()) {
            Optional<List<T>> recipeForDisplays = generator.getRecipeFor(stack);
            if (recipeForDisplays.isPresent()) {
                for (T display : recipeForDisplays.get()) {
                    if (isDisplayVisible(display)) {
                        displayConsumer.accept(display);
                    }
                }
            }
        }
        
        for (EntryStack<?> stack : builder.getUsagesFor()) {
            Optional<List<T>> usageForDisplays = generator.getUsageFor(stack);
            if (usageForDisplays.isPresent()) {
                for (T display : usageForDisplays.get()) {
                    if (isDisplayVisible(display)) {
                        displayConsumer.accept(display);
                    }
                }
            }
        }
        
        Optional<List<T>> displaysGenerated = generator.generate(builder);
        if (displaysGenerated.isPresent()) {
            for (T display : displaysGenerated.get()) {
                if (isDisplayVisible(display)) {
                    displayConsumer.accept(display);
                }
            }
        }
    }
    
    @Override
    public Collection<EntryStack<?>> findCraftableEntriesByItems(Iterable<? extends EntryStack<?>> inventoryItems) {
        Set<EntryStack<?>> craftables = new HashSet<>();
        for (List<Display> displays : DisplayRegistry.getInstance().getAllDisplays().values())
            for (Display display : displays) {
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
        return craftables;
    }
    
    private <T> boolean isStackWorkStationOfCategory(CategoryRegistry.CategoryConfiguration<?> category, EntryStack<T> stack) {
        for (EntryIngredient ingredient : category.getWorkstations()) {
            if (EntryIngredients.testFuzzy(ingredient, stack)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isDisplayVisible(Display display) {
        return DisplayRegistry.getInstance().isDisplayVisible(display);
    }
}
