package me.shedaniel.rei;

import com.google.common.collect.Maps;
import me.shedaniel.rei.api.ItemRegistry;
import me.shedaniel.rei.api.PluginDisabler;
import me.shedaniel.rei.api.REIPlugin;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.client.ConfigManager;
import me.shedaniel.rei.client.ItemRegistryImpl;
import me.shedaniel.rei.client.PluginDisablerImpl;
import me.shedaniel.rei.client.RecipeHelperImpl;
import me.shedaniel.rei.gui.widget.ItemListOverlay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoughlyEnoughItemsCore implements ClientModInitializer, ModInitializer {
    
    public static final Logger LOGGER;
    public static final Identifier DELETE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "delete_item");
    public static final Identifier CREATE_ITEMS_PACKET = new Identifier("roughlyenoughitems", "create_item");
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelperImpl();
    private static final PluginDisabler PLUGIN_DISABLER = new PluginDisablerImpl();
    private static final ItemRegistry ITEM_REGISTRY = new ItemRegistryImpl();
    private static final Map<Identifier, REIPlugin> plugins = Maps.newHashMap();
    private static ConfigManager configManager;
    
    static {
        LOGGER = LogManager.getFormatterLogger("REI");
    }
    
    public static RecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    public static me.shedaniel.rei.api.ConfigManager getConfigManager() {
        return configManager;
    }
    
    public static ItemRegistry getItemRegisterer() {
        return ITEM_REGISTRY;
    }
    
    public static PluginDisabler getPluginDisabler() {
        return PLUGIN_DISABLER;
    }
    
    public static REIPlugin registerPlugin(Identifier identifier, REIPlugin plugin) {
        plugins.put(identifier, plugin);
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Registered plugin %s from %s", identifier.toString(), plugin.getClass().getSimpleName());
        plugin.onFirstLoad(getPluginDisabler());
        return plugin;
    }
    
    public static List<REIPlugin> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static Optional<Identifier> getPluginIdentifier(REIPlugin plugin) {
        for(Identifier identifier : plugins.keySet())
            if (identifier != null && plugins.get(identifier).equals(plugin))
                return Optional.of(identifier);
        return Optional.empty();
    }
    
    @Override
    public void onInitializeClient() {
        configManager = new ConfigManager();
        
        // If pluginloader is not installed, base functionality should still remain
        if (!FabricLoader.getInstance().isModLoaded("pluginloader")) {
            RoughlyEnoughItemsCore.LOGGER.warn("[REI] Plugin Loader is not loaded! Please consider installing https://minecraft.curseforge.com/projects/pluginloader for REI plugin compatibility!");
            registerPlugin(new Identifier("roughlyenoughitems", "default_plugin"), new DefaultPlugin());
        }
        
        if (FabricLoader.getInstance().isModLoaded("cloth")) {
            try {
                Class.forName("me.shedaniel.rei.utils.ClothRegistry").getDeclaredMethod("register").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            RoughlyEnoughItemsCore.LOGGER.fatal("[REI] Cloth NOT found! It is a dependency of REI: https://minecraft.curseforge.com/projects/cloth");
            System.exit(0);
        }
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
                player.addChatMessage(new StringTextComponent(I18n.translate("text.rei.cheat_items").replaceAll("\\{item_name}", ItemListOverlay.tryGetItemStackName(stack.copy())).replaceAll("\\{item_count}", stack.copy().getAmount() + "").replaceAll("\\{player_name}", player.getEntityName())), false);
            else
                player.addChatMessage(new TranslatableTextComponent("text.rei.failed_cheat_items"), false);
        });
    }
    
}
