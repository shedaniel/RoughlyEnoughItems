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

package me.shedaniel.rei.gui;

import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.rei.RoughlyEnoughItemsState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Lazy;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

import java.net.URI;
import java.net.URISyntaxException;

@ApiStatus.Internal
public class WarningAndErrorScreen extends Screen {
    public static final Lazy<WarningAndErrorScreen> INSTANCE = new Lazy<>(WarningAndErrorScreen::new);
    private AbstractButtonWidget buttonExit;
    private StringEntryListWidget listWidget;
    private Screen parent;
    
    private WarningAndErrorScreen() {
        super(NarratorManager.EMPTY);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    public void setParent(Screen parent) {
        this.parent = parent;
    }
    
    private void addText(Text string) {
        for (Text s : textRenderer.wrapStringToWidthAsList(string, width - 80)) {
            listWidget.creditsAddEntry(new TextItem(s));
        }
    }
    
    private void addLink(Text string, String link) {
        for (Text s : textRenderer.wrapStringToWidthAsList(string, width - 80)) {
            listWidget.creditsAddEntry(new LinkItem(s.getString(), link));
        }
    }
    
    @Override
    public void init() {
        children.add(listWidget = new StringEntryListWidget(client, width, height, 32, height - 32));
        listWidget.max = 80;
        listWidget.creditsClearEntries();
        listWidget.creditsAddEntry(new EmptyItem());
        if (!RoughlyEnoughItemsState.getWarnings().isEmpty())
            listWidget.creditsAddEntry(new TextItem(new LiteralText("Warnings:").method_27692(Formatting.RED)));
        for (Pair<String, String> pair : RoughlyEnoughItemsState.getWarnings()) {
            addText(new LiteralText(pair.getLeft()));
            if (pair.getRight() != null)
                addLink(new LiteralText(pair.getRight()), pair.getRight());
            for (int i = 0; i < 2; i++) {
                listWidget.creditsAddEntry(new EmptyItem());
            }
        }
        if (!RoughlyEnoughItemsState.getWarnings().isEmpty() && !RoughlyEnoughItemsState.getErrors().isEmpty()) {
            listWidget.creditsAddEntry(new EmptyItem());
        }
        if (!RoughlyEnoughItemsState.getErrors().isEmpty())
            listWidget.creditsAddEntry(new TextItem(new LiteralText("Errors:").method_27692(Formatting.RED)));
        for (Pair<String, String> pair : RoughlyEnoughItemsState.getErrors()) {
            addText(new LiteralText(pair.getLeft()));
            if (pair.getRight() != null)
                addLink(new LiteralText(pair.getRight()), pair.getRight());
            for (int i = 0; i < 2; i++) {
                listWidget.creditsAddEntry(new EmptyItem());
            }
        }
        for (StringItem child : listWidget.children()) {
            listWidget.max = Math.max(listWidget.max, child.getWidth());
        }
        children.add(buttonExit = new ButtonWidget(width / 2 - 100, height - 26, 200, 20,
                new LiteralText(RoughlyEnoughItemsState.getErrors().isEmpty() ? "Continue" : "Exit"),
                button -> {
                    if (RoughlyEnoughItemsState.getErrors().isEmpty()) {
                        RoughlyEnoughItemsState.clear();
                        RoughlyEnoughItemsState.continues();
                        MinecraftClient.getInstance().openScreen(parent);
                        setParent(null);
                    } else {
                        MinecraftClient.getInstance().scheduleStop();
                    }
                }));
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        return listWidget.mouseScrolled(double_1, double_2, double_3) || super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public void render(MatrixStack matrices, int int_1, int int_2, float float_1) {
        this.renderDirtBackground(0);
        this.listWidget.render(matrices, int_1, int_2, float_1);
        if (RoughlyEnoughItemsState.getErrors().isEmpty()) {
            this.drawCenteredString(matrices, this.textRenderer, "Warnings during Roughly Enough Items' initialization", this.width / 2, 16, 16777215);
        } else {
            this.drawCenteredString(matrices, this.textRenderer, "Errors during Roughly Enough Items' initialization", this.width / 2, 16, 16777215);
        }
        super.render(matrices, int_1, int_2, float_1);
        this.buttonExit.render(matrices, int_1, int_2, float_1);
    }
    
    private static class StringEntryListWidget extends DynamicNewSmoothScrollingEntryListWidget<StringItem> {
        private boolean inFocus;
        private int max = 80;
        
        public StringEntryListWidget(MinecraftClient client, int width, int height, int startY, int endY) {
            super(client, width, height, startY, endY, DrawableHelper.BACKGROUND_TEXTURE);
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
        
        private StringItem rei_getEntry(int int_1) {
            return this.children().get(int_1);
        }
        
        public void creditsAddEntry(StringItem entry) {
            addItem(entry);
        }
        
        @Override
        public int getItemWidth() {
            return max;
        }
        
        @Override
        protected int getScrollbarPosition() {
            return width - 40;
        }
    }
    
    private abstract static class StringItem extends DynamicNewSmoothScrollingEntryListWidget.Entry<StringItem> {
        public abstract int getWidth();
    }
    
    private static class EmptyItem extends StringItem {
        @Override
        public void render(MatrixStack matrixStack, int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b, float v) {
            
        }
        
        @Override
        public int getItemHeight() {
            return 5;
        }
        
        @Override
        public int getWidth() {
            return 0;
        }
    }
    
    private static class TextItem extends StringItem {
        private Text text;
        
        public TextItem(Text text) {
            this.text = text;
        }
        
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            MinecraftClient.getInstance().textRenderer.method_27517(matrices, text, x + 5, y, -1);
        }
        
        @Override
        public int getItemHeight() {
            return 12;
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
        }
        
        @Override
        public int getWidth() {
            return MinecraftClient.getInstance().textRenderer.method_27525(text) + 10;
        }
    }
    
    private class LinkItem extends StringItem {
        private String text;
        private String link;
        private boolean contains;
        
        public LinkItem(String text, String link) {
            this.text = text;
            this.link = link;
        }
        
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            contains = mouseX >= x && mouseX <= x + entryWidth && mouseY >= y && mouseY <= y + entryHeight;
            if (contains) {
                WarningAndErrorScreen.this.renderTooltip(matrices, new LiteralText("Click to open link."), mouseX, mouseY);
                MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, "§n" + text, x + 5, y, 0xff1fc3ff);
            } else {
                MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, text, x + 5, y, 0xff1fc3ff);
            }
        }
        
        @Override
        public int getItemHeight() {
            return 12;
        }
        
        @Override
        public boolean changeFocus(boolean boolean_1) {
            return false;
        }
        
        @Override
        public int getWidth() {
            return MinecraftClient.getInstance().textRenderer.getStringWidth(text) + 10;
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (contains && button == 0) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                try {
                    Util.getOperatingSystem().open(new URI(link));
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
