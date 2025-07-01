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

package me.shedaniel.rei.impl.client.gui.widget.search;

import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.function.Consumer;

public class CalculatorDisplay implements Consumer<String> {
    private final CalculatorDisplayUtils utils = new CalculatorDisplayUtils(7);
    private final OverlaySearchField searchField;
    private final NumberAnimator<Integer> width = ValueAnimator.ofDouble().asInt();
    private Component fullText = Component.empty();
    private FormattedCharSequence text = FormattedCharSequence.EMPTY;
    
    public CalculatorDisplay(OverlaySearchField searchField) {
        this.searchField = searchField;
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int prevWidth = width.value();
        this.width.update(delta);
        if (prevWidth != width.value()) {
            // Keep the search field bounded
            this.searchField.addText("");
        }
        
        if (width.value() <= 0) return;
        Rectangle searchFieldBounds = searchField.getBounds();
        boolean contains = searchFieldBounds.contains(mouseX, mouseY) || searchField.isFocused();
        graphics.fill(searchFieldBounds.getMaxX() - width.value(), searchFieldBounds.y + 1, searchFieldBounds.getMaxX() - 1, searchFieldBounds.getMaxY() - 1, contains ? 0xBBFFFFFF : 0x80FFFFFF);
        graphics.enableScissor(searchFieldBounds.getMaxX() - width.value() + 3, searchFieldBounds.y, searchFieldBounds.getMaxX() - 3, searchFieldBounds.getMaxY());
        graphics.drawString(Minecraft.getInstance().font, text, searchFieldBounds.getMaxX() - width.value() + 3, searchFieldBounds.getCenterY() - 4, 0xFF000000, false);
        graphics.disableScissor();
        
        if (!this.fullText.getString().isEmpty() && contains && new Rectangle(searchFieldBounds.getMaxX() - width.value() + 3, searchFieldBounds.y, width.value() - 6, searchFieldBounds.getHeight()).contains(mouseX, mouseY)) {
            Tooltip.create(this.fullText).queue();
        }
    }
    
    @Override
    public void accept(String text) {
        if (!text.startsWith("=")) {
            this.width.setTo(0, ConfigObject.getInstance().isReducedMotion() ? 0 : 400);
            this.text = Component.literal("NaN").getVisualOrderText();
            this.fullText = Component.empty();
            return;
        }
        
        if (TextCalculator.isValid(text)) {
            double eval = new TextCalculator(text).eval();
            this.text = Component.literal(utils.fmt(eval)).getVisualOrderText();
            this.fullText = Component.literal(CalculatorDisplayUtils.fmtAccurate(eval));
        }
        
        this.width.setTo(Minecraft.getInstance().font.width(this.text) + 6, ConfigObject.getInstance().isReducedMotion() ? 0 : 400);
    }
    
    public int width() {
        return width.value();
    }
}
