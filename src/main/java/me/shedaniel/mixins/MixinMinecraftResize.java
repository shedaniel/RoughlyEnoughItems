package me.shedaniel.mixins;

import me.shedaniel.listenerdefinitions.MinecraftResize;
import net.minecraft.client.MainWindow;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by James on 7/28/2018.
 */
@Mixin(MainWindow.class)
public class MixinMinecraftResize {
    @Inject(method = "updateSize", at = @At("RETURN"))
    private void onResize(CallbackInfo ci) {
        for(MinecraftResize listener : RiftLoader.instance.getListeners(MinecraftResize.class)) {
            listener.resize();
        }
    }
    
}
