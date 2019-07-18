/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.config;

import net.minecraft.client.resources.I18n;

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
        return I18n.format("text.rei.config.list_ordering_button", I18n.format(getOrdering().getNameTranslationKey()), I18n.format(isAscending ? "ordering.rei.ascending" : "ordering.rei.descending"));
    }
}
