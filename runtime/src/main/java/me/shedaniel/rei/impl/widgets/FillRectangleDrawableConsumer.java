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

package me.shedaniel.rei.impl.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.DrawableConsumer;
import net.minecraft.client.gui.GuiComponent;
import org.jetbrains.annotations.NotNull;

public final class FillRectangleDrawableConsumer implements DrawableConsumer {
    @NotNull
    private Rectangle rectangle;
    private int color;
    
    public FillRectangleDrawableConsumer(@NotNull Rectangle rectangle, int color) {
        this.rectangle = rectangle;
        this.color = color;
    }
    
    @Override
    public void render(@NotNull GuiComponent helper, @NotNull PoseStack matrices, int mouseX, int mouseY, float delta) {
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(rectangle.getMaxX(), rectangle.getMinY(), helper.getBlitOffset()).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(rectangle.getMinX(), rectangle.getMinY(), helper.getBlitOffset()).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(rectangle.getMinX(), rectangle.getMaxY(), helper.getBlitOffset()).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(rectangle.getMaxX(), rectangle.getMaxY(), helper.getBlitOffset()).color(r, g, b, a).endVertex();
        tessellator.end();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }
}
