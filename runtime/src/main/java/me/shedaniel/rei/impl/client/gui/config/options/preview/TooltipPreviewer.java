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

package me.shedaniel.rei.impl.client.gui.config.options.preview;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.gui.config.ConfigAccess;
import me.shedaniel.rei.impl.client.gui.config.options.AllREIConfigOptions;
import me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

public class TooltipPreviewer {
    public static WidgetWithBounds create(ConfigAccess access, int width, @Nullable IntSupplier height) {
        Rectangle bounds = new Rectangle();
        return Widgets.withBounds(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            EntryStack<ItemStack> stack = EntryStacks.of(Items.OAK_PLANKS);
            boolean appendModNames = access.get(AllREIConfigOptions.APPEND_MOD_NAMES);
            boolean appendFavorites = access.get(AllREIConfigOptions.APPEND_FAVORITES_HINT);
            List<Tooltip.Entry> entries = new ArrayList<>();
            entries.add(Tooltip.entry(ConfigUtils.translatable("block.minecraft.oak_planks")));
            if (appendModNames) {
                entries.add(Tooltip.entry(ConfigUtils.literal("Minecraft").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
            }
            if (appendFavorites) {
                String name = ConfigObject.getInstance().getFavoriteKeyCode().getLocalizedName().getString();
                entries.addAll(Stream.of(I18n.get("text.rei.favorites_tooltip", name).split("\n"))
                        .map(ConfigUtils::literal).map(Tooltip::entry).toList());
            }
            List<FormattedCharSequence> components = entries.stream().flatMap(entry -> Minecraft.getInstance().font.split(entry.getAsText(), width - 12 - 4).stream()).toList();
            int minWidth = components.stream().mapToInt(component -> Minecraft.getInstance().font.width(component)).max().orElse(0) + 4;
            int minHeight = components.stream().mapToInt(component -> components.get(0) == component && components.size() >= 2 ? 2 + 10 : 10).sum() + 4;
            
            int tX = Math.max(6, (width - minWidth) / 2), tWidth = Math.min(width - 12, minWidth), tY = 24 + 4, tHeight = Math.min(minHeight, height == null ? 100000 : height.getAsInt() - tY - 4);
            graphics.pose().pushMatrix();
            graphics.pose().translate(0, height == null ? 4 : Math.max(0, (height.getAsInt() - (tY + tHeight)) / 2));
            bounds.setSize(width, height == null ? tY + tHeight + 12 : height.getAsInt());
            stack.getRenderer().render(stack, graphics, new Rectangle(width / 2 - 12, 0, 24, 24), mouseX, mouseY, delta);
            
            int finalTY = tY;
            graphics.fillGradient(tX - 3, finalTY - 4, tX + tWidth + 3, finalTY - 3, -267386864, -267386864);
            graphics.fillGradient(tX - 3, finalTY + tHeight + 3, tX + tWidth + 3, finalTY + tHeight + 4, -267386864, -267386864);
            graphics.fillGradient(tX - 3, finalTY - 3, tX + tWidth + 3, finalTY + tHeight + 3, -267386864, -267386864);
            graphics.fillGradient(tX - 4, finalTY - 3, tX - 3, finalTY + tHeight + 3, -267386864, -267386864);
            graphics.fillGradient(tX + tWidth + 3, finalTY - 3, tX + tWidth + 4, finalTY + tHeight + 3, -267386864, -267386864);
            graphics.fillGradient(tX - 3, finalTY - 3 + 1, tX - 3 + 1, finalTY + tHeight + 3 - 1, 1347420415, 1344798847);
            graphics.fillGradient(tX + tWidth + 2, finalTY - 3 + 1, tX + tWidth + 3, finalTY + tHeight + 3 - 1, 1347420415, 1344798847);
            graphics.fillGradient(tX - 3, finalTY - 3, tX + tWidth + 3, finalTY - 3 + 1, 1347420415, 1347420415);
            graphics.fillGradient(tX - 3, finalTY + tHeight + 2, tX + tWidth + 3, finalTY + tHeight + 3, 1344798847, 1344798847);
            
            for (int i = 0; i < components.size(); i++) {
                graphics.drawString(Minecraft.getInstance().font, components.get(i), tX + 2, tY + 2, -1, false);
                tY += 10 + (i == 0 ? 2 : 0);
            }
            
            graphics.pose().popMatrix();
        }), bounds);
    }
    
    private static void fillGradient(Matrix4f pose, VertexConsumer buffer, int x1, int y1, int x2, int y2, int blitOffset, int color1, int color2) {
        float f = (float) (color1 >> 24 & 0xFF) / 255.0F;
        float g = (float) (color1 >> 16 & 0xFF) / 255.0F;
        float h = (float) (color1 >> 8 & 0xFF) / 255.0F;
        float i = (float) (color1 & 0xFF) / 255.0F;
        float j = (float) (color2 >> 24 & 0xFF) / 255.0F;
        float k = (float) (color2 >> 16 & 0xFF) / 255.0F;
        float l = (float) (color2 >> 8 & 0xFF) / 255.0F;
        float m = (float) (color2 & 0xFF) / 255.0F;
        buffer.addVertex(pose, (float) x2, (float) y1, (float) blitOffset).setColor(g, h, i, f);
        buffer.addVertex(pose, (float) x1, (float) y1, (float) blitOffset).setColor(g, h, i, f);
        buffer.addVertex(pose, (float) x1, (float) y2, (float) blitOffset).setColor(k, l, m, j);
        buffer.addVertex(pose, (float) x2, (float) y2, (float) blitOffset).setColor(k, l, m, j);
    }
}
