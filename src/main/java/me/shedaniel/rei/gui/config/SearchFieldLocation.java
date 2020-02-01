/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.config;

import net.minecraft.client.resource.language.I18n;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

@ApiStatus.Internal
public enum SearchFieldLocation {
    CENTER,
    BOTTOM_SIDE,
    TOP_SIDE;
    
    @Override
    public String toString() {
        return I18n.translate("config.roughlyenoughitems.searchFieldLocation.%s", name().toLowerCase(Locale.ROOT));
    }
}
