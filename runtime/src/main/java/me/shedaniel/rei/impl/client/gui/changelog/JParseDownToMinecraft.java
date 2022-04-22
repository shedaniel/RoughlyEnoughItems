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

package me.shedaniel.rei.impl.client.gui.changelog;

import com.mojang.blaze3d.platform.NativeImage;
import me.shedaniel.rei.impl.client.gui.error.ErrorsEntryListWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;

public class JParseDownToMinecraft {
    public static Component toComponent(JParseDown.Inline inline) {
        if (inline instanceof JParseDown.InlineText) {
            return Component.literal(((JParseDown.InlineText) inline).text.replace("\n", " "));
        } else if (inline instanceof JParseDown.InlineBold) {
            return Component.literal(((JParseDown.InlineBold) inline).text.replace("\n", " "))
                    .withStyle(ChatFormatting.BOLD);
        } else if (inline instanceof JParseDown.InlineItalic) {
            return Component.literal(((JParseDown.InlineItalic) inline).text.replace("\n", " "))
                    .withStyle(ChatFormatting.ITALIC);
        } else if (inline instanceof JParseDown.InlineLink) {
            return Component.literal(((JParseDown.InlineLink) inline).text.replace("\n", " "))
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0x1fc3ff)).withUnderlined(true)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(((JParseDown.InlineLink) inline).url)
                                    .withStyle(ChatFormatting.GRAY)))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ((JParseDown.InlineLink) inline).url)));
        } else if (inline instanceof JParseDown.InlineStrikeThrough) {
            return Component.literal(((JParseDown.InlineStrikeThrough) inline).text.replace("\n", " "))
                    .withStyle(ChatFormatting.STRIKETHROUGH);
        }
        return null;
    }
    
    public static void build(ChangelogLoader.Builder builder, JParseDown.Block block) {
        MutableComponent lastComponent = Component.empty();
        ChangelogLoader.Builder finalBuilder = builder;
        for (JParseDown.Inline inline : block.inlines) {
            Component component = toComponent(inline);
            if (component != null) {
                lastComponent = lastComponent.append(component);
            } else {
                builder.add(lastComponent);
                lastComponent = Component.empty();
                if (inline instanceof JParseDown.InlineLineBreak) {
                    continue;
                } else if (inline instanceof JParseDown.InlineHorizontalRule) {
                    builder.add(ErrorsEntryListWidget.HorizontalRuleEntry::new);
                } else if (inline instanceof JParseDown.InlineImage) {
                    InputStream stream = builder.getClass().getClassLoader().getResourceAsStream(((JParseDown.InlineImage) inline).src);
                    if (stream != null) {
                        try {
                            DynamicTexture texture = new DynamicTexture(NativeImage.read(stream));
                            ResourceLocation id = Minecraft.getInstance().getTextureManager().register("rei_md_image_", texture);
                            builder.add(width -> new ErrorsEntryListWidget.ImageEntry(width, texture, id));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (lastComponent != Component.empty()) {
            builder.add(lastComponent);
        }
    }
}
