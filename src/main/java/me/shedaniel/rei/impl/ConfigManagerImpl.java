/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.annotations.Internal;
import me.zeroeightsix.fiber.JanksonSettings;
import me.zeroeightsix.fiber.exception.FiberException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Deprecated
@Internal
public class ConfigManagerImpl implements ConfigManager {
    
    private final File configFile;
    private ConfigObject config;
    private boolean craftableOnly;
    
    public ConfigManagerImpl() {
        this.configFile = new File(FabricLoader.getInstance().getConfigDirectory(), "roughlyenoughitems/config.json5");
        this.craftableOnly = false;
        try {
            loadConfig();
            RoughlyEnoughItemsCore.LOGGER.info("[REI] Config is loaded.");
        } catch (IOException | FiberException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveConfig() throws IOException, FiberException {
        configFile.getParentFile().mkdirs();
        if (!configFile.exists() && !configFile.createNewFile()) {
            RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to save config! Overwriting with default config.");
            config = new OldConfigObjectImpl();
            return;
        }
        try {
            new JanksonSettings().serialize(config.getConfigNode(), Files.newOutputStream(configFile.toPath()), false);
        } catch (Exception e) {
            e.printStackTrace();
            RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to save config! Overwriting with default config.");
            config = new OldConfigObjectImpl();
            return;
        }
    }
    
    @Override
    public void loadConfig() throws IOException, FiberException {
        configFile.getParentFile().mkdirs();
        if (!configFile.exists() || !configFile.canRead()) {
            RoughlyEnoughItemsCore.LOGGER.warn("[REI] Config not found! Creating one.");
            config = new OldConfigObjectImpl();
            saveConfig();
            return;
        }
        boolean failed = false;
        try {
            config = new OldConfigObjectImpl();
            new JanksonSettings().deserialize(config.getConfigNode(), Files.newInputStream(configFile.toPath()));
        } catch (Exception e) {
            e.printStackTrace();
            failed = true;
        }
        if (failed || config == null) {
            RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to load config! Overwriting with default config.");
            config = new OldConfigObjectImpl();
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
        if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            try {
                return Screen.class.cast(Class.forName("me.shedaniel.rei.utils.ClothScreenRegistry").getDeclaredMethod("getConfigScreen", Screen.class).invoke(null, parent));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Screen(new LiteralText("")) {
            @Override
            public void render(int int_1, int int_2, float float_1) {
                renderDirtBackground(0);
                List<String> list = minecraft.textRenderer.wrapStringToWidthAsList(I18n.translate("text.rei.config_api_failed"), width - 100);
                int y = (int) (height / 2 - minecraft.textRenderer.fontHeight * 1.3f / 2 * list.size());
                for (int i = 0; i < list.size(); i++) {
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
