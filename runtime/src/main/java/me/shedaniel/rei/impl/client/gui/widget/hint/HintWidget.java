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

package me.shedaniel.rei.impl.client.gui.widget.hint;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.CloseableScissors;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class HintWidget extends WidgetWithBounds {
    private static final int MAX_WIDTH = 180;
    private static final int MAX_HEIGHT = 130;
    private final HintsContainerWidget parent;
    private final Rectangle bounds = new Rectangle();
    private final Rectangle okayBounds = new Rectangle();
    private final int margin;
    private final Supplier<Point> point;
    private final String uuid;
    private final Collection<? extends FormattedText> lines;
    private List<List<FormattedCharSequence>> wrapped;
    private final NumberAnimator<Double> scroll = ValueAnimator.ofDouble(0);
    private int contentHeight;
    
    public HintWidget(HintsContainerWidget parent, int margin, Supplier<Point> point, String uuid, Collection<? extends FormattedText> lines) {
        this.parent = parent;
        this.margin = margin;
        this.point = point;
        this.uuid = uuid;
        this.lines = lines;
        recalculateBounds();
    }
    
    void recalculateBounds() {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getScreenHeight();
        int width = Mth.clamp(CollectionUtils.<FormattedText, Integer>mapAndMax((Collection<FormattedText>) lines,
                        l -> CollectionUtils.max(font.split(l, MAX_WIDTH - 8),
                                        Comparator.comparingLong(value -> font.width(value) + 8))
                                .map(value -> font.width(value) + 8).orElse(0),
                        Comparator.naturalOrder())
                .orElse(0), 60, MAX_WIDTH);
        Point point = this.point.get();
        int spaceLeft = Math.max(point.x - 4, 0);
        int spaceRight = Math.max(screenWidth - point.x - 4, 0);
        this.bounds.width = Math.min(width, Math.max(spaceLeft, spaceRight));
        if (spaceRight >= spaceLeft) {
            this.bounds.x = point.x + margin;
        } else {
            this.bounds.x = point.x - margin - this.bounds.width;
        }
        this.wrapped = CollectionUtils.map(lines, l -> font.split(l, this.bounds.width - 8));
        int height = 8 + 9;
        for (List<FormattedCharSequence> formattedCharSequences : wrapped) {
            height += formattedCharSequences.size() * 9;
            height += 2;
        }
        this.contentHeight = height - 9 - 2;
        this.bounds.height = Math.min(height, MAX_HEIGHT);
        this.bounds.y = Mth.clamp(point.y + margin - this.bounds.height, 4, screenHeight - this.bounds.height - 4);
    }
    
    @Override
    public Rectangle getBounds() {
        return this.bounds;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        this.scroll.setTarget(ScrollingContainer.handleBounceBack(scroll.target(), this.contentHeight - (this.bounds.height - 8 - 9) - 9, delta, .08));
        this.scroll.update(delta);
        
        RenderSystem.disableDepthTest();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        poses.pushPose();
        poses.translate(0, 0, 450);
        Matrix4f pose = poses.last().pose();
        int background = 0xf0100010;
        int color1 = 0x505000ff;
        int color2 = color1;
        int x = this.bounds.x, y = this.bounds.y, width = this.bounds.width, height = this.bounds.height;
        fillGradient(pose, bufferBuilder, x, y - 1, x + width, y, 400, background, background);
        fillGradient(pose, bufferBuilder, x, y + height, x + width, y + height + 1, 400, background, background);
        fillGradient(pose, bufferBuilder, x, y, x + width, y + height, 400, background, background);
        fillGradient(pose, bufferBuilder, x - 1, y, x, y + height, 400, background, background);
        fillGradient(pose, bufferBuilder, x + width, y, x + width + 1, y + height, 400, background, background);
        fillGradient(pose, bufferBuilder, x, y + 1, x + 1, y + height - 1, 400, color1, color2);
        fillGradient(pose, bufferBuilder, x + width - 1, y + 1, x + width, y + height - 1, 400, color1, color2);
        fillGradient(pose, bufferBuilder, x, y, x + width, y + 1, 400, color1, color1);
        fillGradient(pose, bufferBuilder, x, y + height - 1, x + width, y + height, 400, color2, color2);
        BufferUploader.drawWithShader(bufferBuilder.end());
        int lineY = y + 4;
        
        try (CloseableScissors scissors = Widget.scissor(pose, new Rectangle(x + 4, y + 4, width - 8, height - 8 - 9 - 2))) {
            for (List<FormattedCharSequence> block : wrapped) {
                for (FormattedCharSequence line : block) {
                    font.drawShadow(poses, line, x + 4, lineY - scroll.intValue(), 0xFFFFFFFF);
                    lineY += 9;
                }
                
                lineY += 2;
            }
        }
        
        MutableComponent okay = Component.translatable("gui.ok");
        int okayWidth = font.width(okay);
        int midPoint = x + 4 + (width - 4) / 2;
        this.okayBounds.setBounds(midPoint - okayWidth / 2, lineY, okayWidth, 9);
        if (this.okayBounds.contains(mouseX, mouseY)) {
            okay = okay.withStyle(ChatFormatting.UNDERLINE);
        }
        font.drawShadow(poses, okay, this.okayBounds.x, this.okayBounds.y, 0xFF999999);
        
        poses.popPose();
        RenderSystem.enableDepthTest();
        
        if (this.bounds.contains(mouseX, mouseY)) {
            ScreenOverlayImpl.getInstance().clearTooltips();
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.okayBounds.contains(mouseX, mouseY)) {
            this.parent.removeHint(this);
            Widgets.produceClickSound();
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (containsMouse(mouseX, mouseY)) {
            scroll.setTo(scroll.target() + ClothConfigInitializer.getScrollStep() * amount * (getBounds().getWidth() / -50.0), ClothConfigInitializer.getScrollDuration());
            return true;
        }
        
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
