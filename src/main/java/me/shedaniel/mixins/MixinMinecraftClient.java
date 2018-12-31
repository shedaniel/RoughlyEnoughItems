package me.shedaniel.mixins;

import me.shedaniel.Core;
import me.shedaniel.listenerdefinitions.ClientTickable;
import me.shedaniel.listenerdefinitions.KeybindHandler;
import net.minecraft.class_3689;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    
    @Shadow
    @Final
    private class_3689 profiler;
    
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        profiler.begin("mods");
        for(ClientTickable tickable : Core.getListeners(ClientTickable.class)) {
            profiler.begin(() -> tickable.getClass().getCanonicalName().replace('.', '/'));
            tickable.clientTick();
            profiler.end();
        }
        profiler.end();
    }
    
    @Inject(method = "method_1508", at = @At("HEAD"))
    public void onProcessKeyBinds(CallbackInfo ci) {
        for (KeybindHandler keybindHandler : Core.getListeners(KeybindHandler.class)) {
            keybindHandler.processKeybinds();
        }
    }
    
}
