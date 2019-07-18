/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.GuiButtonRecipeTab;

import java.util.List;

public interface RecipeBookGuiHooks {
    
    GhostRecipe rei_getGhostSlots();
    
    GuiTextField rei_getSearchField();
    
    List<GuiButtonRecipeTab> rei_getTabButtons();
    
}
