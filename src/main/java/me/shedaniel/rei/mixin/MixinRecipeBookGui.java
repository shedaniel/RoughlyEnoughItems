package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.RecipeBookGhostSlots;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RecipeBookGui.class)
public class MixinRecipeBookGui implements RecipeBookGuiHooks {
    
    @Shadow
    @Final
    protected RecipeBookGhostSlots ghostSlots;

    @Shadow
    private TextFieldWidget searchField;
    
    public RecipeBookGhostSlots rei_getGhostSlots() {
        return ghostSlots;
    }

    public TextFieldWidget rei_getRecipeBookSearchField() {
        return searchField;
    }
    
}
