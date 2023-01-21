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

package me.shedaniel.rei.api.client.gui.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

@Environment(EnvType.CLIENT)
public enum EntryPanelOrderingConfig {
    REGISTRY_ASCENDING(EntryPanelOrdering.REGISTRY, true),
    NAME_ASCENDING(EntryPanelOrdering.NAME, true),
    GROUPS_ASCENDING(EntryPanelOrdering.GROUPS, true),
    REGISTRY_DESCENDING(EntryPanelOrdering.REGISTRY, false),
    NAME_DESCENDING(EntryPanelOrdering.NAME, false),
    GROUPS_DESCENDING(EntryPanelOrdering.GROUPS, false);
    
    private EntryPanelOrdering ordering;
    private boolean isAscending;
    
    EntryPanelOrderingConfig(EntryPanelOrdering ordering, boolean isAscending) {
        this.ordering = ordering;
        this.isAscending = isAscending;
    }
    
    public static EntryPanelOrderingConfig from(EntryPanelOrdering ordering, boolean isAscending) {
        int index = ordering.ordinal() + (isAscending ? 0 : 3);
        return values()[index];
    }
    
    public EntryPanelOrdering getOrdering() {
        return ordering;
    }
    
    public boolean isAscending() {
        return isAscending;
    }
    
    @Override
    public String toString() {
        return I18n.get("config.roughlyenoughitems.list_ordering_button", I18n.get(getOrdering().getNameTranslationKey()), I18n.get(isAscending ? "ordering.rei.ascending" : "ordering.rei.descending"));
    }
}
