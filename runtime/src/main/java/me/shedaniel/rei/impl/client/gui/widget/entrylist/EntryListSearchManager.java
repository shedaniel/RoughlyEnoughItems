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

package me.shedaniel.rei.impl.client.gui.widget.entrylist;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.EntryPanelOrdering;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.view.Views;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.search.AsyncSearchManager;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsedStack;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsibleEntryRegistryImpl;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class EntryListSearchManager {
    private static final Comparator<? super EntryStack<?>> ENTRY_NAME_COMPARER = Comparator.comparing(stack -> stack.asFormatStrippedText().getString());
    // private static final Comparator<? super EntryStack<?>> ENTRY_GROUP_COMPARER = VersionAdapter.INSTANCE.getEntryGroupComparator();
    
    public static final EntryListSearchManager INSTANCE = new EntryListSearchManager();
    
    private AsyncSearchManager searchManager = new AsyncSearchManager(EntryRegistry.getInstance()::getPreFilteredList, () -> {
        boolean checkCraftable = ConfigManager.getInstance().isCraftableOnlyEnabled();
        LongSet workingItems = checkCraftable ? new LongOpenHashSet() : null;
        if (checkCraftable) {
            for (EntryStack<?> stack : Views.getInstance().findCraftableEntriesByMaterials()) {
                workingItems.add(EntryStacks.hashExact(stack));
            }
        }
        return checkCraftable ? stack -> workingItems.contains(EntryStacks.hashExact(stack)) : stack -> true;
    }, EntryStack::normalize);
    
    public void update(String searchTerm, boolean ignoreLastSearch, Consumer<List</*EntryStack<?> | CollapsedStack*/ Object>> update) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (ignoreLastSearch) searchManager.markDirty();
        searchManager.updateFilter(searchTerm);
        if (searchManager.isDirty()) {
            searchManager.getAsync((list, filter) -> {
                if (!filter.getFilter().equals(searchTerm)) return;
                if (searchManager.filter() == null || searchManager.filter() != filter) return;
                InternalLogger.getInstance().log(ConfigObject.getInstance().doDebugSearchTimeRequired() ? Level.INFO : Level.TRACE, "Search \"%s\" Used [%s]: %s", filter.getFilter(), Thread.currentThread().toString(), stopwatch.toString());
                List</*EntryStack<?> | CollapsedStack*/ Object> finalList = collapse(copyAndOrder(list), () -> searchManager.filter() != null && searchManager.filter() == filter);
                
                InternalLogger.getInstance().log(ConfigObject.getInstance().doDebugSearchTimeRequired() ? Level.INFO : Level.TRACE, "Search \"%s\" Used and Applied [%s]: %s", filter.getFilter(), Thread.currentThread().toString(), stopwatch.stop().toString());
                
                Minecraft.getInstance().submit(() -> {
                    if (searchManager.filter() == null || searchManager.filter() != filter) return;
                    update.accept(finalList);
                });
            });
        }
    }
    
    private List<EntryStack<?>> copyAndOrder(List<EntryStack<?>> list) {
        list = new ArrayList<>(list);
        EntryPanelOrdering ordering = ConfigObject.getInstance().getItemListOrdering();
        if (ordering == EntryPanelOrdering.NAME)
            list.sort(ENTRY_NAME_COMPARER);
        // if (ordering == EntryPanelOrdering.GROUPS)
            // list.sort(ENTRY_GROUP_COMPARER);
        if (!ConfigObject.getInstance().isItemListAscending()) {
            Collections.reverse(list);
        }
        
        return list;
    }
    
    private List</*EntryStack<?> | CollapsedStack*/ Object> collapse(List<EntryStack<?>> stacks, BooleanSupplier isValid) {
        CollapsibleEntryRegistryImpl collapsibleRegistry = (CollapsibleEntryRegistryImpl) CollapsibleEntryRegistry.getInstance();
        Map<CollapsibleEntryRegistryImpl.Entry, @Nullable CollapsedStack> entries = new HashMap<>();
        
        for (CollapsibleEntryRegistryImpl.Entry entry : collapsibleRegistry.getEntries()) {
            entries.put(entry, null);
        }
        
        if (!isValid.getAsBoolean()) return List.of();
        
        List</*EntryStack<?> | CollapsedStack*/ Object> list = new ArrayList<>();
        
        int i = 0;
        
        for (EntryStack<?> stack : stacks) {
            long hashExact = EntryStacks.hashExact(stack);
            boolean matchedAny = false;
            
            for (Map.Entry<CollapsibleEntryRegistryImpl.Entry, @Nullable CollapsedStack> mapEntry : entries.entrySet()) {
                CollapsibleEntryRegistryImpl.Entry entry = mapEntry.getKey();
                
                if (entry.getMatcher().matches(stack, hashExact)) {
                    CollapsedStack collapsed = mapEntry.getValue();
                    
                    if (collapsed == null) {
                        List<EntryStack<?>> ingredient = new ArrayList<>();
                        ingredient.add(stack);
                        collapsed = new CollapsedStack(ingredient, entry);
                        mapEntry.setValue(collapsed);
                        list.add(collapsed);
                    } else {
                        collapsed.getIngredient().add(stack);
                    }
                    
                    matchedAny = true;
                }
            }
            
            if (i++ % 50 == 0 && !isValid.getAsBoolean()) return List.of();
            
            if (!matchedAny) {
                list.add(stack);
            }
        }
        
        return list;
    }
    
    public boolean matches(EntryStack<?> stack) {
        return searchManager.matches(stack);
    }
}
