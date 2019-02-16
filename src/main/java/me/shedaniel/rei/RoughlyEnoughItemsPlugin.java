package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.shedaniel.rei.api.IRecipePlugin;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.riftloader.ModInfo;
import org.dimdev.riftloader.RiftLoader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class RoughlyEnoughItemsPlugin {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final Map<String, IRecipePlugin> plugins = Maps.newHashMap();
    private static JsonParser parser = new JsonParser();
    private static List<String> disablingPlugins;
    private static boolean loaded = false;
    
    public static IRecipePlugin registerPlugin(String resourceLocation, IRecipePlugin plugin) {
        plugins.put(resourceLocation, plugin);
        RoughlyEnoughItemsPlugin.LOGGER.info("REI: Registered Plugin from %s by %s.", resourceLocation.toString(), plugin.getClass().getSimpleName());
        plugin.onFirstLoad(RoughlyEnoughItemsCore.getPluginDisabler());
        return plugin;
    }
    
    public static List<IRecipePlugin> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static String getPluginResourceLocation(IRecipePlugin plugin) {
        for(String resourceLocation : plugins.keySet())
            if (plugins.get(resourceLocation).equals(plugin))
                return resourceLocation;
        return null;
    }
    
    public static void disablePlugin(ResourceLocation location) {
        if (disablingPlugins.stream().noneMatch(location1 -> {return location.toString().equals(location1);}))
            disablingPlugins.add(location.toString());
    }
    
    public static void discoverPlugins() {
        if (loaded)
            return;
        loaded = true;
        LOGGER.info("REI: Discovering Plugins.");
        disablingPlugins = Lists.newArrayList();
        Collection<ModInfo> modInfoCollection = RiftLoader.instance.getMods();
        modInfoCollection.forEach(modInfo -> {
            try {
                if (modInfo.source.isDirectory()) {
                    File pluginFile = new File(modInfo.source, "plugins/roughlyenoughitems.plugin.json");
                    if (pluginFile.exists())
                        loadPluginInfo(modInfo, new FileReader(pluginFile));
                } else {
                    JarFile jarFile = new JarFile(modInfo.source);
                    ZipEntry entry = jarFile.getEntry("plugins/roughlyenoughitems.plugin.json");
                    if (entry != null)
                        loadPluginInfo(modInfo, new InputStreamReader(jarFile.getInputStream(entry)));
                }
            } catch (Exception e) {
                RoughlyEnoughItemsPlugin.LOGGER.error("REI: Failed to load plugin file from %s. (%s)", (Object) modInfo.id, (Object) e.getLocalizedMessage());
            }
        });
        new LinkedList<>(plugins.keySet()).stream().filter(location -> disablingPlugins.contains(location)).forEach(location -> {
            plugins.remove(location);
            LOGGER.info("REI: Disabled REI plugin %s.", location.toString());
        });
        LOGGER.info("REI: Discovered %d REI Plugins%s", plugins.size(), (plugins.size() > 0 ? ": " + String.join(", ", plugins.keySet().stream().collect(Collectors.toList())) : "."));
    }
    
    private static void loadPluginInfo(ModInfo modInfo, Reader reader) throws Exception {
        JsonElement infoElement = parser.parse(reader);
        if (infoElement.isJsonArray())
            for(JsonElement jsonElement : infoElement.getAsJsonArray())
                parseAndRegisterPlugin(modInfo.id, jsonElement.getAsJsonObject());
        else
            parseAndRegisterPlugin(modInfo.id, infoElement.getAsJsonObject());
        reader.close();
    }
    
    private static void parseAndRegisterPlugin(String modId, JsonObject jsonObject) throws Exception {
        String location = modId + ":" + jsonObject.getAsJsonPrimitive("id").getAsString();
        Class<?> aClass = Class.forName(jsonObject.getAsJsonPrimitive("initializer").getAsString());
        IRecipePlugin plugin = IRecipePlugin.class.cast(aClass.newInstance());
        registerPlugin(location, plugin);
    }
    
}
