/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.config;

import net.minecraft.client.resource.language.I18n;

import java.util.Locale;

public enum ItemCheatingMode {
    REI_LIKE,
    JEI_LIKE;

    @Override
    public String toString() {
        return I18n.translate("config.roughlyenoughitems.itemCheatingMode." + name().toLowerCase(Locale.ROOT));
    }
}
