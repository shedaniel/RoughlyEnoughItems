package me.shedaniel.rei;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.api.Identifier;
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
    private static final Map<Identifier, IRecipePlugin> plugins = Maps.newHashMap();
    private static JsonParser parser = new JsonParser();
    private static boolean loaded = false;
    
    public static IRecipePlugin registerPlugin(Identifier identifier, IRecipePlugin plugin) {
        plugins.put(identifier, plugin);
        RoughlyEnoughItemsPlugin.LOGGER.info("REI: Registered Plugin from %s by %s.", identifier.toString(), plugin.getClass().getSimpleName());
        plugin.onFirstLoad(RoughlyEnoughItemsCore.getPluginDisabler());
        return plugin;
    }
    
    public static List<IRecipePlugin> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static Identifier getIdentifier(IRecipePlugin plugin) {
        for(Identifier identifier : plugins.keySet())
            if (plugins.get(identifier).equals(plugin))
                return identifier;
        return null;
    }
    
    public static void discoverPlugins() {
        if (loaded)
            return;
        loaded = true;
        LOGGER.info("REI: Discovering Plugins.");
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
        LOGGER.info("REI: Discovered %d REI Plugins%s", plugins.size(), (plugins.size() > 0 ? ": " + String.join(", ", plugins.keySet().stream().map(Identifier::toString).collect(Collectors.toList())) : "."));
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
        Identifier location = new Identifier(modId, jsonObject.getAsJsonPrimitive("id").getAsString());
        Class<?> aClass = Class.forName(jsonObject.getAsJsonPrimitive("initializer").getAsString());
        IRecipePlugin plugin = IRecipePlugin.class.cast(aClass.newInstance());
        registerPlugin(location, plugin);
    }
    
}
