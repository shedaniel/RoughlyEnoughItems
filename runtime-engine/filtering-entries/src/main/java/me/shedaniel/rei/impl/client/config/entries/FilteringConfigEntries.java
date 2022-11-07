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

package me.shedaniel.rei.impl.client.config.entries;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonNull;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.DeserializationException;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerInternal;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TranslatableComponent;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public class FilteringConfigEntries implements ConfigManagerInternal.SystemSetup {
    @Override
    public void setup(GuiRegistry registry) {
        registry.registerPredicateProvider((i13n, field, config, defaults, guiProvider) -> {
                    List<EntryStack<?>> value = CollectionUtils.map(Utils.<List<EntryStackProvider<?>>>getUnsafely(field, config, new ArrayList<>()), EntryStackProvider::provide);
                    List<EntryStack<?>> defaultValue = CollectionUtils.map(Utils.<List<EntryStackProvider<?>>>getUnsafely(field, defaults), EntryStackProvider::provide);
                    Consumer<List<EntryStack<?>>> saveConsumer = (newValue) -> {
                        setUnsafely(field, config, CollectionUtils.map(newValue, EntryStackProvider::ofStack));
                    };
                    try {
                        Field filteringRules = config.getClass().getDeclaredField("filteringRules");
                        return REIRuntime.getInstance().getPreviousContainerScreen() == null || Minecraft.getInstance().getConnection() == null || Minecraft.getInstance().getConnection().getRecipeManager() == null ?
                                Collections.singletonList(new NoFilteringEntry(220, value, defaultValue, saveConsumer))
                                :
                                Collections.singletonList(new FilteringEntry(220, value, Utils.getUnsafely(filteringRules, config), defaultValue, saveConsumer, list -> setUnsafely(filteringRules, config, Lists.newArrayList(list))));
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
                , (field) -> field.getType() == List.class && field.getName().equals("filteredStacks"));
        registry.registerPredicateProvider((i13n, field, config, defaults, guiProvider) -> {
                    Map<CategoryIdentifier<?>, Boolean> value = Utils.<Map<CategoryIdentifier<?>, Boolean>>getUnsafely(field, config, new HashMap<>());
                    Map<CategoryIdentifier<?>, Boolean> defaultValue = Utils.getUnsafely(field, defaults);
                    Consumer<Map<CategoryIdentifier<?>, Boolean>> saveConsumer = (newValue) -> {
                        setUnsafely(field, config, newValue);
                    };
                    return REIRuntime.getInstance().getPreviousContainerScreen() == null || Minecraft.getInstance().getConnection() == null || Minecraft.getInstance().getConnection().getRecipeManager() == null ?
                            Collections.singletonList(new NoFilteringCategoriesEntry(new TranslatableComponent(i13n), value, defaultValue, saveConsumer))
                            :
                            Collections.singletonList(new FilteringCategoriesEntry(new TranslatableComponent(i13n), value, defaultValue, saveConsumer));
                }
                , (field) -> field.getType() == Map.class && field.getName().equals("filteringQuickCraftCategories"));
    }
    
    @Override
    public void setup(Jankson.Builder builder) {
        // FilteringRule
        builder.registerSerializer(FilteringRule.class, (value, marshaller) -> {
            try {
                return marshaller.serialize(FilteringRuleType.save(value, new CompoundTag()));
            } catch (Exception e) {
                e.printStackTrace();
                return JsonNull.INSTANCE;
            }
        });
        builder.registerDeserializer(Tag.class, FilteringRule.class, (value, marshaller) -> {
            try {
                return FilteringRuleType.read((CompoundTag) value);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        builder.registerDeserializer(String.class, FilteringRule.class, (value, marshaller) -> {
            try {
                return FilteringRuleType.read(TagParser.parseTag(value));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        
        // CategoryIdentifier
        builder.registerSerializer(CategoryIdentifier.class, (value, marshaller) -> {
            return marshaller.serialize(value.toString());
        });
        builder.registerDeserializer(String.class, CategoryIdentifier.class, (value, marshaller) -> {
            try {
                return CategoryIdentifier.of(value);
            } catch (ResourceLocationException e) {
                throw new DeserializationException(e);
            }
        });
    }
}
