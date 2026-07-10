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

package me.shedaniel.rei.api.common.display;

import com.mojang.serialization.Codec;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The registry for display serializers used for display serialization, useful for persistent displays across reloads,
 * and server-client communication.
 *
 * @see REICommonPlugin#registerDisplaySerializer(DisplaySerializerRegistry)
 */
public interface DisplaySerializerRegistry extends Reloadable<REICommonPlugin> {
    static DisplaySerializerRegistry getInstance() {
        return PluginManager.getInstance().get(DisplaySerializerRegistry.class);
    }
    
    /**
     * Registers a {@link DisplaySerializer} for serializing a {@link Display} for syncing across server-client, and
     * for serializing displays to disk for favorites.
     *
     * @param id         the identifier of the display serializer
     * @param serializer the serializer of the display
     * @param <D>        the type of the display
     */
    <D extends Display> void register(Identifier id, DisplaySerializer<D> serializer);
    
    /**
     * Returns the display serializer for the given id.
     *
     * @param id the identifier of the display serializer
     * @return the display serializer
     */
    @Nullable
    DisplaySerializer<?> get(Identifier id);
    
    /**
     * Returns the id of the display serializer.
     *
     * @param serializer the display serializer
     * @return the id of the display serializer
     */
    @Nullable
    Identifier getId(DisplaySerializer<?> serializer);
    
    /**
     * Returns whether the display serializer is registered.
     *
     * @param serializer the display serializer
     * @return whether the display serializer is registered
     */
    boolean isRegistered(DisplaySerializer<?> serializer);
    
    /**
     * Returns a generic codec for any registered displays.
     *
     * @return a generic codec for any registered displays
     */
    Codec<Display> codec();
    
    /**
     * Returns a generic stream codec for any registered displays.
     *
     * @return a generic stream codec for any registered displays
     */
    StreamCodec<RegistryFriendlyByteBuf, Display> streamCodec();
}
