package me.shedaniel.rei;

import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class PluginDetector {
    @ExpectPlatform
    public static void detectServerPlugins() {
        throw new AssertionError();
    }
    
    @Environment(EnvType.CLIENT)
    @ExpectPlatform
    public static void detectClientPlugins() {
        throw new AssertionError();
    }
}
