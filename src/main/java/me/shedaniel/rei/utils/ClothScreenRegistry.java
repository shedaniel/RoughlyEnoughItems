/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.utils;

import me.shedaniel.cloth.hooks.ScreenHooks;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrderingConfig;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.credits.CreditsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

public class ClothScreenRegistry {
    
    public static final String RESET = "text.cloth-config.reset_value";
    
    @SuppressWarnings("deprecation")
    public static Screen getConfigScreen(Screen parent) {
        final ConfigManager configManager = RoughlyEnoughItemsCore.getConfigManager();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle("text.rei.config.title").setSavingRunnable(() -> {
            try {
                configManager.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ConfigEntryBuilder eb = ConfigEntryBuilder.create();
        ConfigCategory general = builder.getOrCreateCategory("text.rei.config.general");
        general.addEntry(eb.startBooleanToggle("text.rei.config.cheating", configManager.getConfig().cheating)
                .setDefaultValue(false)
                .setSaveConsumer(bool -> configManager.getConfig().cheating = bool)
                .setTooltipSupplier(bool -> {
                    String s = null;
                    if (!bool)
                        s = I18n.translate("text.rei.cheating_disabled");
                    else if (!RoughlyEnoughItemsCore.hasOperatorPermission())
                        s = I18n.translate("text.rei.cheating_enabled_no_perms");
                    else if (RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                        s = I18n.translate("text.rei.cheating_enabled");
                    else
                        s = I18n.translate("text.rei.cheating_limited_enabled");
                    return Optional.ofNullable(new String[]{s});
                })
                .build());
        ConfigCategory appearance = builder.getOrCreateCategory("text.rei.config.appearance");
        appearance.addEntry(eb.startBooleanToggle("text.rei.config.dark_theme", ScreenHelper.isDarkModeEnabled())
                .setDefaultValue(false)
                .setSaveConsumer(bool -> configManager.getConfig().darkTheme = bool)
                .setTooltip(getConfigTooltip("dark_theme"))
                .build());
        appearance.addEntry(eb.startEnumSelector("text.rei.config.recipe_screen_type", RecipeScreenType.class, configManager.getConfig().screenType)
                .setDefaultValue(RecipeScreenType.UNSET)
                .setSaveConsumer(bool -> configManager.getConfig().screenType = (RecipeScreenType) bool)
                .setTooltip(getConfigTooltip("recipe_screen_type"))
                .build());
        appearance.addEntry(eb.startBooleanToggle("text.rei.config.side_search_box", configManager.getConfig().sideSearchField)
                .setDefaultValue(false)
                .setYesNoTextSupplier(bool -> I18n.translate("text.rei.config.side_search_box.text." + bool))
                .setSaveConsumer(bool -> configManager.getConfig().sideSearchField = bool)
                .setTooltip(getConfigTooltip("side_search_box"))
                .build());
        appearance.addEntry(eb.startEnumSelector("text.rei.config.list_ordering", ItemListOrderingConfig.class, ItemListOrderingConfig.from(configManager.getConfig().itemListOrdering, configManager.getConfig().isAscending))
                .setDefaultValue(ItemListOrderingConfig.REGISTRY_ASCENDING)
                .setSaveConsumer(config -> {
                    configManager.getConfig().itemListOrdering = ((ItemListOrderingConfig) config).getOrdering();
                    configManager.getConfig().isAscending = ((ItemListOrderingConfig) config).isAscending();
                })
                .setTooltip(getConfigTooltip("list_ordering", ItemListOrderingConfig.REGISTRY_ASCENDING.toString()))
                .build());
        appearance.addEntry(eb.startBooleanToggle("text.rei.config.item_list_position", configManager.getConfig().mirrorItemPanel)
                .setDefaultValue(false)
                .setYesNoTextSupplier(bool -> I18n.translate(bool ? "text.rei.config.item_list_position.left" : "text.rei.config.item_list_position.right"))
                .setSaveConsumer(bool -> configManager.getConfig().mirrorItemPanel = bool)
                .setTooltip(getConfigTooltip("item_list_position"))
                .build());
        appearance.addEntry(eb.startIntSlider("text.rei.config.max_recipes_per_page", configManager.getConfig().maxRecipePerPage, 2, 99)
                .setDefaultValue(3)
                .setSaveConsumer(i -> configManager.getConfig().maxRecipePerPage = i)
                .setTooltip(getConfigTooltip("max_recipes_per_page"))
                .build());
        appearance.addEntry(eb.startBooleanToggle("text.rei.config.light_gray_recipe_border", configManager.getConfig().lightGrayRecipeBorder)
                .setDefaultValue(false)
                .setYesNoTextSupplier(bool -> I18n.translate("text.rei.config.light_gray_recipe_border.text." + bool))
                .setSaveConsumer(bool -> configManager.getConfig().lightGrayRecipeBorder = bool)
                .setTooltip(getConfigTooltip("light_gray_recipe_border"))
                .build());
        appearance.addEntry(eb.startBooleanToggle("text.rei.config.villager_screen_permanent_scroll_bar", configManager.getConfig().villagerScreenPermanentScrollBar)
                .setYesNoTextSupplier(bool -> I18n.translate("text.rei.config.villager_screen_permanent_scroll_bar.text." + bool))
                .setDefaultValue(false)
                .setSaveConsumer(bool -> configManager.getConfig().villagerScreenPermanentScrollBar = bool)
                .setTooltip(getConfigTooltip("villager_screen_permanent_scroll_bar"))
                .build());
        
        ConfigCategory action = builder.getOrCreateCategory("text.rei.config.action");
        action.addEntry(eb.startEnumSelector("text.rei.config.item_cheating_mode", ItemCheatingMode.class, configManager.getConfig().itemCheatingMode)
                .setDefaultValue(ItemCheatingMode.REI_LIKE)
                .setSaveConsumer(i -> configManager.getConfig().itemCheatingMode = (i instanceof ItemCheatingMode) ? (ItemCheatingMode) i : ItemCheatingMode.REI_LIKE)
                .setEnumNameProvider(e -> I18n.translate("text.rei.config.item_cheating_mode." + ((ItemCheatingMode) e).name().toLowerCase(Locale.ROOT)))
                .setTooltip(getConfigTooltip("item_cheating_mode"))
                .build());
        action.addEntry(eb.startStrField("text.rei.give_command", configManager.getConfig().giveCommand)
                .setDefaultValue("/give {player_name} {item_identifier}{nbt} {count}")
                .setSaveConsumer(s -> configManager.getConfig().giveCommand = s)
                .setTooltip(getConfigTooltip("give_command"))
                .build());
        action.addEntry(eb.startStrField("text.rei.gamemode_command", configManager.getConfig().gamemodeCommand)
                .setDefaultValue("/gamemode {gamemode}")
                .setSaveConsumer(s -> configManager.getConfig().gamemodeCommand = s)
                .setTooltip(getConfigTooltip("gamemode_command"))
                .build());
        action.addEntry(eb.startStrField("text.rei.weather_command", configManager.getConfig().weatherCommand)
                .setDefaultValue("/weather {weather}")
                .setSaveConsumer(s -> configManager.getConfig().weatherCommand = s)
                .setTooltip(getConfigTooltip("weather_command"))
                .build());
        ConfigCategory modules = builder.getOrCreateCategory("text.rei.config.modules");
        modules.addEntry(eb.startBooleanToggle("text.rei.config.enable_craftable_only", configManager.getConfig().enableCraftableOnlyButton)
                .setDefaultValue(false)
                .setSaveConsumer(bool -> configManager.getConfig().enableCraftableOnlyButton = bool)
                .setTooltip(getConfigTooltip("enable_craftable_only"))
                .build());
        modules.addEntry(eb.startBooleanToggle("text.rei.config.enable_util_buttons", configManager.getConfig().showUtilsButtons)
                .setDefaultValue(false)
                .setSaveConsumer(bool -> configManager.getConfig().showUtilsButtons = bool)
                .setTooltip(getConfigTooltip("enable_util_buttons"))
                .build());
        modules.addEntry(eb.startBooleanToggle("text.rei.config.disable_recipe_book", configManager.getConfig().disableRecipeBook)
                .setDefaultValue(false)
                .setSaveConsumer(bool -> configManager.getConfig().disableRecipeBook = bool)
                .setTooltip(getConfigTooltip("disable_recipe_book"))
                .build());
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
