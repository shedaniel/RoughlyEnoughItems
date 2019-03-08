package me.shedaniel.rei;

import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
import me.shedaniel.rei.api.IREIPlugin;
import me.shedaniel.rei.api.REIPlugin;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoughlyEnoughItemsPlugin {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final Map<ResourceLocation, REIPlugin> plugins = Maps.newHashMap();
    private static JsonParser parser = new JsonParser();
    private static boolean loaded = false;
    
    public static REIPlugin registerPlugin(ResourceLocation location, REIPlugin plugin) {
        plugins.put(location, plugin);
        RoughlyEnoughItemsPlugin.LOGGER.info("Registered Plugin from %s by %s.", location.toString(), plugin.getClass().getSimpleName());
        plugin.onFirstLoad(RoughlyEnoughItemsCore.getPluginDisabler());
        return plugin;
    }
    
    public static List<REIPlugin> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static Optional<ResourceLocation> getPluginLocation(REIPlugin plugin) {
        for(ResourceLocation location : plugins.keySet())
            if (location != null && plugins.get(location).equals(plugin))
                return Optional.of(location);
        return Optional.empty();
    }
    
    public static void discoverPlugins() {
        if (loaded)
            return;
        loaded = true;
        LOGGER.info("Discovering Plugins.");
        ModList.get().getAllScanData().forEach(scan -> {
            scan.getAnnotations().forEach(a -> {
                if (a.getAnnotationType().getClassName().equals(IREIPlugin.class.getName())) {
                    String required = (String) a.getAnnotationData().getOrDefault("value", "");
                    if (required.isEmpty() || ModList.get().isLoaded(required)) {
                        try {
                            Class<?> clazz = Class.forName(a.getMemberName());
                            if (REIPlugin.class.isAssignableFrom(clazz)) {
                                REIPlugin plugin = (REIPlugin) clazz.newInstance();
                                registerPlugin(new ResourceLocation(clazz.getAnnotation(IREIPlugin.class).identifier()), plugin);
                            }
                        } catch (Exception e) {
                            LOGGER.error("Can't load REI plugin. %s", a.getMemberName());
                        }
                    }
                }
            });
        });
        LOGGER.info("Discovered %d REI Plugins%s", plugins.size(), (plugins.size() > 0 ? ": " + String.join(", ", plugins.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList())) : "."));
    }
    
}
