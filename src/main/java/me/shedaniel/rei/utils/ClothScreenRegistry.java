/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.utils;

import me.shedaniel.rei.RoughlyEnoughItemsClient;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ScreenHooks;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrderingConfig;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.credits.CreditsScreen;
import me.shedaniel.reiclothconfig2.api.ConfigBuilder;
import me.shedaniel.reiclothconfig2.api.ConfigCategory;
import me.shedaniel.reiclothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.reiclothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.reiclothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.reiclothconfig2.gui.entries.IntegerSliderEntry;
import me.shedaniel.reiclothconfig2.gui.entries.StringListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ClothScreenRegistry {
    
    public static final String RESET = "text.cloth-config.reset_value";
    
    @SuppressWarnings("deprecation")
    public static GuiScreen getConfigScreen(GuiScreen parent) {
        final ConfigManager configManager = RoughlyEnoughItemsClient.getConfigManager();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle("text.rei.config.title").setSavingRunnable(() -> {
            try {
                configManager.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ConfigEntryBuilder eb = ConfigEntryBuilder.create();
        ConfigCategory general = builder.getOrCreateCategory("text.rei.config.general");
        general.addEntry(new BooleanListEntry("text.rei.config.cheating", configManager.getConfig().cheating, RESET, () -> false, bool -> configManager.getConfig().cheating = bool) {
            @Override
            public Optional<String[]> getTooltip() {
                String s = null;
                if (!getValue())
                    s = I18n.format("text.rei.cheating_disabled");
                else if (Minecraft.getInstance().isSingleplayer())
                    s = I18n.format("text.rei.cheating_enabled");
                else
                    s = I18n.format("text.rei.cheating_limited_enabled");
                return Optional.ofNullable(new String[]{s});
            }
        });
        ConfigCategory appearance = builder.getOrCreateCategory("text.rei.config.appearance");
        appearance.addEntry(eb.startBooleanToggle("text.rei.config.dark_theme", ScreenHelper.isDarkModeEnabled()).setDefaultValue(() -> false).setSaveConsumer(bool -> configManager.getConfig().darkTheme = bool).setTooltipSupplier(() -> getConfigTooltip("dark_theme")).buildEntry());
        appearance.addEntry(eb.startEnumSelector("text.rei.config.recipe_screen_type", RecipeScreenType.class, configManager.getConfig().screenType).setDefaultValue(() -> RecipeScreenType.UNSET).setSaveConsumer(bool -> configManager.getConfig().screenType = (RecipeScreenType) bool).setTooltipSupplier(() -> getConfigTooltip("recipe_screen_type")).buildEntry());
        appearance.addEntry(eb.startBooleanToggle("text.rei.config.side_search_box", configManager.getConfig().sideSearchField).setDefaultValue(() -> false).setSaveConsumer(bool -> configManager.getConfig().sideSearchField = bool).setTooltipSupplier(() -> getConfigTooltip("side_search_box")).buildEntry());
        appearance.addEntry(eb.startEnumSelector("text.rei.config.list_ordering", ItemListOrderingConfig.class, ItemListOrderingConfig.from(configManager.getConfig().itemListOrdering, configManager.getConfig().isAscending)).setDefaultValue(() -> ItemListOrderingConfig.REGISTRY_ASCENDING).setSaveConsumer(config -> {
            configManager.getConfig().itemListOrdering = ((ItemListOrderingConfig) config).getOrdering();
            configManager.getConfig().isAscending = ((ItemListOrderingConfig) config).isAscending();
        }).setTooltipSupplier(() -> getConfigTooltip("list_ordering", ItemListOrderingConfig.REGISTRY_ASCENDING.toString())).buildEntry());
        appearance.addEntry(eb.startBooleanToggle("text.rei.config.item_list_position", configManager.getConfig().mirrorItemPanel).setDefaultValue(() -> false).setYesNoTextSupplier(bool -> I18n.format(bool ? "text.rei.config.item_list_position.left" : "text.rei.config.item_list_position.right")).setSaveConsumer(bool -> configManager.getConfig().mirrorItemPanel = bool).buildEntry());
        appearance.addEntry(new IntegerSliderEntry("text.rei.config.max_recipes_per_page", 2, 99, configManager.getConfig().maxRecipePerPage, RESET, () -> 3, i -> configManager.getConfig().maxRecipePerPage = i, () -> getConfigTooltip("max_recipes_per_page")));
        appearance.addEntry(new BooleanListEntry("text.rei.config.light_gray_recipe_border", configManager.getConfig().lightGrayRecipeBorder, RESET, () -> false, bool -> configManager.getConfig().lightGrayRecipeBorder = bool, () -> getConfigTooltip("light_gray_recipe_border")));
        appearance.addEntry(new BooleanListEntry("text.rei.config.villager_screen_permanent_scroll_bar", configManager.getConfig().villagerScreenPermanentScrollBar, RESET, () -> false, bool -> configManager.getConfig().villagerScreenPermanentScrollBar = bool, () -> getConfigTooltip("villager_screen_permanent_scroll_bar")));
        ConfigCategory action = builder.getOrCreateCategory("text.rei.config.action");
        action.addEntry(new EnumListEntry<>("text.rei.config.item_cheating_mode", ItemCheatingMode.class, configManager.getConfig().itemCheatingMode, RESET, () -> ItemCheatingMode.REI_LIKE, i -> configManager.getConfig().itemCheatingMode = i, e -> {
            return I18n.format("text.rei.config.item_cheating_mode." + e.name().toLowerCase(Locale.ROOT));
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
            GuiButton w = new GuiButton(14, 6, 6, 60, 20, I18n.format("text.rei.credits")) {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    super.onClick(mouseX, mouseY);
                    Minecraft.getInstance().displayGuiScreen(new CreditsScreen(Minecraft.getInstance().currentScreen));
                }
            };
            ((ScreenHooks) screen).cloth_getButtonWidgets().add(0, w);
            ((ScreenHooks) screen).cloth_getChildren().add(0, w);
        }).build();
    }
    
    private static Optional<String[]> getConfigTooltip(String s, Object... o) {
        if (I18n.hasKey("tooltip.rei.config." + s))
            return Optional.ofNullable(I18n.format("tooltip.rei.config." + s, o).split("\n"));
        return Optional.empty();
    }
    
    public static void openConfigScreen() {
        Minecraft.getInstance().displayGuiScreen(getConfigScreenNullable(Minecraft.getInstance().currentScreen));
    }
    
    public static void openConfigScreen(GuiScreen parent) {
        Minecraft.getInstance().displayGuiScreen(getConfigScreenNullable(parent));
    }
    
    public static GuiScreen getConfigScreenNullable(GuiScreen parent) {
        try {
            return getConfigScreen(parent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new GuiScreen() {
            @Override
            public void render(int int_1, int int_2, float float_1) {
                drawWorldBackground(0);
                List<String> list = mc.fontRenderer.listFormattedStringToWidth(I18n.format("text.rei.config_api_failed"), width - 100);
                int y = (int) (height / 2 - mc.fontRenderer.FONT_HEIGHT * 1.3f / 2 * list.size());
                for(int i = 0; i < list.size(); i++) {
                    String s = list.get(i);
                    drawCenteredString(mc.fontRenderer, s, width / 2, y, -1);
                    y += mc.fontRenderer.FONT_HEIGHT;
                }
                super.render(int_1, int_2, float_1);
            }
            
            @Override
            protected void initGui() {
                super.initGui();
                addButton(new GuiButton(139183, width / 2 - 100, height - 26, 200, 20, I18n.format("text.rei.back")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        super.onClick(mouseX, mouseY);
                        Minecraft.getInstance().displayGuiScreen(parent);
                    }
                });
            }
            
            @Override
            public boolean keyPressed(int int_1, int int_2, int int_3) {
                if (int_1 == 256 && this.allowCloseWithEscape()) {
                    this.mc.displayGuiScreen(parent);
                    return true;
                }
                return super.keyPressed(int_1, int_2, int_3);
            }
        };
    }
    
}
