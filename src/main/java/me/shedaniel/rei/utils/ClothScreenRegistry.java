package me.shedaniel.rei.utils;

import me.shedaniel.cloth.api.ConfigScreenBuilder;
import me.shedaniel.cloth.gui.entries.BooleanListEntry;
import me.shedaniel.cloth.gui.entries.IntegerListEntry;
import me.shedaniel.cloth.gui.entries.StringListEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.gui.config.ItemListOrderingEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.util.Pair;

import java.io.IOException;

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
        appearance.addOption(new ItemListOrderingEntry("text.rei.config.list_ordering", new Pair<>(RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering, RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending)));
        appearance.addOption(new BooleanListEntry("text.rei.config.mirror_rei", RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel = bool));
        appearance.addOption(new IntegerListEntry("text.rei.config.max_recipes_per_page", RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage, "text.cloth-config.reset_value", () -> 3, i -> RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage = i).setMinimum(2).setMaximum(99));
        ConfigScreenBuilder.CategoryBuilder modules = builder.addCategory("text.rei.config.modules");
        modules.addOption(new BooleanListEntry("text.rei.config.enable_craftable_only", RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton, "text.cloth-config.reset_value", () -> true, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton = bool));
        modules.addOption(new BooleanListEntry("text.rei.config.enable_util_buttons", RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons = bool));
        modules.addOption(new BooleanListEntry("text.rei.config.disable_recipe_book", RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook = bool));
        ConfigScreenBuilder.CategoryBuilder advanced = builder.addCategory("text.rei.config.advanced");
        advanced.addOption(new StringListEntry("text.rei.give_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand, "text.cloth-config.reset_value", () -> "/give {player_name} {item_identifier}{nbt} {count}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand = s));
        advanced.addOption(new StringListEntry("text.rei.gamemode_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand, "text.cloth-config.reset_value", () -> "/gamemode {gamemode}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand = s));
        advanced.addOption(new StringListEntry("text.rei.weather_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand, "text.cloth-config.reset_value", () -> "/weather {weather}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand = s));
        advanced.addOption(new BooleanListEntry("text.rei.config.prefer_visible_recipes", RoughlyEnoughItemsCore.getConfigManager().getConfig().preferVisibleRecipes, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().preferVisibleRecipes = bool));
        advanced.addOption(new BooleanListEntry("text.rei.config.enable_legacy_speedcraft_support", RoughlyEnoughItemsCore.getConfigManager().getConfig().enableLegacySpeedCraftSupport, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().enableLegacySpeedCraftSupport = bool));
        ConfigScreenBuilder.CategoryBuilder aprilFools = builder.addCategory("text.rei.config.april_fools");
        aprilFools.addOption(new BooleanListEntry("text.rei.config.april_fools.2019", RoughlyEnoughItemsCore.getConfigManager().getConfig().aprilFoolsFish2019, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().aprilFoolsFish2019 = bool));
        MinecraftClient.getInstance().openScreen(builder.build());
    }
    
}
