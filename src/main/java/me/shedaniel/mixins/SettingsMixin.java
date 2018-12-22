package me.shedaniel.mixins;

import me.shedaniel.listenerdefinitions.PreLoadOptions;
import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 8/7/2018.
 */

@Mixin(GameSettings.class)
public class SettingsMixin {
    @Shadow
    public KeyBinding[] keyBindings;
    
    public SettingsMixin() {
        System.out.println("loaded");
    }
    
    @Inject(method = "loadOptions", at = @At("HEAD"))
    public void beforeLoadOptions(CallbackInfo ci) {
        
        RiftLoader.instance.getListeners(PreLoadOptions.class).stream().forEach(f -> processNewBindings(f.loadOptions()));
    }
    
    private void processNewBindings(List<KeyBinding> newBindings) {
        List<KeyBinding> toAdd = new ArrayList<>();
        toAdd.addAll(newBindings);
        for(KeyBinding keyBinding : keyBindings) {
            toAdd.add(keyBinding);
        }
        keyBindings = (KeyBinding[]) toAdd.toArray(new KeyBinding[0]);
    }
    
}
