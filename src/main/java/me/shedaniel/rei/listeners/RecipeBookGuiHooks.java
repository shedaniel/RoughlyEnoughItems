package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.recipebook.GroupButtonWidget;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.RecipeBookGhostSlots;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RecipeBookGui.class)
public interface RecipeBookGuiHooks {
    
    @Accessor("ghostSlots")
    RecipeBookGhostSlots rei_getGhostSlots();
    
    @Accessor("searchField")
    TextFieldWidget rei_getSearchField();
    
    @Accessor("tabButtons")
    List<GroupButtonWidget> rei_getTabButtons();
    
}
