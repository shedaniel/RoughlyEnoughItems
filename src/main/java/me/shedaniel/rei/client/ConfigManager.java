package me.shedaniel.rei.client;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Screen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigManager implements me.shedaniel.rei.api.ConfigManager {
    
    private static final Gson GSON = new GsonBuilder().create();
    private static final Jankson JANKSON = Jankson.builder().build();
    private final File configFile, veryOldConfigFile, oldConfigFile;
    private ConfigObject config;
    private boolean craftableOnly;
    
    public ConfigManager() {
        this.veryOldConfigFile = new File(FabricLoader.getInstance().getConfigDirectory(), "rei.json");
        this.oldConfigFile = new File(FabricLoader.getInstance().getConfigDirectory(), "roughlyenoughitems/config.json");
        this.configFile = new File(FabricLoader.getInstance().getConfigDirectory(), "roughlyenoughitems/config.json5");
        this.craftableOnly = false;
        try {
            loadConfig();
            RoughlyEnoughItemsCore.LOGGER.info("[REI] Config is loaded.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveConfig() throws IOException {
        configFile.getParentFile().mkdirs();
        if (!configFile.exists() && !configFile.createNewFile()) {
            RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to save config! Overwriting with default config.");
            config = new ConfigObject();
            return;
        }
        try {
            String result = JANKSON.toJson(config).toJson(true, true, 0);
            if (!configFile.exists())
                configFile.createNewFile();
            FileOutputStream out = new FileOutputStream(configFile, false);
            
            out.write(result.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to save config! Overwriting with default config.");
            config = new ConfigObject();
            return;
        }
    }
    
    @Override
    public void loadConfig() throws IOException {
        configFile.getParentFile().mkdirs();
        if (!configFile.exists() && veryOldConfigFile.exists()) {
            RoughlyEnoughItemsCore.LOGGER.info("[REI] Detected old config file, trying to move it.");
            try {
                Files.move(veryOldConfigFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to move config file.");
            }
        }
        if (!configFile.exists() && oldConfigFile.exists()) {
            RoughlyEnoughItemsCore.LOGGER.info("[REI] Detected old config file, trying to move it.");
            try {
                Files.move(oldConfigFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to move config file.");
            }
        }
        if (!configFile.exists() || !configFile.canRead()) {
            RoughlyEnoughItemsCore.LOGGER.warn("[REI] Config not found! Creating one.");
            config = new ConfigObject();
            saveConfig();
            return;
        }
        boolean failed = false;
        try {
            JsonObject configJson = JANKSON.load(configFile);
            String regularized = configJson.toJson(false, false, 0);
            
            config = GSON.fromJson(regularized, ConfigObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            failed = true;
        }
        if (failed || config == null) {
            RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to load config! Overwriting with default config.");
            config = new ConfigObject();
        }
        saveConfig();
    }
    
    @Override
    public ConfigObject getConfig() {
        return config;
    }
    
    @Override
    public boolean isCraftableOnlyEnabled() {
        return craftableOnly;
    }
    
    @Override
    public void toggleCraftableOnly() {
        craftableOnly = !craftableOnly;
    }
    
    @Override
    public void openConfigScreen(Screen parent) {
        try {
            Class.forName("me.shedaniel.rei.utils.ClothRegistry").getDeclaredMethod("openConfigScreen", Screen.class).invoke(null, parent);
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
}
