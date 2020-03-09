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
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Widgets;
import net.minecraft.client.gui.Element;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @see Widgets#createLabel(Point, String)
 * @see Widgets#createClickableLabel(Point, String, Consumer)
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public class LabelWidget extends WidgetWithBounds {
    
    private Point pos;
    private String text;
    private int defaultColor;
    private boolean hasShadows = true;
    private boolean centered = true;
    private Supplier<String> tooltipSupplier;
    
    @ApiStatus.Internal
    public LabelWidget(me.shedaniel.math.api.Point point, String text) {
        this((Point) point, text);
    }
    
    @ApiStatus.Internal
    public LabelWidget(Point point, String text) {
        this.pos = point;
        this.text = text;
        this.defaultColor = REIHelper.getInstance().isDarkThemeEnabled() ? 0xFFBBBBBB : -1;
    }
    
    public static LabelWidget create(Point point, String text) {
        return new LabelWidget(point, text);
    }
    
    public static LabelWidget create(me.shedaniel.math.api.Point point, String text) {
        return new LabelWidget(point, text);
    }
    
    public static ClickableLabelWidget createClickable(me.shedaniel.math.api.Point point, String text, Consumer<ClickableLabelWidget> onClicked) {
        return createClickable((Point) point, text, onClicked);
    }
    
    public static ClickableLabelWidget createClickable(Point point, String text, Consumer<ClickableLabelWidget> onClicked) {
        ClickableLabelWidget[] widget = {null};
        widget[0] = new ClickableLabelWidget(point, text) {
            @Override
            public void onLabelClicked() {
                onClicked.accept(widget[0]);
            }
        };
        return widget[0];
    }
    
    public LabelWidget tooltip(Supplier<String> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public boolean isCentered() {
        return centered;
    }
    
    public void setCentered(boolean centered) {
        this.centered = centered;
    }
    
    public LabelWidget centered() {
        setCentered(true);
        return this;
    }
    
    public LabelWidget leftAligned() {
        setCentered(false);
        return this;
    }
    
    public boolean isHasShadows() {
        return hasShadows;
    }
    
    public void setHasShadows(boolean hasShadows) {
        this.hasShadows = hasShadows;
    }
    
    public LabelWidget noShadow() {
        setHasShadows(false);
        return this;
    }
    
    public int getDefaultColor() {
        return defaultColor;
    }
    
    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }
    
    /**
     * @return the position of this label
     * @deprecated Use {@link #getLocation()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public me.shedaniel.math.api.Point getPosition() {
        return new me.shedaniel.math.api.Point(getLocation());
    }
    
    public Point getLocation() {
        return pos;
    }
    
    public LabelWidget setPosition(me.shedaniel.math.api.Point position) {
        return setLocation(position);
    }
    
    public LabelWidget setLocation(Point position) {
        this.pos = position;
        return this;
    }
    
    public String getText() {
        return text;
    }
    
    public LabelWidget setText(String text) {
        this.text = text;
        return this;
    }
    
    public LabelWidget color(int defaultColor) {
        this.defaultColor = defaultColor;
        return this;
    }
    
    public Optional<String> getTooltips() {
        return Optional.ofNullable(tooltipSupplier).map(Supplier::get);
    }
    
    @Override
    public Rectangle getBounds() {
        int width = font.getStringWidth(text);
        Point pos = getPosition();
        if (isCentered())
            return new Rectangle(pos.x - width / 2 - 1, pos.y - 5, width + 2, 14);
        return new Rectangle(pos.x - 1, pos.y - 5, width + 2, 14);
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        int width = font.getStringWidth(text);
        Point pos = getPosition();
        if (isCentered()) {
            if (hasShadows)
                font.drawWithShadow(text, pos.x - width / 2f, pos.y, defaultColor);
            else
                font.draw(text, pos.x - width / 2f, pos.y, defaultColor);
        } else {
            if (hasShadows)
                font.drawWithShadow(text, pos.x, pos.y, defaultColor);
            else
                font.draw(text, pos.x, pos.y, defaultColor);
        }
    }
    
    protected void drawTooltips(int mouseX, int mouseY) {
        if (getTooltips().isPresent())
            if (containsMouse(mouseX, mouseY))
                REIHelper.getInstance().addTooltip(QueuedTooltip.create(getTooltips().get().split("\n")));
    }
}
