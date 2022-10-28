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
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextType;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringResultImpl;
import me.shedaniel.rei.impl.common.InternalLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FilteredStacksVisibilityHandler implements DisplayVisibilityPredicate {
    private boolean checkHiddenStacks;
    private Reference2BooleanMap<Display> visible = Reference2BooleanMaps.synchronize(new Reference2BooleanOpenHashMap<>());
    private List<FilteringRule<?>> filteringRules;
    private Map<FilteringRule<?>, Object> cache = new HashMap<>();
    private final Predicate<Display> displayPredicate = this::checkHiddenStacks;
    
    @Override
    public EventResult handleDisplay(DisplayCategory<?> category, Display display) {
        if (checkHiddenStacks) {
            return visible.computeBooleanIfAbsent(display, displayPredicate) ? EventResult.pass() : EventResult.interruptFalse();
        }
        
        return EventResult.pass();
    }
    
    public void reset() {
        checkHiddenStacks = ConfigObject.getInstance().shouldFilterDisplays();
        visible = Reference2BooleanMaps.synchronize(new Reference2BooleanOpenHashMap<>());
        
        if (checkHiddenStacks) {
            filteringRules = ((ConfigObjectImpl) ConfigObject.getInstance()).getFilteringRules();
            cache = new HashMap<>();
            for (int i = filteringRules.size() - 1; i >= 0; i--) {
                FilteringRule<?> rule = filteringRules.get(i);
                cache.put(rule, rule.prepareCache(false));
            }
            
            cacheExisting();
        } else {
            filteringRules = null;
            cache = null;
        }
    }
    
    public void cacheExisting() {
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
    
    private boolean checkHiddenStacks(Display display) {
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
    
    private static boolean isEntryIngredientAllHidden(EntryIngredient ingredient, Map<FilteringRule<?>, Object> cache, List<FilteringRule<?>> rules) {
        FilteringContextImpl context = new FilteringContextImpl(false, ingredient);
        for (int i = rules.size() - 1; i >= 0; i--) {
            FilteringRule<?> rule = rules.get(i);
            context.handleResult((FilteringResultImpl) ((FilteringRule<Object>) rule).processFilteredStacks(context,
                    () -> new FilteringResultImpl(new ArrayList<>(), new ArrayList<>()),
                    cache.get(rule), false));
        }
        return context.stacks.get(FilteringContextType.SHOWN).isEmpty() && context.stacks.get(FilteringContextType.DEFAULT).isEmpty();
    }
}
