package me.shedaniel.rei;

import net.minecraft.client.Minecraft;
import org.dimdev.rift.listener.client.ClientTickable;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public class REIMixinInit implements InitializationListener {
    
    @Override
    public void onInitialization() {
        MixinBootstrap.init();
        Mixins.addConfiguration("roughlyenoughitems.client.json");
    }
    
}
