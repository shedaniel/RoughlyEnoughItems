package me.shedaniel.rei.client;

import net.minecraft.client.resource.language.I18n;

import java.util.Locale;

public enum RecipeScreenType {
    UNSET,
    ORIGINAL,
    VILLAGER;
    
    @Override
    public String toString() {
        return I18n.translate("text.rei.config.recipe_screen_type." + name().toLowerCase(Locale.ROOT));
    }
}
