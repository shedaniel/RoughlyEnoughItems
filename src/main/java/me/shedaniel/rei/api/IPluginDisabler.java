package me.shedaniel.rei.api;

public interface IPluginDisabler {
    
    default public void disablePluginFunctions(Identifier plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            disablePluginFunction(plugin, function);
    }
    
    default public void enablePluginFunctions(Identifier plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            enablePluginFunction(plugin, function);
    }
    
    public void disablePluginFunction(Identifier plugin, PluginFunction function);
    
    public void enablePluginFunction(Identifier plugin, PluginFunction function);
    
    public boolean isFunctionEnabled(Identifier plugin, PluginFunction function);
    
}
