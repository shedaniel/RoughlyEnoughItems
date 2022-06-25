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

package me.shedaniel.rei.mixin.fabric;

import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.plugin.common.displays.tag.TagNode;
import me.shedaniel.rei.plugin.common.displays.tag.TagNodes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(TagLoader.class)
public class MixinTagLoader<T> {
    @Shadow @Final private String directory;
    
    @Inject(method = "build", at = @At("HEAD"))
    private void load(Map<ResourceLocation, Tag.Builder> map, CallbackInfoReturnable<Map<ResourceLocation, Tag<T>>> cir) {
        TagNodes.RAW_TAG_DATA_MAP.put(directory, new HashMap<>());
        TagNodes.CURRENT_TAG_DIR.set(directory);
    }
    
    @Inject(method = "build", at = @At("RETURN"))
    private void loadPost(Map<ResourceLocation, Tag.Builder> map, CallbackInfoReturnable<Map<ResourceLocation, Tag<T>>> cir) {
        HashBiMap<ResourceLocation, Tag<T>> biMap = HashBiMap.create(cir.getReturnValue());
        ResourceKey<? extends Registry<?>> resourceKey = TagNodes.TAG_DIR_MAP.get(directory);
        if (resourceKey == null) return;
        TagNodes.TAG_DATA_MAP.put(resourceKey, new HashMap<>());
        Map<ResourceLocation, TagNodes.TagData> tagDataMap = TagNodes.TAG_DATA_MAP.get(resourceKey);
        Registry<T> registry = ((Registry<Registry<T>>) Registry.REGISTRY).get((ResourceKey<Registry<T>>) resourceKey);
        Stopwatch stopwatch = Stopwatch.createStarted();
        
        Iterator<Map.Entry<Tag<?>, TagNodes.RawTagData>> entryIterator = TagNodes.RAW_TAG_DATA_MAP.getOrDefault(directory, Collections.emptyMap())
                .entrySet().iterator();
        
        if (!entryIterator.hasNext()) return;
        
        while (entryIterator.hasNext()) {
            Map.Entry<Tag<?>, TagNodes.RawTagData> entry = entryIterator.next();
            Tag<?> tag = entry.getKey();
            entryIterator.remove();
            
            if (registry != null) {
                ResourceLocation tagLoc = biMap.inverse().get(tag);
                
                if (tagLoc != null) {
                    TagNodes.RawTagData rawTagData = entry.getValue();
                    IntList elements = new IntArrayList();
                    for (ResourceLocation element : rawTagData.otherElements()) {
                        T t = registry.get(element);
                        if (t != null) {
                            elements.add(registry.getId(t));
                        }
                    }
                    tagDataMap.put(tagLoc, new TagNodes.TagData(elements, rawTagData.otherTags()));
                }
            }
        }
        
        RoughlyEnoughItemsCore.LOGGER.info("Processed %d tags in %s for %s", tagDataMap.size(), stopwatch.stop(), resourceKey.location());
    }
}
