package me.shedaniel.mixins;

import me.shedaniel.Core;
import me.shedaniel.listenerdefinitions.MinecraftResize;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by James on 7/28/2018.
 */
@Mixin(Window.class)
public abstract class MixinMinecraftResize implements AutoCloseable {
    
    @Shadow
    private int scaledHeight;
    
    @Shadow
    private int scaledWidth;
    
    @Inject(method = "onSizeChanged", at = @At("RETURN"))
    private void onSizeChanged(long long_1, int int_1, int int_2, CallbackInfo ci) {
        for(MinecraftResize listener : Core.getListeners(MinecraftResize.class)) {
            listener.resize(this.scaledWidth, this.scaledHeight);
        }
    }
    
}
