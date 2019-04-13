package me.shedaniel.rei.utils;

import me.shedaniel.cloth.api.ConfigScreenBuilder;
import me.shedaniel.cloth.gui.ClothConfigScreen;
import me.shedaniel.cloth.gui.entries.BooleanListEntry;
import me.shedaniel.cloth.gui.entries.EnumListEntry;
import me.shedaniel.cloth.gui.entries.IntegerSliderEntry;
import me.shedaniel.cloth.gui.entries.StringListEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
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
        ConfigScreenBuilder builder = new ClothConfigScreen.Builder(parent, "text.rei.config.title", savedConfig -> {
            try {
                RoughlyEnoughItemsCore.getConfigManager().saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }) {
            @Override
            public ClothConfigScreen build() {
                return new ClothConfigScreen(this.getParentScreen(), this.getTitle(), this.getDataMap(), this.doesConfirmSave(), this.shouldProcessErrors()) {
                    public void onSave(Map<String, List<Pair<String, Object>>> o) {
                        if (getOnSave() != null) {
                            getOnSave().accept(new SelfSavedConfig(o));
                        }
                    }
                    
                    @Override
                    protected void init() {
                        super.init();
                        ButtonWidget w;
                        buttons.add(0, w = new ButtonWidget(6, 6, 60, 20, I18n.translate("text.rei.credits"), widget -> MinecraftClient.getInstance().openScreen(new CreditsScreen(MinecraftClient.getInstance().currentScreen))));
                        children.add(0, w);
                    }
                };
            }
        };
        builder.addCategory("text.rei.config.general").addOption(new BooleanListEntry("text.rei.config.cheating", RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating = bool));
        ConfigScreenBuilder.CategoryBuilder appearance = builder.addCategory("text.rei.config.appearance");
        appearance.addOption(new BooleanListEntry("text.rei.config.side_search_box", RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField, "text.cloth-config.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField = bool));
        appearance.addOption(new EnumListEntry<ItemListOrderingConfig>("text.rei.config.list_ordering", ItemListOrderingConfig.class, ItemListOrderingConfig.from(RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering, RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending), "text.cloth-config.reset_value", () -> ItemListOrderingConfig.REGISTRY_ASCENDING, config -> {
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
    
    private static class SelfSavedConfig extends ClothConfigScreen.Builder.SavedConfig {
        protected SelfSavedConfig(Map<String, List<Pair<String, Object>>> map) {
            super(map);
        }
    }
    
}
