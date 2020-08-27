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

package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.REIHelper;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @see net.minecraft.client.gui.widget.TextFieldWidget
 */
@ApiStatus.Internal
public class TextFieldWidget extends WidgetWithBounds implements TickableBlockEntity {
    
    public Function<String, String> stripInvalid;
    protected int focusedTicks;
    protected boolean editable;
    protected int firstCharacterIndex;
    protected int selectionStart;
    protected int selectionEnd;
    protected int editableColor;
    protected int notEditableColor;
    protected BiFunction<String, Integer, String> renderTextProvider;
    private Rectangle bounds;
    private String text;
    private int maxLength;
    private boolean hasBorder;
    private boolean focusUnlocked;
    private boolean focused;
    private boolean visible;
    private boolean selecting;
    private String suggestion;
    private Consumer<String> changedListener;
    private Predicate<String> textPredicate;
    
    public TextFieldWidget(Rectangle rectangle) {
        this.text = "";
        this.maxLength = 32;
        this.hasBorder = true;
        this.focusUnlocked = true;
        this.editable = true;
        this.editableColor = 14737632;
        this.notEditableColor = 7368816;
        this.visible = true;
        this.textPredicate = s -> true;
        this.renderTextProvider = (string_1, integer_1) -> {
            return string_1;
        };
        this.bounds = rectangle;
        this.stripInvalid = SharedConstants::filterText;
    }
    
    public TextFieldWidget(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }
    
    public String getSuggestion() {
        return suggestion;
    }
    
