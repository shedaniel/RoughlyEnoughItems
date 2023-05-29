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

package me.shedaniel.rei.api.client.gui.widgets;

import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.ClientInternals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public interface Tooltip {
    static Tooltip create(@Nullable Point point, Collection<Component> texts) {
        return from(point, texts);
    }
    
    static Tooltip create(@Nullable Point point, Component... texts) {
        return create(point, Arrays.asList(texts));
    }
    
    static Tooltip create(Collection<Component> texts) {
        return create(null, texts);
    }
    
    static Tooltip create(Component... texts) {
        return create(Arrays.asList(texts));
    }
    
    static Tooltip from(@Nullable Point point, Collection<Component> entries) {
        return ClientInternals.createTooltip(point, entries);
    }
    
    static Tooltip from(@Nullable Point point, Component... entries) {
        return from(point, Arrays.asList(entries));
    }
    
    static Tooltip from(Collection<Component> entries) {
        return from(null, entries);
    }
    
    static Tooltip from(Component... entries) {
        return from(Arrays.asList(entries));
    }
    
    int getX();
    
    int getY();
    
    List<Component> getText();
    
    Tooltip add(Component text);
    
    default Tooltip addAll(Component... text) {
        for (Component component : text) {
            add(component);
        }
        return this;
    }
    
    default Tooltip addAllTexts(Iterable<Component> text) {
        for (Component component : text) {
            add(component);
        }
        return this;
    }
    
    default void queue() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> REIRuntime.getInstance().queueTooltip(this));
    }
    
    EntryStack<?> getContextStack();
    
    Tooltip withContextStack(EntryStack<?> stack);
}
