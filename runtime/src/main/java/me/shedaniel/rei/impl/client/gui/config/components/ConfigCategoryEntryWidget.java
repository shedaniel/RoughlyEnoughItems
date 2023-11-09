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

package me.shedaniel.rei.impl.client.gui.config.components;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils;
import me.shedaniel.rei.impl.client.gui.config.options.OptionCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import static me.shedaniel.rei.api.client.gui.widgets.Widget.scissor;

public class ConfigCategoryEntryWidget {
    public static WidgetWithBounds create(OptionCategory category, int width) {
        boolean hasDescription = !category.getDescription().getString().endsWith(".desc");
        Label label = Widgets.createLabel(new Point(21, hasDescription ? 5 : 7), category.getName().copy().withStyle(style -> style.withColor(0xFFC0C0C0)))
                .leftAligned();
        Font font = Minecraft.getInstance().font;
        MutableComponent description = category.getDescription().copy().withStyle(style -> style.withColor(0xFF909090));
        Widget descriptionLabel = Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            renderTextScrolling(graphics, description, 0, 0, (int) ((width - 21 - 6) / 0.75), 0xFF909090);
        });
        Rectangle bounds = new Rectangle(0, 0, label.getBounds().getMaxX(), hasDescription ? 24 : 7 * 3);
        return Widgets.concatWithBounds(
                bounds,
                label,
                hasDescription ? Widgets.withTranslate(Widgets.withTranslate(descriptionLabel, new Matrix4f().scaling(0.75f, 0.75f, 0.75f)), 21, 5 + 10, 0) : Widgets.noOp(),
                Widgets.createTexturedWidget(category.getIcon(), new Rectangle(3, hasDescription ? 5 : 3, 16, 16), 0, 0, 1, 1, 1, 1)
        );
    }
    
    public static WidgetWithBounds createTiny(OptionCategory category) {
        Rectangle bounds = new Rectangle(0, 0, 16, 16);
        Component[] texts = category.getDescription().getString().endsWith(".desc") ? new Component[]{category.getName()}
                : new Component[]{category.getName().copy().withStyle(ChatFormatting.UNDERLINE), category.getDescription()};
        return Widgets.withTooltip(
                Widgets.withBounds(Widgets.createTexturedWidget(category.getIcon(), bounds, 0, 0, 1, 1, 1, 1), bounds),
                texts
        );
    }
    
    private static void renderTextScrolling(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        renderTextScrolling(graphics, text.getVisualOrderText(), x, y, width, color);
    }
    
    private static void renderTextScrolling(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int width, int color) {
        try (CloseableScissors scissors = scissor(graphics, new Rectangle(x, y, width, y + 9))) {
            Font font = Minecraft.getInstance().font;
            int textWidth = font.width(text);
            textWidth = MatrixUtils.transform(MatrixUtils.inverse(graphics.pose().last().pose()), new Rectangle(0, 0, textWidth, 100)).width;
            width = MatrixUtils.transform(MatrixUtils.inverse(graphics.pose().last().pose()), new Rectangle(0, 0, width, 100)).width;
            if (textWidth > width && !ConfigUtils.isReducedMotion()) {
                graphics.pose().pushPose();
                float textX = (System.currentTimeMillis() % ((textWidth + 10) * textWidth / 3)) / (float) textWidth * 3;
                graphics.pose().translate(-textX, 0, 0);
                graphics.drawString(font, text, x + width - textWidth - 10, y, color);
                graphics.drawString(font, text, x + width, y, color);
                graphics.pose().popPose();
            } else {
                graphics.drawString(font, text, x, y, color);
            }
        }
    }
}
