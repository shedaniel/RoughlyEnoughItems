package me.shedaniel.rei.forge;

import me.shedaniel.rei.gui.plugin.DefaultRuntimePlugin;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.DefaultServerContainerPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static me.shedaniel.rei.RoughlyEnoughItemsCore.registerPlugin;

public class PluginDetectorImpl {
    public static void detectServerPlugins() {
        new DefaultServerContainerPlugin().run();
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void detectClientPlugins() {
        registerPlugin(new DefaultPlugin());
        registerPlugin(new DefaultRuntimePlugin());
    }
}
