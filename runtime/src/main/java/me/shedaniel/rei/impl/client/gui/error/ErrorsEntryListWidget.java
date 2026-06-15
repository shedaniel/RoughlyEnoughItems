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

package me.shedaniel.rei.impl.client.gui.error;

import com.mojang.blaze3d.platform.NativeImage;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget;
import me.shedaniel.clothconfig2.gui.widget.DynamicSmoothScrollingEntryListWidget;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.util.*;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector3f;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class ErrorsEntryListWidget extends DynamicSmoothScrollingEntryListWidget<ErrorsEntryListWidget.Entry> {
    private boolean inFocus;
    
    public ErrorsEntryListWidget(Minecraft client, int width, int height, int startY, int endY) {
        super(client, width, height, startY, endY, InternalTextures.LEGACY_DIRT);
    }
    
    public void _clearItems() {
        clearItems();
    }
    
    private Entry _getEntry(int index) {
        return this.children().get(index);
    }
    
    public void _addEntry(Entry entry) {
        addItem(entry);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        extractRenderState(graphics, mouseX, mouseY, delta);
    }
    
    @Override
    public int getItemWidth() {
        return width - 80;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return width - 40;
    }
    
    public static abstract class Entry extends DynamicEntryListWidget.Entry<Entry> {
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            render(GuiGraphics.of(graphics), index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
        
        @Override
        public void setFocused(boolean bl) {
        }
        
        @Override
        public boolean isFocused() {
            return false;
        }
    }
    
    public static class EmptyEntry extends Entry {
        public final int height;
        
        public EmptyEntry(int height) {
            this.height = height;
        }
        
        @Override
        public void render(GuiGraphics graphics, int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b, float v) {
        }
        
        @Override
        public int getItemHeight() {
            return height;
        }
    }
    
    public static class IndentedEntry extends Entry implements ContainerEventHandler {
        public final Entry entry;
        public final int indent;
        
        public IndentedEntry(Entry entry, int indent) {
            this.entry = entry;
            this.indent = indent;
        }
        
        public Entry getEntry() {
            entry.setParent(getParent());
            return entry;
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            getEntry().render(graphics, index, y, x + indent, entryWidth - indent, entryHeight, mouseX, mouseY, isSelected, delta);
        }
        
        @Override
        public int getItemHeight() {
            return getEntry().getItemHeight();
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return getEntry().narratables();
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(getEntry());
        }
        
        @Nullable
        private GuiEventListener focused;
        private boolean isDragging;
        
        @Override
        public final boolean isDragging() {
            return this.isDragging;
        }
        
        @Override
        public final void setDragging(boolean dragging) {
            this.isDragging = dragging;
        }
        
        @Override
        @Nullable
        public GuiEventListener getFocused() {
            return this.focused;
        }
        
        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            this.focused = focused;
        }
    }
    
    public static class ScaledEntry extends Entry implements ContainerEventHandler {
        public final Entry entry;
        public final float scale;
        public final Matrix3x2f transform;
        
        public ScaledEntry(Entry entry, float scale) {
            this.entry = entry;
            this.scale = scale;
            this.transform = new Matrix3x2f().scale(scale, scale);
        }
        
        public Entry getEntry() {
            entry.setParent(getParent());
            return entry;
        }
        
        private Vector3f transformMouse(double mouseX, double mouseY) {
            Vector3f mouse = new Vector3f((float) mouseX, (float) mouseY, 1);
            transform.transform(mouse);
            return mouse;
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            Vector3f mouse = transformMouse(mouseX, mouseY);
            graphics.pose().pushMatrix();
            graphics.pose().mul(transform);
            
            Vector3f pos = new Vector3f(x, y, 1);
            pos.mul(new Matrix3x2f().scale(1 / scale, 1 / scale));
            getEntry().render(graphics, index, Math.round(pos.y()), Math.round(pos.x()), Math.round(entryWidth / scale), Math.round(entryHeight / scale), (int) mouse.x(), (int) mouse.y(), isSelected, delta);
            graphics.pose().popMatrix();
        }
        
        @Override
        public int getItemHeight() {
            return getEntry().getItemHeight();
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return getEntry().narratables();
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(getEntry());
        }
        
        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            Vector3f mouse = transformMouse(mouseX, mouseY);
            return super.isMouseOver(mouse.x(), mouse.y());
        }
        
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            Vector3f mouse = transformMouse(event.x(), event.y());
            return super.mouseClicked(new MouseButtonEvent(mouse.x(), mouse.y(), event.buttonInfo()), doubleClick);
        }
        
        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            Vector3f mouse = transformMouse(event.x(), event.y());
            return super.mouseReleased(new MouseButtonEvent(mouse.x(), mouse.y(), event.buttonInfo()));
        }
        
        @Override
        public boolean mouseDragged(MouseButtonEvent event, double f, double g) {
            Vector3f mouse = transformMouse(event.x(), event.y());
            return super.mouseDragged(new MouseButtonEvent(mouse.x(), mouse.y(), event.buttonInfo()), f, g);
        }
        
        @Override
        public boolean mouseScrolled(double d, double e, double amountX, double amountY) {
            Vector3f mouse = transformMouse(d, e);
            return super.mouseScrolled(mouse.x(), mouse.y(), amountX, amountY);
        }
        
        @Nullable
        private GuiEventListener focused;
        private boolean isDragging;
        
        @Override
        public final boolean isDragging() {
            return this.isDragging;
        }
        
        @Override
        public final void setDragging(boolean dragging) {
            this.isDragging = dragging;
        }
        
        @Override
        @Nullable
        public GuiEventListener getFocused() {
            return this.focused;
        }
        
        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            this.focused = focused;
        }
    }
    
    public static class TextEntry extends Entry {
        private Component text;
        private int width;
        private List<FormattedCharSequence> textSplit;
        private int savedX;
        private int savedY;
        
        public TextEntry(Component text, int width) {
            this.text = text;
            this.width = width - 6;
            this.textSplit = text.getString().trim().isEmpty() ? Collections.singletonList(text.getVisualOrderText()) : Minecraft.getInstance().font.split(text, this.width);
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            if (this.width != entryWidth - 6) {
                this.width = entryWidth - 6;
                this.textSplit = text.getString().trim().isEmpty() ? Collections.singletonList(text.getVisualOrderText()) : Minecraft.getInstance().font.split(text, width);
            }
            this.savedX = x;
            this.savedY = y;
            int yy = y;
            for (FormattedCharSequence textSp : textSplit) {
                graphics.drawString(Minecraft.getInstance().font, textSp, x + 5, yy, -1);
                yy += 12;
            }
            
            Style style = this.getTextAt(mouseX, mouseY);
            Screen screen = Minecraft.getInstance().screen;
            if (style != null && screen != null) {
                if (style.getHoverEvent() != null) {
                    HoverEvent hoverEvent = style.getHoverEvent();
                    if (hoverEvent instanceof HoverEvent.ShowText(Component component)) {
                        graphics.setTooltipForNextFrame(Minecraft.getInstance().font, Minecraft.getInstance().font.split(component, Math.max(this.width / 2, 200)), x, y);
                    }
                }
            }
        }
        
        @Override
        public int getItemHeight() {
            return 12 * textSplit.size();
        }
        
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() == 0) {
                Style style = this.getTextAt(event.x(), event.y());
                if (style != null && style.getClickEvent() != null) {


                    Screen.defaultHandleGameClickEvent(style.getClickEvent(), Minecraft.getInstance(), Minecraft.getInstance().screen);
                    return true;
                }
            }
            
            return super.mouseClicked(event, doubleClick);
        }
        
        @Nullable
        private Style getTextAt(double x, double y) {
            int lineCount = this.textSplit.size();
            if (lineCount > 0) {
                int textX = Mth.floor(x - (double) this.savedX);
                int textY = Mth.floor(y - (double) this.savedY);
                if (textX >= 0 && textY >= 0 && textX <= this.width && textY < 12 * lineCount + lineCount) {
                    int line = textY / 12;
                    if (line < this.textSplit.size()) {
                        FormattedCharSequence orderedText = this.textSplit.get(line);
                        return styleAtWidth(orderedText, textX, Minecraft.getInstance().font);
                    }
                }
            }
            
            return null;
        }
    }

    @Nullable
    private static Style styleAtWidth(FormattedCharSequence text, int width, Font font) {
        StringSplitter splitter = font.getSplitter();
        StringSplitter.WidthLimitedCharSink sink =
                splitter.new WidthLimitedCharSink(width);

        final MutableObject<Style> result = new MutableObject<>();

        text.accept((i, style, codepoint) -> {
            if (!sink.accept(i, style, codepoint)) {
                result.setValue(style);
                return false;
            }
            return true;
        });

        return result.getValue();
    }
    
    public static class HorizontalRuleEntry extends Entry {
        private int width;
        
        public HorizontalRuleEntry(int width) {
            this.width = width;
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            if (this.width != entryWidth) {
                this.width = entryWidth;
            }
            graphics.fill(x + 2, y + 2, x + width - 6 - 2, y + 3, 0xFF777777);
        }
        
        @Override
        public int getItemHeight() {
            return 5;
        }
    }
    
    public static class ImageEntry extends Entry {
        private DynamicTexture texture;
        private Identifier id;
        private int width;
        private int height;
        
        public ImageEntry(int width, DynamicTexture texture, Identifier id) {
            this.id = id;
            this.texture = texture;
            this.width = (width - 6) / 2;
            NativeImage image = texture.getPixels();
            this.height = (int) ((double) this.width * ((double) image.getHeight() / (double) image.getWidth()));
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            NativeImage image = texture.getPixels();
            width = (entryWidth - 6) / 2;
            this.height = (int) ((double) width * ((double) image.getHeight() / (double) image.getWidth()));
            graphics.fill(x, y, x + width, y + height + 2, 0xFFFFFFFF);
            graphics.blit(RenderPipelines.GUI_TEXTURED, id, x + 1, y + 1, 0, 0, width - 2, height, width - 2, height);
        }
        
        @Override
        public int getItemHeight() {
            return height + 2;
        }
    }
    
    public static class LinkEntry extends Entry {
        private Component text;
        private List<FormattedCharSequence> textSplit;
        private String link;
        private boolean contains;
        
        public LinkEntry(Component text, String link, int width) {
            this.text = text;
            this.textSplit = Minecraft.getInstance().font.split(text, width - 6);
            this.link = link;
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            contains = mouseX >= x && mouseX <= x + entryWidth && mouseY >= y && mouseY <= y + entryHeight;
            if (contains) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, Component.literal("Click to open link."), mouseX, mouseY);
                int yy = y;
                for (FormattedCharSequence textSp : textSplit) {
                    FormattedCharSequence underlined = characterVisitor -> {
                        return textSp.accept((charIndex, style, codePoint) -> characterVisitor.accept(charIndex, style.applyFormat(ChatFormatting.UNDERLINE), codePoint));
                    };
                    graphics.drawString(Minecraft.getInstance().font, underlined, x + 5, yy, 0xff1fc3ff);
                    yy += 12;
                }
            } else {
                int yy = y;
                for (FormattedCharSequence textSp : textSplit) {
                    graphics.drawString(Minecraft.getInstance().font, textSp, x + 5, yy, 0xff1fc3ff);
                    yy += 12;
                }
            }
        }
        
        @Override
        public int getItemHeight() {
            return 12 * textSplit.size();
        }
        
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (contains && event.button() == 0) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                try {
                    Util.getPlatform().openUri(new URI(link));
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
