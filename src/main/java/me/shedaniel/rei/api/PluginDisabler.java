/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.util.Identifier;

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
