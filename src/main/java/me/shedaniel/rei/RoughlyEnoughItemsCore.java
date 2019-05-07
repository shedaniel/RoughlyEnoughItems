package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.cloth.hooks.ClothClientHooks;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.*;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.client.gui.ingame.PlayerInventoryScreen;
import net.minecraft.client.gui.widget.RecipeBookButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoughlyEnoughItemsCore implements ClientModInitializer {
    
    public static final Logger LOGGER;
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelperImpl();
    private static final PluginDisabler PLUGIN_DISABLER = new PluginDisablerImpl();
    private static final ItemRegistry ITEM_REGISTRY = new ItemRegistryImpl();
    private static final DisplayHelper DISPLAY_HELPER = new DisplayHelperImpl();
    private static final Map<Identifier, REIPluginEntry> plugins = Maps.newHashMap();
    private static ConfigManagerImpl configManager;
    
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
    
    public static DisplayHelper getDisplayHelper() {
        return DISPLAY_HELPER;
    }
    
    public static REIPluginEntry registerPlugin(Identifier identifier, REIPluginEntry plugin) {
        plugins.put(identifier, plugin);
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Registered plugin %s from %s", identifier.toString(), plugin.getClass().getSimpleName());
        plugin.onFirstLoad(getPluginDisabler());
        return plugin;
    }
    
    public static List<REIPluginEntry> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static Optional<Identifier> getPluginIdentifier(REIPluginEntry plugin) {
        for(Identifier identifier : plugins.keySet())
            if (identifier != null && plugins.get(identifier).equals(plugin))
                return Optional.of(identifier);
        return Optional.empty();
    }
    
    public static boolean hasPermissionToUsePackets() {
        return hasOperatorPermission() && canUsePackets();
    }
    
    public static boolean hasOperatorPermission() {
        return MinecraftClient.getInstance().getNetworkHandler().getCommandSource().hasPermissionLevel(1);
    }
    
    public static boolean canUsePackets() {
        return ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET) && ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET);
    }
    
    @Override
    public void onInitializeClient() {
        configManager = new ConfigManagerImpl();
        
        registerClothEvents();
        discoverOldPlugins();
        discoverPluginEntries();
    }
    
    private void discoverPluginEntries() {
        for(REIPluginEntry reiPlugin : FabricLoader.getInstance().getEntrypoints("rei_plugins", REIPluginEntry.class)) {
            try {
                if (reiPlugin instanceof REIPlugin)
                    throw new IllegalStateException("REI Plugins on Entry Points should not implement REIPlugin");
                registerPlugin(reiPlugin.getPluginIdentifier(), reiPlugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Can't load REI plugins from %s: %s", reiPlugin.getClass(), e.getLocalizedMessage());
            }
        }
    }
    
    private void discoverOldPlugins() {
        List<Pair<Identifier, String>> list = Lists.newArrayList();
        for(ModMetadata metadata : FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).filter(metadata -> metadata.containsCustomElement("roughlyenoughitems:plugins")).collect(Collectors.toList())) {
            RoughlyEnoughItemsCore.LOGGER.warn("[REI] %s(%s) is still using the old way to register its plugin! Support will be dropped in the future!", metadata.getName(), metadata.getId());
            try {
                JsonElement pluginsElement = metadata.getCustomElement("roughlyenoughitems:plugins");
                if (pluginsElement.isJsonArray()) {
                    for(JsonElement element : pluginsElement.getAsJsonArray())
                        if (element.isJsonObject())
                            loadPluginFromJsonObject(list, metadata, element.getAsJsonObject());
                        else
                            throw new IllegalStateException("Custom Element in an array is not an object.");
                } else if (pluginsElement.isJsonObject()) {
                    loadPluginFromJsonObject(list, metadata, pluginsElement.getAsJsonObject());
                } else
                    throw new IllegalStateException("Custom Element not an array or an object.");
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Can't load REI plugins from %s: %s", metadata.getId(), e.getLocalizedMessage());
            }
        }
        for(Pair<Identifier, String> pair : list) {
            String s = pair.getRight();
            try {
                Class<?> aClass = Class.forName(s);
                if (!REIPlugin.class.isAssignableFrom(aClass)) {
                    RoughlyEnoughItemsCore.LOGGER.error("[REI] Plugin class from %s is not implementing REIPlugin!", s);
                    break;
                }
                REIPlugin o = REIPlugin.class.cast(aClass.newInstance());
                registerPlugin(pair.getLeft(), o);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Can't load REI plugin class from %s!", s);
            } catch (ClassCastException e) {
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to cast plugin class from %s to REIPlugin!", s);
            }
        }
    }
    
    private void loadPluginFromJsonObject(List<Pair<Identifier, String>> list, ModMetadata modMetadata, JsonObject object) {
        String namespace = modMetadata.getId();
        if (object.has("namespace"))
            namespace = object.get("namespace").getAsString();
        String id = object.get("id").getAsString();
        String className = object.get("class").getAsString();
        list.add(new Pair<>(new Identifier(namespace, id), className));
    }
    
    private void registerClothEvents() {
        ClothClientHooks.SYNC_RECIPES.register((minecraftClient, recipeManager, synchronizeRecipesS2CPacket) -> {
            ((RecipeHelperImpl) RoughlyEnoughItemsCore.getRecipeHelper()).recipesLoaded(recipeManager);
        });
        ClothClientHooks.SCREEN_ADD_BUTTON.register((minecraftClient, screen, abstractButtonWidget) -> {
            if (RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook && screen instanceof ContainerScreen && abstractButtonWidget instanceof RecipeBookButtonWidget)
                return ActionResult.FAIL;
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_INIT_POST.register((minecraftClient, screen, screenHooks) -> {
            if (screen instanceof ContainerScreen) {
                if (screen instanceof PlayerInventoryScreen && minecraftClient.interactionManager.hasCreativeInventory())
                    return;
                ScreenHelper.setLastContainerScreen((ContainerScreen) screen);
                boolean alreadyAdded = false;
                for(Element element : Lists.newArrayList(screenHooks.cloth_getInputListeners()))
                    if (ContainerScreenOverlay.class.isAssignableFrom(element.getClass()))
                        if (alreadyAdded)
                            screenHooks.cloth_getInputListeners().remove(element);
                        else
                            alreadyAdded = true;
                if (!alreadyAdded)
                    screenHooks.cloth_getInputListeners().add(ScreenHelper.getLastOverlay(true, false));
            }
        });
        ClothClientHooks.SCREEN_RENDER_POST.register((minecraftClient, screen, i, i1, v) -> {
            if (screen instanceof ContainerScreen)
                ScreenHelper.getLastOverlay().render(i, i1, v);
        });
        ClothClientHooks.SCREEN_MOUSE_CLICKED.register((minecraftClient, screen, v, v1, i) -> {
            if (screen instanceof CreativePlayerInventoryScreen)
                if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseClicked(v, v1, i)) {
                    screen.setFocused(ScreenHelper.getLastOverlay());
                    if (i == 0)
                        screen.setDragging(true);
                    return ActionResult.SUCCESS;
                }
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_MOUSE_SCROLLED.register((minecraftClient, screen, v, v1, v2) -> {
            if (screen instanceof ContainerScreen)
                if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().isInside(ClientUtils.getMouseLocation()) && ScreenHelper.getLastOverlay().mouseScrolled(v, v1, v2))
                    return ActionResult.SUCCESS;
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_CHAR_TYPED.register((minecraftClient, screen, character, keyCode) -> {
            if (screen instanceof ContainerScreen)
                if (ScreenHelper.getLastOverlay().charTyped(character, keyCode))
                    return ActionResult.SUCCESS;
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_LATE_RENDER.register((minecraftClient, screen, i, i1, v) -> {
            if (!ScreenHelper.isOverlayVisible())
                return;
            if (screen instanceof ContainerScreen)
                ScreenHelper.getLastOverlay().lateRender(i, i1, v);
        });
        ClothClientHooks.SCREEN_KEY_PRESSED.register((minecraftClient, screen, i, i1, i2) -> {
            if (screen.getFocused() != null && screen.getFocused() instanceof TextFieldWidget)
                return ActionResult.PASS;
            if (screen instanceof ContainerScreen)
                if (ScreenHelper.getLastOverlay().keyPressed(i, i1, i2))
                    return ActionResult.SUCCESS;
            return ActionResult.PASS;
        });
    }
    
}
