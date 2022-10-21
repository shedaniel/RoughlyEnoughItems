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

package me.shedaniel.rei.impl.client.gui.overlay.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ScaleIndicatorWidget extends Widget {
    private final NumberAnimator<Double> scaleIndicator = ValueAnimator.ofDouble(0.0D)
            .withConvention(() -> 0.0D, 8000);
    private int x, y;
    
    public void setCenter(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void set() {
        this.scaleIndicator.setAs(10);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        scaleIndicator.update(delta);
        if (scaleIndicator.value() > 0.04) {
            matrices.pushPose();
            matrices.translate(0, 0, 500);
            TextComponent component = new TextComponent(Math.round(ConfigObject.getInstance().getEntrySize() * 100) + "%");
            int width = font.width(component);
            int backgroundColor = ((int) Math.round(0xa0 * Mth.clamp(scaleIndicator.value(), 0.0, 1.0))) << 24;
            int textColor = ((int) Math.round(0xdd * Mth.clamp(scaleIndicator.value(), 0.0, 1.0))) << 24;
            fillGradient(matrices, x - width / 2 - 2, y - 6, x + width / 2 + 2, y + 6, backgroundColor, backgroundColor);
            font.draw(matrices, component, x - width / 2, y - 4, 0xFFFFFF | textColor);
            matrices.popPose();
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
