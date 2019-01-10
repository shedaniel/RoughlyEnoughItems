package me.shedaniel.rei.client;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.fabricmc.loader.FabricLoader;
import org.apache.logging.log4j.core.Core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class ConfigManager {
    
    private final File configFile;
    private REIConfig config;
    private boolean craftableOnly;
    
    public ConfigManager() {
        this.configFile = new File(FabricLoader.INSTANCE.getConfigDirectory(), "rei.json");
        this.craftableOnly = false;
        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveConfig() throws IOException {
        configFile.getParentFile().mkdirs();
        if (!configFile.exists() && !configFile.createNewFile()) {
            RoughlyEnoughItemsCore.LOGGER.error("REI: Failed to save config! Overwriting with default config.");
            config = new REIConfig();
            return;
        }
        FileWriter writer = new FileWriter(configFile, false);
        try {
            REIConfig.GSON.toJson(config, writer);
        } finally {
            writer.close();
        }
    }
    
    public void loadConfig() throws IOException {
        if (!configFile.exists() || !configFile.canRead()) {
            config = new REIConfig();
            saveConfig();
            return;
        }
        boolean failed = false;
        try {
            config = REIConfig.GSON.fromJson(new InputStreamReader(Files.newInputStream(configFile.toPath())), REIConfig.class);
        } catch (Exception e) {
            failed = true;
        }
        if (failed || config == null) {
            RoughlyEnoughItemsCore.LOGGER.error("REI: Failed to load config! Overwriting with default config.");
            config = new REIConfig();
        }
        saveConfig();
    }
    
    public REIItemListOrdering getItemListOrdering() {
        return config.itemListOrdering;
    }
    
    public boolean isAscending() {
        return config.isAscending;
    }
    
    public boolean craftableOnly() {
        return craftableOnly && config.enableCraftableOnlyButton;
    }
    
}
