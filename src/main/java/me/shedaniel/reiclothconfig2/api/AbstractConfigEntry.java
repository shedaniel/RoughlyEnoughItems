package me.shedaniel.reiclothconfig2.api;

import me.shedaniel.reiclothconfig2.gui.ClothConfigScreen;
import me.shedaniel.reiclothconfig2.gui.widget.DynamicElementListWidget;

import java.util.Optional;

public abstract class AbstractConfigEntry<T> extends DynamicElementListWidget.ElementEntry<AbstractConfigEntry<T>> {
    private ClothConfigScreen screen;
    
    public abstract boolean isRequiresRestart();
    
    public abstract void setRequiresRestart(boolean requiresRestart);
    
    public abstract String getFieldName();
    
    public abstract T getValue();
    
    public Optional<String> getError() {
        return Optional.empty();
    }
    
    public abstract Optional<T> getDefaultValue();
    
    public final ClothConfigScreen.ListWidget getParent() {
        return screen.listWidget;
    }
    
    public final ClothConfigScreen getScreen() {
        return screen;
    }
    
    @Deprecated
    public final void setScreen(ClothConfigScreen screen) {
        this.screen = screen;
    }
    
    public abstract void save();
    
    @Override
    public int getItemHeight() {
        return 24;
    }
}
