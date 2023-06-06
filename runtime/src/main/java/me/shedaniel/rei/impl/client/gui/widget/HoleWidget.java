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

package me.shedaniel.rei.impl.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.gui.widget.DynamicErrorFreeEntryListWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntSupplier;

public class HoleWidget {
    // 32 for list background, 64 for header and footer
    public static Widget create(Rectangle bounds, IntSupplier yOffset, int colorIntensity) {
        return Widgets.withBounds(
                Widgets.concat(
                        createBackground(bounds, yOffset, colorIntensity),
                        createInnerShadow(bounds)
                ),
                bounds
        );
    }
    
    public static Widget create(Rectangle bounds, ResourceLocation backgroundLocation, IntSupplier yOffset, int colorIntensity) {
        return Widgets.withBounds(
                Widgets.concat(
                        createBackground(bounds, backgroundLocation, yOffset, colorIntensity),
                        createInnerShadow(bounds)
                ),
                bounds
        );
    }
    
    public static Widget createBackground(Rectangle bounds, IntSupplier yOffset, int colorIntensity) {
        return createBackground(bounds, Screen.BACKGROUND_LOCATION, yOffset, colorIntensity);
    }
    
    public static Widget createBackground(Rectangle bounds, ResourceLocation backgroundLocation, IntSupplier yOffset, int colorIntensity) {
        return Widgets.withBounds(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            DynamicErrorFreeEntryListWidget.renderBackBackground(matrices, buffer, tesselator, backgroundLocation, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), yOffset.getAsInt(), colorIntensity);
        }), bounds);
    }
    
    public static Widget createInnerShadow(Rectangle bounds) {
        return Widgets.withBounds(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 771, 0, 1);
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            Matrix4f matrix = matrices.last().pose();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, bounds.x, bounds.y + 4, 0.0F).uv(0, 1).color(0, 0, 0, 0).endVertex();
            buffer.vertex(matrix, bounds.getMaxX(), bounds.y + 4, 0.0F).uv(1, 1).color(0, 0, 0, 0).endVertex();
            buffer.vertex(matrix, bounds.getMaxX(), bounds.y, 0.0F).uv(1, 0).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, bounds.x, bounds.y, 0.0F).uv(0, 0).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, bounds.x, bounds.getMaxY(), 0.0F).uv(0, 1).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, bounds.getMaxX(), bounds.getMaxY(), 0.0F).uv(1, 1).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, bounds.getMaxX(), bounds.getMaxY() - 4, 0.0F).uv(1, 0).color(0, 0, 0, 0).endVertex();
            buffer.vertex(matrix, bounds.x, bounds.getMaxY() - 4, 0.0F).uv(0, 0).color(0, 0, 0, 0).endVertex();
            tesselator.end();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }), bounds);
    }
}
