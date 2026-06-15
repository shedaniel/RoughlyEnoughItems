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

package me.shedaniel.rei.api.client.gui.config;

import net.minecraft.client.resources.language.I18n;

import java.util.Locale;

/**
 * Controls whether REI synthesizes recipe displays from the client's own data
 * packs when connected to a server, for servers that do not sync recipe data.
 */
public enum ForceLocalRecipesMode {
    /**
     * Never load recipes locally; only display what the server sends (REI display
     * sync or vanilla recipe book entries).
     */
    NEVER,
    /**
     * Additively load recipes locally as a fallback while the server has not sent
     * an REI display sync. Locally-loaded displays are superseded once the server
     * syncs its own displays.
     */
    AUTO,
    /**
     * Always load recipes locally, even when the server provides its own displays.
     */
    ALWAYS,
    ;

    @Override
    public String toString() {
        return I18n.get("config.rei.value.filtering.force_local_recipes." + name().toLowerCase(Locale.ROOT));
    }
}
