package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.GhostSlotsHooks;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.RecipeBookGhostSlots;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RecipeBookGui.class)
public class MixinRecipeBookGui implements GhostSlotsHooks {
    
    @Shadow
    @Final
    protected RecipeBookGhostSlots ghostSlots;
    
    public RecipeBookGhostSlots rei_getGhostSlots() {
        return ghostSlots;
    }
    
}
