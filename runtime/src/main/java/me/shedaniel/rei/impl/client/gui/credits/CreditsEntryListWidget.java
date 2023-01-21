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

package me.shedaniel.rei.impl.client.gui.credits;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.gui.widget.DynamicSmoothScrollingEntryListWidget;
import me.shedaniel.rei.impl.client.gui.text.TextTransformations;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class CreditsEntryListWidget extends DynamicSmoothScrollingEntryListWidget<CreditsEntryListWidget.CreditsItem> {
    private boolean inFocus;
    
    public CreditsEntryListWidget(Minecraft client, int width, int height, int startY, int endY) {
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
    
    public void creditsClearEntries() {
        clearItems();
    }
    
    private CreditsItem rei_getEntry(int index) {
        return this.children().get(index);
    }
    
    public void creditsAddEntry(CreditsItem entry) {
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
    
    public static abstract class CreditsItem extends DynamicSmoothScrollingEntryListWidget.Entry<CreditsItem> {
        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
    }
    
    public static class TextCreditsItem extends CreditsItem {
        private Component text;
        
        public TextCreditsItem(Component text) {
            this.text = text;
        }
        
        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            Minecraft.getInstance().font.drawShadow(matrices, text.getVisualOrderText(), x + 5, y + 5, -1);
        }
        
        @Override
        public int getItemHeight() {
            return 12;
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
        }
    }
    
    public static class TranslationCreditsItem extends CreditsItem {
        private Component language;
        private List<FormattedCharSequence> translators;
        private int maxWidth;
        
        public TranslationCreditsItem(Component language, Component translators, int width, int maxWidth) {
            this.language = language;
            this.translators = Minecraft.getInstance().font.split(translators, width);
            this.maxWidth = maxWidth;
        }
        
        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            Minecraft.getInstance().font.drawShadow(matrices, language.getVisualOrderText(), x + 5, y + 5, -1);
            int yy = y + 5;
            for (FormattedCharSequence translator : translators) {
                Minecraft.getInstance().font.drawShadow(matrices, translator, x + 5 + maxWidth, yy, -1);
                yy += 12;
            }
        }
        
        @Override
        public int getItemHeight() {
            return 12 * translators.size();
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
        }
    }
    
    public static class LinkItem extends CreditsItem {
        private Component text;
        private List<FormattedCharSequence> textSplit;
        private String link;
        private boolean contains;
        private boolean rainbow;
        
        public LinkItem(Component text, String link, int width, boolean rainbow) {
            this.text = text;
            this.textSplit = Minecraft.getInstance().font.split(text, width);
            this.link = link;
            this.rainbow = rainbow;
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
                    if (rainbow) underlined = TextTransformations.applyRainbow(underlined, x + 5, yy);
                    Minecraft.getInstance().font.drawShadow(matrices, underlined, x + 5, yy, 0xff1fc3ff);
                    yy += 12;
                }
            } else {
                int yy = y;
                for (FormattedCharSequence textSp : textSplit) {
                    if (rainbow) textSp = TextTransformations.applyRainbow(textSp, x + 5, yy);
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
