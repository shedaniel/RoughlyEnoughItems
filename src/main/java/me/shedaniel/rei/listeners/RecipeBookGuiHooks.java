package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.widget.RecipeBookGhostSlots;
import net.minecraft.client.gui.widget.TextFieldWidget;

public interface RecipeBookGuiHooks {
    
    RecipeBookGhostSlots rei_getGhostSlots();
    
    TextFieldWidget rei_getSearchField();
    
}
