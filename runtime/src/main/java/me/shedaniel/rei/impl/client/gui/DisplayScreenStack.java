/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.LinkedHashSet;

public class DisplayScreenStack {
    private static final LinkedHashSet<DisplayScreen> LAST_DISPLAY_SCREENS = Sets.newLinkedHashSetWithExpectedSize(10);
    
    public static void storeDisplayScreen(DisplayScreen screen) {
        while (LAST_DISPLAY_SCREENS.size() >= 10)
            LAST_DISPLAY_SCREENS.remove(Iterables.get(LAST_DISPLAY_SCREENS, 0));
        LAST_DISPLAY_SCREENS.add(screen);
    }
    
    public static boolean hasLastDisplayScreen() {
        return !LAST_DISPLAY_SCREENS.isEmpty();
    }
    
    public static Screen getLastDisplayScreen() {
        DisplayScreen screen = Iterables.getLast(LAST_DISPLAY_SCREENS);
        LAST_DISPLAY_SCREENS.remove(screen);
        screen.recalculateCategoryPage();
        return (Screen) screen;
    }
    
    public static void clear() {
        LAST_DISPLAY_SCREENS.clear();
    }
}
