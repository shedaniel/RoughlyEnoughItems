package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.IMixinRecipeBookGui;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiRecipeBook.class)
public class MixinGuiRecipeBook implements IMixinRecipeBookGui {
    
    @Shadow @Final protected GhostRecipe ghostRecipe;
    
    @Override
    public GhostRecipe getGhostRecipe() {
        return ghostRecipe;
    }
    
}
