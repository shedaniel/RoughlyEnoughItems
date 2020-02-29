package me.shedaniel.rei.gui.config;

import net.minecraft.client.resource.language.I18n;

import java.util.Locale;

public enum RecipeBorderType {
    DEFAULT(66),
    LIGHTER(0),
    NONE(0, false);
    
    private int offset;
    private boolean render;
    
    RecipeBorderType(int offset) {
        this(offset, true);
    }
    
    RecipeBorderType(int offset, boolean render) {
        this.offset = offset;
        this.render = render;
    }
    
    public int getYOffset() {
        return offset;
    }
    
    public boolean isRendering() {
        return render;
    }
    
    @Override
    public String toString() {
        return I18n.translate("config.roughlyenoughitems.recipeBorder." + name().toLowerCase(Locale.ROOT));
    }
}
