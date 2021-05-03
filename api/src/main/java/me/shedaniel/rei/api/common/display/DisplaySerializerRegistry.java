/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.api.common.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.minecraft.nbt.CompoundTag;

public interface DisplaySerializerRegistry extends Reloadable<REIPlugin<?>> {
    static DisplaySerializerRegistry getInstance() {
        return PluginManager.getInstance().get(DisplaySerializerRegistry.class);
    }
    
    /**
     * Registers a {@link DisplaySerializer} for serializing a {@link Display} for syncing across server-client, and
     * for serializing displays to disk for favorites.
     * <p>
     * Since REI 6, all {@link me.shedaniel.rei.api.client.registry.display.DisplayCategory}s are required to register their serializers,
     * or mark themselves as unavailable for serialization.
     *
     * @param categoryId the category identifier of the display
     * @param serializer the serializer of the display
     * @param <D>        the type of the display
     */
    <D extends Display> void register(CategoryIdentifier<? extends D> categoryId, DisplaySerializer<D> serializer);
    
    /**
     * Marks a {@link Display} as unavailable to sync across server-client, and
     * for serializing displays to disk for favorites.
     * <p>
     * Since REI 6, all {@link me.shedaniel.rei.api.client.registry.display.DisplayCategory}s are required to register their serializers,
     * or mark themselves as unavailable for serialization.
     *
     * @param categoryId the category identifier of the display
     * @param <D>        the type of the display
     */
    <D extends Display> void registerNotSerializable(CategoryIdentifier<D> categoryId);
    
    <D extends Display> boolean hasRegistered(CategoryIdentifier<D> categoryId);
    
    <D extends Display> boolean hasSerializer(CategoryIdentifier<D> categoryId);
    
    <D extends Display> CompoundTag save(CategoryIdentifier<? extends D> categoryId, D display, CompoundTag tag);
    
    <D extends Display> D read(CategoryIdentifier<? extends D> categoryId, CompoundTag tag);
}
