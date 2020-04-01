package me.shedaniel.rei.gui.subsets;

import me.shedaniel.rei.gui.widget.Widget;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@ApiStatus.Internal
public abstract class SubsetsMenuEntry extends Widget {
    @Deprecated
    SubsetsMenu parent = null;
    
    public final SubsetsMenu getParent() {
        return parent;
    }
    
    public abstract int getEntryWidth();
    
    public abstract int getEntryHeight();
    
    public abstract void updateInformation(int xPos, int yPos, boolean selected, boolean containsMouse, boolean rendering, int width);
}
