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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OptionGroup {
    private final String id;
    private final Component groupName;
    private final List<CompositeOption<?>> options = new ArrayList<>();
    @Nullable
    private String groupNameHighlight = null;
    
    public OptionGroup(String id, Component groupName) {
        this.id = id;
        this.groupName = groupName;
    }
    
    public OptionGroup add(CompositeOption<?> option) {
        this.options.add(option);
        return this;
    }
    
    public void setGroupNameHighlight(@Nullable String groupNameHighlight) {
        this.groupNameHighlight = groupNameHighlight;
    }
    
    public String getId() {
        return id;
    }
    
    public Component getGroupName() {
        return groupName;
    }
    
    public List<CompositeOption<?>> getOptions() {
        return options;
    }
    
    @Nullable
    public String getGroupNameHighlight() {
        return groupNameHighlight;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OptionGroup group) {
            return group.groupName.equals(groupName);
        }
        
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(groupName);
    }
    
    public OptionGroup copy() {
        OptionGroup group = new OptionGroup(id, groupName);
        for (CompositeOption<?> option : options) {
            group.add(option);
        }
        return group;
    }
}
