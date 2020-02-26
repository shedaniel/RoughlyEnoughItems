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

package me.shedaniel.rei.gui.config;

import net.minecraft.client.resource.language.I18n;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum ItemListOrderingConfig {
    REGISTRY_ASCENDING(ItemListOrdering.registry, true),
    NAME_ASCENDING(ItemListOrdering.name, true),
    GROUPS_ASCENDING(ItemListOrdering.item_groups, true),
    REGISTRY_DESCENDING(ItemListOrdering.registry, false),
    NAME_DESCENDING(ItemListOrdering.name, false),
    GROUPS_DESCENDING(ItemListOrdering.item_groups, false);
    
    private ItemListOrdering ordering;
    private boolean isAscending;
    
    ItemListOrderingConfig(ItemListOrdering ordering, boolean isAscending) {
        this.ordering = ordering;
        this.isAscending = isAscending;
    }
    
    public static ItemListOrderingConfig from(ItemListOrdering ordering, boolean isAscending) {
        int index = ordering.ordinal() + (isAscending ? 0 : 3);
        return values()[index];
    }
    
    public ItemListOrdering getOrdering() {
        return ordering;
    }
    
    public boolean isAscending() {
        return isAscending;
    }
    
    @Override
    public String toString() {
        return I18n.translate("config.roughlyenoughitems.list_ordering_button", I18n.translate(getOrdering().getNameTranslationKey()), I18n.translate(isAscending ? "ordering.rei.ascending" : "ordering.rei.descending"));
    }
}
