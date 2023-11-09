/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.gui.config.options;

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.gui.config.*;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.config.collapsible.CollapsibleConfigManager;
import me.shedaniel.rei.impl.client.config.entries.ConfigureCategoriesScreen;
import me.shedaniel.rei.impl.client.config.entries.FilteringEntry;
import me.shedaniel.rei.impl.client.gui.config.REIConfigScreen;
import me.shedaniel.rei.impl.client.gui.config.options.configure.PanelBoundariesConfiguration;
import me.shedaniel.rei.impl.client.gui.performance.PerformanceScreen;
import me.shedaniel.rei.impl.client.gui.screen.ConfigReloadingScreen;
import me.shedaniel.rei.impl.client.gui.screen.collapsible.CollapsibleEntriesScreen;
import me.shedaniel.rei.impl.client.search.argument.Argument;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsibleEntryRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.literal;
import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public interface AllREIConfigOptions {
    static <T> CompositeOption<T> make(String id, Function<ConfigObjectImpl, T> bind,
                                       BiConsumer<ConfigObjectImpl, T> save) {
        return new CompositeOption<>(id, translatable("config.rei.options." + id),
                translatable("config.rei.options." + id + ".desc"), bind, save);
    }
    
    CompositeOption<AppearanceTheme> THEME = make("appearance.theme", i -> i.appearance.theme, (i, v) -> i.appearance.theme = v)
            .enumOptions();
    CompositeOption<RecipeBorderType> RECIPE_BORDER = make("appearance.recipe_border", i -> i.appearance.recipeBorder, (i, v) -> i.appearance.recipeBorder = v)
            .enumOptions();
    CompositeOption<Boolean> REDUCED_MOTION = make("appearance.reduced_motion", i -> i.basics.reduceMotion, (i, v) -> i.basics.reduceMotion = v)
            .enabledDisabled();
    CompositeOption<DisplayScreenType> RECIPE_LOOKUP_STYLE = make("appearance.recipe_lookup_style", i -> i.appearance.recipeScreenType, (i, v) -> i.appearance.recipeScreenType = v)
            .options(DisplayScreenType.ORIGINAL, DisplayScreenType.COMPOSITE)
            .defaultValue(() -> DisplayScreenType.ORIGINAL);
    CompositeOption<Boolean> APPEND_MOD_NAMES = make("appearance.append_mod_names", i -> i.advanced.tooltips.appendModNames, (i, v) -> i.advanced.tooltips.appendModNames = v)
            .enabledDisabled();
    CompositeOption<Boolean> APPEND_FAVORITES_HINT = make("appearance.append_favorites_hint", i -> i.advanced.tooltips.displayFavoritesTooltip, (i, v) -> i.advanced.tooltips.displayFavoritesTooltip = v)
            .enabledDisabled();
    CompositeOption<ModifierKeyCode> RECIPE_KEYBIND = make("input.recipe", i -> i.basics.keyBindings.recipeKeybind.copy(), (i, v) -> i.basics.keyBindings.recipeKeybind = v)
            .keybind();
    CompositeOption<ModifierKeyCode> USAGE_KEYBIND = make("input.usage", i -> i.basics.keyBindings.usageKeybind.copy(), (i, v) -> i.basics.keyBindings.usageKeybind = v)
            .keybind();
    CompositeOption<ModifierKeyCode> HIDE_KEYBIND = make("input.hide", i -> i.basics.keyBindings.hideKeybind.copy(), (i, v) -> i.basics.keyBindings.hideKeybind = v)
            .keybind();
    CompositeOption<ModifierKeyCode> PREVIOUS_PAGE_KEYBIND = make("input.previous_page", i -> i.basics.keyBindings.previousPageKeybind.copy(), (i, v) -> i.basics.keyBindings.previousPageKeybind = v)
            .keybind();
    CompositeOption<ModifierKeyCode> NEXT_PAGE_KEYBIND = make("input.next_page", i -> i.basics.keyBindings.nextPageKeybind.copy(), (i, v) -> i.basics.keyBindings.nextPageKeybind = v)
            .keybind();
    CompositeOption<ModifierKeyCode> FOCUS_SEARCH_KEYBIND = make("input.focus_search", i -> i.basics.keyBindings.focusSearchFieldKeybind.copy(), (i, v) -> i.basics.keyBindings.focusSearchFieldKeybind = v)
            .keybind();
    CompositeOption<ModifierKeyCode> COPY_RECIPE_ID_KEYBIND = make("input.copy_recipe_id", i -> i.basics.keyBindings.copyRecipeIdentifierKeybind.copy(), (i, v) -> i.basics.keyBindings.copyRecipeIdentifierKeybind = v)
            .keybind();
    CompositeOption<ModifierKeyCode> FAVORITE_KEYBIND = make("input.favorite", i -> i.basics.keyBindings.favoriteKeybind.copy(), (i, v) -> i.basics.keyBindings.favoriteKeybind = v)
            .keybind();
    CompositeOption<ModifierKeyCode> EXPORT_IMAGE_KEYBIND = make("input.export_image", i -> i.basics.keyBindings.exportImageKeybind.copy(), (i, v) -> i.basics.keyBindings.exportImageKeybind = v)
            .keybind();
    CompositeOption<ModifierKeyCode> BACK_KEYBIND = make("input.back", i -> i.basics.keyBindings.previousScreenKeybind.copy(), (i, v) -> i.basics.keyBindings.previousScreenKeybind = v)
            .keybind();
    // TODO: NATIVE KEYBINDS
    CompositeOption<Boolean> USE_NATIVE_KEYBINDS = make("input.use_native_keybinds", i -> i.basics.keyBindings.useNativeKeybinds, (i, v) -> i.basics.keyBindings.useNativeKeybinds = v)
            .enabledDisabled();
    CompositeOption<SearchFieldLocation> SEARCH_FIELD_LOCATION = make("layout.search_field_location", i -> i.appearance.layout.searchFieldLocation, (i, v) -> i.appearance.layout.searchFieldLocation = v)
            .entry(OptionValueEntry.<SearchFieldLocation>enumOptions().overrideText(location -> {
                if (Minecraft.getInstance().screen instanceof REIConfigScreen configScreen) {
                    return literal(location.toString(configScreen.getOptions().get(AllREIConfigOptions.LOCATION) == DisplayPanelLocation.RIGHT));
                } else {
                    return literal(location.toString(true));
                }
            }));
    CompositeOption<ConfigButtonPosition> CONFIG_BUTTON_LOCATION = make("layout.config_button_location", i -> i.appearance.layout.configButtonLocation, (i, v) -> i.appearance.layout.configButtonLocation = v)
            .entry(OptionValueEntry.<ConfigButtonPosition>enumOptions().overrideText(location -> {
                if (Minecraft.getInstance().screen instanceof REIConfigScreen configScreen) {
                    return literal(location.toString(configScreen.getOptions().get(AllREIConfigOptions.LOCATION) == DisplayPanelLocation.RIGHT));
                } else {
                    return literal(location.toString(true));
                }
            }));
    CompositeOption<Boolean> CRAFTABLE_FILTER = make("layout.craftable_filter", i -> i.appearance.layout.showCraftableOnlyButton, (i, v) -> i.appearance.layout.showCraftableOnlyButton = v)
            .enabledDisabled();
    CompositeOption<PanelBoundary> BOUNDARIES = make("layout.boundaries", i -> {
        return new PanelBoundary(i.appearance.horizontalEntriesBoundaries, i.appearance.verticalEntriesBoundaries,
                i.appearance.horizontalEntriesBoundariesColumns, i.appearance.verticalEntriesBoundariesRows,
                i.appearance.horizontalEntriesBoundariesAlignment, i.appearance.verticalEntriesBoundariesAlignment);
    }, (i, v) -> {
        i.appearance.horizontalEntriesBoundaries = v.horizontalPercentage();
        i.appearance.verticalEntriesBoundaries = v.verticalPercentage();
        i.appearance.horizontalEntriesBoundariesColumns = v.horizontalLimit();
        i.appearance.verticalEntriesBoundariesRows = v.verticalLimit();
        i.appearance.horizontalEntriesBoundariesAlignment = v.horizontalAlign();
        i.appearance.verticalEntriesBoundariesAlignment = v.verticalAlign();
    }).configure(PanelBoundariesConfiguration.INSTANCE);
    CompositeOption<DisplayPanelLocation> LOCATION = make("layout.location", i -> i.advanced.accessibility.displayPanelLocation, (i, v) -> i.advanced.accessibility.displayPanelLocation = v)
            .enumOptions();
    CompositeOption<Boolean> HIDE_LIST_IF_IDLE = make("layout.hide_when_idle", i -> i.appearance.hideEntryPanelIfIdle, (i, v) -> i.appearance.hideEntryPanelIfIdle = v)
            .enabledDisabled();
    CompositeOption<Boolean> LARGER_TABS = make("accessibility.larger_tabs", i -> !i.advanced.accessibility.useCompactTabs, (i, v) -> i.advanced.accessibility.useCompactTabs = !v)
            .enabledDisabled();
    CompositeOption<Boolean> LARGER_ARROW_BUTTONS = make("accessibility.larger_arrow_buttons", i -> !i.advanced.accessibility.useCompactTabButtons, (i, v) -> i.advanced.accessibility.useCompactTabButtons = !v)
            .enabledDisabled();
    CompositeOption<Boolean> SCROLLBAR_VISIBILITY = make("accessibility.scrollbar_visibility", i -> i.advanced.accessibility.compositeScrollBarPermanent, (i, v) -> i.advanced.accessibility.compositeScrollBarPermanent = v)
            .ofBoolean(translatable("config.rei.value.accessibility.scrollbar_visibility.when_scrolling"), translatable("config.rei.value.accessibility.scrollbar_visibility.always"));
    CompositeOption<Boolean> CLICKABLE_RECIPE_ARROWS = make("accessibility.clickable_recipe_arrows", i -> i.advanced.miscellaneous.clickableRecipeArrows, (i, v) -> i.advanced.miscellaneous.clickableRecipeArrows = v)
            .enabledDisabled();
    CompositeOption<Boolean> VANILLA_RECIPE_BOOK = make("accessibility.vanilla_recipe_book", i -> !i.functionality.disableRecipeBook, (i, v) -> i.functionality.disableRecipeBook = !v)
            .enabledDisabled();
    CompositeOption<Boolean> STATUS_EFFECTS_LOCATION = make("accessibility.status_effects_location", i -> i.functionality.leftSideMobEffects, (i, v) -> i.functionality.leftSideMobEffects = v)
            .ofBoolean(translatable("config.rei.value.accessibility.status_effects_location.right"), translatable("config.rei.value.accessibility.status_effects_location.left"));
    CompositeOption<Boolean> INVENTORY_SEARCH = make("accessibility.inventory_search", i -> i.functionality.allowInventoryHighlighting, (i, v) -> i.functionality.allowInventoryHighlighting = v)
            .enabledDisabled();
    CompositeOption<ConfigureCategoriesScreen> CATEGORIES = make("filtering.categories", i -> {
        return new ConfigureCategoriesScreen(
                new HashMap<>(i.getFilteringQuickCraftCategories()),
                new HashSet<>(i.getHiddenCategories()),
                new ArrayList<>(i.getCategoryOrdering())
        );
    }, (i, screen) -> {
        i.setFilteringQuickCraftCategories(screen.getFilteringQuickCraftCategories());
        i.setHiddenCategories(screen.getHiddenCategories());
        i.setCategoryOrdering(screen.getCategoryOrdering());
    }).configure((access, option, onClose) -> {
        ConfigureCategoriesScreen screen = access.get(option);
        screen.parent = Minecraft.getInstance().screen;
        Minecraft.getInstance().setScreen(screen);
    }).requiresLevel();
    CompositeOption<FilteringEntry> CUSTOMIZED_FILTERING = make("filtering.customized_filtering", i -> {
        return FilteringEntry.of(
                CollectionUtils.map(i.advanced.filtering.filteredStacks, EntryStackProvider::provide),
                i.advanced.filtering.filteringRules
        );
    }, (i, entry) -> {
        i.advanced.filtering.filteredStacks = CollectionUtils.map(entry.configFiltered(), EntryStackProvider::ofStack);
        i.advanced.filtering.filteringRules = new ArrayList<>(entry.rules());
    }).configure((access, option, onClose) -> {
        FilteringEntry entry = access.get(option);
        entry.filteringRulesScreen().parent = Minecraft.getInstance().screen;
        Minecraft.getInstance().setScreen(entry.filteringRulesScreen());
    }).requiresLevel();
    CompositeOption<Boolean> FILTER_DISPLAYS = make("filtering.filter_displays", i -> i.advanced.filtering.shouldFilterDisplays, (i, v) -> i.advanced.filtering.shouldFilterDisplays = v)
            .enabledDisabled();
    CompositeOption<Boolean> MERGE_DISPLAYS = make("filtering.merge_displays", i -> i.advanced.layout.mergeDisplayUnderOne, (i, v) -> i.advanced.layout.mergeDisplayUnderOne = v)
            .enabledDisabled();
    CompositeOption<Boolean> DISPLAY_MODE = make("list.display_mode", i -> i.appearance.scrollingEntryListWidget, (i, v) -> i.appearance.scrollingEntryListWidget = v)
            .ofBoolean(translatable("config.rei.value.list.display_mode.paginated"), translatable("config.rei.value.list.display_mode.scrolled"));
    CompositeOption<EntryPanelOrderingConfig> ORDERING = make("list.ordering", i -> i.advanced.layout.entryPanelOrdering, (i, v) -> i.advanced.layout.entryPanelOrdering = v)
            .enumOptions();
    CompositeOption<Double> ZOOM = make("list.zoom", i -> i.advanced.accessibility.entrySize, (i, v) -> i.advanced.accessibility.entrySize = v);
    CompositeOption<Boolean> FOCUS_MODE = make("list.focus_mode", i -> i.appearance.isFocusModeZoomed, (i, v) -> i.appearance.isFocusModeZoomed = v)
            .ofBoolean(translatable("config.rei.value.list.focus_mode.highlighted"), translatable("config.rei.value.list.focus_mode.zoomed"));
    CompositeOption<CollapsibleConfigManager.CollapsibleConfigObject> COLLAPSIBLE_ENTRIES = make("list.collapsible_entries", i -> {
        CollapsibleConfigManager.CollapsibleConfigObject object = new CollapsibleConfigManager.CollapsibleConfigObject();
        object.disabledGroups.addAll(CollapsibleConfigManager.getInstance().getConfig().disabledGroups);
        object.customGroups.addAll(CollectionUtils.map(CollapsibleConfigManager.getInstance().getConfig().customGroups, CollapsibleConfigManager.CustomGroup::copy));
        return object;
    }, (i, object) -> {
        CollapsibleConfigManager.CollapsibleConfigObject actualConfig = CollapsibleConfigManager.getInstance().getConfig();
        actualConfig.disabledGroups.clear();
        actualConfig.disabledGroups.addAll(object.disabledGroups);
        actualConfig.customGroups.clear();
        actualConfig.customGroups.addAll(object.customGroups);
        CollapsibleConfigManager.getInstance().saveConfig();
        ((CollapsibleEntryRegistryImpl) CollapsibleEntryRegistry.getInstance()).recollectCustomEntries();
    }).configure((access, option, onClose) -> {
        Minecraft.getInstance().setScreen(new CollapsibleEntriesScreen(onClose, access.get(option)));
    }).requiresLevel();
    CompositeOption<Boolean> FAVORITES_MODE = make("favorites.mode", i -> i.basics.favoritesEnabled, (i, v) -> i.basics.favoritesEnabled = v)
            .enabledDisabled();
    CompositeOption<FavoriteAddWidgetMode> NEW_FAVORITES_BUTTON_VISIBILITY = make("favorites.new_favorites_button_visibility", i -> i.advanced.layout.favoriteAddWidgetMode, (i, v) -> i.advanced.layout.favoriteAddWidgetMode = v)
            .enumOptions();
    CompositeOption<String> GAME_MODE_COMMAND = make("favorites.game_mode_command", i -> i.advanced.commands.gamemodeCommand, (i, v) -> i.advanced.commands.gamemodeCommand = v)
            .string();
    CompositeOption<String> TIME_COMMAND = make("favorites.time_command", i -> i.advanced.commands.timeCommand, (i, v) -> i.advanced.commands.timeCommand = v)
            .string();
    CompositeOption<String> WEATHER_COMMAND = make("favorites.weather_command", i -> i.advanced.commands.weatherCommand, (i, v) -> i.advanced.commands.weatherCommand = v)
            .string();
    CompositeOption<SyntaxHighlightingMode> SYNTAX_HIGHLIGHTING = make("search.syntax_highlighting", i -> i.appearance.syntaxHighlightingMode, (i, v) -> i.appearance.syntaxHighlightingMode = v)
            .enumOptions();
    CompositeOption<SearchMode> MOD_SEARCH = make("search.mod_search", i -> i.advanced.search.modSearch, (i, v) -> i.advanced.search.modSearch = v)
            .enumOptions();
    CompositeOption<SearchMode> TOOLTIP_SEARCH = make("search.tooltip_search", i -> i.advanced.search.tooltipSearch, (i, v) -> i.advanced.search.tooltipSearch = v)
            .enumOptions();
    CompositeOption<SearchMode> TAG_SEARCH = make("search.tag_search", i -> i.advanced.search.tagSearch, (i, v) -> i.advanced.search.tagSearch = v)
            .enumOptions();
    CompositeOption<SearchMode> IDENTIFIER_SEARCH = make("search.identifier_search", i -> i.advanced.search.identifierSearch, (i, v) -> i.advanced.search.identifierSearch = v)
            .enumOptions();
    CompositeOption<Boolean> ASYNC_SEARCH = make("search.async_search", i -> i.advanced.search.asyncSearch, (i, v) -> i.advanced.search.asyncSearch = v)
            .enabledDisabled();
    CompositeOption<CheatingMode> CHEATS_MODE = make("cheats.mode", i -> i.basics.cheating, (i, v) -> i.basics.cheating = v)
            .enumOptions();
    CompositeOption<ItemCheatingStyle> CHEATS_METHOD = make("cheats.method", i -> i.basics.cheatingStyle, (i, v) -> i.basics.cheatingStyle = v)
            .enumOptions();
    CompositeOption<ItemCheatingMode> CHEATS_AMOUNT = make("cheats.amount", i -> i.functionality.itemCheatingMode, (i, v) -> i.functionality.itemCheatingMode = v)
            .enumOptions();
    CompositeOption<String> GIVE_COMMAND = make("cheats.give_command", i -> i.advanced.commands.giveCommand, (i, v) -> i.advanced.commands.giveCommand = v)
            .string();
    CompositeOption<Boolean> BATCHED_RENDERING = make("performance.batched_rendering", i -> i.advanced.miscellaneous.newFastEntryRendering, (i, v) -> i.advanced.miscellaneous.newFastEntryRendering = v)
            .enabledDisabled();
    CompositeOption<Boolean> CACHED_RENDERING = make("performance.cached_rendering", i -> i.advanced.miscellaneous.cachingFastEntryRendering, (i, v) -> i.advanced.miscellaneous.cachingFastEntryRendering = v)
            .enabledDisabled();
    CompositeOption<Boolean> RELOAD_THREAD = make("performance.reload_thread", i -> i.advanced.miscellaneous.registerRecipesInAnotherThread, (i, v) -> i.advanced.miscellaneous.registerRecipesInAnotherThread = v)
            .ofBoolean(translatable("config.rei.values.performance.reload_thread.main_thread"), translatable("config.rei.values.performance.reload_thread.rei_thread"));
    CompositeOption<Boolean> CACHED_DISPLAY_LOOKUP = make("performance.cached_display_lookup", i -> i.advanced.miscellaneous.cachingDisplayLookup, (i, v) -> i.advanced.miscellaneous.cachingDisplayLookup = v)
            .enabledDisabled();
    CompositeOption<Object> PLUGINS_PERFORMANCE = make("debug.plugins_performance", i -> null, (i, v) -> new Object())
            .details((access, option, onClose) -> Minecraft.getInstance().setScreen(new PerformanceScreen(onClose)))
            .requiresLevel();
    CompositeOption<Boolean> SEARCH_PERFORMANCE = make("debug.search_performance", i -> i.advanced.search.debugSearchTimeRequired, (i, v) -> i.advanced.search.debugSearchTimeRequired = v)
            .enabledDisabled();
    CompositeOption<Boolean> ENTRY_LIST_PERFORMANCE = make("debug.entry_list_performance", i -> i.advanced.layout.debugRenderTimeRequired, (i, v) -> i.advanced.layout.debugRenderTimeRequired = v)
            .enabledDisabled();
    CompositeOption<Object> RELOAD_PLUGINS = make("reset.reload_plugins", i -> null, (i, v) -> new Object())
            .reload((access, option, onClose) -> {
                RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.clear();
                RoughlyEnoughItemsCoreClient.reloadPlugins(null, null);
                while (!PluginManager.areAnyReloading()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Minecraft.getInstance().setScreen(new ConfigReloadingScreen(new TranslatableComponent("text.rei.config.is.reloading"), PluginManager::areAnyReloading, onClose, null));
            }).requiresLevel();
    CompositeOption<Object> RELOAD_SEARCH = make("reset.reload_search", i -> null, (i, v) -> new Object())
            .reload((access, option, onClose) -> {
                Argument.resetCache(true);
            }).requiresLevel();
    // TODO: RESET
}
