/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

public interface PluginDisabler {
    
    /**
     * Disables multiple functions from a plugin
     *
     * @param plugin    the identifier of the plugin
     * @param functions the array of functions to be disabled
     */
    default void disablePluginFunctions(Identifier plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            disablePluginFunction(plugin, function);
    }
    
    /**
     * Enables multiple functions from a plugin
     *
     * @param plugin    the identifier of the plugin
     * @param functions the array of functions to be enabled
     */
    default void enablePluginFunctions(Identifier plugin, PluginFunction... functions) {
        for(PluginFunction function : functions)
            enablePluginFunction(plugin, function);
    }
    
    /**
     * Disables a function from a plugin
     *
     * @param plugin   the identifier of the plugin
     * @param function the function to be disabled
     */
    void disablePluginFunction(Identifier plugin, PluginFunction function);
    
    /**
     * Enables a function from a plugin
     *
     * @param plugin   the identifier of the plugin
     * @param function the function to be enabled
     */
    void enablePluginFunction(Identifier plugin, PluginFunction function);
    
    /**
     * Checks if a plugin function has been disabled
     *
     * @param plugin   the identifier of the plugin
     * @param function the function to check
     * @return whether if it has been disabled
     */
    boolean isFunctionEnabled(Identifier plugin, PluginFunction function);
    
}
