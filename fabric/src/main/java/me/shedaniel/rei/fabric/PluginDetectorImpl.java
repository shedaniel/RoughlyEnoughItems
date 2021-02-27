package me.shedaniel.rei.fabric;

import com.google.common.collect.Iterables;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.plugins.REIPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import static me.shedaniel.rei.RoughlyEnoughItemsCore.registerPlugin;

public class PluginDetectorImpl {
    public static void detectServerPlugins() {
        FabricLoader.getInstance().getEntrypoints("rei_containers", Runnable.class).forEach(Runnable::run);
    }
    
    @Environment(EnvType.CLIENT)
    public static void detectClientPlugins() {
        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            if (modContainer.getMetadata().containsCustomElement("roughlyenoughitems:plugins"))
                RoughlyEnoughItemsCore.LOGGER.error("REI plugin from " + modContainer.getMetadata().getId() + " is not loaded because it is too old!");
        }
        
        for (REIPlugin plugin : Iterables.concat(
                FabricLoader.getInstance().getEntrypoints("rei_plugins", REIPlugin.class),
                FabricLoader.getInstance().getEntrypoints("rei", REIPlugin.class)
        )) {
            try {
                registerPlugin(plugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("Can't load REI plugins from %s: %s", plugin.getClass(), e.getLocalizedMessage());
            }
        }
        for (REIPlugin reiPlugin : FabricLoader.getInstance().getEntrypoints("rei_plugins_v0", REIPlugin.class)) {
            try {
                registerPlugin(reiPlugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("Can't load REI plugins from %s: %s", reiPlugin.getClass(), e.getLocalizedMessage());
            }
        }
        if (FabricLoader.getInstance().isModLoaded("libblockattributes-fluids")) {
            try {
                registerPlugin((REIPlugin) Class.forName("me.shedaniel.rei.compat.LBASupportPlugin").getConstructor().newInstance());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
