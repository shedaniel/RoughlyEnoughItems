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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.plugin.common.displays.tag.TagNode;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.Holder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class TagTreeWidget<S, T> extends WidgetWithBounds {
    private final Rectangle bounds;
    private final TagNode<S> node;
    private final Rectangle overflowBounds;
    private final TagNodeWidget<S, T> rootWidget;
    private final List<TagTreeWidget<S, T>> childWidgets;
    private final List<WidgetWithBounds> children;
    
    public TagTreeWidget(TagNode<S> node, Function<Holder<S>, EntryStack<T>> mapper, Rectangle overflowBounds) {
        this.node = node;
        this.overflowBounds = overflowBounds;
        this.rootWidget = TagNodeWidget.create(node, mapper, overflowBounds);
        this.childWidgets = new ArrayList<>();
        for (TagNode<S> childNode : node.children()) {
            TagTreeWidget<S, T> childWidget = new TagTreeWidget<>(childNode, mapper, overflowBounds);
            childWidget.getBounds().y = rootWidget.getBounds().getMaxY() + 16;
            this.childWidgets.add(childWidget);
        }
        int childrenTotalWidth = childWidgets.stream().map(WidgetWithBounds::getBounds).mapToInt(value -> value.width + 6).sum() - 6;
        int x = 0;
        for (TagTreeWidget<S, T> childWidget : this.childWidgets) {
            childWidget.getBounds().x = rootWidget.getBounds().getCenterX() - childrenTotalWidth / 2 + x;
            x += childWidget.getBounds().width + 6;
        }
        this.children = Stream.concat(Stream.of(this.rootWidget), this.childWidgets.stream()).toList();
        this.bounds = new Rectangle(this.children.stream()
                .map(WidgetWithBounds::getBounds)
                .reduce(Rectangle::union)
                .orElse(new Rectangle())
                .getSize());
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        this.rootWidget.getBounds().setLocation(getBounds().getCenterX() - this.rootWidget.getBounds().getWidth() / 2,
                getBounds().y);
        this.rootWidget.render(poses, mouseX, mouseY, delta);
        if (childWidgets.isEmpty()) return;
        vLine(poses, rootWidget.getBounds().getCenterX(), rootWidget.getBounds().getMaxY(), rootWidget.getBounds().getMaxY() + 6, 0xFFFFFFFF);
        int childrenTotalWidth = childWidgets.stream().map(WidgetWithBounds::getBounds).mapToInt(value -> value.width + 6).sum() - 6;
        hLine(poses, rootWidget.getBounds().getCenterX() - childrenTotalWidth / 2 + childWidgets.get(0).getBounds().width / 2,
                rootWidget.getBounds().getCenterX() + childrenTotalWidth / 2 - childWidgets.get(childWidgets.size() - 1).getBounds().width / 2,
                rootWidget.getBounds().getMaxY() + 6, 0xFFFFFFFF);
        int x = 0;
        for (TagTreeWidget<S, T> childWidget : this.childWidgets) {
            vLine(poses, getBounds().getCenterX() - childrenTotalWidth / 2 + x + childWidget.getBounds().width / 2,
                    rootWidget.getBounds().getMaxY() + 6, rootWidget.getBounds().getMaxY() + 16, 0xFFFFFFFF);
            childWidget.getBounds().setLocation(getBounds().getCenterX() - childrenTotalWidth / 2 + x,
                    this.rootWidget.getBounds().getMaxY() + 16);
            if (this.overflowBounds.intersects(MatrixUtils.transform(poses.last().pose(), childWidget.getBounds()))) {
                childWidget.render(poses, mouseX, mouseY, delta);
            }
            x += childWidget.getBounds().width + 6;
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
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
