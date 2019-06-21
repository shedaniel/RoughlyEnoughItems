package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.widget.RecipeBookButtonWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookButtonWidget.class)
public interface RecipeBookButtonWidgetHooks {
    @Accessor("texture")
    Identifier rei_getTexture();
}
