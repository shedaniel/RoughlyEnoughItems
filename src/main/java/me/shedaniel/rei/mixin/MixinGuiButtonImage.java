package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.RecipeBookButtonWidgetHooks;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiButtonImage.class)
public class MixinGuiButtonImage implements RecipeBookButtonWidgetHooks {
    
    @Shadow @Final private ResourceLocation resourceLocation;
    
    @Override
    public ResourceLocation rei_getTexture() {
        return resourceLocation;
    }
    
}
