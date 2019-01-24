package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.shedaniel.rei.api.IRecipePlugin;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.riftloader.ModInfo;
import org.dimdev.riftloader.RiftLoader;
import org.dimdev.riftloader.listener.InitializationListener;

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

public class RoughlyEnoughItemsPlugin implements InitializationListener {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final Map<ResourceLocation, IRecipePlugin> plugins = Maps.newHashMap();
    private static JsonParser parser = new JsonParser();
    private static List<ResourceLocation> disablingPlugins;
    
    public static IRecipePlugin registerPlugin(ResourceLocation resourceLocation, IRecipePlugin plugin) {
        plugins.put(resourceLocation, plugin);
        RoughlyEnoughItemsPlugin.LOGGER.info("REI: Registered Plugin from %s by %s.", resourceLocation.toString(), plugin.getClass().getSimpleName());
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
    
    public static void disablePlugin(ResourceLocation location) {
        if (disablingPlugins.stream().noneMatch(location1 -> {return location.equals(location1);}))
            disablingPlugins.add(location);
    }
    
    @Override
    public void onInitialization() {
        discoverPlugins();
    }
    
    private void discoverPlugins() {
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
        plugins.forEach((location, plugin) -> plugin.onFirstLoad());
        plugins.keySet().stream().filter(location -> {
            return disablingPlugins.contains(location);
        }).collect(Collectors.toList()).forEach(location -> {
            plugins.remove(location);
            LOGGER.info("REI: Disabled REI plugin %s.", location.toString());
        });
        LOGGER.info("REI: Discovered %d REI Plugins%s", plugins.size(), (plugins.size() > 0 ? ": " + String.join(", ", plugins.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList())) : "."));
    }
    
    private void loadPluginInfo(ModInfo modInfo, Reader reader) throws Exception {
        JsonElement infoElement = parser.parse(reader);
        if (infoElement.isJsonArray())
            for(JsonElement jsonElement : infoElement.getAsJsonArray())
                parseAndRegisterPlugin(modInfo.id, jsonElement);
        else
            parseAndRegisterPlugin(modInfo.id, infoElement);
        reader.close();
    }
    
    private void parseAndRegisterPlugin(String modId, JsonElement jsonElement) throws Exception {
        ResourceLocation location = new ResourceLocation(modId, jsonElement.getAsJsonObject().getAsJsonPrimitive("id").getAsString());
        Class<?> aClass = Class.forName(jsonElement.getAsJsonObject().getAsJsonPrimitive("initializer").getAsString());
        IRecipePlugin plugin = IRecipePlugin.class.cast(aClass.newInstance());
        registerPlugin(location, plugin);
    }
    
}
