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

import me.shedaniel.math.Dimension;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.DrawableConsumer;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.widgets.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class Widgets {
    private Widgets() {}
    
    public static Widget createDrawableWidget(DrawableConsumer drawable) {
        return new DrawableWidget(drawable);
    }
    
    public static Widget createTexturedWidget(Identifier identifier, Rectangle bounds) {
        return createTexturedWidget(identifier, bounds, 0, 0);
    }
    
    public static Widget createTexturedWidget(Identifier identifier, int x, int y, int width, int height) {
        return createTexturedWidget(identifier, x, y, 0, 0, width, height);
    }
    
    public static Widget createTexturedWidget(Identifier identifier, Rectangle bounds, float u, float v) {
        return createTexturedWidget(identifier, bounds, u, v, 256, 256);
    }
    
    public static Widget createTexturedWidget(Identifier identifier, int x, int y, float u, float v, int width, int height) {
        return createTexturedWidget(identifier, x, y, u, v, width, height, 256, 256);
    }
    
    public static Widget createTexturedWidget(Identifier identifier, Rectangle bounds, float u, float v, int textureWidth, int textureHeight) {
        return createTexturedWidget(identifier, bounds.x, bounds.y, u, v, bounds.width, bounds.height, bounds.width, bounds.height, textureWidth, textureHeight);
    }
    
    public static Widget createTexturedWidget(Identifier identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        return createTexturedWidget(identifier, x, y, u, v, width, height, width, height, textureWidth, textureHeight);
    }
    
    public static Widget createTexturedWidget(Identifier identifier, Rectangle bounds, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        return createTexturedWidget(identifier, bounds.x, bounds.y, u, v, bounds.width, bounds.height, uWidth, vHeight, textureWidth, textureHeight);
    }
    
    public static Widget createTexturedWidget(Identifier identifier, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        return createDrawableWidget(new TexturedDrawableConsumer(identifier, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight));
    }
    
    public static Widget createFilledRectangle(Rectangle rectangle, int color) {
        return createDrawableWidget(new FillRectangleDrawableConsumer(rectangle, color));
    }
    
    public static Label createLabel(Point point, @NotNull String text) {
        return new LabelWidget(point, text);
    }
    
    public static Label createClickableLabel(Point point, @NotNull String text, @Nullable Consumer<Label> onClick) {
        return new LabelWidget(point, text).clickable().onClick(onClick);
    }
    
    public static Arrow createArrow(Point point) {
        return new ArrowWidget(new Rectangle(point, new Dimension(24, 17)));
    }
    
    public static BurningFire createBurningFire(Point point) {
        return new BurningFireWidget(new Rectangle(point, new Dimension(14, 14)));
    }
    
    public static Widget createSlotBackground(Point point) {
        return createSlotBase(new Rectangle(point.x - 1, point.y - 1, 18, 18));
    }
    
    public static Widget createResultSlotBackground(Point point) {
        return createSlotBase(new Rectangle(point.x - 5, point.y - 5, 26, 26));
    }
    
    public static Panel createRecipeBase(Rectangle rectangle) {
        return new PanelWidget(rectangle).yTextureOffset(ConfigObject.getInstance().getRecipeBorderType().getYOffset()).rendering(Widgets::shouldRecipeBaseRender);
    }
    
    private static boolean shouldRecipeBaseRender(Panel panel) {
        return ConfigObject.getInstance().getRecipeBorderType().isRendering() && PanelWidget.isRendering(panel);
    }
    
    public static Panel createRecipeBase(Rectangle rectangle, int color) {
        return createRecipeBase(rectangle).color(color);
    }
    
    public static Panel createSlotBase(Rectangle rectangle) {
        return new PanelWidget(rectangle).yTextureOffset(-66).rendering(Widgets::shouldSlotBaseRender).innerColor(-7631989, -13619152);
    }
    
    private static boolean shouldSlotBaseRender(Panel panel) {
        return true;
    }
    
    public static Panel createSlotBase(Rectangle rectangle, int color) {
        return createSlotBase(rectangle).color(color);
    }
    
    @SuppressWarnings("deprecation")
    public static Slot createSlot(Point point) {
        return EntryWidget.create(point.x, point.y);
    }
    
    public static void produceClickSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
