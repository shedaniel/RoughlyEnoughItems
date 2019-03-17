package me.shedaniel.rei.api;

public interface PluginDisabler {
    
    default void disablePluginFunctions(Identifier plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            disablePluginFunction(plugin, function);
    }
    
    default void enablePluginFunctions(Identifier plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            enablePluginFunction(plugin, function);
    }
    
    void disablePluginFunction(Identifier plugin, PluginFunction function);
    
    void enablePluginFunction(Identifier plugin, PluginFunction function);
    
    boolean isFunctionEnabled(Identifier plugin, PluginFunction function);
    
}
