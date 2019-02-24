package me.shedaniel.rei;

import me.shedaniel.rei.api.IItemRegisterer;
import me.shedaniel.rei.api.IPluginDisabler;
import me.shedaniel.rei.client.ConfigHelper;
import me.shedaniel.rei.client.ItemListHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.plugin.PluginManager;
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
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelper();
    private static final ItemListHelper ITEM_LIST_HELPER = new ItemListHelper();
    private static final PluginManager PLUGIN_MANAGER = new PluginManager();
    static ConfigHelper configHelper;
    
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
    
    public static ConfigHelper getConfigHelper() {
        return configHelper;
    }
    
    public static RecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    public static IItemRegisterer getItemRegisterer() {
        return ITEM_LIST_HELPER;
    }
    
    public static IPluginDisabler getPluginDisabler() {
        return PLUGIN_MANAGER;
    }
    
}
