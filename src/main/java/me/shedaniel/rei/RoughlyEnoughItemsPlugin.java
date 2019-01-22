package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.rei.api.IRecipePlugin;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.riftloader.ModInfo;
import org.dimdev.riftloader.RiftLoader;
import org.dimdev.riftloader.listener.InitializationListener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class RoughlyEnoughItemsPlugin implements InitializationListener {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final Map<ResourceLocation, IRecipePlugin> plugins = Maps.newHashMap();
    private static JsonParser parser = new JsonParser();
    
    public static IRecipePlugin registerPlugin(ResourceLocation resourceLocation, IRecipePlugin plugin) {
        plugins.put(resourceLocation, plugin);
        RoughlyEnoughItemsPlugin.LOGGER.info("REI: Registered Plugin from %s by %s.", resourceLocation.toString(), plugin.getClass());
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
        discoverPlugins();
    }
    
    private void discoverPlugins() {
        Collection<ModInfo> modInfoCollection = RiftLoader.instance.getMods();
        List<Pair<ResourceLocation, String>> plugins = Lists.newArrayList();
        modInfoCollection.forEach(modContainer -> {
            JsonElement jsonArray = null;
            if (modContainer.source.isFile())
                try (JarFile file = new JarFile(modContainer.source)) {
                    ZipEntry entry = file.getEntry("plugins" + File.separatorChar + "roughlyenoughitems.plugin.json");
                    if (entry != null)
                        jsonArray = readFromInputStream(file.getInputStream(entry));
                } catch (Exception e) {
                    RoughlyEnoughItemsPlugin.LOGGER.error("REI: Failed to read plugin file from %s. (%s)", modContainer.id, e.getLocalizedMessage());
                }
            else if (modContainer.source.isDirectory()) {
                File modInfo = new File(modContainer.source, "plugins" + File.separatorChar + "roughlyenoughitems.plugin.json");
                if (modInfo.exists())
                    try {
                        jsonArray = readFromInputStream(Files.newInputStream(modInfo.toPath(), StandardOpenOption.READ));
                    } catch (Exception e) {
                        RoughlyEnoughItemsPlugin.LOGGER.error("REI: Failed to read plugin file from %s. (%s)", modContainer.id, e.getLocalizedMessage());
                    }
            }
            if (jsonArray != null && jsonArray.isJsonArray()) {
                try {
                    jsonArray.getAsJsonArray().forEach(element -> {
                        String id = element.getAsJsonObject().get("id").getAsString();
                        String initializer = element.getAsJsonObject().get("initializer").getAsString();
                        plugins.add(new Pair<>(new ResourceLocation(modContainer.id, id), initializer));
                    });
                } catch (Exception e) {
                    RoughlyEnoughItemsPlugin.LOGGER.error("REI: Failed to grab plugin from %s. (%s)", modContainer.id, e.getLocalizedMessage());
                }
            }
        });
        plugins.stream().forEachOrdered(pair -> {
            try {
                Class<?> aClass = Class.forName(pair.getSecond());
                IRecipePlugin plugin = IRecipePlugin.class.cast(aClass.newInstance());
                RoughlyEnoughItemsPlugin.registerPlugin(pair.getFirst(), plugin);
            } catch (Exception e) {
                RoughlyEnoughItemsPlugin.LOGGER.error("REI: Failed to register plugin from %s. (%s)", pair.getFirst().toString(), e.getLocalizedMessage());
            }
        });
    }
    
    private JsonElement readFromInputStream(InputStream stream) throws IOException {
        JsonElement element = readFromReader(new InputStreamReader(stream));
        stream.close();
        return element;
    }
    
    private JsonElement readFromReader(Reader reader) throws IOException {
        JsonElement element = parser.parse(reader);
        reader.close();
        return element;
    }
    
}
