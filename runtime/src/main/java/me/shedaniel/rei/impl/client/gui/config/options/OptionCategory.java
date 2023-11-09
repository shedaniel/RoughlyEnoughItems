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

package me.shedaniel.rei.impl.client.gui.config.options;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class OptionCategory {
    private final String key;
    private final ResourceLocation icon;
    private final Component name;
    private final Component description;
    private final List<OptionGroup> groups = new ArrayList<>();
    
    private OptionCategory(String key, ResourceLocation icon, Component name, Component description) {
        this.key = key;
        this.icon = icon;
        this.name = name;
        this.description = description;
    }
    
    public static OptionCategory of(String key, ResourceLocation icon, Component name, Component description) {
        return new OptionCategory(key, icon, name, description);
    }
    
    public OptionCategory add(OptionGroup group) {
        this.groups.add(group);
        return this;
    }
    
    public String getKey() {
        return key;
    }
    
    public ResourceLocation getIcon() {
        return icon;
    }
    
    public Component getName() {
        return name;
    }
    
    public Component getDescription() {
        return description;
    }
    
    public List<OptionGroup> getGroups() {
        return groups;
    }
    
    public OptionCategory copy() {
        OptionCategory category = new OptionCategory(key, icon, name, description);
        for (OptionGroup group : groups) {
            category.add(group.copy());
        }
        return category;
    }
}
