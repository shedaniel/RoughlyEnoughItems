/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionParsingException;
import net.minecraft.util.Identifier;

/**
 * Get base class of a REI plugin.
 */
public interface REIPluginEntry {

    default SemanticVersion getMinimumVersion() throws VersionParsingException {
        return null;
    }

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
