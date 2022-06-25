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

import com.mojang.datafixers.util.Either;
import me.shedaniel.rei.plugin.common.displays.tag.TagNodes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(Tag.Builder.class)
public class MixinTagBuilder<T> {
    @Shadow @Final private List<Tag.BuilderEntry> entries;
    
    @Inject(method = "build", at = @At("RETURN"))
    private void load(Function<ResourceLocation, Tag<T>> tagResolver, Function<ResourceLocation, T> valueResolver, CallbackInfoReturnable<Either<Collection<Tag.BuilderEntry>, Tag<T>>> cir) {
        Tag<T> tag = cir.getReturnValue().right().orElse(null);
        if (tag != null) {
            String currentTagDirectory = TagNodes.CURRENT_TAG_DIR.get();
            if (currentTagDirectory == null) return;
            ResourceKey<? extends Registry<?>> resourceKey = TagNodes.TAG_DIR_MAP.get(currentTagDirectory);
            if (resourceKey == null) return;
            Map<Tag<?>, TagNodes.RawTagData> dataMap = TagNodes.RAW_TAG_DATA_MAP.get(currentTagDirectory);
            if (dataMap == null) return;
            List<ResourceLocation> otherElements = new ArrayList<>();
            List<ResourceLocation> otherTags = new ArrayList<>();
            
            for (Tag.BuilderEntry builderEntry : this.entries) {
                if (builderEntry.entry() instanceof Tag.OptionalTagEntry tagEntry) {
                    Tag<T> apply = tagResolver.apply(tagEntry.id);
                    if (apply != null) {
                        otherTags.add(tagEntry.id);
                    }
                } else if (builderEntry.entry() instanceof Tag.TagEntry tagEntry) {
                    Tag<T> apply = tagResolver.apply(tagEntry.id);
                    if (apply != null) {
                        otherTags.add(tagEntry.id);
                    }
                } else if (builderEntry.entry() instanceof Tag.OptionalElementEntry tagEntry) {
                    T apply = valueResolver.apply(tagEntry.id);
                    if (apply != null) {
                        otherElements.add(tagEntry.id);
                    }
                } else if (builderEntry.entry() instanceof Tag.ElementEntry tagEntry) {
                    T apply = valueResolver.apply(tagEntry.id);
                    if (apply != null) {
                        otherElements.add(tagEntry.id);
                    }
                }
            }
            
            dataMap.put(tag, new TagNodes.RawTagData(otherElements, otherTags));
        }
    }
}
