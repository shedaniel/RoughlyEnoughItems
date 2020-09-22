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

package me.shedaniel.rei.gui.credits;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.shedaniel.clothconfig2.forge.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.rei.impl.TextTransformations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.ApiStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@ApiStatus.Internal
public class CreditsEntryListWidget extends DynamicNewSmoothScrollingEntryListWidget<CreditsEntryListWidget.CreditsItem> {
    
    private boolean inFocus;
    
    public CreditsEntryListWidget(Minecraft client, int width, int height, int startY, int endY) {
        super(client, width, height, startY, endY, AbstractGui.BACKGROUND_LOCATION);
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
    
    private CreditsItem rei_getEntry(int int_1) {
        return this.children().get(int_1);
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
    
    public static abstract class CreditsItem extends DynamicNewSmoothScrollingEntryListWidget.Entry<CreditsItem> {
        
    }
    
    public static class TextCreditsItem extends CreditsItem {
        private ITextComponent text;
        
        public TextCreditsItem(ITextComponent text) {
            this.text = text;
        }
        
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
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
        private ITextComponent language;
        private List<IReorderingProcessor> translators;
        private int maxWidth;
        
        public TranslationCreditsItem(ITextComponent language, ITextComponent translators, int width, int maxWidth) {
            this.language = language;
            this.translators = Minecraft.getInstance().font.split(translators, width);
            this.maxWidth = maxWidth;
        }
        
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            Minecraft.getInstance().font.drawShadow(matrices, language.getVisualOrderText(), x + 5, y + 5, -1);
            int yy = y + 5;
            for (IReorderingProcessor translator : translators) {
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
        private ITextComponent text;
        private List<IReorderingProcessor> textSplit;
        private String link;
        private boolean contains;
        private boolean rainbow;
        
        public LinkItem(ITextComponent text, String link, int width, boolean rainbow) {
            this.text = text;
            this.textSplit = Minecraft.getInstance().font.split(text, width);
            this.link = link;
            this.rainbow = rainbow;
        }
        
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            contains = mouseX >= x && mouseX <= x + entryWidth && mouseY >= y && mouseY <= y + entryHeight;
            if (contains) {
                Minecraft.getInstance().screen.renderTooltip(matrices, new StringTextComponent("Click to open link."), mouseX, mouseY);
                int yy = y;
                for (IReorderingProcessor textSp : textSplit) {
                    IReorderingProcessor underlined = characterVisitor -> {
                        return textSp.accept((charIndex, style, codePoint) -> characterVisitor.accept(charIndex, style.applyFormat(TextFormatting.UNDERLINE), codePoint));
                    };
                    if (rainbow) underlined = TextTransformations.applyRainbow(underlined, x + 5, yy);
                    Minecraft.getInstance().font.drawShadow(matrices, underlined, x + 5, yy, 0xff1fc3ff);
                    yy += 12;
                }
            } else {
                int yy = y;
                for (IReorderingProcessor textSp : textSplit) {
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
                Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
