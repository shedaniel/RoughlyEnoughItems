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

package me.shedaniel.rei.api.client.entry.renderer;

import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * A functional interface to provide {@link EntryRenderer} for {@link EntryStack}.
 *
 * @param <T> the entry type
 * @see EntryRenderer
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface EntryRendererProvider<T> {
    static <T> EntryRendererProvider<T> empty() {
        return EntryRenderer.empty();
    }
    
    /**
     * Returns a new {@link EntryRenderer} for a specific {@link EntryStack},
     * a previous {@link EntryRenderer} will be provided, this may be an empty renderer.
     * {@code null} is not an accepted value, return the previous renderer if this provider
     * does not modify the renderer.
     *
     * @param entry the entry stack to render for, do not store or cache this
     * @param last the previous entry renderer
     * @return the new entry renderer, {@code null} is not accepted here
     */
    EntryRenderer<T> provide(EntryStack<T> entry, EntryRenderer<T> last);
}
