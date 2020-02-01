/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.config;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
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
