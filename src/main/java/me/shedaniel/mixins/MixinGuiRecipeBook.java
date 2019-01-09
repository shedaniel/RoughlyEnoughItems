package me.shedaniel.mixins;

import me.shedaniel.listenerdefinitions.IMixinGuiRecipeBook;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiRecipeBook.class)
public class MixinGuiRecipeBook implements IMixinGuiRecipeBook {
    
    @Shadow
    @Final
    protected GhostRecipe ghostRecipe;
    
    @Override
    public GhostRecipe getGhostSlots() {
        return ghostRecipe;
    }
    
}
