package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.client.ConfigHelper;
import me.shedaniel.rei.listeners.IListener;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoughlyEnoughItemsCore {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    public static final ResourceLocation DELETE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "delete_item");
    public static final ResourceLocation CREATE_ITEMS_PACKET = new ResourceLocation("roughlyenoughitems", "create_item");
    public static final ResourceLocation DEFAULT_PLUGIN = new ResourceLocation("roughlyenoughitems", "default_plugin");
    private static final List<IListener> listeners = Lists.newArrayList();
    private static final Map<ResourceLocation, IRecipePlugin> plugins = Maps.newHashMap();
    private static final ConfigHelper configHelper = new ConfigHelper();
    
    public static <T> List<T> getListeners(Class<T> listenerClass) {
        return listeners.stream().filter(listener -> {
            return listenerClass.isAssignableFrom(listener.getClass());
        }).map(listener -> {
            return listenerClass.cast(listener);
        }).collect(Collectors.toList());
    }
    
    public static ConfigHelper getConfigHelper() {
        return configHelper;
    }
    
    static IListener registerListener(IListener listener) {
        listeners.add(listener);
        return listener;
    }
    
    public static IRecipePlugin registerPlugin(ResourceLocation ResourceLocation, IRecipePlugin plugin) {
        plugins.put(ResourceLocation, plugin);
        return plugin;
    }
    
    public static List<IRecipePlugin> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static ResourceLocation getPluginResourceLocation(IRecipePlugin plugin) {
        for(ResourceLocation ResourceLocation : plugins.keySet())
            if (plugins.get(ResourceLocation).equals(plugin))
                return ResourceLocation;
        return null;
    }
    
    private boolean removeListener(IListener listener) {
        if (!listeners.contains(listener))
            return false;
        listeners.remove(listener);
        return true;
    }
    
}
