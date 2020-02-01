/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface ConfigManager {
    
    @SuppressWarnings("deprecation")
    static ConfigManager getInstance() {
        return RoughlyEnoughItemsCore.getConfigManager();
    }
    
    List<EntryStack> getFavorites();
    
    /**
     * Saves the config.
     */
    void saveConfig();
    
    /**
     * Gets the config instance
     *
     * @return the config instance
     * @deprecated Use {@link ConfigObject#getInstance()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    ConfigObject getConfig();
    
    /**
     * Gets if craftable only filter is enabled
     *
     * @return whether craftable only filter is enabled
     */
    boolean isCraftableOnlyEnabled();
    
    /**
     * Toggles the craftable only filter
     */
    void toggleCraftableOnly();
    
    /**
     * Opens the config screen
     *
     * @param parent the screen shown before
     */
    default void openConfigScreen(Screen parent) {
        MinecraftClient.getInstance().openScreen(getConfigScreen(parent));
    }
    
    /**
     * Gets the config screen
     *
     * @param parent the screen shown before
     * @return the config screen
     */
    Screen getConfigScreen(Screen parent);
    
}
