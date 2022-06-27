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

package me.shedaniel.rei.impl.client.gui.widget.favorites.history;

import com.google.common.collect.Iterables;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DisplayHistoryManager {
    public static final DisplayHistoryManager INSTANCE = new DisplayHistoryManager();
    private Map<String, DisplayEntry> entries = new LinkedHashMap<>();
    private long lastCheckTime = -1;
    
    public Collection<DisplayEntry> getEntries(DisplayHistoryWidget parent) {
        if ((lastCheckTime == -1 || Util.getMillis() - lastCheckTime > 4000) && !PluginManager.areAnyReloading()) {
            updateEntries(parent);
            lastCheckTime = Util.getMillis();
        }
        
        return Collections.unmodifiableCollection(entries.values());
    }
    
    private void updateEntries(DisplayHistoryWidget parent) {
        List<CompoundTag> displayHistory = ConfigManagerImpl.getInstance().getConfig().getDisplayHistory();
        Map<String, DisplayEntry> copy = new LinkedHashMap<>(entries);
        entries.clear();
        for (CompoundTag tag : displayHistory) {
            String uuid = tag.getString("DisplayHistoryUUID");
            
            DisplayEntry entry = copy.get(uuid);
            if (entry != null) {
                entries.put(entry.getUuid().toString(), entry);
            } else if (tag.getBoolean("DisplayHistoryContains")) {
                try {
                    CategoryIdentifier<?> categoryIdentifier = CategoryIdentifier.of(tag.getString("DisplayHistoryCategory"));
                    if (CategoryRegistry.getInstance().tryGet(categoryIdentifier).isPresent()) {
                        Display display = DisplaySerializerRegistry.getInstance().read(categoryIdentifier, tag.getCompound("DisplayHistoryData"));
                        DisplayEntry newEntry = new DisplayEntry(parent, display, null);
                        newEntry.setUuid(UUID.fromString(uuid));
                        entries.put(newEntry.getUuid().toString(), newEntry);
                    }
                } catch (Exception e) {
                    InternalLogger.getInstance().warn("Failed to read display history entry", e);
                }
            }
        }
    }
    
    public void removeEntry(DisplayEntry entry) {
        this.entries.remove(entry.getUuid().toString());
        List<CompoundTag> displayHistory = ConfigManagerImpl.getInstance().getConfig().getDisplayHistory();
        displayHistory.removeIf(tag -> tag.getString("DisplayHistoryUUID").equals(entry.getUuid().toString()));
        save();
    }
    
    public void addEntry(DisplayHistoryWidget parent, @Nullable Rectangle bounds, Display display) {
        List<CompoundTag> displayHistory = ConfigManagerImpl.getInstance().getConfig().getDisplayHistory();
        Iterator<DisplayEntry> iterator = this.entries.values().iterator();
        while (iterator.hasNext()) {
            DisplayEntry entry = iterator.next();
            if (entry.getDisplay() == display) {
                displayHistory.removeIf(tag -> tag.getString("DisplayHistoryUUID").equals(entry.getUuid().toString()));
                iterator.remove();
            }
        }
        DisplayEntry newEntry = new DisplayEntry(parent, display, bounds);
        Map<String, DisplayEntry> copy = new LinkedHashMap<>();
        copy.put(newEntry.getUuid().toString(), newEntry);
        copy.putAll(this.entries);
        this.entries = copy;
        while (entries.size() >= 10) {
            DisplayEntry entry = Iterables.get(entries.values(), entries.size() - 1);
            displayHistory.removeIf(tag -> tag.getString("DisplayHistoryUUID").equals(entry.getUuid().toString()));
            this.entries.remove(entry.getUuid().toString());
        }
        
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("DisplayHistoryContains", false);
        compoundTag.putString("DisplayHistoryUUID", newEntry.getUuid().toString());
        compoundTag.putString("DisplayHistoryCategory", display.getCategoryIdentifier().toString());
        displayHistory.add(0, compoundTag);
        
        save();
    }
    
    private void save() {
        List<CompoundTag> displayHistory = ConfigManagerImpl.getInstance().getConfig().getDisplayHistory();
        for (CompoundTag compoundTag : displayHistory) {
            String uuid = compoundTag.getString("DisplayHistoryUUID");
            DisplayEntry entry = entries.get(uuid);
            
            if (entry != null) {
                compoundTag.putBoolean("DisplayHistoryContains", false);
                Display display = entry.getDisplay();
                boolean hasSerializer = DisplaySerializerRegistry.getInstance().hasSerializer(display.getCategoryIdentifier());
                
                if (hasSerializer) {
                    try {
                        compoundTag.put("DisplayHistoryData", DisplaySerializerRegistry.getInstance().save(display, new CompoundTag()));
                        compoundTag.putBoolean("DisplayHistoryContains", true);
                    } catch (Exception e) {
                        InternalLogger.getInstance().warn("Failed to save display history entry", e);
                    }
                }
            }
        }
        
        ConfigManagerImpl.getInstance().saveConfig();
    }
}
