package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.riftloader.ModInfo;
import org.dimdev.riftloader.RiftLoader;
import org.dimdev.riftloader.listener.InitializationListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
        discoverPlugins();
    }
    
    private void discoverPlugins() {
        Collection<ModInfo> modInfoCollection = RiftLoader.instance.getMods();
        List<REIPluginInfo> pluginInfoList = Lists.newArrayList();
        JsonParser parser = new JsonParser();
        modInfoCollection.forEach(modContainer -> {
            JsonElement jsonElement = null;
            if (modContainer.source.isFile())
                try (JarFile file = new JarFile(modContainer.source)) {
                    ZipEntry entry = file.getEntry("plugins" + File.separatorChar + "rei.plugin.json");
                    if (entry != null) {
                        Reader reader = new InputStreamReader(file.getInputStream(entry));
                        jsonElement = parser.parse(reader);
                        reader.close();
                    }
                } catch (Exception e) {
                    RoughlyEnoughItemsPlugin.LOGGER.error("REI: Failed to load plugin file from " + modContainer.id + ". (" + e.getLocalizedMessage() + ")");
                }
            else if (modContainer.source.isDirectory()) {
                File modInfo = new File(modContainer.source, "plugins" + File.separatorChar + "rei.plugin.json");
                if (modInfo.exists())
                    try {
                        Reader reader = new InputStreamReader(Files.newInputStream(modInfo.toPath(), StandardOpenOption.READ));
                        jsonElement = parser.parse(reader);
                        reader.close();
                    } catch (Exception e) {
                        RoughlyEnoughItemsPlugin.LOGGER.error("REI: Failed to load plugin file from " + modContainer.id + ". (" + e.getLocalizedMessage() + ")");
                    }
            }
            if (jsonElement != null && jsonElement.isJsonObject()) {
                try {
                    REIPluginInfo info = REIPluginInfo.GSON.fromJson(jsonElement, REIPluginInfo.class);
                    if (info != null)
                        pluginInfoList.add(info);
                } catch (Exception e) {
                    RoughlyEnoughItemsPlugin.LOGGER.error("REI: Failed to load REI plugin info from " + modContainer.id + ". (" + e.getLocalizedMessage() + ")");
                }
            }
        });
        pluginInfoList.stream().forEachOrdered(reiPluginInfo -> {
            reiPluginInfo.getPlugins().forEach(reiPlugin -> {
                try {
                    ResourceLocation identifier = new ResourceLocation(reiPlugin.getIdentifier());
                    Class<?> aClass = Class.forName(reiPlugin.getPluginClass());
                    IRecipePlugin plugin = IRecipePlugin.class.cast(aClass.newInstance());
                    RoughlyEnoughItemsPlugin.registerPlugin(identifier, plugin);
                    RoughlyEnoughItemsPlugin.LOGGER.info("REI: Registered REI plugin: " + reiPlugin.getIdentifier());
                } catch (Exception e) {
                    RoughlyEnoughItemsPlugin.LOGGER.error("REI: Failed to register REI plugin: " + reiPlugin.getIdentifier() + ". (" + e.getLocalizedMessage() + ")");
                }
            });
        });
    }
    
}
