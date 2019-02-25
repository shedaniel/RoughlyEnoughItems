package me.shedaniel.rei.mixin;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.texture.Texture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* This should not be used by the public, just useful for me, enable in config file */
@Mixin(TextureManager.class)
public abstract class MixinTextureManager {
    
    private boolean rei_already = false;
    
    @Inject(method = "registerTexture", at = @At("HEAD"), cancellable = true)
    private void registerTexture(Identifier identifier_1, Texture texture_1, final CallbackInfoReturnable<Boolean> cir) {
        if (!RoughlyEnoughItemsCore.getConfigHelper().getConfig().fixRamUsage)
            return;
        if (identifier_1.equals(new Identifier("textures/gui/title/mojang.png")))
            if (rei_already)
                cir.setReturnValue(false);
            else
                rei_already = true;
    }
    
}
