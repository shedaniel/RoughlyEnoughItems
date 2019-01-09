package me.shedaniel.mixins;

import me.shedaniel.listenerdefinitions.CharInput;
import net.minecraft.client.KeyboardListener;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by James on 8/4/2018.
 */
@Mixin(KeyboardListener.class)
public class MixinKeyboardListener {
    
    @Inject(method = "onCharEvent", at = @At("RETURN"), cancellable = true)
    private void onCharEvent(long p_onCharEvent_1_, int p_onCharEvent_3_, int p_onCharEvent_4_, CallbackInfo ci) {
        for(CharInput listener : RiftLoader.instance.getListeners(CharInput.class))
            if (listener.charInput(p_onCharEvent_1_, p_onCharEvent_3_, p_onCharEvent_4_)) {
                ci.cancel();
                break;
            }
    }
    
}
