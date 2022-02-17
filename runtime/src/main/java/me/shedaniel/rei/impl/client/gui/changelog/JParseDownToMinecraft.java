package me.shedaniel.rei.impl.client.gui.changelog;

import com.mojang.blaze3d.platform.NativeImage;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
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
            return new TextComponent(((JParseDown.InlineText) inline).text.replace("\n", " "));
        } else if (inline instanceof JParseDown.InlineBold) {
            return new TextComponent(((JParseDown.InlineBold) inline).text.replace("\n", " "))
                    .withStyle(ChatFormatting.BOLD);
        } else if (inline instanceof JParseDown.InlineItalic) {
            return new TextComponent(((JParseDown.InlineItalic) inline).text.replace("\n", " "))
                    .withStyle(ChatFormatting.ITALIC);
        } else if (inline instanceof JParseDown.InlineLink) {
            return new TextComponent(((JParseDown.InlineLink) inline).text.replace("\n", " "))
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0x1fc3ff)).withUnderlined(true)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(((JParseDown.InlineLink) inline).url)
                                    .withStyle(ChatFormatting.GRAY)))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ((JParseDown.InlineLink) inline).url)));
        } else if (inline instanceof JParseDown.InlineStrikeThrough) {
            return new TextComponent(((JParseDown.InlineStrikeThrough) inline).text.replace("\n", " "))
                    .withStyle(ChatFormatting.STRIKETHROUGH);
        }
        return null;
    }
    
    public static void build(ChangelogLoader.Builder builder, JParseDown.Block block) {
        MutableComponent lastComponent = ImmutableTextComponent.EMPTY;
        ChangelogLoader.Builder finalBuilder = builder;
        for (JParseDown.Inline inline : block.inlines) {
            Component component = toComponent(inline);
            if (component != null) {
                lastComponent = lastComponent.append(component);
            } else {
                builder.add(lastComponent);
                lastComponent = ImmutableTextComponent.EMPTY;
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
        if (lastComponent != ImmutableTextComponent.EMPTY) {
            builder.add(lastComponent);
        }
    }
}
