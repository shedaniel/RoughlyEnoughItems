package me.shedaniel.rei;

import com.google.common.collect.Maps;
import me.shedaniel.rei.api.IItemRegisterer;
import me.shedaniel.rei.api.IPluginDisabler;
import me.shedaniel.rei.api.IRecipeHelper;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.client.ConfigHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.ItemListHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.PluginManager;
import me.shedaniel.rei.update.UpdateChecker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.FabricLoader;
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
import java.util.Optional;

public class RoughlyEnoughItemsCore implements ClientModInitializer, ModInitializer {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    public static final Identifier DELETE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "delete_item");
    public static final Identifier CREATE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "create_item");
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelper();
    private static final PluginManager PLUGIN_MANAGER = new PluginManager();
    private static final ItemListHelper ITEM_LIST_HELPER = new ItemListHelper();
    private static final Map<Identifier, IRecipePlugin> plugins = Maps.newHashMap();
    private static ConfigHelper configHelper;
    
    public static IRecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    public static ConfigHelper getConfigHelper() {
        return configHelper;
    }
    
    public static IItemRegisterer getItemRegisterer() {
        return ITEM_LIST_HELPER;
    }
    
    public static IPluginDisabler getPluginDisabler() {
        return PLUGIN_MANAGER;
    }
    
    public static IRecipePlugin registerPlugin(Identifier identifier, IRecipePlugin plugin) {
        plugins.put(identifier, plugin);
        RoughlyEnoughItemsCore.LOGGER.info("REI: Registered plugin %s from %s", identifier.toString(), plugin.getClass().getSimpleName());
        plugin.onFirstLoad(getPluginDisabler());
        return plugin;
    }
    
    public static List<IRecipePlugin> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static Optional<Identifier> getPluginIdentifier(IRecipePlugin plugin) {
        for(Identifier identifier : plugins.keySet())
            if (identifier != null && plugins.get(identifier).equals(plugin))
                return Optional.of(identifier);
        return Optional.empty();
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeClient() {
        configHelper = new ConfigHelper();
        
        // If pluginloader is not installed, base functionality should still remain
        if (!FabricLoader.INSTANCE.getModContainers().stream().map(modContainer -> modContainer.getInfo().getId()).anyMatch(s -> s.equalsIgnoreCase("pluginloader"))) {
            RoughlyEnoughItemsCore.LOGGER.warn("REI: Plugin Loader is not loaded! Please consider installing https://minecraft.curseforge.com/projects/pluginloader for REI plugin compatibility!");
            registerPlugin(new Identifier("roughlyenoughitems", "default_plugin"), new DefaultPlugin());
        }
        
        ClientTickCallback.EVENT.register(GuiHelper::onTick);
        if (getConfigHelper().getConfig().checkUpdates)
            ClientTickCallback.EVENT.register(UpdateChecker::onTick);
        
        new UpdateChecker().onInitializeClient();
    }
    
    @Override
    public void onInitialize() {
        registerFabricPackets();
    }
    
    private void registerFabricPackets() {
        ServerSidePacketRegistry.INSTANCE.register(DELETE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            if (!player.inventory.getCursorStack().isEmpty())
                player.inventory.setCursorStack(ItemStack.EMPTY);
        });
        ServerSidePacketRegistry.INSTANCE.register(CREATE_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
            ItemStack stack = packetByteBuf.readItemStack();
            if (player.inventory.insertStack(stack.copy()))
                player.sendChatMessage(new StringTextComponent(I18n.translate("text.rei.cheat_items").replaceAll("\\{item_name}", stack.copy().getDisplayName().getFormattedText()).replaceAll("\\{item_count}", stack.copy().getAmount() + "").replaceAll("\\{player_name}", player.getEntityName())), ChatMessageType.SYSTEM);
            else
                player.sendChatMessage(new TranslatableTextComponent("text.rei.failed_cheat_items"), ChatMessageType.SYSTEM);
        });
    }
    
}
