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

package me.shedaniel.rei.impl.common.registry.displays;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DisplaysHolderImpl implements DisplaysHolder {
    private final Map<CategoryIdentifier<?>, DisplaysList> displays = new ConcurrentHashMap<>();
    private final Map<CategoryIdentifier<?>, List<Display>> unmodifiableDisplays = new RemappingMap<>(
            Collections.unmodifiableMap(displays), list -> {
        if (list == null) {
            return null;
        } else {
            return ((DisplaysList) list).synchronizedList;
        }
    }, this::checkCategory);
    
    protected final WeakHashMap<Display, Object> originsMap = new WeakHashMap<>();
    private final MutableInt displayCount = new MutableInt(0);
    
    @Override
    public void add(Display display, @Nullable Object origin) {
        this.displays.computeIfAbsent(display.getCategoryIdentifier(), location -> new DisplaysList())
                .add(display);
        this.displayCount.increment();
        if (origin != null) {
            synchronized (this.originsMap) {
                this.originsMap.put(display, origin);
            }
        }
    }
    
    @Override
    public boolean remove(Display display) {
        if (this.displays.get(display.getCategoryIdentifier()).remove(display)) {
            removeFallout(display);
            if (this.displays.get(display.getCategoryIdentifier()).isEmpty()) {
                this.displays.remove(display.getCategoryIdentifier());
            }
            return true;
        }
        
        return false;
    }
    
    protected void removeFallout(Display display) {
        this.displayCount.decrement();
        synchronized (this.originsMap) {
            this.originsMap.remove(display);
        }
    }
    
    @Override
    public int size() {
        return this.displayCount.intValue();
    }
    
    @Override
    public Map<CategoryIdentifier<?>, List<Display>> getUnmodifiable() {
        return this.unmodifiableDisplays;
    }
    
    @Override
    @Nullable
    public Object getDisplayOrigin(Display display) {
        synchronized (this.originsMap) {
            Object origin = this.originsMap.get(display);
            
            if (origin != null) {
                return origin;
            }
        }
        
        return null;
    }
    
    protected boolean checkCategory(CategoryIdentifier<?> key) {
        return true;
    }
    
    public static class ByKey extends DisplaysHolderImpl implements DisplaysHolder.ByKey {
        private final SetMultimap<DisplayKey, Display> displaysByKey = Multimaps.newSetMultimap(new IdentityHashMap<>(), ReferenceOpenHashSet::new);
        
        @Override
        public void add(Display display, @Nullable Object origin) {
            super.add(display, origin);
            Optional<Identifier> location = display.getDisplayLocation();
            if (location.isPresent()) {
                this.displaysByKey.put(DisplayKey.create(display.getCategoryIdentifier(), location.get()), display);
            }
        }
        
        @Override
        protected void removeFallout(Display display) {
            Optional<Identifier> location = display.getDisplayLocation();
            if (location.isPresent()) {
                this.displaysByKey.remove(DisplayKey.create(display.getCategoryIdentifier(), location.get()), display);
            }
            super.removeFallout(display);
        }
        
        @Override
        public Set<Display> getDisplaysByKey(DisplayKey key) {
            return this.displaysByKey.get(key);
        }
    }
    
    private static class DisplaysList extends ArrayList<Display> {
        private final List<Display> synchronizedList;
        
        public DisplaysList() {
            List<Display> unmodifiableList = Collections.unmodifiableList(this);
            this.synchronizedList = Collections.synchronizedList(unmodifiableList);
        }
    }
}
