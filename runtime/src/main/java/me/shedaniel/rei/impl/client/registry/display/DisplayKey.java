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

package me.shedaniel.rei.impl.client.registry.display;

import com.google.common.collect.Maps;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Map;

public class DisplayKey {
    private static final Map<String, DisplayKey> VALUES = Collections.synchronizedMap(Maps.newIdentityHashMap());
    private final CategoryIdentifier<?> categoryIdentifier;
    private final ResourceLocation location;
    
    public static DisplayKey create(CategoryIdentifier<?> categoryIdentifier, ResourceLocation location) {
        String string = (categoryIdentifier + ":" + location).intern();
        return VALUES.computeIfAbsent(string, $ -> new DisplayKey(categoryIdentifier, location));
    }
    
    private DisplayKey(CategoryIdentifier<?> categoryIdentifier, ResourceLocation location) {
        this.categoryIdentifier = categoryIdentifier;
        this.location = location;
    }
    
    @Override
    public String toString() {
        return "DisplayKey[" + this.categoryIdentifier + " / " + this.location + "]";
    }
    
    public CategoryIdentifier<?> categoryIdentifier() {
        return this.categoryIdentifier;
    }
    
    public ResourceLocation location() {
        return this.location;
    }
}
