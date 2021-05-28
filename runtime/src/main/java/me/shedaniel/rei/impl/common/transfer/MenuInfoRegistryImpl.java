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

package me.shedaniel.rei.impl.common.transfer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoProvider;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class MenuInfoRegistryImpl implements MenuInfoRegistry {
    private final Map<CategoryIdentifier<?>, Map<Class<? extends AbstractContainerMenu>, List<MenuInfoProvider<?, ?>>>> map = Maps.newLinkedHashMap();
    private final Map<Predicate<CategoryIdentifier<?>>, List<MenuInfoProvider<?, ?>>> mapGeneric = Maps.newLinkedHashMap();
    
    @Override
    public <C extends AbstractContainerMenu, D extends Display> void register(CategoryIdentifier<D> category, Class<C> menuClass, MenuInfoProvider<C, D> menuInfo) {
        map.computeIfAbsent(category, id -> Maps.newLinkedHashMap())
                .computeIfAbsent(menuClass, c -> Lists.newArrayList())
                .add(menuInfo);
    }
    
    @Override
    public <D extends Display> void registerGeneric(Predicate<CategoryIdentifier<?>> categoryPredicate, MenuInfoProvider<?, D> menuInfo) {
        mapGeneric.computeIfAbsent(categoryPredicate, id -> Lists.newArrayList()).add(menuInfo);
    }
    
    @Override
    @Nullable
    public <T extends AbstractContainerMenu, D extends Display> MenuInfo<T, D> get(CategoryIdentifier<D> category, Class<T> menuClass) {
        Map<Class<? extends AbstractContainerMenu>, List<MenuInfoProvider<?, ?>>> infoMap = map.get(category);
        if (infoMap != null && !infoMap.isEmpty()) {
            if (infoMap.containsKey(menuClass)) {
                for (MenuInfoProvider<?, ?> provider : infoMap.get(menuClass)) {
                    Optional<MenuInfo<T, D>> info = ((MenuInfoProvider<T, D>) provider).provide(category, menuClass);
                    if (info.isPresent()) {
                        return info.get();
                    }
                }
            }
            for (Map.Entry<Class<? extends AbstractContainerMenu>, List<MenuInfoProvider<?, ?>>> entry : infoMap.entrySet()) {
                if (entry.getKey().isAssignableFrom(menuClass)) {
                    for (MenuInfoProvider<?, ?> provider : entry.getValue()) {
                        Optional<MenuInfo<T, D>> info = ((MenuInfoProvider<T, D>) provider).provide(category, menuClass);
                        if (info.isPresent()) {
                            return info.get();
                        }
                    }
                }
            }
        }
        
        for (Map.Entry<Predicate<CategoryIdentifier<?>>, List<MenuInfoProvider<?, ?>>> entry : mapGeneric.entrySet()) {
            if (entry.getKey().test(category) && !entry.getValue().isEmpty()) {
                List<MenuInfoProvider<?, ?>> infoList = entry.getValue();
                if (!infoList.isEmpty()) {
                    Optional<MenuInfo<T, D>> info = ((MenuInfoProvider<T, D>) infoList.get(0)).provide(category, menuClass);
                    if (info.isPresent()) {
                        return info.get();
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public int infoSize() {
        return map.size() + mapGeneric.size();
    }
    
    @Override
    public void startReload() {
        map.clear();
        mapGeneric.clear();
    }
    
    @Override
    public void acceptPlugin(REIServerPlugin plugin) {
        plugin.registerMenuInfo(this);
    }
}
