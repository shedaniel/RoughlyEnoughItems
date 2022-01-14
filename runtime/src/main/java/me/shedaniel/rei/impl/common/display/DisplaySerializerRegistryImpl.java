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

package me.shedaniel.rei.impl.common.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DisplaySerializerRegistryImpl implements DisplaySerializerRegistry {
    private final Map<CategoryIdentifier<?>, DisplaySerializer<?>> serializers = new HashMap<>();
    
    @Override
    public <D extends Display> void register(CategoryIdentifier<? extends D> categoryId, DisplaySerializer<D> serializer) {
        serializers.put(categoryId, serializer);
    }
    
    @Override
    public <D extends Display> void registerNotSerializable(CategoryIdentifier<D> categoryId) {
        serializers.remove(categoryId);
    }
    
    @Override
    public <D extends Display> boolean hasSerializer(CategoryIdentifier<D> categoryId) {
        return serializers.containsKey(categoryId);
    }
    
    @Override
    public <D extends Display> CompoundTag save(D display, CompoundTag tag) {
        CategoryIdentifier<?> categoryId = display.getCategoryIdentifier();
        return Objects.requireNonNull((DisplaySerializer<D>) serializers.get(categoryId), "Category " + categoryId + " does not have a display serializer!")
                .save(tag, display);
    }
    
    @Override
    public <D extends Display> D read(CategoryIdentifier<? extends D> categoryId, CompoundTag tag) {
        return Objects.requireNonNull((DisplaySerializer<D>) serializers.get(categoryId), "Category " + categoryId + " does not have a display serializer!")
                .read(tag);
    }
    
    @Override
    public void startReload() {
        serializers.clear();
    }
    
    @Override
    public void acceptPlugin(REIPlugin<?> plugin) {
        plugin.registerDisplaySerializer(this);
    }
}
