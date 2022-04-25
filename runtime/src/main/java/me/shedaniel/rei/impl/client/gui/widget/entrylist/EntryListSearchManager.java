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

package me.shedaniel.rei.impl.client.gui.widget.entrylist;

import com.google.common.base.Stopwatch;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.EntryPanelOrdering;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.impl.client.search.AsyncSearchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class EntryListSearchManager {
    private static final Comparator<? super EntryStack<?>> ENTRY_NAME_COMPARER = Comparator.comparing(stack -> stack.asFormatStrippedText().getString());
    private static final Comparator<? super EntryStack<?>> ENTRY_GROUP_COMPARER = Comparator.comparingInt(stack -> {
        if (stack.getType() == VanillaEntryTypes.ITEM) {
            CreativeModeTab group = ((ItemStack) stack.getValue()).getItem().getItemCategory();
            if (group != null)
                return group.getId();
        }
        return Integer.MAX_VALUE;
    });
    
    public static final EntryListSearchManager INSTANCE = new EntryListSearchManager();
    
    private AsyncSearchManager searchManager = AsyncSearchManager.createDefault();
    
    public void update(String searchTerm, boolean ignoreLastSearch, Consumer<List<EntryStack<?>>> update) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (ignoreLastSearch) searchManager.markDirty();
        searchManager.updateFilter(searchTerm);
        if (searchManager.isDirty()) {
            searchManager.getAsync(list -> {
                List<EntryStack<?>> finalList = copyAndOrder(list);
                
                if (ConfigObject.getInstance().doDebugSearchTimeRequired()) {
                    RoughlyEnoughItemsCore.LOGGER.info("Search Used: %s", stopwatch.stop().toString());
                }
                
                Minecraft.getInstance().executeBlocking(() -> {
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
        if (ordering == EntryPanelOrdering.GROUPS)
            list.sort(ENTRY_GROUP_COMPARER);
        if (!ConfigObject.getInstance().isItemListAscending()) {
            Collections.reverse(list);
        }
        
        return list;
    }
    
    public boolean matches(EntryStack<?> stack) {
        return searchManager.matches(stack);
    }
}
