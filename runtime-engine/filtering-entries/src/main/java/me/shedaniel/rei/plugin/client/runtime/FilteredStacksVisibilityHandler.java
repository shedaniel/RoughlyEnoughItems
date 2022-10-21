/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.plugin.client.runtime;

import com.google.common.base.Stopwatch;
import dev.architectury.event.EventResult;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerInternal;
import me.shedaniel.rei.impl.client.entry.filtering.*;
import me.shedaniel.rei.impl.client.entry.filtering.rules.ManualFilteringRule;
import me.shedaniel.rei.impl.client.entry.type.EntryRegistryListener;
import me.shedaniel.rei.impl.common.InternalLogger;

import java.util.List;
import java.util.function.Predicate;

public class FilteredStacksVisibilityHandler implements REIClientPlugin, EntryRegistryListener {
    private static boolean checkHiddenStacks;
    private static Reference2BooleanMap<Display> visible = Reference2BooleanMaps.synchronize(new Reference2BooleanOpenHashMap<>());
    private static List<FilteringRule> filteringRules;
    private static FilteringCacheImpl cache;
    private static final Predicate<Display> displayPredicate = FilteredStacksVisibilityHandler::checkHiddenStacks;
    
    public static void reset() {
        checkHiddenStacks = ConfigObject.getInstance().shouldFilterDisplays();
        visible = Reference2BooleanMaps.synchronize(new Reference2BooleanOpenHashMap<>());
        
        if (checkHiddenStacks) {
            filteringRules = CollectionUtils.concatUnmodifiable(
                    List.of(new ManualFilteringRule()),
                    (List<FilteringRule>) ConfigManagerInternal.getInstance().get("advanced.filtering.filteringRules")
            );
            cache = new FilteringCacheImpl();
            for (int i = filteringRules.size() - 1; i >= 0; i--) {
                FilteringRuleInternal rule = (FilteringRuleInternal) filteringRules.get(i);
                cache.setCache(rule, rule.prepareCache(false));
            }
            
            cacheExisting();
        } else {
            filteringRules = null;
            cache = null;
        }
    }
    
    public static void cacheExisting() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        DisplayRegistry.getInstance().getAll().values().parallelStream().map(displays -> {
            Reference2BooleanMap<Display> current = new Reference2BooleanOpenHashMap<>();
            for (Display display : displays) {
                current.put(display, checkHiddenStacks(display));
            }
            return current;
        }).forEach(map -> {
            visible.putAll(map);
        });
        InternalLogger.getInstance().debug("Computed existing filtered displays with %d rules in %s", filteringRules.size(), stopwatch.stop());
    }
    
    private static boolean checkHiddenStacks(Display display) {
        for (EntryIngredient ingredient : display.getInputEntries()) {
            if (!ingredient.isEmpty() && isEntryIngredientAllHidden(ingredient, cache, filteringRules)) {
                return false;
            }
        }
        for (EntryIngredient ingredient : display.getOutputEntries()) {
            if (!ingredient.isEmpty() && isEntryIngredientAllHidden(ingredient, cache, filteringRules)) {
                return false;
            }
        }
        
        return true;
    }
    
    private static boolean isEntryIngredientAllHidden(EntryIngredient ingredient, FilteringCache cache, List<FilteringRule> rules) {
        FilteringContextImpl context = new FilteringContextImpl(false, ingredient);
        for (int i = rules.size() - 1; i >= 0; i--) {
            FilteringRuleInternal rule = (FilteringRuleInternal) rules.get(i);
            context.handleResult(rule.processFilteredStacks(context, cache, false));
        }
        return context.stacks.get(FilteringContextType.SHOWN).isEmpty() && context.stacks.get(FilteringContextType.DEFAULT).isEmpty();
    }
    
    @Override
    public void onReFilter(List<EntryStack<?>> stacks) {
        reset();
    }
    
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        reset();
        registry.registerVisibilityPredicate(new DisplayPredicate());
    }
    
    private class DisplayPredicate implements DisplayVisibilityPredicate {
        @Override
        public EventResult handleDisplay(DisplayCategory<?> category, Display display) {
            if (checkHiddenStacks) {
                return visible.computeIfAbsent(display, displayPredicate) ? EventResult.pass() : EventResult.interruptFalse();
            }
            
            return EventResult.pass();
        }
    }
}
