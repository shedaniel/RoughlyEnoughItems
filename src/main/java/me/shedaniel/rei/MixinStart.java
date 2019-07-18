package me.shedaniel.rei;

import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public class MixinStart implements InitializationListener {
    
    @Override
    public void onInitialization() {
        MixinBootstrap.init();
        Mixins.addConfiguration("roughlyenoughitems.client.json");
    }
    
}
