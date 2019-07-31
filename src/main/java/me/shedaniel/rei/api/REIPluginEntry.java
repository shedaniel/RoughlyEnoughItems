/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.util.Identifier;

/**
 * Get base class of a REI plugin.
 */
public interface REIPluginEntry {
    
    /**
     * Gets the priority of the plugin.
     *
     * @return the priority
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Get the identifier of the plugin
     *
     * @return the identifier
     */
    Identifier getPluginIdentifier();
    
}
