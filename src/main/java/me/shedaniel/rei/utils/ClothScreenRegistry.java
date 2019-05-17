/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.utils;

import me.shedaniel.cloth.api.ConfigScreenBuilder;
import me.shedaniel.cloth.gui.entries.BooleanListEntry;
import me.shedaniel.cloth.gui.entries.EnumListEntry;
import me.shedaniel.cloth.gui.entries.IntegerSliderEntry;
import me.shedaniel.cloth.gui.entries.StringListEntry;
import me.shedaniel.cloth.hooks.ScreenHooks;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ItemCheatingMode;
import me.shedaniel.rei.client.RecipeScreenType;
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
        ConfigScreenBuilder builder = ConfigScreenBuilder.create(parent, "text.rei.config.title", savedConfig -> {
            try {
                RoughlyEnoughItemsCore.getConfigManager().saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        builder.addCategory("text.rei.config.general").addOption(new BooleanListEntry("text.rei.config.cheating", RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating, RESET, () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating = bool) {
            @Override
            public Optional<String[]> getTooltip() {
                String s = null;
                if (!getObject())
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
        ConfigScreenBuilder.CategoryBuilder appearance = builder.addCategory("text.rei.config.appearance");
        appearance.addOption(new EnumListEntry<>("text.rei.config.recipe_screen_type", RecipeScreenType.class, RoughlyEnoughItemsCore.getConfigManager().getConfig().screenType, RESET, () -> RecipeScreenType.UNSET, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().screenType = bool, EnumListEntry.DEFAULT_NAME_PROVIDER, () -> getConfigTooltip("recipe_screen_type")));
        appearance.addOption(new BooleanListEntry("text.rei.config.side_search_box", RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField, RESET, () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField = bool, () -> getConfigTooltip("side_search_box")));
        appearance.addOption(new EnumListEntry<>("text.rei.config.list_ordering", ItemListOrderingConfig.class, ItemListOrderingConfig.from(RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering, RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending), RESET, () -> ItemListOrderingConfig.REGISTRY_ASCENDING, config -> {
            RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering = config.getOrdering();
            RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending = config.isAscending();
        }, EnumListEntry.DEFAULT_NAME_PROVIDER, () -> getConfigTooltip("list_ordering", ItemListOrderingConfig.REGISTRY_ASCENDING.toString())));
        appearance.addOption(new BooleanListEntry("text.rei.config.item_list_position", RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel, RESET, () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel = bool, () -> getConfigTooltip("item_list_position")) {
            @Override
            public String getYesNoText(boolean bool) {
                return I18n.translate(bool ? "text.rei.config.item_list_position.left" : "text.rei.config.item_list_position.right");
            }
        });
        appearance.addOption(new IntegerSliderEntry("text.rei.config.max_recipes_per_page", 2, 99, RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage, RESET, () -> 3, i -> RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage = i, () -> getConfigTooltip("max_recipes_per_page")));
        appearance.addOption(new BooleanListEntry("text.rei.config.light_gray_recipe_border", RoughlyEnoughItemsCore.getConfigManager().getConfig().lightGrayRecipeBorder, RESET, () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().lightGrayRecipeBorder = bool, () -> getConfigTooltip("light_gray_recipe_border")));
        appearance.addOption(new BooleanListEntry("text.rei.config.prefer_visible_recipes", RoughlyEnoughItemsCore.getConfigManager().getConfig().preferVisibleRecipes, RESET, () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().preferVisibleRecipes = bool, () -> getConfigTooltip("prefer_visible_recipes")));
        ConfigScreenBuilder.CategoryBuilder action = builder.addCategory("text.rei.config.action");
        action.addOption(new EnumListEntry<>("text.rei.config.item_cheating_mode", ItemCheatingMode.class, RoughlyEnoughItemsCore.getConfigManager().getConfig().itemCheatingMode, RESET, () -> ItemCheatingMode.REI_LIKE, i -> RoughlyEnoughItemsCore.getConfigManager().getConfig().itemCheatingMode = i, e -> {
            return I18n.translate("text.rei.config.item_cheating_mode." + e.name().toLowerCase());
        }, () -> getConfigTooltip("item_cheating_mode")));
        action.addOption(new StringListEntry("text.rei.give_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand, RESET, () -> "/give {player_name} {item_identifier}{nbt} {count}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand = s, () -> getConfigTooltip("give_command")));
        action.addOption(new StringListEntry("text.rei.gamemode_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand, RESET, () -> "/gamemode {gamemode}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand = s, () -> getConfigTooltip("gamemode_command")));
        action.addOption(new StringListEntry("text.rei.weather_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand, RESET, () -> "/weather {weather}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand = s, () -> getConfigTooltip("weather_command")));
        ConfigScreenBuilder.CategoryBuilder modules = builder.addCategory("text.rei.config.modules");
        modules.addOption(new BooleanListEntry("text.rei.config.enable_craftable_only", RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton, RESET, () -> true, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton = bool, () -> getConfigTooltip("enable_craftable_only")));
        modules.addOption(new BooleanListEntry("text.rei.config.enable_util_buttons", RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons, RESET, () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons = bool, () -> getConfigTooltip("enable_util_buttons")));
        modules.addOption(new BooleanListEntry("text.rei.config.disable_recipe_book", RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook, RESET, () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook = bool, () -> getConfigTooltip("disable_recipe_book")));
        ConfigScreenBuilder.CategoryBuilder aprilFools = builder.addCategory("text.rei.config.april_fools");
        aprilFools.addOption(new BooleanListEntry("text.rei.config.april_fools.2019", RoughlyEnoughItemsCore.getConfigManager().getConfig().aprilFoolsFish2019, RESET, () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().aprilFoolsFish2019 = bool, () -> getConfigTooltip("april_fools.2019")));
        return builder.build(screen -> {
            ButtonWidget w = new ButtonWidget(6, 6, 60, 20, I18n.translate("text.rei.credits"), widget -> MinecraftClient.getInstance().openScreen(new CreditsScreen(MinecraftClient.getInstance().currentScreen)));
            ((ScreenHooks) screen).cloth_getButtonWidgets().add(0, w);
            ((ScreenHooks) screen).cloth_getChildren().add(0, w);
        });
    }
    
    private static Optional<String[]> getConfigTooltip(String s, Object... o) {
        if (I18n.hasTranslation("tooltip.rei.config." + s))
            return Optional.ofNullable(I18n.translate("tooltip.rei.config." + s, o).split("\n"));
        return Optional.empty();
    }
    
}