    public void setSuggestion(String string_1) {
        this.suggestion = string_1;
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    public void setChangedListener(Consumer<String> biConsumer_1) {
        this.changedListener = biConsumer_1;
    }
    
    public void setRenderTextProvider(BiFunction<String, Integer, String> biFunction_1) {
        this.renderTextProvider = biFunction_1;
    }
    
    @Override
    public void tick() {
        ++this.focusedTicks;
    }
    
    public String getText() {
        return this.text;
    }
    
    public void setText(String string_1) {
        if (this.textPredicate.test(string_1)) {
            if (string_1.length() > this.maxLength) {
                this.text = string_1.substring(0, this.maxLength);
            } else {
                this.text = string_1;
            }
            
            this.onChanged(string_1);
            this.setCursorToEnd();
        }
    }
    
    public String getSelectedText() {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.text.substring(i, j);
    }
    
    public void setTextPredicate(Predicate<String> predicate_1) {
        this.textPredicate = predicate_1;
    }
    
    public void addText(String string_1) {
        String string_2 = "";
        String string_3 = stripInvalid.apply(string_1);
        int int_1 = Math.min(this.selectionStart, this.selectionEnd);
        int int_2 = Math.max(this.selectionStart, this.selectionEnd);
        int int_3 = this.maxLength - this.text.length() - (int_1 - int_2);
        if (!this.text.isEmpty()) {
            string_2 = string_2 + this.text.substring(0, int_1);
        }
        
        int int_5;
        if (int_3 < string_3.length()) {
            string_2 = string_2 + string_3.substring(0, int_3);
            int_5 = int_3;
        } else {
            string_2 = string_2 + string_3;
            int_5 = string_3.length();
        }
        
        if (!this.text.isEmpty() && int_2 < this.text.length()) {
            string_2 = string_2 + this.text.substring(int_2);
        }
        
        if (this.textPredicate.test(string_2)) {
            this.text = string_2;
            this.setSelectionStart(int_1 + int_5);
            this.setSelectionEnd(this.selectionStart);
            this.onChanged(this.text);
        }
    }
    
    public void onChanged(String string_1) {
        if (this.changedListener != null) {
            this.changedListener.accept(string_1);
        }
        
    }
    
    private void erase(int int_1) {
        if (Screen.hasControlDown()) {
            this.eraseWords(int_1);
        } else {
            this.eraseCharacters(int_1);
        }
    }
    
    public void eraseWords(int wordOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.addText("");
            } else {
                this.eraseCharacters(this.getWordSkipPosition(wordOffset) - this.selectionStart);
            }
        }
    }
    
    public void eraseCharacters(int characterOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.addText("");
            } else {
                int i = this.getMovedCursorIndex(characterOffset);
                int j = Math.min(i, this.selectionStart);
                int k = Math.max(i, this.selectionStart);
                if (j != k) {
                    String string = (new StringBuilder(this.text)).delete(j, k).toString();
                    if (this.textPredicate.test(string)) {
                        this.text = string;
                        this.setCursor(j);
                    }
                }
                this.onChanged(this.text);
            }
        }
    }
    
    public int getWordSkipPosition(int int_1) {
        return this.getWordSkipPosition(int_1, this.getCursor());
    }
    
    public int getWordSkipPosition(int int_1, int int_2) {
        return this.getWordSkipPosition(int_1, int_2, true);
    }
    
    public int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
        int i = cursorPosition;
        boolean bl = wordOffset < 0;
        int j = Math.abs(wordOffset);
        
        for (int k = 0; k < j; ++k) {
            if (!bl) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }
                
                while (i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }
        
        return i;
    }
    
    public void moveCursor(int int_1) {
        this.setCursor(this.selectionStart + int_1);
    }
    
    private int getMovedCursorIndex(int i) {
        return Util.offsetByCodepoints(this.text, this.selectionStart, i);
    }
    
    public void setCursor(int int_1) {
        this.setSelectionStart(int_1);
        if (!selecting) {
            this.setSelectionEnd(this.selectionStart);
        }
    }
    
    public void setCursorToStart() {
        this.setCursor(0);
    }
    
    public void setCursorToEnd() {
        this.setCursor(this.text.length());
    }
    
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.isVisible() && this.isFocused()) {
            this.selecting = Screen.hasShiftDown();
            if (Screen.isSelectAll(int_1)) {
                this.setCursorToEnd();
                this.setSelectionEnd(0);
                return true;
            } else if (Screen.isCopy(int_1)) {
                minecraft.keyboardHandler.setClipboard(this.getSelectedText());
                return true;
            } else if (Screen.isPaste(int_1)) {
                if (this.editable) {
                    this.addText(minecraft.keyboardHandler.getClipboard());
                }
                
                return true;
            } else if (Screen.isCut(int_1)) {
                minecraft.keyboardHandler.setClipboard(this.getSelectedText());
                if (this.editable) {
                    this.addText("");
                }
                
                return true;
            } else {
                switch (int_1) {
                    case 259:
                        if (this.editable) {
                            this.selecting = false;
                            this.erase(-1);
                            this.selecting = Screen.hasShiftDown();
                        }
                        
                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return int_1 != 256;
                    case 261:
                        if (this.editable) {
                            this.selecting = false;
                            this.erase(1);
                            this.selecting = Screen.hasShiftDown();
                        }
                        
                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(1));
                        } else {
                            this.moveCursor(1);
                        }
                        
                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }
                        
                        return true;
                    case 268:
                        this.setCursorToStart();
                        return true;
                    case 269:
                        this.setCursorToEnd();
                        return true;
                }
            }
        } else {
            return false;
        }
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (this.isVisible() && this.isFocused()) {
            if (SharedConstants.isAllowedChatCharacter(char_1) && !(
                    Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown() && (
                            char_1 == 'a' || char_1 == 'c' || char_1 == 'v'
                    )
            )) {
                if (this.editable) {
                    this.addText(Character.toString(char_1));
                }
                
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    @Override
    public List<Widget> children() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean boolean_1 = double_1 >= (double) this.bounds.x && double_1 < (double) (this.bounds.x + this.bounds.width) && double_2 >= (double) this.bounds.y && double_2 < (double) (this.bounds.y + this.bounds.height);
            if (this.focusUnlocked) {
                this.setFocused(boolean_1);
            }
            
            if (this.focused && boolean_1 && int_1 == 0) {
                int int_2 = Mth.floor(double_1) - this.bounds.x;
                if (this.hasBorder) {
                    int_2 -= 4;
                }
                
                String string_1 = this.font.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), this.getWidth());
                this.setCursor(this.font.plainSubstrByWidth(string_1, int_2).length() + this.firstCharacterIndex);
                return true;
            } else {
                return false;
            }
        }
    }
    
    public void renderBorder(PoseStack matrices) {
        if (this.hasBorder()) {
            if (containsMouse(PointHelper.ofMouse()) || focused)
                fill(matrices, this.bounds.x - 1, this.bounds.y - 1, this.bounds.x + this.bounds.width + 1, this.bounds.y + this.bounds.height + 1, REIHelper.getInstance().isDarkThemeEnabled() ? -17587 : -1);
            else
                fill(matrices, this.bounds.x - 1, this.bounds.y - 1, this.bounds.x + this.bounds.width + 1, this.bounds.y + this.bounds.height + 1, -6250336);
            fill(matrices, this.bounds.x, this.bounds.y, this.bounds.x + this.bounds.width, this.bounds.y + this.bounds.height, -16777216);
        }
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (this.isVisible()) {
            renderBorder(matrices);
            
            int color = this.editable ? this.editableColor : this.notEditableColor;
            int int_4 = this.selectionStart - this.firstCharacterIndex;
            int int_5 = this.selectionEnd - this.firstCharacterIndex;
            String string_1 = this.font.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), this.getWidth());
            boolean boolean_1 = int_4 >= 0 && int_4 <= string_1.length();
            boolean boolean_2 = this.focused && this.focusedTicks / 6 % 2 == 0 && boolean_1;
            int x = this.hasBorder ? this.bounds.x + 4 : this.bounds.x;
            int y = this.hasBorder ? this.bounds.y + (this.bounds.height - 8) / 2 : this.bounds.y;
            int int_8 = x;
            if (int_5 > string_1.length()) {
                int_5 = string_1.length();
            }
            
            if (!string_1.isEmpty()) {
                String string_2 = boolean_1 ? string_1.substring(0, int_4) : string_1;
                int_8 = this.font.drawShadow(matrices, this.renderTextProvider.apply(string_2, this.firstCharacterIndex), (float) x, (float) y, color);
            }
            
            boolean isCursorInsideText = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
            int int_9 = int_8;
            if (!boolean_1) {
                int_9 = int_4 > 0 ? x + this.bounds.width : x;
            } else if (isCursorInsideText) {
                --int_8;
            }
            int_9--;
            
            if (!string_1.isEmpty() && boolean_1 && int_4 < string_1.length()) {
                this.font.drawShadow(matrices, this.renderTextProvider.apply(string_1.substring(int_4), this.selectionStart), (float) int_8, (float) y, color);
            }
            
            if (!isCursorInsideText && text.isEmpty() && this.suggestion != null) {
                renderSuggestion(matrices, x, y);
            }
            
            if (boolean_2) {
                fill(matrices, int_9 + 1, y, int_9 + 2, y + 9, ((0xFF) << 24) | ((((color >> 16 & 255) / 4) & 0xFF) << 16) | ((((color >> 8 & 255) / 4) & 0xFF) << 8) | ((((color & 255) / 4) & 0xFF)));
                fill(matrices, int_9, y - 1, int_9 + 1, y + 8, ((0xFF) << 24) | color);
            }
            
            // Render selection overlay
            if (int_5 != int_4) {
                int int_10 = x + this.font.width(string_1.substring(0, int_5));
                this.renderSelection(matrices, int_9, y - 1, int_10 - 1, y + 9, color);
            }
        }
    }
    
    protected void renderSuggestion(PoseStack matrices, int x, int y) {
        this.font.drawShadow(matrices, this.font.plainSubstrByWidth(this.suggestion, this.getWidth()), x, y, -8355712);
    }
    
    protected void renderSelection(PoseStack matrices, int x1, int y1, int x2, int y2, int color) {
        int tmp;
        if (x1 < x2) {
            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        
        if (y1 < y2) {
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        
        if (x2 > this.bounds.x + this.bounds.width) {
            x2 = this.bounds.x + this.bounds.width;
        }
        
        if (x1 > this.bounds.x + this.bounds.width) {
            x1 = this.bounds.x + this.bounds.width;
        }
        
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(7425);
        Matrix4f matrix = matrices.last().pose();
        buffer.begin(7, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, x1, y2, getBlitOffset() + 50f).color(r, g, b, 120).endVertex();
        buffer.vertex(matrix, x2, y2, getBlitOffset() + 50f).color(r, g, b, 120).endVertex();
        buffer.vertex(matrix, x2, y1, getBlitOffset() + 50f).color(r, g, b, 120).endVertex();
        buffer.vertex(matrix, x1, y1, getBlitOffset() + 50f).color(r, g, b, 120).endVertex();
        tessellator.end();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }
    
    public int getMaxLength() {
        return this.maxLength;
    }
    
    public void setMaxLength(int int_1) {
        this.maxLength = int_1;
        if (this.text.length() > int_1) {
            this.text = this.text.substring(0, int_1);
            this.onChanged(this.text);
        }
    }
    
    public int getCursor() {
        return this.selectionStart;
    }
    
    public void setSelectionStart(int int_1) {
        this.selectionStart = Mth.clamp(int_1, 0, this.text.length());
    }
    
    public boolean hasBorder() {
        return this.hasBorder;
    }
    
    public void setHasBorder(boolean boolean_1) {
        this.hasBorder = boolean_1;
    }
    
    public void setEditableColor(int int_1) {
        this.editableColor = int_1;
    }
    
    public void setNotEditableColor(int int_1) {
        this.notEditableColor = int_1;
    }
    
    @Override
    public boolean changeFocus(boolean boolean_1) {
        if (this.visible && this.editable) {
            this.setFocused(!this.focused);
            return this.focused;
        }
        return false;
    }
    
    public boolean isFocused() {
        return this.focused;
    }
    
    public void setFocused(boolean boolean_1) {
        if (boolean_1 && !this.focused)
            this.focusedTicks = 0;
        this.focused = boolean_1;
    }
    
    public void setIsEditable(boolean boolean_1) {
        this.editable = boolean_1;
    }
    
    public int getWidth() {
        return this.hasBorder() ? this.bounds.width - 8 : this.bounds.width;
    }
    
    public void setSelectionEnd(int i) {
        int j = this.text.length();
        this.selectionEnd = Mth.clamp(i, 0, j);
        if (this.font != null) {
            if (this.firstCharacterIndex > j) {
                this.firstCharacterIndex = j;
            }
            
            int int_3 = this.getWidth();
            String string_1 = this.font.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), int_3);
            int int_4 = string_1.length() + this.firstCharacterIndex;
            if (this.selectionEnd == this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.font.plainSubstrByWidth(this.text, int_3, true).length();
            }
            
            if (this.selectionEnd > int_4) {
                this.firstCharacterIndex += this.selectionEnd - int_4;
            } else if (this.selectionEnd <= this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.firstCharacterIndex - this.selectionEnd;
            }
            
            this.firstCharacterIndex = Mth.clamp(this.firstCharacterIndex, 0, j);
        }
        
    }
    
    public void setFocusUnlocked(boolean boolean_1) {
        this.focusUnlocked = boolean_1;
    }
    
    public boolean isVisible() {
        return this.visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public int getCharacterX(int index) {
        return index > this.text.length() ? this.bounds.x : this.bounds.x + this.font.width(this.text.substring(0, index));
    }
    
}
