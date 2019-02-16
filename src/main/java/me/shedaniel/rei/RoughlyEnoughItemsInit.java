package me.shedaniel.rei;

import me.shedaniel.rei.update.UpdateChecker;
import org.dimdev.riftloader.listener.InitializationListener;

import java.lang.reflect.InvocationTargetException;

public class RoughlyEnoughItemsInit implements InitializationListener {
    
    @Override
    public void onInitialization() {
        UpdateChecker.onInitialization();
        try {
            Class<?> pluginClass = Class.forName("me.shedaniel.rei.RoughlyEnoughItemsPlugin");
            pluginClass.getMethod("discoverPlugins").invoke(null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
}
