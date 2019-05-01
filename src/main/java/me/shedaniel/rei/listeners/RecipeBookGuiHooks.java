package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.recipebook.GroupButtonWidget;
import net.minecraft.client.gui.widget.RecipeBookGhostSlots;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.List;

public interface RecipeBookGuiHooks {
    
    RecipeBookGhostSlots rei_getGhostSlots();
    
    TextFieldWidget rei_getSearchField();
    
    List<GroupButtonWidget> rei_getTabButtons();
    
}
