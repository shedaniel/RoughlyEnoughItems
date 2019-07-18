package me.shedaniel.rei.api;

import net.minecraft.client.gui.IGuiEventListener;

public interface GuiEventHandlerHooks {
    
    void rei_setFocused(IGuiEventListener listener);
    
    void rei_setDragging(boolean dragging);
    
}
