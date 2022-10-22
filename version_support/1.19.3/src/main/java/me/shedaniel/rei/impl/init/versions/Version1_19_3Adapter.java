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

package me.shedaniel.rei.impl.init.versions;

import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.VersionAdapter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Version1_19_3Adapter implements VersionAdapter {
    @Override
    @Environment(EnvType.CLIENT)
    public List<ItemStack> appendStacksForItem(Item item, Comparator<ItemStack> comparator) {
        return List.of(item.getDefaultInstance());
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public ResourceLocation spriteAtlasLocation(TextureAtlasSprite sprite) {
        return sprite.atlasLocation();
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public ResourceLocation spriteName(TextureAtlasSprite sprite) {
        return sprite.contents().name();
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public int spriteWidth(TextureAtlasSprite sprite) {
        return sprite.contents().width();
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public int spriteHeight(TextureAtlasSprite sprite) {
        return sprite.contents().height();
    }
    
    @Override
    public <T> Optional<Holder<T>> getHolder(Registry<T> registry, ResourceKey<T> key) {
        return registry.getHolder(key).map(Function.identity());
    }
    
    @Override
    public <T> Optional<Holder<T>> getHolder(Registry<T> registry, int id) {
        return registry.getHolder(id).map(Function.identity());
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void sendCommand(String command) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        player.connection.sendCommand(command);
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public Comparator<? super EntryStack<?>> getEntryGroupComparator() {
        return Comparator.comparingInt(stack -> {
            return Integer.MAX_VALUE;
        });
    }
}
