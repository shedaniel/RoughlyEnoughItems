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

package me.shedaniel.rei.api.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.AbstractContainerEventHandler;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Stack;

/**
 * The base class for a screen widget
 *
 * @see WidgetWithBounds for a widget with bounds
 */
@Environment(EnvType.CLIENT)
public abstract class Widget extends AbstractContainerEventHandler implements Renderable, Renderer {
    
    /**
     * The Minecraft Client instance
     */
    protected final Minecraft minecraft = Minecraft.getInstance();
    /**
     * The font for rendering text
     */
    protected final Font font = minecraft.font;
    private static final Stack<Point> mouseStack = new Stack<>();
    
    public static Point mouse() {
        return mouseStack.empty() ? PointHelper.ofMouse() : mouseStack.peek();
    }
    
    public static Point pushMouse(Point mouse) {
        return mouseStack.push(mouse);
    }
    
    public static Point popMouse() {
        return mouseStack.pop();
    }
    
    public static Point translateMouse(PoseStack poses) {
        return translateMouse(poses.last().pose());
    }
    
    public static Point translateMouse(double x, double y, double z) {
        return translateMouse(new Matrix4f().translate((float) x, (float) y, (float) z));
    }
    
    public static Point translateMouse(Matrix4f pose) {
        Point mouse = mouse();
        Vector4f mouseVec = new Vector4f(mouse.x, mouse.y, 0, 1);
        pose.transform(mouseVec);
        return pushMouse(new Point(mouseVec.x(), mouseVec.y()));
    }
    
    public int getZ() {
        return this.getBlitOffset();
    }
    
    public void setZ(int z) {
        this.setBlitOffset(z);
    }
    
    public boolean containsMouse(double mouseX, double mouseY) {
        return false;
    }
    
    @SuppressWarnings("RedundantCast")
    public final boolean containsMouse(int mouseX, int mouseY) {
        return containsMouse((double) mouseX, (double) mouseY);
    }
    
    @SuppressWarnings("RedundantCast")
    public final boolean containsMouse(Point point) {
        return containsMouse((double) point.x, (double) point.y);
    }
    
    @Override
    public final boolean isMouseOver(double mouseX, double mouseY) {
        return containsMouse(mouseX, mouseY);
    }
    
    @Override
    @Deprecated
    public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        render(matrices, mouseX, mouseY, delta);
    }
    
    @ApiStatus.Experimental
    public double getZRenderingPriority() {
        return 0;
    }
    
    @ApiStatus.Experimental
    public static CloseableScissors scissor(PoseStack matrices, Rectangle bounds) {
        return scissor(matrices.last().pose(), bounds);
    }
    
    @ApiStatus.Experimental
    public static CloseableScissors scissor(Matrix4f matrix, Rectangle bounds) {
        ScissorsHandler.INSTANCE.scissor(MatrixUtils.transform(matrix, bounds));
        return ScissorsHandler.INSTANCE::removeLastScissor;
    }
}
