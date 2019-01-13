package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.IMixinRecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.RecipeBookGhostSlots;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RecipeBookGui.class)
public class MixinRecipeBookGui implements IMixinRecipeBookGui {
    
    @Shadow @Final protected RecipeBookGhostSlots ghostSlots;
    
    @Override
    public RecipeBookGhostSlots getGhostSlots() {
        return ghostSlots;
    }
    
}
