/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.utils;

import me.shedaniel.cloth.hooks.ScreenHooks;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ItemCheatingMode;
import me.shedaniel.rei.client.RecipeScreenType;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.config.ItemListOrderingConfig;
import me.shedaniel.rei.gui.credits.CreditsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

import java.io.IOException;
import java.util.Optional;

public class ClothScreenRegistry {
    
    public static final String RESET = "text.cloth-config.reset_value";
    
    public static Screen getConfigScreen(Screen parent) {
        final ConfigManager configManager = RoughlyEnoughItemsCore.getConfigManager();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle("text.rei.config.title").setSavingRunnable(() -> {
            try {
                configManager.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        ConfigCategory general = builder.getOrCreateCategory("text.rei.config.general");
        general.addEntry(new BooleanListEntry("text.rei.config.cheating", configManager.getConfig().cheating, RESET, () -> false, bool -> configManager.getConfig().cheating = bool) {
            @Override
            public Optional<String[]> getTooltip() {
                String s = null;
                if (!getValue())
                    s = I18n.translate("text.rei.cheating_disabled");
                else if (!RoughlyEnoughItemsCore.hasOperatorPermission())
                    s = I18n.translate("text.rei.cheating_enabled_no_perms");
                else if (RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                    s = I18n.translate("text.rei.cheating_enabled");
                else
                    s = I18n.translate("text.rei.cheating_limited_enabled");
                return Optional.ofNullable(new String[]{s});
            }
        });
        ConfigCategory appearance = builder.getOrCreateCategory("text.rei.config.appearance");
        appearance.addEntry(entryBuilder.startBooleanToggle("text.rei.config.dark_theme", ScreenHelper.isDarkModeEnabled()).setDefaultValue(() -> false).setSaveConsumer(bool -> configManager.getConfig().darkTheme = bool).setTooltipSupplier(() -> getConfigTooltip("dark_theme")).buildEntry());
        appearance.addEntry(entryBuilder.startBooleanToggle("text.rei.config.appendModNames", configManager.getConfig().appendModNames)
                .setDefaultValue(true)
                .setSaveConsumer(bool -> configManager.getConfig().appendModNames = bool)
                .setTooltip(getConfigTooltip("appendModNames"))
                .build());
        appearance.addEntry(new EnumListEntry<>("text.rei.config.recipe_screen_type", RecipeScreenType.class, configManager.getConfig().screenType, RESET, () -> RecipeScreenType.UNSET, bool -> configManager.getConfig().screenType = bool, EnumListEntry.DEFAULT_NAME_PROVIDER, () -> getConfigTooltip("recipe_screen_type")));
        appearance.addEntry(entryBuilder.startBooleanToggle("text.rei.config.side_search_box", configManager.getConfig().sideSearchField).setDefaultValue(() -> false).setSaveConsumer(bool -> configManager.getConfig().sideSearchField = bool).setTooltipSupplier(() -> getConfigTooltip("side_search_box")).buildEntry());
        appearance.addEntry(new EnumListEntry<>("text.rei.config.list_ordering", ItemListOrderingConfig.class, ItemListOrderingConfig.from(configManager.getConfig().itemListOrdering, configManager.getConfig().isAscending), RESET, () -> ItemListOrderingConfig.REGISTRY_ASCENDING, config -> {
            configManager.getConfig().itemListOrdering = config.getOrdering();
            configManager.getConfig().isAscending = config.isAscending();
        }, EnumListEntry.DEFAULT_NAME_PROVIDER, () -> getConfigTooltip("list_ordering", ItemListOrderingConfig.REGISTRY_ASCENDING.toString())));
        appearance.addEntry(new BooleanListEntry("text.rei.config.item_list_position", configManager.getConfig().mirrorItemPanel, RESET, () -> false, bool -> configManager.getConfig().mirrorItemPanel = bool, () -> getConfigTooltip("item_list_position")) {
            @Override
            public String getYesNoText(boolean bool) {
                return I18n.translate(bool ? "text.rei.config.item_list_position.left" : "text.rei.config.item_list_position.right");
            }
        });
        appearance.addEntry(new IntegerSliderEntry("text.rei.config.max_recipes_per_page", 2, 99, configManager.getConfig().maxRecipePerPage, RESET, () -> 3, i -> configManager.getConfig().maxRecipePerPage = i, () -> getConfigTooltip("max_recipes_per_page")));
        appearance.addEntry(new BooleanListEntry("text.rei.config.light_gray_recipe_border", configManager.getConfig().lightGrayRecipeBorder, RESET, () -> false, bool -> configManager.getConfig().lightGrayRecipeBorder = bool, () -> getConfigTooltip("light_gray_recipe_border")));
        appearance.addEntry(new BooleanListEntry("text.rei.config.villager_screen_permanent_scroll_bar", configManager.getConfig().villagerScreenPermanentScrollBar, RESET, () -> false, bool -> configManager.getConfig().villagerScreenPermanentScrollBar = bool, () -> getConfigTooltip("villager_screen_permanent_scroll_bar")));
        ConfigCategory action = builder.getOrCreateCategory("text.rei.config.action");
        action.addEntry(new EnumListEntry<>("text.rei.config.item_cheating_mode", ItemCheatingMode.class, configManager.getConfig().itemCheatingMode, RESET, () -> ItemCheatingMode.REI_LIKE, i -> configManager.getConfig().itemCheatingMode = i, e -> {
            return I18n.translate("text.rei.config.item_cheating_mode." + e.name().toLowerCase());
        }, () -> getConfigTooltip("item_cheating_mode")));
        action.addEntry(new StringListEntry("text.rei.give_command", configManager.getConfig().giveCommand, RESET, () -> "/give {player_name} {item_identifier}{nbt} {count}", s -> configManager.getConfig().giveCommand = s, () -> getConfigTooltip("give_command")));
        action.addEntry(new StringListEntry("text.rei.gamemode_command", configManager.getConfig().gamemodeCommand, RESET, () -> "/gamemode {gamemode}", s -> configManager.getConfig().gamemodeCommand = s, () -> getConfigTooltip("gamemode_command")));
        action.addEntry(new StringListEntry("text.rei.weather_command", configManager.getConfig().weatherCommand, RESET, () -> "/weather {weather}", s -> configManager.getConfig().weatherCommand = s, () -> getConfigTooltip("weather_command")));
        action.addEntry(new BooleanListEntry("text.rei.config.register_in_other_thread", configManager.getConfig().registerRecipesInAnotherThread, RESET, () -> true, bool -> configManager.getConfig().registerRecipesInAnotherThread = bool, () -> getConfigTooltip("register_in_other_thread")));
        ConfigCategory modules = builder.getOrCreateCategory("text.rei.config.modules");
        modules.addEntry(new BooleanListEntry("text.rei.config.enable_craftable_only", configManager.getConfig().enableCraftableOnlyButton, RESET, () -> true, bool -> configManager.getConfig().enableCraftableOnlyButton = bool, () -> getConfigTooltip("enable_craftable_only")));
        modules.addEntry(new BooleanListEntry("text.rei.config.enable_util_buttons", configManager.getConfig().showUtilsButtons, RESET, () -> false, bool -> configManager.getConfig().showUtilsButtons = bool, () -> getConfigTooltip("enable_util_buttons")));
        modules.addEntry(new BooleanListEntry("text.rei.config.disable_recipe_book", configManager.getConfig().disableRecipeBook, RESET, () -> false, bool -> configManager.getConfig().disableRecipeBook = bool, () -> getConfigTooltip("disable_recipe_book")));
        return builder.setAfterInitConsumer(screen -> {
            ButtonWidget w = new ButtonWidget(6, 6, 60, 20, I18n.translate("text.rei.credits"), widget -> MinecraftClient.getInstance().openScreen(new CreditsScreen(MinecraftClient.getInstance().currentScreen)));
            ((ScreenHooks) screen).cloth_getButtonWidgets().add(0, w);
            ((ScreenHooks) screen).cloth_getChildren().add(0, w);
        }).build();
    }
    
    private static Optional<String[]> getConfigTooltip(String s, Object... o) {
        if (I18n.hasTranslation("tooltip.rei.config." + s))
            return Optional.ofNullable(I18n.translate("tooltip.rei.config." + s, o).split("\n"));
        return Optional.empty();
    }
    
}
