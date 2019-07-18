/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.config;

import net.minecraft.client.resources.I18n;

import java.util.Locale;

public enum RecipeScreenType {
    UNSET,
    ORIGINAL,
    VILLAGER;
    
    @Override
    public String toString() {
        return I18n.format("text.rei.config.recipe_screen_type." + name().toLowerCase(Locale.ROOT));
    }
}
