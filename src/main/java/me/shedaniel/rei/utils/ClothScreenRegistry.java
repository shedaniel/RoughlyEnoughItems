/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.utils;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrderingConfig;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.zeroeightsix.fiber.exception.FiberException;
import me.zeroeightsix.fiber.tree.ConfigValue;
import net.minecraft.client.gui.screen.Screen;

import java.io.IOException;
import java.util.List;

import static me.shedaniel.fiber2cloth.impl.Fiber2ClothImpl.error;
import static me.shedaniel.fiber2cloth.impl.Fiber2ClothImpl.splitLine;

public class ClothScreenRegistry {
    
    public static Screen getConfigScreen(Screen parent) {
        final ConfigManager configManager = RoughlyEnoughItemsCore.getConfigManager();
        ConfigEntryBuilder configEntryBuilder = ConfigEntryBuilder.create();
        return Fiber2Cloth.create(parent, "roughlyenoughitems", configManager.getConfig().getConfigNode(), "config.roughlyenoughitems.title").setSaveRunnable(() -> {
            try {
                configManager.saveConfig();
            } catch (IOException | FiberException e) {
                e.printStackTrace();
            }
        }).registerNodeEntryFunction(ItemListOrderingConfig.class, o -> {
            ConfigValue<ItemListOrderingConfig> configValue = (ConfigValue<ItemListOrderingConfig>) o;
            return configEntryBuilder.startEnumSelector("config.roughlyenoughitems." + configValue.getName(), ItemListOrderingConfig.class, configValue.getValue())
                    .setDefaultValue(configValue.getDefaultValue())
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue((ItemListOrderingConfig) var))
                    .setErrorSupplier(var -> error((List) configValue.getConstraints(), var, ItemListOrderingConfig.class))
                    .build();
        }).registerNodeEntryFunction(RecipeScreenType.class, o -> {
            ConfigValue<RecipeScreenType> configValue = (ConfigValue<RecipeScreenType>) o;
            return configEntryBuilder.startEnumSelector("config.roughlyenoughitems." + configValue.getName(), RecipeScreenType.class, configValue.getValue())
                    .setDefaultValue(configValue.getDefaultValue())
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue((RecipeScreenType) var))
                    .setErrorSupplier(var -> error((List) configValue.getConstraints(), var, RecipeScreenType.class))
                    .build();
        }).registerNodeEntryFunction(ItemCheatingMode.class, o -> {
            ConfigValue<ItemCheatingMode> configValue = (ConfigValue<ItemCheatingMode>) o;
            return configEntryBuilder.startEnumSelector("config.roughlyenoughitems." + configValue.getName(), ItemCheatingMode.class, configValue.getValue())
                    .setDefaultValue(configValue.getDefaultValue())
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue((ItemCheatingMode) var))
                    .setErrorSupplier(var -> error((List) configValue.getConstraints(), var, ItemCheatingMode.class))
                    .build();
        }).registerNodeEntryFunction(SearchFieldLocation.class, o -> {
            ConfigValue<SearchFieldLocation> configValue = (ConfigValue<SearchFieldLocation>) o;
            return configEntryBuilder.startEnumSelector("config.roughlyenoughitems." + configValue.getName(), SearchFieldLocation.class, configValue.getValue())
                    .setDefaultValue(configValue.getDefaultValue())
                    .setTooltip(splitLine(configValue.getComment()))
                    .setSaveConsumer(var -> configValue.setValue((SearchFieldLocation) var))
                    .setErrorSupplier(var -> error((List) configValue.getConstraints(), var, SearchFieldLocation.class))
                    .build();
        })
                .build().getScreen();
    }
    
}
