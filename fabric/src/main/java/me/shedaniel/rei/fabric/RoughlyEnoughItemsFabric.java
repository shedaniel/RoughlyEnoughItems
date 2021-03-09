package me.shedaniel.rei.fabric;

import me.shedaniel.rei.impl.IssuesDetector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Locale;

public class RoughlyEnoughItemsFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        IssuesDetector.register(() -> {
            try {
                FabricLoader instance = FabricLoader.getInstance();
                for (Field field : instance.getClass().getDeclaredFields()) {
                    if (Logger.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Logger logger = (Logger) field.get(instance);
                        if (logger.getName().toLowerCase(Locale.ROOT).contains("subsystem"))
                            return true;
                    }
                }
            } catch (Throwable ignored) {
            }
            return false;
        }, ".ignoresubsystem", "Subsystem is detected (probably though Aristois), please contact support from them if anything happens.");
        
    }
}
