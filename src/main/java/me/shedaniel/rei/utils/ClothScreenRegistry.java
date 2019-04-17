package me.shedaniel.rei.utils;

import me.shedaniel.cloth.api.ConfigScreenBuilder;
import me.shedaniel.cloth.gui.ClothConfigScreen;
import me.shedaniel.cloth.gui.entries.BooleanListEntry;
import me.shedaniel.cloth.gui.entries.EnumListEntry;
import me.shedaniel.cloth.gui.entries.IntegerSliderEntry;
import me.shedaniel.cloth.gui.entries.StringListEntry;
import me.shedaniel.cloth.hooks.ScreenHooks;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrderingConfig;
import me.shedaniel.rei.gui.credits.CreditsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ClothScreenRegistry {
    
    public static void openConfigScreen(Screen parent) {
        ConfigScreenBuilder builder = ConfigScreenBuilder.create(parent, "text.rei.config.title", savedConfig -> {
            try {
                RoughlyEnoughItemsCore.getConfigManager().saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        builder.addCategory("text.rei.config.general").addOption(new BooleanListEntry("text.rei.config.cheating", RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating = bool));
        ConfigScreenBuilder.CategoryBuilder appearance = builder.addCategory("text.rei.config.appearance");
        appearance.addOption(new BooleanListEntry("text.rei.config.side_search_box", RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField = bool));
        appearance.addOption(new EnumListEntry<>("text.rei.config.list_ordering", ItemListOrderingConfig.class, ItemListOrderingConfig.from(RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering, RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending), "text.cloth-config.reset_value", () -> ItemListOrderingConfig.REGISTRY_ASCENDING, config -> {
            RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering = config.getOrdering();
            RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending = config.isAscending();
        }));
        appearance.addOption(new BooleanListEntry("text.rei.config.item_list_position", RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel = bool) {
            @Override
            public String getYesNoText(boolean bool) {
                return I18n.translate(bool ? "text.rei.config.item_list_position.left" : "text.rei.config.item_list_position.right");
            }
        });
        appearance.addOption(new IntegerSliderEntry("text.rei.config.max_recipes_per_page", 2, 99, RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage, "text.cloth-config.reset_value", () -> 3, i -> RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage = i));
        appearance.addOption(new BooleanListEntry("text.rei.config.light_gray_recipe_border", RoughlyEnoughItemsCore.getConfigManager().getConfig().lightGrayRecipeBorder, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().lightGrayRecipeBorder = bool));
        appearance.addOption(new BooleanListEntry("text.rei.config.prefer_visible_recipes", RoughlyEnoughItemsCore.getConfigManager().getConfig().preferVisibleRecipes, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().preferVisibleRecipes = bool));
        ConfigScreenBuilder.CategoryBuilder action = builder.addCategory("text.rei.config.action");
        action.addOption(new EnumListEntry<>("text.rei.config.item_cheating_mode", ItemCheatingMode.class, RoughlyEnoughItemsCore.getConfigManager().getConfig().itemCheatingMode, "text.cloth-config.reset_value", () -> ItemCheatingMode.REI_LIKE, i -> RoughlyEnoughItemsCore.getConfigManager().getConfig().itemCheatingMode = i, e -> {
            return I18n.translate("text.rei.config.item_cheating_mode." + e.name().toLowerCase());
        }));
        action.addOption(new StringListEntry("text.rei.give_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand, "text.cloth-config.reset_value", () -> "/give {player_name} {item_identifier}{nbt} {count}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand = s));
        action.addOption(new StringListEntry("text.rei.gamemode_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand, "text.cloth-config.reset_value", () -> "/gamemode {gamemode}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand = s));
        action.addOption(new StringListEntry("text.rei.weather_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand, "text.cloth-config.reset_value", () -> "/weather {weather}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand = s));
        ConfigScreenBuilder.CategoryBuilder modules = builder.addCategory("text.rei.config.modules");
        modules.addOption(new BooleanListEntry("text.rei.config.enable_craftable_only", RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton, "text.cloth-config.reset_value", () -> true, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton = bool));
        modules.addOption(new BooleanListEntry("text.rei.config.enable_util_buttons", RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons = bool));
        modules.addOption(new BooleanListEntry("text.rei.config.disable_recipe_book", RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook = bool));
        ConfigScreenBuilder.CategoryBuilder advanced = builder.addCategory("text.rei.config.advanced");
        advanced.addOption(new BooleanListEntry("text.rei.config.enable_legacy_speedcraft_support", RoughlyEnoughItemsCore.getConfigManager().getConfig().enableLegacySpeedCraftSupport, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().enableLegacySpeedCraftSupport = bool));
        ConfigScreenBuilder.CategoryBuilder aprilFools = builder.addCategory("text.rei.config.april_fools");
        aprilFools.addOption(new BooleanListEntry("text.rei.config.april_fools.2019", RoughlyEnoughItemsCore.getConfigManager().getConfig().aprilFoolsFish2019, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().aprilFoolsFish2019 = bool));
        MinecraftClient.getInstance().openScreen(builder.build(screen -> {
            ButtonWidget w = new ButtonWidget(6, 6, 60, 20, I18n.translate("text.rei.credits"), widget -> MinecraftClient.getInstance().openScreen(new CreditsScreen(MinecraftClient.getInstance().currentScreen)));
            ((ScreenHooks) screen).cloth_getButtonWidgets().add(0, w);
            ((ScreenHooks) screen).cloth_getChildren().add(0, w);
        }));
    }
    
    private static class SelfSavedConfig extends ClothConfigScreen.Builder.SavedConfig {
        protected SelfSavedConfig(Map<String, List<Pair<String, Object>>> map) {
            super(map);
        }
    }
    
}
