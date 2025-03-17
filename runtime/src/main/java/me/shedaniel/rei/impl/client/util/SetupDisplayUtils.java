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

package me.shedaniel.rei.impl.client.util;

import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.settings.EntryIngredientSetting;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SetupDisplayUtils {
    @SuppressWarnings("RedundantCast")
    public static void TransformFiltering(List<? extends GuiEventListener> setupDisplay) {
        for (EntryWidget widget : Widgets.<EntryWidget>walk(setupDisplay, EntryWidget.class::isInstance)) {
            if (widget.getEntries().size() > 1) {
                Collection<EntryStack<?>> refiltered = EntryRegistry.getInstance().refilterNew(false, widget.getEntries());
                EntryIngredient asEntryIngredient = widget.getEntries() instanceof EntryIngredient ingredient ? ingredient : null;
                if (!refiltered.isEmpty() && !widget.getEntries().equals(refiltered)) {
                    widget.clearStacks();
                    EntryIngredient newIngredient = EntryIngredient.of(refiltered);
                    if (asEntryIngredient != null && (Object) asEntryIngredient.getSetting(EntryIngredientSetting.FOCUS_UUID) instanceof UUID uuid) {
                        newIngredient.setting(EntryIngredientSetting.FOCUS_UUID,
                                new UUID(uuid.getMostSignificantBits() ^ refiltered.size(), uuid.getLeastSignificantBits() ^ refiltered.size()));
                    }
                    widget.entries(newIngredient);
                }
            }
        }
    }
}
