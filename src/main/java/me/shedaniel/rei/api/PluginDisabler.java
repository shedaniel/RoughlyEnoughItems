package me.shedaniel.rei.api;

import net.minecraft.util.ResourceLocation;

public interface PluginDisabler {
    
    default void disablePluginFunctions(ResourceLocation plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            disablePluginFunction(plugin, function);
    }
    
    default void enablePluginFunctions(ResourceLocation plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            enablePluginFunction(plugin, function);
    }
    
    void disablePluginFunction(ResourceLocation plugin, PluginFunction function);
    
    void enablePluginFunction(ResourceLocation plugin, PluginFunction function);
    
    boolean isFunctionEnabled(ResourceLocation plugin, PluginFunction function);
    
}
