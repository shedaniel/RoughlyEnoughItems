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

import static me.shedaniel.rei.impl.client.gui.config.options.AllREIConfigOptions.*;
import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public interface AllREIConfigGroups {
    static <T> OptionGroup make(String id) {
        return new OptionGroup(translatable("config.rei.options.groups." + id));
    }
    
    OptionGroup APPEARANCE_INTERFACE = make("appearance.interface")
            .add(THEME)
            .add(RECIPE_BORDER)
            .add(REDUCED_MOTION)
            .add(RECIPE_LOOKUP_STYLE);
    OptionGroup APPEARANCE_TOOLTIPS = make("appearance.tooltips")
            .add(APPEND_MOD_NAMES)
            .add(APPEND_FAVORITES_HINT);
    OptionGroup KEYBINDS_KEYBINDS = make("keybinds.keybinds")
            .add(RECIPE_KEYBIND)
            .add(USAGE_KEYBIND)
            .add(HIDE_KEYBIND)
            .add(PREVIOUS_PAGE_KEYBIND)
            .add(NEXT_PAGE_KEYBIND)
            .add(FOCUS_SEARCH_KEYBIND)
            .add(COPY_RECIPE_ID_KEYBIND)
            .add(FAVORITE_KEYBIND)
            .add(EXPORT_IMAGE_KEYBIND)
            .add(BACK_KEYBIND);
    OptionGroup KEYBINDS_ADVANCED = make("keybinds.advanced")
            .add(USE_NATIVE_KEYBINDS);
    OptionGroup CHEATS_CHEATS = make("cheats.cheats")
            .add(CHEATS_MODE)
            .add(CHEATS_METHOD)
            .add(CHEATS_AMOUNT);
    OptionGroup CHEATS_ADVANCED = make("cheats.advanced")
            .add(GIVE_COMMAND);
    OptionGroup LAYOUT_WIDGETS = make("layout.widgets")
            .add(SEARCH_FIELD_LOCATION)
            .add(CONFIG_BUTTON_LOCATION)
            .add(CRAFTABLE_FILTER);
    OptionGroup LAYOUT_PANEL = make("layout.panel")
            .add(BOUNDARIES)
            .add(LOCATION)
            .add(HIDE_LIST_IF_IDLE);
    OptionGroup ACCESSIBILITY_DISPLAY = make("accessibility.display")
            .add(LARGER_TABS)
            .add(LARGER_ARROW_BUTTONS);
    OptionGroup ACCESSIBILITY_WIDGETS = make("accessibility.widgets")
            .add(SCROLLBAR_VISIBILITY)
            .add(CLICKABLE_RECIPE_ARROWS);
    OptionGroup ACCESSIBILITY_FEATURES = make("accessibility.features")
            .add(VANILLA_RECIPE_BOOK)
            .add(STATUS_EFFECTS_LOCATION)
            .add(INVENTORY_SEARCH);
    OptionGroup FAVORITES_FAVORITES = make("favorites.favorites")
            .add(FAVORITES_MODE)
            .add(NEW_FAVORITES_BUTTON_VISIBILITY);
    OptionGroup FAVORITES_ADVANCED = make("favorites.advanced")
            .add(GAME_MODE_COMMAND)
            .add(TIME_COMMAND)
            .add(WEATHER_COMMAND);
    OptionGroup PERFORMANCE_RENDERING = make("performance.rendering")
            .add(BATCHED_RENDERING)
            .add(CACHED_RENDERING);
    OptionGroup PERFORMANCE_RELOAD = make("performance.reload")
            .add(RELOAD_THREAD)
            .add(CACHED_DISPLAY_LOOKUP);
    OptionGroup SEARCH_APPEARANCE = make("search.appearance")
            .add(SYNTAX_HIGHLIGHTING);
    OptionGroup SEARCH_FILTERS = make("search.filters")
            .add(MOD_SEARCH)
            .add(TOOLTIP_SEARCH)
            .add(TAG_SEARCH)
            .add(IDENTIFIER_SEARCH);
    OptionGroup SEARCH_ADVANCED = make("search.advanced");
    OptionGroup FILTERING_FILTERING = make("filtering.filtering")
            .add(CATEGORIES)
            .add(CUSTOMIZED_FILTERING);
    OptionGroup FILTERING_ADVANCED = make("filtering.advanced")
            .add(FILTER_DISPLAYS)
            .add(MERGE_DISPLAYS);
    OptionGroup LIST_ENTRIES = make("list.entries")
            .add(DISPLAY_MODE)
            .add(ORDERING)
            .add(ZOOM)
            .add(FOCUS_MODE);
    OptionGroup LIST_COLLAPSIBLE_GROUPS = make("list.collapsible_groups")
            .add(COLLAPSIBLE_ENTRIES);
    OptionGroup DEBUG_PERFORMANCE = make("debug.performance")
            .add(PLUGINS_PERFORMANCE)
            .add(SEARCH_PERFORMANCE)
            .add(ENTRY_LIST_PERFORMANCE);
    OptionGroup RESET_RELOAD = make("reset.reload")
            .add(RELOAD_PLUGINS)
            .add(RELOAD_SEARCH);
    OptionGroup RESET_RESET = make("reset.reset");
}
