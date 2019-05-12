/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

public enum ItemListOrdering {
    
    registry("ordering.rei.registry"),
    name("ordering.rei.name"),
    item_groups("ordering.rei.item_groups");
    
    private String nameTranslationKey;
    
    ItemListOrdering(String nameTranslationKey) {
        this.nameTranslationKey = nameTranslationKey;
    }
    
    public String getNameTranslationKey() {
        return nameTranslationKey;
    }
    
}
