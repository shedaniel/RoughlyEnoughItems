package me.shedaniel.rei.api;

import com.google.common.collect.Maps;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.util.ResourceLocation;
import org.dimdev.riftloader.listener.InitializationListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RoughlyEnoughItemsPlugin implements InitializationListener {
    
    private static final Map<ResourceLocation, IRecipePlugin> plugins = Maps.newHashMap();
    public static final ResourceLocation DEFAULT_PLUGIN = new ResourceLocation("roughlyenoughitems", "default_plugin");
    
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
    
    @Override
    public void onInitialization() {
        RoughlyEnoughItemsPlugin.registerPlugin(RoughlyEnoughItemsPlugin.DEFAULT_PLUGIN, new DefaultPlugin());
    }
    
}
