/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.network.chat.TextComponent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ConfigManagerImpl implements ConfigManager {
    
    private static final Gson GSON = new GsonBuilder().create();
    private static final Jankson JANKSON = Jankson.builder().build();
    private final File configFile, veryOldConfigFile, oldConfigFile;
    private ConfigObject config;
    private boolean craftableOnly;
    
    public ConfigManagerImpl() {
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
        MinecraftClient.getInstance().openScreen(getConfigScreen(parent));
    }
    
    @Override
    public Screen getConfigScreen(Screen parent) {
        if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
            try {
                return Screen.class.cast(Class.forName("me.shedaniel.rei.utils.ClothScreenRegistry").getDeclaredMethod("getConfigScreen", Screen.class).invoke(null, parent));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Screen(new TextComponent("")) {
            @Override
            public void render(int int_1, int int_2, float float_1) {
                renderDirtBackground(0);
                List<String> list = minecraft.textRenderer.wrapStringToWidthAsList(I18n.translate("text.rei.config_api_failed"), width - 100);
                int y = (int) (height / 2 - minecraft.textRenderer.fontHeight * 1.3f / 2 * list.size());
                for(int i = 0; i < list.size(); i++) {
                    String s = list.get(i);
                    drawCenteredString(minecraft.textRenderer, s, width / 2, y, -1);
                    y += minecraft.textRenderer.fontHeight;
                }
                super.render(int_1, int_2, float_1);
            }
            
            @Override
            protected void init() {
                super.init();
                addButton(new net.minecraft.client.gui.widget.ButtonWidget(width / 2 - 100, height - 26, 200, 20, I18n.translate("text.rei.back"), buttonWidget -> {
                    this.minecraft.openScreen(parent);
                }));
            }
            
            @Override
            public boolean keyPressed(int int_1, int int_2, int int_3) {
                if (int_1 == 256 && this.shouldCloseOnEsc()) {
                    this.minecraft.openScreen(parent);
                    return true;
                }
                return super.keyPressed(int_1, int_2, int_3);
            }
        };
    }
    
}
