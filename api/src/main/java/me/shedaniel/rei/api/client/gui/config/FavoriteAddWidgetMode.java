package me.shedaniel.rei.api.client.gui.config;

import net.minecraft.client.resources.language.I18n;

import java.util.Locale;

public enum FavoriteAddWidgetMode {
    ALWAYS_INVISIBLE,
    AUTO_HIDE,
    ALWAYS_VISIBLE,
    ;
    
    @Override
    public String toString() {
        return I18n.get("config.roughlyenoughitems.layout.favoriteAddWidgetMode." + name().toLowerCase(Locale.ROOT));
    }
}
