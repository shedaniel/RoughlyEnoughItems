package me.shedaniel.rei.api;

import net.minecraft.util.ResourceLocation;

public interface IPluginDisabler {
    
    default public void disablePluginFunctions(ResourceLocation plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            disablePluginFunction(plugin, function);
    }
    
    default public void enablePluginFunctions(ResourceLocation plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            enablePluginFunction(plugin, function);
    }
    
    public void disablePluginFunction(ResourceLocation plugin, PluginFunction function);
    
    public void enablePluginFunction(ResourceLocation plugin, PluginFunction function);
    
    public boolean isFunctionEnabled(ResourceLocation plugin, PluginFunction function);
    
}
