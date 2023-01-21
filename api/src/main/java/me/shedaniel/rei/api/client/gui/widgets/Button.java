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

package me.shedaniel.rei.api.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Button extends BaseWidget<Button> {
    public abstract void setTextColor(@Nullable BiFunction<Button, Point, Integer> textColorFunction);
    
    public final Button textColor(@Nullable BiFunction<Button, Point, Integer> textColorFunction) {
        setTextColor(textColorFunction);
        return this;
    }
    
    public abstract int getTextColor(Point mouse);
    
    public abstract void setTextureId(@Nullable BiFunction<Button, Point, Integer> textureIdFunction);
    
    public final Button textureId(@Nullable BiFunction<Button, Point, Integer> textureIdFunction) {
        setTextureId(textureIdFunction);
        return this;
    }
    
    public abstract int getTextureId(Point mouse);
    
    public abstract void onClick();
    
    public abstract boolean isEnabled();
    
    public abstract void setEnabled(boolean enabled);
    
    public final Button enabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }
    
    public abstract OptionalInt getTint();
    
    public abstract void setTint(int tint);
    
    public abstract void removeTint();
    
    public final Button tint(@Nullable Integer tint) {
        if (tint == null)
            removeTint();
        else setTint(tint);
        return this;
    }
    
    public abstract Component getText();
    
    public abstract void setText(Component text);
    
    public final Button text(Component text) {
        setText(text);
        return this;
    }
    
    @Nullable
    public abstract Consumer<Button> getOnClick();
    
    public abstract void setOnClick(@Nullable Consumer<Button> onClick);
    
    public final Button onClick(@Nullable Consumer<Button> onClick) {
        setOnClick(onClick);
        return this;
    }
    
    @Nullable
    public abstract BiConsumer<PoseStack, Button> getOnRender();
    
    public abstract void setOnRender(@Nullable BiConsumer<PoseStack, Button> onRender);
    
    public final Button onRender(@Nullable BiConsumer<PoseStack, Button> onRender) {
        setOnRender(onRender);
        return this;
    }
    
    /**
     * @return whether the button is focusable by pressing tab, ignored if not clickable.
     */
    public abstract boolean isFocusable();
    
    /**
     * Sets whether the button is focusable by pressing tab, ignored if not clickable.
     *
     * @param focusable whether the button is focusable by pressing tab, ignored if not clickable.
     */
    public abstract void setFocusable(boolean focusable);
    
    /**
     * Sets whether the button is focusable by pressing tab, ignored if not clickable.
     *
     * @param focusable whether the label is focusable by pressing tab, ignored if not clickable.
     * @return the button itself.
     */
    public final Button focusable(boolean focusable) {
        setFocusable(focusable);
        return this;
    }
    
    /**
     * @return the tooltip from the current tooltip function, null if no tooltip.
     */
    @Nullable
    public abstract Component[] getTooltip();
    
    /**
     * Sets the tooltip function used to get the tooltip.
     *
     * @param tooltip the tooltip function used to get the tooltip.
     */
    public abstract void setTooltip(@Nullable Function<Button, @Nullable Component[]> tooltip);
    
    /**
     * Sets the tooltip.
     *
     * @param tooltip the lines of tooltip.
     * @return the button itself.
     */
    public final Button tooltipLines(Component... tooltip) {
        return tooltipSupplier(button -> tooltip);
    }
    
    /**
     * Sets the tooltip.
     *
     * @param tooltip the line of tooltip.
     * @return the button itself.
     */
    public final Button tooltipLine(@Nullable Component tooltip) {
        return tooltipLines(tooltip);
    }
    
    /**
     * Sets the tooltip function.
     *
     * @param tooltip the tooltip function used to get the tooltip.
     * @return the button itself.
     */
    public final Button tooltipSupplier(@Nullable Function<Button, @Nullable Component[]> tooltip) {
        setTooltip(tooltip);
        return this;
    }
    
    /**
     * Sets the tooltip function.
     *
     * @param tooltip the tooltip function used to get the tooltip.
     * @return the button itself.
     */
    public final Button tooltipLineSupplier(@Nullable Function<Button, @Nullable Component> tooltip) {
        setTooltip(button -> new Component[]{tooltip.apply(button)});
        return this;
    }
    
    public abstract boolean isFocused();
}
