package me.shedaniel.rei.api.widgets;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;

import java.util.Collection;
import java.util.List;

public abstract class Slot extends WidgetWithBounds {
    public Slot unmarkInputOrOutput() {
        setNoticeMark((byte) 0);
        return this;
    }
    
    public final Slot markInput() {
        setNoticeMark((byte) 1);
        return this;
    }
    
    public final Slot markOutput() {
        setNoticeMark((byte) 2);
        return this;
    }
    
    public abstract void setNoticeMark(byte mark);
    
    public abstract byte getNoticeMark();
    
    public abstract void setInteractable(boolean interactable);
    
    public abstract boolean isInteractable();
    
    public Slot interactable(boolean interactable) {
        setInteractable(interactable);
        return this;
    }
    
    public Slot noInteractable() {
        return interactable(false);
    }
    
    public abstract void setInteractableFavorites(boolean interactableFavorites);
    
    public abstract boolean isInteractableFavorites();
    
    public Slot interactableFavorites(boolean interactableFavorites) {
        setInteractableFavorites(interactableFavorites);
        return this;
    }
    
    public Slot noFavoritesInteractable() {
        return interactableFavorites(false);
    }
    
    public abstract void setHighlightEnabled(boolean highlights);
    
    public abstract boolean isHighlightEnabled();
    
    public final Slot highlightEnabled(boolean highlight) {
        setHighlightEnabled(highlight);
        return this;
    }
    
    public final Slot disableHighlight() {
        return highlightEnabled(false);
    }
    
    public abstract void setTooltipsEnabled(boolean tooltipsEnabled);
    
    public abstract boolean isTooltipsEnabled();
    
    public final Slot tooltipsEnabled(boolean tooltipsEnabled) {
        setTooltipsEnabled(tooltipsEnabled);
        return this;
    }
    
    public final Slot disableTooltips() {
        return tooltipsEnabled(false);
    }
    
    public abstract void setBackgroundEnabled(boolean backgroundEnabled);
    
    public abstract boolean isBackgroundEnabled();
    
    public final Slot backgroundEnabled(boolean backgroundEnabled) {
        setBackgroundEnabled(backgroundEnabled);
        return this;
    }
    
    public final Slot disableBackground() {
        return backgroundEnabled(false);
    }
    
    public abstract Slot clearEntries();
    
    public abstract Slot entry(EntryStack stack);
    
    public abstract Slot entries(Collection<EntryStack> stacks);
    
    public abstract List<EntryStack> getEntries();
    
    public abstract QueuedTooltip getCurrentTooltip(int mouseX, int mouseY);
}
