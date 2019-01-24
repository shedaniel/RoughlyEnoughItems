package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ConfigHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.listeners.IListener;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.loader.Loader;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sortme.ChatMessageType;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        RoughlyEnoughItemsCore.LOGGER.info("REI: Registered plugin %s from %s", identifier.toString(), plugin.getClass().getSimpleName());
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
        // If pluginloader is not installed, base functionality should still remain
        if (!Loader.getInstance().isModLoaded("pluginloader")) {
            RoughlyEnoughItemsCore.LOGGER.warn("REI: Plugin Loader is not loaded! Please consider installing https://minecraft.curseforge.com/projects/pluginloader for REI plugin compatibility!");
            registerPlugin(new Identifier("roughlyenoughitems", "default_plugin"), new DefaultPlugin());
        }
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
                player.sendChatMessage(new StringTextComponent(I18n.translate("text.rei.cheat_items")
                        .replaceAll("\\{item_name}", stack.copy().getDisplayName().getFormattedText())
                        .replaceAll("\\{item_count}", stack.copy().getAmount() + "")
                        .replaceAll("\\{player_name}", player.getEntityName())
                ), ChatMessageType.SYSTEM);
            else
                player.sendChatMessage(new TranslatableTextComponent("text.rei.failed_cheat_items"), ChatMessageType.SYSTEM);
        });
    }
    
}
