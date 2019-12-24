/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RecipeBookWidget.class)
public interface RecipeBookGuiHooks {

    @Accessor("ghostSlots")
    RecipeBookGhostSlots rei_getGhostSlots();

    @Accessor("searchField")
    TextFieldWidget rei_getSearchField();

    @Accessor("tabButtons")
    List<RecipeGroupButtonWidget> rei_getTabButtons();

}
