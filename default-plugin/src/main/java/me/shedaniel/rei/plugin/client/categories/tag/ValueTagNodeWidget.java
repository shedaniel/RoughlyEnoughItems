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

package me.shedaniel.rei.plugin.client.categories.tag;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.plugin.common.displays.tag.TagNode;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ValueTagNodeWidget<S, T> extends TagNodeWidget<S, T> {
    private final Rectangle bounds;
    private final List<Widget> widgets;
    private final WidgetWithBounds widget;
    private final List<? extends GuiEventListener> children;
    private final Rectangle overflowBounds;
    
    public ValueTagNodeWidget(TagNode<S> node, Function<Holder<S>, EntryStack<T>> mapper, Rectangle overflowBounds) {
        this.overflowBounds = overflowBounds;
        HolderSet<S> holders = node.getValue();
        int width = Math.min(4, holders.size());
        int height = Math.max((int) Math.ceil(holders.size() * 1.0 / width), 1);
        this.bounds = new Rectangle(0, 0, 16 * width + 12, 16 * height + 12);
        Panel background = Widgets.createRecipeBase(bounds.clone())
                .rendering(Predicates.alwaysTrue());
        Panel slotBackground = Widgets.createSlotBase(new Rectangle(5, 5, 16 * width + 2, 16 * height + 2));
        int i = 0;
        this.widgets = new ArrayList<>();
        this.widgets.add(background);
        this.widgets.add(slotBackground);
        for (Holder<S> holder : holders) {
            int x = i % width;
            int y = i / width;
            Slot slot = Widgets.createSlot(new Rectangle(x * 16 + 5, y * 16 + 5, 18, 18))
                    .entry(mapper.apply(holder))
                    .disableBackground();
            this.widgets.add(slot);
            i++;
        }
        this.widget = Widgets.withTranslate(Widgets.concat(this.widgets),
                $ -> new Matrix4f().translate(getBounds().x, getBounds().y, 0));
        this.children = Collections.singletonList(this.widget);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        Rectangle bounds = getBounds();
        if (this.overflowBounds.intersects(MatrixUtils.transform(poses.last().pose(), bounds))) {
            poses.pushPose();
            poses.translate(bounds.x, bounds.y, 0);
            Point mouse = new Point(mouseX - bounds.x, mouseY - bounds.y);
            for (Widget widget : this.widgets) {
                if (!(widget instanceof WidgetWithBounds withBounds) ||
                    this.overflowBounds.intersects(MatrixUtils.transform(poses.last().pose(), withBounds.getBounds()))) {
                    widget.render(poses, mouse.x, mouse.y, delta);
                }
            }
            poses.popPose();
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiEventListener element : children())
            if (element.mouseReleased(mouseX, mouseY, button))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiEventListener element : children())
            if (element.keyPressed(keyCode, scanCode, modifiers))
                return true;
        return false;
    }
}
