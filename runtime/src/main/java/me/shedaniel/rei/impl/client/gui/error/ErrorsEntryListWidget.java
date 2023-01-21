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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget;
import me.shedaniel.clothconfig2.gui.widget.DynamicSmoothScrollingEntryListWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class ErrorsEntryListWidget extends DynamicSmoothScrollingEntryListWidget<ErrorsEntryListWidget.Entry> {
    private boolean inFocus;
    
    public ErrorsEntryListWidget(Minecraft client, int width, int height, int startY, int endY) {
        super(client, width, height, startY, endY, GuiComponent.BACKGROUND_LOCATION);
    }
    
    @Override
    public boolean changeFocus(boolean boolean_1) {
        if (!this.inFocus && this.getItemCount() == 0) {
            return false;
        } else {
            this.inFocus = !this.inFocus;
            if (this.inFocus && this.getFocused() == null && this.getItemCount() > 0) {
                this.moveSelection(1);
            } else if (this.inFocus && this.getFocused() != null) {
                this.moveSelection(0);
            }
            
            return this.inFocus;
        }
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
    
    @Override
    public int getItemWidth() {
        return width - 80;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return width - 40;
    }
    
    public static abstract class Entry extends DynamicEntryListWidget.Entry<Entry> {
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
    }
    
    public static class EmptyEntry extends Entry {
        public final int height;
        
        public EmptyEntry(int height) {
            this.height = height;
        }
        
        @Override
        public void render(PoseStack poseStack, int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b, float v) {
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
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            getEntry().render(matrices, index, y, x + indent, entryWidth - indent, entryHeight, mouseX, mouseY, isSelected, delta);
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
        public final Matrix4f transform;
        
        public ScaledEntry(Entry entry, float scale) {
            this.entry = entry;
            this.scale = scale;
            this.transform = new Matrix4f().scale(scale, scale, scale);
        }
        
        public Entry getEntry() {
            entry.setParent(getParent());
            return entry;
        }
        
        private Vector4f transformMouse(double mouseX, double mouseY) {
            Vector4f mouse = new Vector4f((float) mouseX, (float) mouseY, 0, 1);
            transform.transform(mouse);
            return mouse;
        }
        
        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            Vector4f mouse = transformMouse(mouseX, mouseY);
            matrices.pushPose();
            matrices.last().pose().mul(transform);
            
            Vector4f pos = new Vector4f(x, y, 0, 1);
            pos.mul(new Matrix4f().scale(1 / scale, 1 / scale, 1 / scale));
            getEntry().render(matrices, index, Math.round(pos.y()), Math.round(pos.x()), Math.round(entryWidth / scale), Math.round(entryHeight / scale), (int) mouse.x(), (int) mouse.y(), isSelected, delta);
            matrices.popPose();
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
            Vector4f mouse = transformMouse(mouseX, mouseY);
            return super.isMouseOver(mouse.x(), mouse.y());
        }
        
        @Override
        public boolean mouseClicked(double d, double e, int i) {
            Vector4f mouse = transformMouse(d, e);
            return super.mouseClicked(mouse.x(), mouse.y(), i);
        }
        
        @Override
        public boolean mouseReleased(double d, double e, int i) {
            Vector4f mouse = transformMouse(d, e);
            return super.mouseReleased(mouse.x(), mouse.y(), i);
        }
        
        @Override
        public boolean mouseDragged(double d, double e, int i, double f, double g) {
            Vector4f mouse = transformMouse(d, e);
            return super.mouseDragged(mouse.x(), mouse.y(), i, f, g);
        }
        
        @Override
        public boolean mouseScrolled(double d, double e, double f) {
            Vector4f mouse = transformMouse(d, e);
            return super.mouseScrolled(mouse.x(), mouse.y(), f);
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
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            if (this.width != entryWidth - 6) {
                this.width = entryWidth - 6;
                this.textSplit = text.getString().trim().isEmpty() ? Collections.singletonList(text.getVisualOrderText()) : Minecraft.getInstance().font.split(text, width);
            }
            this.savedX = x;
            this.savedY = y;
            int yy = y;
            for (FormattedCharSequence textSp : textSplit) {
                Minecraft.getInstance().font.drawShadow(matrices, textSp, x + 5, yy, -1);
                yy += 12;
            }
            
            Style style = this.getTextAt(mouseX, mouseY);
            Screen screen = Minecraft.getInstance().screen;
            if (style != null && screen != null) {
                if (style.getHoverEvent() != null) {
                    HoverEvent hoverEvent = style.getHoverEvent();
                    Component component = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
                    if (component != null) {
                        screen.renderTooltip(matrices, Minecraft.getInstance().font.split(component, Math.max(this.width / 2, 200)), x, y);
                    }
                }
            }
        }
        
        @Override
        public int getItemHeight() {
            return 12 * textSplit.size();
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                Style style = this.getTextAt(mouseX, mouseY);
                if (style != null && style.getClickEvent() != null) {
                    ClickEvent clickEvent = style.getClickEvent();
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    try {
                        Util.getPlatform().openUri(new URI(clickEvent.getValue()));
                        return true;
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            
            return super.mouseClicked(mouseX, mouseY, button);
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
                        return Minecraft.getInstance().font.getSplitter().componentStyleAtWidth(orderedText, textX);
                    }
                }
            }
            
            return null;
        }
    }
    
    public static class HorizontalRuleEntry extends Entry {
        private int width;
        
        public HorizontalRuleEntry(int width) {
            this.width = width;
        }
        
        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            if (this.width != entryWidth) {
                this.width = entryWidth;
            }
            ErrorsEntryListWidget.fill(matrices, x + 2, y + 2, x + width - 6 - 2, y + 3, 0xFF777777);
        }
        
        @Override
        public int getItemHeight() {
            return 5;
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
        }
    }
    
    public static class ImageEntry extends Entry {
        private DynamicTexture texture;
        private ResourceLocation id;
        private int width;
        private int height;
        
        public ImageEntry(int width, DynamicTexture texture, ResourceLocation id) {
            this.id = id;
            this.texture = texture;
            this.width = (width - 6) / 2;
            NativeImage image = texture.getPixels();
            this.height = (int) ((double) this.width * ((double) image.getHeight() / (double) image.getWidth()));
        }
        
        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            RenderSystem.setShaderTexture(0, id);
            NativeImage image = texture.getPixels();
            width = (entryWidth - 6) / 2;
            this.height = (int) ((double) width * ((double) image.getHeight() / (double) image.getWidth()));
            ErrorsEntryListWidget.fill(matrices, x, y, x + width, y + height + 2, 0xFFFFFFFF);
            ErrorsEntryListWidget.innerBlit(matrices.last().pose(), x + 1, x + width - 1, y + 1, y + height + 1, 0, 0, 1, 0, 1);
        }
        
        @Override
        public int getItemHeight() {
            return height + 2;
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
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
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            contains = mouseX >= x && mouseX <= x + entryWidth && mouseY >= y && mouseY <= y + entryHeight;
            if (contains) {
                Minecraft.getInstance().screen.renderTooltip(matrices, Component.literal("Click to open link."), mouseX, mouseY);
                int yy = y;
                for (FormattedCharSequence textSp : textSplit) {
                    FormattedCharSequence underlined = characterVisitor -> {
                        return textSp.accept((charIndex, style, codePoint) -> characterVisitor.accept(charIndex, style.applyFormat(ChatFormatting.UNDERLINE), codePoint));
                    };
                    Minecraft.getInstance().font.drawShadow(matrices, underlined, x + 5, yy, 0xff1fc3ff);
                    yy += 12;
                }
            } else {
                int yy = y;
                for (FormattedCharSequence textSp : textSplit) {
                    Minecraft.getInstance().font.drawShadow(matrices, textSp, x + 5, yy, 0xff1fc3ff);
                    yy += 12;
                }
            }
        }
        
        @Override
        public int getItemHeight() {
            return 12 * textSplit.size();
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (contains && button == 0) {
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
