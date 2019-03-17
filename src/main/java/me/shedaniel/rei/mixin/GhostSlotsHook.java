package me.shedaniel.rei.mixin;

import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRecipeBook.class)
public interface GhostSlotsHook {
    
    @Accessor("ghostRecipe")
    GhostRecipe rei_getGhostRecipe();
    
}
