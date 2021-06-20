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

package me.shedaniel.rei.impl.client.gui.widget;


import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

/**
 * @see Tooltip
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class QueuedTooltip implements Tooltip {
    private Point location;
    private List<Tooltip.Entry> components;
    
    private QueuedTooltip(Point location, Collection<Tooltip.Entry> components) {
        this.location = location;
        if (this.location == null) {
            this.location = PointHelper.ofMouse();
        }
        this.components = Lists.newArrayList(components);
    }
    
    public static QueuedTooltip impl(Point location, Collection<Tooltip.Entry> text) {
        return new QueuedTooltip(location, text);
    }
    
    @Override
    public int getX() {
        return location.x;
    }
    
    @Override
    public int getY() {
        return location.y;
    }
    
    @Override
    public List<Component> getText() {
        return CollectionUtils.filterAndMap(components, Tooltip.Entry::isText, Tooltip.Entry::getAsText);
    }
    
    @Override
    public List<Entry> entries() {
        return components;
    }
    
    @Override
    public Tooltip add(Component text) {
        components.add(new TooltipEntryImpl(text));
        return this;
    }
    
    @Override
    public Tooltip add(ClientTooltipComponent component) {
        components.add(new TooltipEntryImpl(component));
        return this;
    }
    
    @Override
    public void queue() {
        Tooltip.super.queue();
    }
    
    public record TooltipEntryImpl(Object obj) implements Tooltip.Entry {
        @Override
        public Component getAsText() {
            return (Component) obj;
        }
        
        @Override
        public boolean isText() {
            return obj instanceof Component;
        }
        
        @Override
        public ClientTooltipComponent getAsComponent() {
            return (ClientTooltipComponent) obj;
        }
    }
}
