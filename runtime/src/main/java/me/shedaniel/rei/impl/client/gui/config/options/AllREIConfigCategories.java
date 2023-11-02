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

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static me.shedaniel.rei.impl.client.gui.config.options.AllREIConfigGroups.*;
import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public interface AllREIConfigCategories {
    static OptionCategory make(String key) {
        return OptionCategory.of(new ResourceLocation("roughlyenoughitems:textures/gui/config/" + key + ".png"),
                translatable("config.rei.categories." + key));
    }
    
    OptionCategory APPEARANCE = make("appearance")
            .add(APPEARANCE_INTERFACE)
            .add(APPEARANCE_TOOLTIPS);
    OptionCategory KEYBINDS = make("keybinds")
            .add(KEYBINDS_KEYBINDS)
            /*.add(KEYBINDS_ADVANCED)*/;
    OptionCategory CHEATS = make("cheats")
            .add(CHEATS_CHEATS)
            .add(CHEATS_ADVANCED);
    OptionCategory LAYOUT = make("layout")
            .add(LAYOUT_WIDGETS)
            .add(LAYOUT_PANEL);
    OptionCategory ACCESSIBILITY = make("accessibility")
            .add(ACCESSIBILITY_DISPLAY)
            .add(ACCESSIBILITY_WIDGETS)
            .add(ACCESSIBILITY_FEATURES);
    OptionCategory FAVORITES = make("favorites")
            .add(FAVORITES_FAVORITES)
            .add(FAVORITES_ADVANCED);
    OptionCategory PERFORMANCE = make("performance")
            .add(PERFORMANCE_RENDERING)
            .add(PERFORMANCE_RELOAD);
    OptionCategory SEARCH = make("search")
            .add(SEARCH_APPEARANCE)
            .add(SEARCH_FILTERS)
            .add(SEARCH_ADVANCED);
    OptionCategory FILTERING = make("filtering")
            .add(FILTERING_FILTERING)
            .add(FILTERING_ADVANCED);
    OptionCategory LIST = make("list")
            .add(LIST_ENTRIES)
            .add(LIST_COLLAPSIBLE_GROUPS);
    OptionCategory DEBUG = make("debug")
            .add(DEBUG_PERFORMANCE);
    OptionCategory FLAGS = make("flags");
    OptionCategory RESET = make("reset")
            .add(RESET_RELOAD)
            .add(RESET_RESET);
    List<OptionCategory> CATEGORIES = ImmutableList.of(
            APPEARANCE,
            KEYBINDS,
            CHEATS,
            LAYOUT,
            ACCESSIBILITY,
            FAVORITES,
            PERFORMANCE,
            SEARCH,
            FILTERING,
            LIST,
            DEBUG,
            FLAGS,
            RESET
    );
}
