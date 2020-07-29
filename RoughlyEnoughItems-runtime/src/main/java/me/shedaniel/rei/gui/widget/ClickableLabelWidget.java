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

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.api.widgets.Widgets;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @see Widgets#createClickableLabel(Point, String, Consumer)
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public abstract class ClickableLabelWidget extends LabelWidget {
    
    public boolean focused;
    private boolean clickable = true;
    private int hoveredColor;
    
    @ApiStatus.Internal
    protected ClickableLabelWidget(Point point, Text text) {
        super(point, text);
        this.hoveredColor = REIHelper.getInstance().isDarkThemeEnabled() ? -1 : 0xFF66FFCC;
    }
    
    public LabelWidget hoveredColor(int hoveredColor) {
        this.hoveredColor = hoveredColor;
        return this;
    }
    
    public LabelWidget clickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }
    
    public boolean isClickable() {
        return clickable;
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int color = getDefaultColor();
        if (isClickable() && isHovered(mouseX, mouseY))
            color = getHoveredColor();
        Point pos = getLocation();
        int width = font.getStringWidth(getText());
        if (isCentered()) {
            if (isHasShadows())
                font.drawWithShadow(matrices, text.method_30937(), pos.x - width / 2f, pos.y, color);
            else
                font.draw(matrices, text.method_30937(), pos.x - width / 2f, pos.y, color);
        } else {
            if (isHasShadows())
                font.drawWithShadow(matrices, text.method_30937(), pos.x, pos.y, color);
            else
                font.draw(matrices, text.method_30937(), pos.x, pos.y, color);
        }
        drawTooltips(mouseX, mouseY);
    }
    
    @Override
    protected void drawTooltips(int mouseX, int mouseY) {
        if (isClickable() && getTooltips().isPresent())
            if (!focused && containsMouse(mouseX, mouseY))
                Tooltip.create(Stream.of(getTooltips().get().split("\n")).map(LiteralText::new).collect(Collectors.toList())).queue();
            else if (focused)
                Tooltip.create(getLocation(), Stream.of(getTooltips().get().split("\n")).map(LiteralText::new).collect(Collectors.toList())).queue();
    }
    
    public int getHoveredColor() {
        return hoveredColor;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isClickable() && containsMouse(mouseX, mouseY)) {
            onLabelClicked();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (!isClickable() || !focused)
            return false;
        if (int_1 != 257 && int_1 != 32 && int_1 != 335)
            return false;
        this.onLabelClicked();
        return true;
    }
    
    @Override
    public boolean changeFocus(boolean boolean_1) {
        if (!isClickable())
            return false;
        this.focused = !this.focused;
        return true;
    }
    
    public boolean isHovered(int mouseX, int mouseY) {
        return isClickable() && (containsMouse(mouseX, mouseY) || focused);
    }
    
    public abstract void onLabelClicked();
    
}
