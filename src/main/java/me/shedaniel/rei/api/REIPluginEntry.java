/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionParsingException;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Get base class of a REI plugin.
 */
public interface REIPluginEntry {
    
    @ApiStatus.OverrideOnly
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
