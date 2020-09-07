/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.gui.widget;


import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.forge.api.PointHelper;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Tooltip;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @see Tooltip
 */
@ApiStatus.Internal
public class QueuedTooltip implements Tooltip {
    
    private Point location;
    private List<ITextComponent> text;
    
    private QueuedTooltip(Point location, Collection<ITextComponent> text) {
        this.location = location;
        if (this.location == null && FMLEnvironment.dist == Dist.CLIENT) {
            applyMouseClient();
        }
        this.text = Lists.newArrayList(text);
    }
    
    @OnlyIn(Dist.CLIENT)
    private void applyMouseClient() {
        this.location = PointHelper.ofMouse();
    }
    
    @NotNull
    public static QueuedTooltip create(Point location, List<ITextComponent> text) {
        return new QueuedTooltip(location, text);
    }
    
    @NotNull
    public static QueuedTooltip create(Point location, Collection<ITextComponent> text) {
        return new QueuedTooltip(location, text);
    }
    
    @NotNull
    public static QueuedTooltip create(Point location, ITextComponent... text) {
        return QueuedTooltip.create(location, Arrays.asList(text));
    }
    
    @NotNull
    public static QueuedTooltip create(List<ITextComponent> text) {
        return QueuedTooltip.create(null, text);
    }
    
    @NotNull
    public static QueuedTooltip create(Collection<ITextComponent> text) {
        return QueuedTooltip.create(null, text);
    }
    
    @NotNull
    public static QueuedTooltip create(ITextComponent... text) {
        return QueuedTooltip.create(null, text);
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
    public List<ITextComponent> getText() {
        return text;
    }
    
    @Override
    public void queue() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            queueClient();
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private void queueClient() {
        REIHelper.getInstance().queueTooltip(this);
    }
}
