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

package me.shedaniel.rei.plugin.client.runtime;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.entry.filtering.*;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Iterator;

/**
 * A plugin to hide any ingredients from the <code>c:hidden_from_recipe_viewers</code> tag.
 * This can be implemented in a few lines, but is instead implemented as a filtering rule type
 * such that users may view the list of hidden ingredients separately in config.
 */
@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class HideIngredientsFromTagsPlugin implements REIClientPlugin {
    private static final ResourceLocation HIDDEN_TAG = new ResourceLocation("c:hidden_from_recipe_viewers");
    
    static {
        FilteringRuleTypeRegistry.getInstance().register(new ResourceLocation("roughlyenoughitems", "hidden_from_recipe_viewers"), HideTagsFilteringRuleType.INSTANCE);
        RoughlyEnoughItemsCoreClient.POST_UPDATE_TAGS.register(HideTagsFilteringRule.INSTANCE::markDirty);
    }
    
    private enum HideTagsFilteringRuleType implements FilteringRuleType<HideTagsFilteringRule> {
        INSTANCE;
        
        @Override
        public CompoundTag saveTo(HideTagsFilteringRule rule, CompoundTag tag) {
            return tag;
        }
        
        @Override
        public HideTagsFilteringRule readFrom(CompoundTag tag) {
            return HideTagsFilteringRule.INSTANCE;
        }
        
        @Override
        public Component getTitle(HideTagsFilteringRule rule) {
            return Component.translatable("rule.roughlyenoughitems.filtering.hide.tag");
        }
        
        @Override
        public Component getSubtitle(HideTagsFilteringRule rule) {
            return Component.translatable("rule.roughlyenoughitems.filtering.hide.tag.subtitle");
        }
        
        @Override
        public HideTagsFilteringRule createNew() {
            return HideTagsFilteringRule.INSTANCE;
        }
        
        @Override
        public boolean isSingular() {
            return true;
        }
    }
    
    private enum HideTagsFilteringRule implements FilteringRule<HideTagsFilteringRule.Cache> {
        INSTANCE;
        
        private Cache cache;
        
        private record Cache(EntryIngredient ingredient, LongSet hashes) {}
        
        @Override
        public FilteringRuleType<? extends FilteringRule<HideTagsFilteringRule.Cache>> getType() {
            return HideTagsFilteringRuleType.INSTANCE;
        }
        
        @Override
        public Cache prepareCache(boolean async) {
            try {
                EntryIngredient ingredient = EntryIngredient.builder()
                        .addAll(EntryIngredients.ofItemTag(TagKey.create(Registries.ITEM, HIDDEN_TAG)))
                        .addAll(EntryIngredients.ofItemTag(TagKey.create(Registries.BLOCK, HIDDEN_TAG)))
                        .addAll(EntryIngredients.ofFluidTag(TagKey.create(Registries.FLUID, HIDDEN_TAG)))
                        .build();
                LongSet hashes = new LongOpenHashSet();
                for (EntryStack<?> stack : ingredient) {
                    hashes.add(EntryStacks.hashExact(stack));
                }
                return this.cache = new Cache(ingredient, hashes);
            } catch (Throwable e) {
                InternalLogger.getInstance().warn("Failed to load hidden ingredients from tag, falling back to empty cache.", e);
                return this.cache = null;
            }
        }
        
        @Override
        public FilteringResult processFilteredStacks(FilteringContext context, FilteringResultFactory resultFactory, HideTagsFilteringRule.Cache cache, boolean async) {
            FilteringResult result = resultFactory.create();
            if (cache != null) {
                process(result, context.getShownStacks(), context.getShownExactHashes(), cache);
                process(result, context.getUnsetStacks(), context.getUnsetExactHashes(), cache);
            }
            return result;
        }
        
        private void process(FilteringResult result, Collection<EntryStack<?>> stacks, LongCollection hashes, HideTagsFilteringRule.Cache cache) {
            Iterator<EntryStack<?>> stackIterator = stacks.iterator();
            LongIterator hashIterator = hashes.iterator();
            while (stackIterator.hasNext()) {
                EntryStack<?> stack = stackIterator.next();
                long hash = hashIterator.nextLong();
                if (cache.hashes().contains(hash)) {
                    result.hide(stack);
                }
            }
        }
        
        private void markDirty() {
            InternalLogger.getInstance().debug("Marking hidden ingredients from tag cache as dirty.");
            if (this.cache != null) {
                this.markDirty(this.cache.ingredient(), this.cache.hashes());
            }
            try {
                VanillaEntryTypes.ITEM.getDefinition();
            } catch (NullPointerException ignored) {
                this.cache = null;
                return;
            }
            this.cache = this.prepareCache(false);
            if (this.cache != null) {
                this.markDirty(this.cache.ingredient(), this.cache.hashes());
            }
            InternalLogger.getInstance().debug("Marked %d hidden ingredients from tag cache as dirty.", this.cache == null ? 0 : this.cache.hashes().size());
        }
    }
}
