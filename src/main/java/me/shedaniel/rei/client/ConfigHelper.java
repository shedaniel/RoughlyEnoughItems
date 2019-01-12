package me.shedaniel.rei.client;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.fabricmc.loader.FabricLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class ConfigHelper {
    
    private final File configFile;
    private REIConfig config;
    private boolean craftableOnly;
    
    public ConfigHelper() {
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
    
    public void setItemListOrdering(REIItemListOrdering ordering) {
        config.itemListOrdering = ordering;
    }
    
    public boolean isAscending() {
        return config.isAscending;
    }
    
    public void setAscending(boolean ascending) {
        config.isAscending = ascending;
    }
    
    public boolean craftableOnly() {
        return craftableOnly && config.enableCraftableOnlyButton;
    }
    
    public void toggleCraftableOnly() {
        craftableOnly = !craftableOnly;
    }
    
    public boolean showCraftableOnlyButton() {
        return config.enableCraftableOnlyButton;
    }
    
    public void setShowCraftableOnlyButton(boolean enableCraftableOnlyButton) {
        config.enableCraftableOnlyButton = enableCraftableOnlyButton;
    }
    
    public boolean sideSearchField() {
        return config.sideSearchField;
    }
    
    public void setSideSearchField(boolean sideSearchField) {
        config.sideSearchField = sideSearchField;
    }
    
}
