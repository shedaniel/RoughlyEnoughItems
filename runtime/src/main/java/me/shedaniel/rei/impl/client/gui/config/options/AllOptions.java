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

import java.util.function.BiConsumer;
import java.util.function.Function;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

interface AllOptions {
    static <T> CompositeOption<T> make(String id, Function<ConfigObjectImpl, T> bind,
                                       BiConsumer<ConfigObjectImpl, T> save) {
        return new CompositeOption<>(translatable("config.rei.options." + id),
                translatable("config.rei.options." + id + ".desc"), bind);
    }
    
    CompositeOption<CheatingMode> CHEATING_MODE = make("cheating_mode", i -> i.basics.cheating, (i, v) -> i.basics.cheating = v);
    CompositeOption<Boolean> FAVORITES = make("favorites", i -> i.basics.favoritesEnabled, (i, v) -> i.basics.favoritesEnabled = v)
            .enabledDisabled();
    CompositeOption<Boolean> REDUCED_MOTION = make("reduced_motion", i -> i.basics.reduceMotion, (i, v) -> i.basics.reduceMotion = v)
            .trueFalse();
    CompositeOption<ItemCheatingStyle> CHEATING_STYLE = make("cheating_style", i -> i.basics.cheatingStyle, (i, v) -> i.basics.cheatingStyle = v);
    CompositeOption<DisplayScreenType> DISPLAY_SCREEN_TYPE = make("display_screen_type", i -> i.appearance.recipeScreenType, (i, v) -> i.appearance.recipeScreenType = v);
    CompositeOption<AppearanceTheme> THEME = make("theme", i -> i.appearance.theme, (i, v) -> i.appearance.theme = v);
    CompositeOption<SearchFieldLocation> SEARCH_FIELD_LOCATION = make("search_field_location", i -> i.appearance.layout.searchFieldLocation, (i, v) -> i.appearance.layout.searchFieldLocation = v);
    CompositeOption<ConfigButtonPosition> CONFIG_BUTTON_LOCATION = make("config_button_location", i -> i.appearance.layout.configButtonLocation, (i, v) -> i.appearance.layout.configButtonLocation = v);
    CompositeOption<Boolean> CRAFTABLE_FILTER = make("craftable_filter", i -> i.appearance.layout.showCraftableOnlyButton, (i, v) -> i.appearance.layout.showCraftableOnlyButton = v)
            .enabledDisabled();
}
