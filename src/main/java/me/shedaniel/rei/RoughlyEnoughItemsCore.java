package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ConfigHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.listeners.IListener;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.riftloader.listener.InitializationListener;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoughlyEnoughItemsCore {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final List<IListener> listeners = Arrays.asList(new ClientHelper(), new RecipeHelper());
    private static final ConfigHelper configHelper = new ConfigHelper();
    
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
    
    private boolean removeListener(IListener listener) {
        if (!listeners.contains(listener))
            return false;
        listeners.remove(listener);
        return true;
    }
    
}
