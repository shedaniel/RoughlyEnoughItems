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

package me.shedaniel.rei.impl.client.gui.config.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Label;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.impl.client.gui.config.ConfigAccess;
import me.shedaniel.rei.impl.client.gui.config.options.CompositeOption;
import me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils;
import me.shedaniel.rei.impl.client.gui.text.TextTransformations;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.literal;
import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public class ConfigOptionWidget {
    public static <T> WidgetWithBounds create(ConfigAccess access, CompositeOption<T> option, int width) {
        List<Widget> widgets = new ArrayList<>();
        int[] stableHeight = {12};
        int[] height = {12};
        Label fieldNameLabel;
        widgets.add(fieldNameLabel = Widgets.createLabel(new Point(0, 0), TextTransformations.highlightText(option.getName().copy(), option.getOptionNameHighlight(), style -> style.withColor(0xFFC0C0C0)))
                .leftAligned());
        WidgetWithBounds optionValue = ConfigOptionValueWidget.create(access, option, width - 10 - fieldNameLabel.getBounds().width);
        widgets.add(Widgets.withTranslate(optionValue, () -> Matrix4f.createTranslateMatrix(width - optionValue.getBounds().width - optionValue.getBounds().x, 0, 0)));
        widgets.add(new WidgetWithBounds() {
            final MutableComponent description = Util.make(() -> {
                MutableComponent description = option.getDescription().copy();
                if (description.getString().endsWith(".desc")) {
                    return literal("");
                } else {
                    return TextTransformations.highlightText(description, option.getOptionDescriptionHighlight(), style -> style.withColor(0xFF757575));
                }
            });
            
            final List<FormattedCharSequence> split = Minecraft.getInstance().font.split(description, width);
            final boolean hasPreview = option.hasPreview();
            final Label previewLabel = Widgets.createLabel(new Point(), translatable("config.rei.texts.preview"))
                    .color(0xFFA5F4FF)
                    .hoveredColor(0xFFD1FAFF)
                    .noShadow()
                    .clickable()
                    .onClick($ -> clickPreview())
                    .rightAligned();
            @Nullable
            WidgetWithBounds preview = null;
            boolean previewVisible = false;
            Matrix4f previewTranslation = new Matrix4f();
            final NumberAnimator<Float> previewHeight = ValueAnimator.ofFloat()
                    .withConvention(() -> previewVisible ? preview.getBounds().getHeight() : 0f, ValueAnimator.typicalTransitionTime());
            boolean nextLinePreview = false;
            
            {
                stableHeight[0] += 12 * split.size();
                if (hasPreview) {
                    int lastWidth = Minecraft.getInstance().font.width(split.get(split.size() - 1));
                    if (lastWidth + this.previewLabel.getBounds().width + 10 > width) {
                        nextLinePreview = true;
                        stableHeight[0] += 12;
                    }
                }
            }
            
            @Override
            public Rectangle getBounds() {
                return new Rectangle(0, 12, width, 12 + 12 * split.size());
            }
            
            @Override
            public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                this.previewHeight.update(delta);
                if (ConfigUtils.isReducedMotion()) this.previewHeight.completeImmediately();
                height[0] = stableHeight[0] + Math.round(this.previewHeight.value());
                
                for (int i = 0; i < split.size(); i++) {
                    Minecraft.getInstance().font.draw(poses, split.get(i), 0, 12 + 12 * i, -1);
                }
                
                if (hasPreview) {
                    if (nextLinePreview) {
                        this.previewLabel.setPoint(new Point(width, 12 + 12 * split.size()));
                    } else {
                        this.previewLabel.setPoint(new Point(width, 12 + 12 * split.size() - 12));
                    }
                    
                    this.previewLabel.render(poses, mouseX, mouseY, delta);
                    
                    if (this.preview != null && this.previewHeight.value() > 0.1f) {
                        ScissorsHandler.INSTANCE.scissor(MatrixUtils.transform(poses.last().pose(), new Rectangle(0, 24 + 12 * split.size() - (nextLinePreview ? 0 : 12), width, this.previewHeight.value())));
                        this.previewTranslation = Matrix4f.createTranslateMatrix(0, 12 + 12 * split.size(), 100);
                        this.preview.render(poses, mouseX, mouseY, delta);
                        ScissorsHandler.INSTANCE.removeLastScissor();
                    }
                }
            }
            
            private void clickPreview() {
                if (this.preview == null) {
                    this.preview = option.getPreviewer().preview(width, () -> access.get(option));
                    this.preview = Widgets.withTranslate(this.preview, () -> this.previewTranslation);
                }
                
                this.previewVisible = !this.previewVisible;
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                if (this.preview != null && this.previewHeight.value() > 0.1f) return List.of(this.previewLabel, this.preview);
                return List.of(this.previewLabel);
            }
        });
        
        height[0] = stableHeight[0];
        return Widgets.concatWithBounds(() -> new Rectangle(0, 0, width, height[0]), widgets);
    }
}
