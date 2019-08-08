/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.config;

import net.minecraft.client.resource.language.I18n;

import java.util.Locale;

public enum RecipeScreenType {
    UNSET,
    ORIGINAL,
    VILLAGER;
    
    @Override
    public String toString() {
        return I18n.translate("config.roughlyenoughitems.recipeScreenType." + name().toLowerCase(Locale.ROOT));
    }
}
