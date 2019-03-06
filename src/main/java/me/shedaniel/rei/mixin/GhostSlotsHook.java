package me.shedaniel.rei.mixin;

import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.RecipeBookGhostSlots;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookGui.class)
public interface GhostSlotsHook {
    
    @Accessor("ghostSlots")
    RecipeBookGhostSlots rei_getGhostSlots();
    
}
