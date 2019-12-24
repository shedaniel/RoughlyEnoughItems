/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.config;

import net.minecraft.client.resource.language.I18n;

import java.util.Locale;

public enum SearchFieldLocation {
    CENTER,
    BOTTOM_SIDE,
    TOP_SIDE;

    @Override
    public String toString() {
        return I18n.translate("config.roughlyenoughitems.searchFieldLocation.%s", name().toLowerCase(Locale.ROOT));
    }
}
