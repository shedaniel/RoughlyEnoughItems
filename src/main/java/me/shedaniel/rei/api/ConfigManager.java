/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.gui.screen.Screen;

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
     */
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
    void openConfigScreen(Screen parent);
    
    /**
     * Gets the config screen
     *
     * @param parent the screen shown before
     * @return the config screen
     */
    Screen getConfigScreen(Screen parent);
    
}
