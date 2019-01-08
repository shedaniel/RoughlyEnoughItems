package me.shedaniel.mixins;

import me.shedaniel.Core;
import me.shedaniel.listenerdefinitions.CharInput;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by James on 8/4/2018.
 */
@Mixin(Keyboard.class)
public class MixinKeyboardListener {
    
    @Inject(method = "onChar", at = @At("RETURN"), cancellable = true)
    private void onCharEvent(long p_onCharEvent_1_, int p_onCharEvent_3_, int p_onCharEvent_4_, CallbackInfo ci) {
        boolean handled = false;
        for(CharInput listener : Core.getListeners(CharInput.class)) {
            if (listener.charInput(p_onCharEvent_1_, p_onCharEvent_3_, p_onCharEvent_4_)) {
                handled = true;
            }
        }
        if (handled)
            ci.cancel();
    }
    
}
