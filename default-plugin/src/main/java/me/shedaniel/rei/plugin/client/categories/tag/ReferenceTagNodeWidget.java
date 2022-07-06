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

package me.shedaniel.rei.plugin.client.categories.tag;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.tag.TagNode;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ReferenceTagNodeWidget<S, T> extends TagNodeWidget<S, T> {
    private final TagNode<S> node;
    private final Rectangle overflowBounds;
    private final Rectangle bounds;
    private final Slot slot;
    private final List<? extends GuiEventListener> children;
    
    public ReferenceTagNodeWidget(TagNode<S> node, Function<Holder<S>, EntryStack<T>> mapper, Rectangle overflowBounds) {
        this.node = node;
        this.overflowBounds = overflowBounds;
        this.bounds = new Rectangle(0, 0, 24, 23);
        this.slot = Widgets.createSlot(new Rectangle(0, 0, 18, 18))
                .disableBackground()
                .disableHighlight()
                .disableTooltips()
                .entries(EntryIngredients.ofTag(node.getReference(), mapper));
        this.children = Collections.singletonList(this.slot);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        if (this.overflowBounds.intersects(MatrixUtils.transform(poses.last().pose(), getBounds()))) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, new ResourceLocation("textures/gui/advancements/widgets.png"));
            this.blit(poses, bounds.x, bounds.y, 1, 128 + 27, 24, 24);
            this.slot.getBounds().setLocation(bounds.getCenterX() - this.slot.getBounds().getWidth() / 2, bounds.y + (bounds.height - this.slot.getBounds().getHeight()) / 2 + 1);
            this.slot.render(poses, mouseX, mouseY, delta);
            if (this.containsMouse(mouseX, mouseY)) {
            Tooltip.create(Component.literal("#" + this.node.getReference().location().toString())).queue();
            }
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
}
