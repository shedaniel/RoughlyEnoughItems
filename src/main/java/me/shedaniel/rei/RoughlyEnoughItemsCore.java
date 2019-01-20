package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.api.REIPluginInfo;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ConfigHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.listeners.IListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sortme.ChatMessageType;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class RoughlyEnoughItemsCore implements ClientModInitializer, ModInitializer {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    public static final Identifier DELETE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "delete_item");
    public static final Identifier CREATE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "create_item");
    private static final List<IListener> listeners = Lists.newArrayList();
    private static final Map<Identifier, IRecipePlugin> plugins = Maps.newHashMap();
    private static ConfigHelper configHelper;
    
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
    
    private static IListener registerListener(IListener listener) {
        listeners.add(listener);
        return listener;
    }
    
    public static IRecipePlugin registerPlugin(Identifier identifier, IRecipePlugin plugin) {
        plugins.put(identifier, plugin);
        return plugin;
    }
    
    public static List<IRecipePlugin> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static Identifier getPluginIdentifier(IRecipePlugin plugin) {
        for(Identifier identifier : plugins.keySet())
            if (plugins.get(identifier).equals(plugin))
                return identifier;
        return null;
    }
    
    @Override
    public void onInitializeClient() {
        registerREIListeners();
        discoverPlugins();
        configHelper = new ConfigHelper();
    }
    
    private void registerREIListeners() {
        registerListener(new ClientHelper());
        registerListener(new RecipeHelper());
    }
    
    private boolean removeListener(IListener listener) {
        if (!listeners.contains(listener))
            return false;
        listeners.remove(listener);
        return true;
    }
    
    @Override
    public void onInitialize() {
        registerFabricPackets();
    }
    
    private void discoverPlugins() {
        Collection<ModContainer> modContainers = FabricLoader.INSTANCE.getModContainers();
        List<REIPluginInfo> pluginInfos = Lists.newArrayList();
        JsonParser parser = new JsonParser();
        modContainers.forEach(modContainer -> {
            if (modContainer.getOriginFile().isFile())
                try (JarFile file = new JarFile(modContainer.getOriginFile())) {
                    ZipEntry entry = file.getEntry("plugins" + File.separator + "rei.plugin.json");
                    if (entry != null) {
                        InputStream in = file.getInputStream(entry);
                        JsonElement jsonElement = parser.parse(new InputStreamReader(in));
                        if (jsonElement != null && jsonElement.isJsonObject()) {
                            REIPluginInfo info = REIPluginInfo.GSON.fromJson(jsonElement, REIPluginInfo.class);
                            if (info != null)
                                pluginInfos.add(info);
                        }
                    }
                } catch (Exception e) {
                    RoughlyEnoughItemsCore.LOGGER.error("REI: Failed to load REI plugin info from " + modContainer.getInfo().getId() + " when it should can. (" + e.getLocalizedMessage() + ")");
                }
            else if (modContainer.getOriginFile().isDirectory()) {
                File modInfo = new File(modContainer.getOriginFile(), "plugins" + File.separator + "rei.plugin.json");
                if (modInfo.exists())
                    try {
                        InputStream in = Files.newInputStream(modInfo.toPath());
                        JsonElement jsonElement = parser.parse(new InputStreamReader(in));
                        if (jsonElement != null && jsonElement.isJsonObject()) {
                            REIPluginInfo info = REIPluginInfo.GSON.fromJson(jsonElement, REIPluginInfo.class);
                            if (info != null)
                                pluginInfos.add(info);
                        }
                    } catch (Exception e) {
                        RoughlyEnoughItemsCore.LOGGER.error("REI: Failed to load REI plugin info from " + modContainer.getInfo().getId() + " when it should can. (" + e.getLocalizedMessage() + ")");
                    }
            }
        });
        pluginInfos.stream().forEachOrdered(reiPluginInfo -> {
            reiPluginInfo.getPlugins().forEach(reiPlugin -> {
                try {
                    Identifier identifier = new Identifier(reiPlugin.getIdentifier());
                    Class<?> aClass = Class.forName(reiPlugin.getPluginClass());
                    IRecipePlugin plugin = IRecipePlugin.class.cast(aClass.newInstance());
                    RoughlyEnoughItemsCore.registerPlugin(identifier, plugin);
                    RoughlyEnoughItemsCore.LOGGER.info("REI: Registered REI plugin: " + reiPlugin.getIdentifier());
                } catch (Exception e) {
                    RoughlyEnoughItemsCore.LOGGER.error("REI: Failed to load REI plugin: " + reiPlugin.getIdentifier() + " (" + e.getLocalizedMessage() + ")");
                }
            });
        });
    }
    
    private void registerFabricPackets() {
        CustomPayloadPacketRegistry.SERVER.register(DELETE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            if (!player.inventory.getCursorStack().isEmpty())
                player.inventory.setCursorStack(ItemStack.EMPTY);
        });
        CustomPayloadPacketRegistry.SERVER.register(CREATE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            ItemStack stack = packetByteBuf.readItemStack();
            if (player.inventory.insertStack(stack.copy()))
                player.sendChatMessage(new TranslatableTextComponent("text.rei.cheat_items", stack.getDisplayName().getFormattedText(), stack.getAmount(), player.getEntityName()), ChatMessageType.SYSTEM);
            else
                player.sendChatMessage(new TranslatableTextComponent("text.rei.failed_cheat_items"), ChatMessageType.SYSTEM);
        });
    }
    
}
