package me.shedaniel.rei.api.client.gui.config;

import net.minecraft.client.resources.language.I18n;

public enum CheatingMode {
    OFF,
    ON,
    WHEN_CREATIVE,
    ;
    
    @Override
    public String toString() {
        switch (this) {
            case ON:
                return I18n.get("text.cloth-config.boolean.value.true");
            case OFF:
                return I18n.get("text.cloth-config.boolean.value.false");
            case WHEN_CREATIVE:
                return I18n.get("config.roughlyenoughitems.cheating.when_creative");
            default:
                throw new IllegalStateException("Unknown CheatingMode: " + this);
        }
    }
}
