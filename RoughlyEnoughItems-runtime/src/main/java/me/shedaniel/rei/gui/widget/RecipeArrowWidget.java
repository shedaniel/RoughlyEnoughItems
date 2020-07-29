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
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.REIHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @see me.shedaniel.rei.api.widgets.Widgets#createArrow(me.shedaniel.math.Point)
 */
@ApiStatus.ScheduledForRemoval
@Deprecated
public class RecipeArrowWidget extends WidgetWithBounds {
    
    private int x, y;
    private double time = 250d;
    private boolean animated;
    
    @ApiStatus.Internal
    public RecipeArrowWidget(int x, int y, boolean animated) {
        this.x = x;
        this.y = y;
        this.animated = animated;
    }
    
    public static RecipeArrowWidget create(Point point, boolean animated) {
        return new RecipeArrowWidget(point.x, point.y, animated);
    }
    
    public RecipeArrowWidget time(double time) {
        this.time = time;
        return this;
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, 24, 17);
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(REIHelper.getInstance().getDefaultDisplayTexture());
        drawTexture(matrices, x, y, 106, 91, 24, 17);
        if (animated) {
            int width = MathHelper.ceil((System.currentTimeMillis() / (time / 24) % 24d) / 1f);
            drawTexture(matrices, x, y, 82, 91, width, 17);
        }
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
}
