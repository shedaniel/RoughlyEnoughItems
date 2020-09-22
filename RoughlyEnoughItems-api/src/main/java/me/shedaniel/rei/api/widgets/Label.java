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

package me.shedaniel.rei.api.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Label extends WidgetWithBounds {
    public static final int LEFT_ALIGNED = -1;
    public static final int CENTER = 0;
    public static final int RIGHT_ALIGNED = 1;
    
    /**
     * @return whether the label is clickable, ignores if onClick is set.
     */
    public abstract boolean isClickable();
    
    /**
     * Sets whether the label is clickable, ignores if onClick is set.
     *
     * @param clickable whether the label is clickable.
     */
    public abstract void setClickable(boolean clickable);
    
    /**
     * Sets the label as clickable, ignores if onClick is set.
     *
     * @return the label itself.
     */
    @NotNull
    public final Label clickable() {
        return clickable(true);
    }
    
    /**
     * Sets whether the label is clickable, ignores if onClick is set.
     *
     * @param clickable whether the label is clickable.
     * @return the label itself.
     */
    @NotNull
    public final Label clickable(boolean clickable) {
        setClickable(clickable);
        return this;
    }
    
    /**
     * @return the consumer on click, only applicable if the label is clickable, null if not set.
     */
    @Nullable
    public abstract Consumer<Label> getOnClick();
    
    /**
     * Sets the on click consumer, only applicable if the label is clickable.
     *
     * @param onClick the on click consumer, only applicable if the label is clickable.
     */
    public abstract void setOnClick(@Nullable Consumer<Label> onClick);
    
    /**
     * Sets the on click consumer, only applicable if the label is clickable.
     *
     * @param onClick the on click consumer, only applicable if the label is clickable.
     * @return the label itself.
     */
    @NotNull
    public final Label onClick(@Nullable Consumer<Label> onClick) {
        setOnClick(onClick);
        return this;
    }
    
    /**
     * @return the consumer before render, null if not set.
     */
    @Nullable
    public abstract BiConsumer<PoseStack, Label> getOnRender();
    
    /**
     * Sets the consumer before render.
     *
     * @param onRender the consumer before render.
     */
    public abstract void setOnRender(@Nullable BiConsumer<PoseStack, Label> onRender);
    
    /**
     * Sets the consumer before render.
     *
     * @param onRender the consumer before render.
     * @return the label itself.
     */
    @NotNull
    public final Label onRender(@Nullable BiConsumer<PoseStack, Label> onRender) {
        setOnRender(onRender);
        return this;
    }
    
    /**
     * @return whether the label is focusable by pressing tab, ignored if not clickable.
     */
    public abstract boolean isFocusable();
    
    /**
     * Sets whether the label is focusable by pressing tab, ignored if not clickable.
     *
     * @param focusable whether the label is focusable by pressing tab, ignored if not clickable.
     */
    public abstract void setFocusable(boolean focusable);
    
    /**
     * Sets whether the label is focusable by pressing tab, ignored if not clickable.
     *
     * @param focusable whether the label is focusable by pressing tab, ignored if not clickable.
     * @return the label itself.
     */
    @NotNull
    public final Label focusable(boolean focusable) {
        setFocusable(focusable);
        return this;
    }
    
    /**
     * @return the tooltip from the current tooltip function, null if no tooltip.
     */
    @Nullable
    public abstract String getTooltip();
    
    /**
     * Sets the tooltip function used to get the tooltip.
     *
     * @param tooltip the tooltip function used to get the tooltip.
     */
    public abstract void setTooltip(@Nullable Function<Label, @Nullable String> tooltip);
    
    /**
     * Sets the tooltip.
     *
     * @param tooltip the lines of tooltip.
     * @return the label itself.
     */
    @NotNull
    public final Label tooltipLines(@NotNull String... tooltip) {
        return tooltipLine(String.join("\n", tooltip));
    }
    
    /**
     * Sets the tooltip.
     *
     * @param tooltip the line of tooltip.
     * @return the label itself.
     */
    @NotNull
    public final Label tooltipLine(@Nullable String tooltip) {
        return tooltipSupplier(label -> tooltip);
    }
    
    /**
     * Sets the tooltip function.
     *
     * @param tooltip the tooltip function used to get the tooltip.
     * @return the label itself.
     */
    @NotNull
    public final Label tooltipSupplier(@Nullable Function<Label, @Nullable String> tooltip) {
        setTooltip(tooltip);
        return this;
    }
    
    /**
     * Gets the horizontal alignment of the label, defaulted as centered.
     *
     * @return {@link Label#LEFT_ALIGNED} if left aligned, {@link Label#CENTER} if centered or {@link Label#RIGHT_ALIGNED} if right aligned}.
     */
    public abstract int getHorizontalAlignment();
    
    @NotNull
    public final Label centered() {
        return horizontalAlignment(CENTER);
    }
    
    @NotNull
    public final Label leftAligned() {
        return horizontalAlignment(LEFT_ALIGNED);
    }
    
    @NotNull
    public final Label rightAligned() {
        return horizontalAlignment(RIGHT_ALIGNED);
    }
    
    public abstract void setHorizontalAlignment(int horizontalAlignment);
    
    public final Label horizontalAlignment(int horizontalAlignment) {
        setHorizontalAlignment(horizontalAlignment);
        return this;
    }
    
    public abstract boolean hasShadow();
    
    @NotNull
    public final Label noShadow() {
        return shadow(false);
    }
    
    @NotNull
    public final Label shadow() {
        return shadow(true);
    }
    
    public abstract void setShadow(boolean hasShadow);
    
    @NotNull
    public final Label shadow(boolean hasShadow) {
        setShadow(hasShadow);
        return this;
    }
    
    public abstract int getColor();
    
    public abstract void setColor(int color);
    
    @NotNull
    public final Label color(int lightModeColor, int darkModeColor) {
        return color(REIHelper.getInstance().isDarkThemeEnabled() ? darkModeColor : lightModeColor);
    }
    
    @NotNull
    public final Label color(int color) {
        setColor(color);
        return this;
    }
    
    public abstract int getHoveredColor();
    
    public abstract void setHoveredColor(int hoveredColor);
    
    @NotNull
    public final Label hoveredColor(int lightModeColor, int darkModeColor) {
        return hoveredColor(REIHelper.getInstance().isDarkThemeEnabled() ? darkModeColor : lightModeColor);
    }
    
    @NotNull
    public final Label hoveredColor(int color) {
        setHoveredColor(color);
        return this;
    }
    
    @NotNull
    public abstract Point getPoint();
    
    public final int getX() {
        return getPoint().getX();
    }
    
    public final int getY() {
        return getPoint().getY();
    }
    
    public abstract void setPoint(@NotNull Point point);
    
    @NotNull
    public final Label point(@NotNull Point point) {
        setPoint(point);
        return this;
    }
    
    @NotNull
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public Component getText() {
        return new TextComponent("");
    }
    
    public abstract FormattedText getMessage();
    
    public final void setText(@NotNull Component text) {
        setMessage(text);
    }
    
    public abstract void setMessage(@NotNull FormattedText message);
    
    public abstract void setRainbow(boolean rainbow);
    
    @NotNull
    public final Label text(@NotNull Component text) {
        setText(text);
        return this;
    }
    
    @NotNull
    public final Label message(@NotNull FormattedText message) {
        setMessage(message);
        return this;
    }
    
    @NotNull
    public final Label rainbow(boolean rainbow) {
        setRainbow(rainbow);
        return this;
    }
}
