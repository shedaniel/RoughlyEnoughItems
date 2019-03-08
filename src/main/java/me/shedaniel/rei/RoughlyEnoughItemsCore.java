package me.shedaniel.rei;

import me.shedaniel.rei.api.ItemRegistry;
import me.shedaniel.rei.api.PluginDisabler;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.client.ConfigManager;
import me.shedaniel.rei.client.ItemRegistryImpl;
import me.shedaniel.rei.client.RecipeHelperImpl;
import me.shedaniel.rei.plugin.PluginDisablerImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mod(RoughlyEnoughItemsCore.MOD_ID)
public class RoughlyEnoughItemsCore {
    
    public static final String MOD_ID = "roughlyenoughitems";
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("roughlyenoughitems");
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelperImpl();
    private static final ItemRegistry ITEM_REGISTRY = new ItemRegistryImpl();
    private static final PluginDisabler PLUGIN_DISABLER = new PluginDisablerImpl();
    static ConfigManager configManager;
    
    public RoughlyEnoughItemsCore() {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            try {
                Class c = Class.forName("me.shedaniel.rei.RoughlyEnoughItemsClient");
                Method method = c.getDeclaredMethod("setup");
                method.invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
    
    public static ConfigManager getConfigManager() {
        return configManager;
    }
    
    public static RecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    public static ItemRegistry getItemRegistry() {
        return ITEM_REGISTRY;
    }
    
    public static PluginDisabler getPluginDisabler() {
        return PLUGIN_DISABLER;
    }
    
}
