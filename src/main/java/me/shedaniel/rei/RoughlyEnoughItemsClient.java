package me.shedaniel.rei;

import com.google.common.collect.Maps;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.PluginDisabler;
import me.shedaniel.rei.api.REIPluginEntry;
import me.shedaniel.rei.client.ConfigManagerImpl;
import me.shedaniel.rei.client.PluginDisablerImpl;
import me.shedaniel.rei.plugin.DefaultAutoCraftingPlugin;
import me.shedaniel.rei.plugin.DefaultPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.riftloader.RiftLoader;
import org.dimdev.riftloader.listener.InitializationListener;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class RoughlyEnoughItemsClient implements InitializationListener {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    static final Map<Identifier, REIPluginEntry> plugins = Maps.newHashMap();
    private static final PluginDisabler PLUGIN_DISABLER = new PluginDisablerImpl();
    private static final ConfigManager configManager = new ConfigManagerImpl();
    
    public static PluginDisabler getPluginDisabler() {
        return PLUGIN_DISABLER;
    }
    
    public static ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Registers a REI plugin
     *
     * @param identifier the identifier of the plugin
     * @param plugin     the plugin instance
     * @return the plugin itself
     * @deprecated Check REI wiki
     */
    @Deprecated
    public static REIPluginEntry registerPlugin(REIPluginEntry plugin) {
        plugins.put(plugin.getPluginIdentifier(), plugin);
        LOGGER.info("[REI] Registered plugin %s from %s", plugin.getPluginIdentifier().toString(), plugin.getClass().getSimpleName());
        plugin.onFirstLoad(getPluginDisabler());
        return plugin;
    }
    
    @Override
    public void onInitialization() {
        DefaultPlugin defaultPlugin = new DefaultPlugin();
        registerPlugin(defaultPlugin);
        DefaultAutoCraftingPlugin defaultAutoCraftingPlugin = new DefaultAutoCraftingPlugin();
        registerPlugin(defaultAutoCraftingPlugin);
        LOGGER.warn("[REI] Plugins are NOT loaded automatically in the Rift version of REI!");
        if (RiftLoader.instance.getMods().stream().map(modInfo -> modInfo.id).anyMatch(s -> s.equalsIgnoreCase("riftmodlist"))) {
            try {
                Class.forName("me.shedaniel.api.ConfigRegistry").getDeclaredMethod("registerConfig",String.class,Runnable.class).invoke(null, "roughlyenoughitems", new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Class.forName("me.shedaniel.rei.utils.ClothScreenRegistry").getDeclaredMethod("openConfigScreen").invoke(null);
                        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
