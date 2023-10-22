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

import me.shedaniel.rei.api.client.gui.config.*;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.config.options.preview.ThemePreviewer;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

interface AllREIConfigOptions {
    static <T> CompositeOption<T> make(String id, Function<ConfigObjectImpl, T> bind,
                                       BiConsumer<ConfigObjectImpl, T> save) {
        return new CompositeOption<>(translatable("config.rei.options." + id),
                translatable("config.rei.options." + id + ".desc"), bind, save);
    }
    
    CompositeOption<AppearanceTheme> THEME = make("appearance.theme", i -> i.appearance.theme, (i, v) -> i.appearance.theme = v)
            .enumOptions()
            .previewer(ThemePreviewer.INSTANCE);
    CompositeOption<RecipeBorderType> RECIPE_BORDER = make("appearance.recipe_border", i -> i.appearance.recipeBorder, (i, v) -> i.appearance.recipeBorder = v)
            .enumOptions();
    CompositeOption<Boolean> REDUCED_MOTION = make("appearance.reduced_motion", i -> i.basics.reduceMotion, (i, v) -> i.basics.reduceMotion = v)
            .enabledDisabled();
    CompositeOption<DisplayScreenType> RECIPE_LOOKUP_STYLE = make("appearance.recipe_lookup_style", i -> i.appearance.recipeScreenType, (i, v) -> i.appearance.recipeScreenType = v)
            .enumOptions();
    CompositeOption<Boolean> APPEND_MOD_NAMES = make("appearance.append_mod_names", i -> i.advanced.tooltips.appendModNames, (i, v) -> i.advanced.tooltips.appendModNames = v)
            .enabledDisabled();
    CompositeOption<Boolean> APPEND_FAVORITES_HINT = make("appearance.append_favorites_hint", i -> i.advanced.tooltips.displayFavoritesTooltip, (i, v) -> i.advanced.tooltips.displayFavoritesTooltip = v)
            .enabledDisabled();
    // TODO: NATIVE KEYBINDS
    CompositeOption<Boolean> USE_NATIVE_KEYBINDS = make("keybinds.use_native_keybinds", i -> i.basics.keyBindings.useNativeKeybinds, (i, v) -> i.basics.keyBindings.useNativeKeybinds = v)
            .enabledDisabled();
    CompositeOption<CheatingMode> CHEATS_MODE = make("cheats.mode", i -> i.basics.cheating, (i, v) -> i.basics.cheating = v)
            .enumOptions();
    CompositeOption<ItemCheatingStyle> CHEATS_METHOD = make("cheats.method", i -> i.basics.cheatingStyle, (i, v) -> i.basics.cheatingStyle = v)
            .enumOptions();
    CompositeOption<ItemCheatingMode> CHEATS_AMOUNT = make("cheats.amount", i -> i.functionality.itemCheatingMode, (i, v) -> i.functionality.itemCheatingMode = v)
            .enumOptions();
    CompositeOption<String> GIVE_COMMAND = make("cheats.give_command", i -> i.advanced.commands.giveCommand, (i, v) -> i.advanced.commands.giveCommand = v);
    CompositeOption<SearchFieldLocation> SEARCH_FIELD_LOCATION = make("layout.search_field_location", i -> i.appearance.layout.searchFieldLocation, (i, v) -> i.appearance.layout.searchFieldLocation = v)
            .enumOptions();
    CompositeOption<ConfigButtonPosition> CONFIG_BUTTON_LOCATION = make("layout.config_button_location", i -> i.appearance.layout.configButtonLocation, (i, v) -> i.appearance.layout.configButtonLocation = v)
            .enumOptions();
    CompositeOption<Boolean> CRAFTABLE_FILTER = make("layout.craftable_filter", i -> i.appearance.layout.showCraftableOnlyButton, (i, v) -> i.appearance.layout.showCraftableOnlyButton = v)
            .enabledDisabled();
    // TODO: BOUNDARIES
    CompositeOption<Boolean> BOUNDARIES = make("layout.boundaries", i -> true, (i, v) -> {
    });
    CompositeOption<Boolean> LARGER_TABS = make("accessibility.larger_tabs", i -> !i.advanced.accessibility.useCompactTabs, (i, v) -> i.advanced.accessibility.useCompactTabs = !v)
            .enabledDisabled();
    CompositeOption<Boolean> LARGER_ARROW_BUTTONS = make("accessibility.larger_arrow_buttons", i -> !i.advanced.accessibility.useCompactTabButtons, (i, v) -> i.advanced.accessibility.useCompactTabButtons = !v)
            .enabledDisabled();
    CompositeOption<Boolean> SCROLLBAR_VISIBILITY = make("accessibility.scrollbar_visibility", i -> i.advanced.accessibility.compositeScrollBarPermanent, (i, v) -> i.advanced.accessibility.compositeScrollBarPermanent = v)
            .ofBoolean(translatable("config.rei.value.accessibility.scrollbar_visibility.when_scrolling"), translatable("config.rei.value.accessibility.scrollbar_visibility.always"));
    CompositeOption<Boolean> CLICKABLE_RECIPE_ARROWS = make("accessibility.clickable_recipe_arrows", i -> i.advanced.miscellaneous.clickableRecipeArrows, (i, v) -> i.advanced.miscellaneous.clickableRecipeArrows = v)
            .enabledDisabled();
    CompositeOption<Boolean> FAVORITES_MODE = make("favorites.mode", i -> i.basics.favoritesEnabled, (i, v) -> i.basics.favoritesEnabled = v)
            .enabledDisabled();
    CompositeOption<FavoriteAddWidgetMode> NEW_FAVORITES_BUTTON_VISIBILITY = make("favorites.new_favorites_button_visibility", i -> i.advanced.layout.favoriteAddWidgetMode, (i, v) -> i.advanced.layout.favoriteAddWidgetMode = v)
            .enumOptions();
    CompositeOption<String> GAME_MODE_COMMAND = make("favorites.game_mode_command", i -> i.advanced.commands.gamemodeCommand, (i, v) -> i.advanced.commands.gamemodeCommand = v);
    CompositeOption<String> TIME_COMMAND = make("favorites.time_command", i -> i.advanced.commands.timeCommand, (i, v) -> i.advanced.commands.timeCommand = v);
    CompositeOption<String> WEATHER_COMMAND = make("favorites.weather_command", i -> i.advanced.commands.weatherCommand, (i, v) -> i.advanced.commands.weatherCommand = v);
    CompositeOption<Boolean> BATCHED_RENDERING = make("performance.batched_rendering", i -> i.advanced.miscellaneous.newFastEntryRendering, (i, v) -> i.advanced.miscellaneous.newFastEntryRendering = v)
            .enabledDisabled();
    CompositeOption<Boolean> CACHED_RENDERING = make("performance.cached_rendering", i -> i.advanced.miscellaneous.cachingFastEntryRendering, (i, v) -> i.advanced.miscellaneous.cachingFastEntryRendering = v)
            .enabledDisabled();
    CompositeOption<Boolean> RELOAD_THREAD = make("performance.reload_thread", i -> i.advanced.miscellaneous.registerRecipesInAnotherThread, (i, v) -> i.advanced.miscellaneous.registerRecipesInAnotherThread = v)
            .ofBoolean(translatable("config.rei.values.performance.reload_thread.main_thread"), translatable("config.rei.values.performance.reload_thread.rei_thread"));
    CompositeOption<Boolean> CACHED_DISPLAY_LOOKUP = make("performance.cached_display_lookup", i -> i.advanced.miscellaneous.cachingDisplayLookup, (i, v) -> i.advanced.miscellaneous.cachingDisplayLookup = v)
            .enabledDisabled();
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
    // TODO: ASYNC_SEARCH
    // TODO: CUSTOMIZED_FILTERING
    CompositeOption<Boolean> FILTER_DISPLAYS = make("filtering.filter_displays", i -> i.advanced.filtering.shouldFilterDisplays, (i, v) -> i.advanced.filtering.shouldFilterDisplays = v)
            .enabledDisabled();
    CompositeOption<Boolean> DISPLAY_MODE = make("list.display_mode", i -> i.appearance.scrollingEntryListWidget, (i, v) -> i.appearance.scrollingEntryListWidget = v)
            .ofBoolean(translatable("config.rei.value.list.display_mode.paginated"), translatable("config.rei.value.list.display_mode.scrolled"));
    CompositeOption<EntryPanelOrderingConfig> ORDERING = make("list.ordering", i -> i.advanced.layout.entryPanelOrdering, (i, v) -> i.advanced.layout.entryPanelOrdering = v)
            .enumOptions();
    CompositeOption<Double> ZOOM = make("list.zoom", i -> i.advanced.accessibility.entrySize, (i, v) -> i.advanced.accessibility.entrySize = v);
    CompositeOption<Boolean> FOCUS_MODE = make("list.focus_mode", i -> i.appearance.isFocusModeZoomed, (i, v) -> i.appearance.isFocusModeZoomed = v)
            .ofBoolean(translatable("config.rei.value.list.focus_mode.highlighted"), translatable("config.rei.value.list.focus_mode.zoomed"));
    // TODO: PLUGINS_PERFORMANCE
    CompositeOption<Boolean> SEARCH_PERFORMANCE = make("debug.search_performance", i -> i.advanced.search.debugSearchTimeRequired, (i, v) -> i.advanced.search.debugSearchTimeRequired = v)
            .enabledDisabled();
    CompositeOption<Boolean> ENTRY_LIST_PERFORMANCE = make("debug.entry_list_performance", i -> i.advanced.layout.debugRenderTimeRequired, (i, v) -> i.advanced.layout.debugRenderTimeRequired = v)
            .enabledDisabled();
    // TODO: RELOAD
    // TODO: RESET
}
