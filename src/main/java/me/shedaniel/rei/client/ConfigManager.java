package me.shedaniel.rei.client;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.util.Pair;
import me.shedaniel.cloth.api.ConfigScreenBuilder;
import me.shedaniel.cloth.gui.ClothConfigScreen;
import me.shedaniel.cloth.gui.entries.BooleanListEntry;
import me.shedaniel.cloth.gui.entries.IntegerListEntry;
import me.shedaniel.cloth.gui.entries.StringListEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.gui.config.ItemListOrderingEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.resource.language.I18n;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        ClothConfigScreen.Builder builder = new ClothConfigScreen.Builder(parent, I18n.translate("text.rei.config.title"), null);
        builder.addCategory(I18n.translate("text.rei.config.general")).addOption(new BooleanListEntry("text.rei.config.cheating", config.cheating, bool -> config.cheating = bool));
        ConfigScreenBuilder.CategoryBuilder appearance = builder.addCategory(I18n.translate("text.rei.config.appearance"));
        appearance.addOption(new BooleanListEntry("text.rei.config.side_search_box", config.sideSearchField, bool -> config.sideSearchField = bool));
        appearance.addOption(new ItemListOrderingEntry("text.rei.config.list_ordering", new Pair<>(config.itemListOrdering, config.isAscending)));
        appearance.addOption(new BooleanListEntry("text.rei.config.mirror_rei", config.mirrorItemPanel, bool -> config.mirrorItemPanel = bool));
        appearance.addOption(new IntegerListEntry("text.rei.config.max_recipes_per_page", config.maxRecipePerPage, i -> config.maxRecipePerPage = i).setMinimum(2).setMaximum(99));
        ConfigScreenBuilder.CategoryBuilder modules = builder.addCategory(I18n.translate("text.rei.config.modules"));
        modules.addOption(new BooleanListEntry("text.rei.config.enable_craftable_only", config.enableCraftableOnlyButton, bool -> config.enableCraftableOnlyButton = bool));
        modules.addOption(new BooleanListEntry("text.rei.config.enable_util_buttons", config.showUtilsButtons, bool -> config.showUtilsButtons = bool));
        modules.addOption(new BooleanListEntry("text.rei.config.disable_recipe_book", config.disableRecipeBook, bool -> config.disableRecipeBook = bool));
        ConfigScreenBuilder.CategoryBuilder advanced = builder.addCategory(I18n.translate("text.rei.config.advanced"));
        advanced.addOption(new StringListEntry("text.rei.give_command", config.giveCommand, s -> config.giveCommand = s));
        advanced.addOption(new StringListEntry("text.rei.gamemode_command", config.gamemodeCommand, s -> config.gamemodeCommand = s));
        advanced.addOption(new StringListEntry("text.rei.weather_command", config.weatherCommand, s -> config.weatherCommand = s));
        advanced.addOption(new BooleanListEntry("text.rei.config.prefer_visible_recipes", config.preferVisibleRecipes, bool -> config.preferVisibleRecipes = bool));
        builder.setOnSave(savedConfig -> {
            try {
                ConfigManager.this.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        MinecraftClient.getInstance().openScreen(builder.build());
    }
    
}
