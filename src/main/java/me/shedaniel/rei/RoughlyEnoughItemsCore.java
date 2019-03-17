package me.shedaniel.rei;

import com.google.common.collect.Maps;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.ConfigManager;
import me.shedaniel.rei.client.ItemRegistryImpl;
import me.shedaniel.rei.client.PluginDisablerImpl;
import me.shedaniel.rei.client.RecipeHelperImpl;
import me.shedaniel.rei.plugin.DefaultPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.riftloader.RiftLoader;
import org.dimdev.riftloader.listener.InitializationListener;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoughlyEnoughItemsCore implements InitializationListener {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    public static final Identifier DELETE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "delete_item");
    public static final Identifier CREATE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "create_item");
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelperImpl();
    private static final PluginDisabler PLUGIN_DISABLER = new PluginDisablerImpl();
    private static final ItemRegistry ITEM_REGISTRY = new ItemRegistryImpl();
    private static final Map<Identifier, REIPlugin> plugins = Maps.newHashMap();
    private static ConfigManager configManager;
    
    public static RecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    public static me.shedaniel.rei.api.ConfigManager getConfigManager() {
        return configManager;
    }
    
    public static ItemRegistry getItemRegisterer() {
        return ITEM_REGISTRY;
    }
    
    public static PluginDisabler getPluginDisabler() {
        return PLUGIN_DISABLER;
    }
    
    public static REIPlugin registerPlugin(Identifier identifier, REIPlugin plugin) {
        plugins.put(identifier, plugin);
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Registered plugin %s from %s", identifier.toString(), plugin.getClass().getSimpleName());
        plugin.onFirstLoad(getPluginDisabler());
        return plugin;
    }
    
    public static List<REIPlugin> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static Optional<Identifier> getPluginIdentifier(REIPlugin plugin) {
        for(Identifier identifier : plugins.keySet())
            if (identifier != null && plugins.get(identifier).equals(plugin))
                return Optional.of(identifier);
        return Optional.empty();
    }
    
    @Override
    public void onInitialization() {
        configManager = new ConfigManager();
        
        // TODO: Load 3rd party mods
        RoughlyEnoughItemsCore.LOGGER.warn("[REI] REI Addons need to be registered themselves! An automatic way might come in the future.");
        registerPlugin(new Identifier("roughlyenoughitems", "default_plugin"), new DefaultPlugin());
        
        if (RiftLoader.instance.getMods().stream().map(modInfo -> modInfo.id).anyMatch(s -> s.equalsIgnoreCase("riftmodlist"))) {
            try {
                Class.forName("me.shedaniel.rei.utils.RiftModListRegistry").getDeclaredMethod("register").invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
}
