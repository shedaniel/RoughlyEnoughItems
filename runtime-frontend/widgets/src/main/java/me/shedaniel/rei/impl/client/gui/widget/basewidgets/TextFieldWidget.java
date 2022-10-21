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

package me.shedaniel.rei.impl.client.gui.widget.basewidgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@ApiStatus.Internal
final class TextFieldWidget extends WidgetWithBounds implements TextField {
    private final Rectangle bounds;
    private Function<String, String> textTransformer = SharedConstants::filterText;
    private int frame;
    private boolean editable = true;
    private int firstCharacterIndex;
    private int cursorPos;
    private int highlightPos;
    private int editableColor = 0xe0e0e0;
    private int notEditableColor = 0x707070;
    private TextFormatter formatter = TextFormatter.DEFAULT;
    private SuggestionRenderer suggestionRenderer = (matrices, x, y, color) ->
            this.font.drawShadow(matrices, this.font.plainSubstrByWidth(this.suggestion, this.getWidth()), x, y, color);
    private BorderColorProvider borderColorProvider = BorderColorProvider.DEFAULT;
    private String text = "";
    private int maxLength = 32;
    private boolean hasBorder = true;
    private boolean focusUnlocked = true;
    private boolean focused = false;
    private boolean selecting = false;
    @Nullable
    private String suggestion;
    private Consumer<String> responder = str -> {};
    private BooleanConsumer focusedResponder = bool -> {};
    private Predicate<String> filter = s -> true;
    
    TextFieldWidget(Rectangle bounds) {
        this.bounds = bounds;
    }
    
    public TextFieldWidget(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }
    
    @Override
    public void setTextTransformer(Function<String, String> textTransformer) {
        this.textTransformer = textTransformer;
    }
    
    @Override
    public String getSuggestion() {
        return suggestion;
    }
    
    @Override
    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
    
    @Override
    public void setBorderColorProvider(BorderColorProvider borderColorProvider) {
        this.borderColorProvider = borderColorProvider;
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }
    
    @Override
    public void setFocusedResponder(BooleanConsumer responder) {
        this.focusedResponder = responder;
    }
    
    @Override
    public void setFormatter(TextFormatter formatter) {
        this.formatter = formatter;
    }
    
    @Override
    public TextFormatter getFormatter() {
        return formatter;
    }
    
    @Override
    public void setSuggestionRenderer(SuggestionRenderer suggestionRenderer) {
        this.suggestionRenderer = suggestionRenderer;
    }
    
    @Override
    public SuggestionRenderer getSuggestionRenderer() {
        return suggestionRenderer;
    }
    
    @Override
    public void tick() {
        this.frame++;
    }
    
    @Override
    public String getText() {
        return this.text;
    }
    
    @Override
    public void setText(String text) {
        if (this.filter.test(text)) {
            if (text.length() > this.maxLength) {
                this.text = text.substring(0, this.maxLength);
            } else {
                this.text = text;
            }
            
            this.onChanged(text);
            this.moveCursorToEnd();
        }
    }
    
    @Override
    public String getSelectedText() {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        return this.text.substring(i, j);
    }
    
    public void setFilter(Predicate<String> filter) {
        this.filter = filter;
    }
    
    @Override
    public void addText(String text) {
        int highlightStart = Math.min(this.cursorPos, this.highlightPos);
        int highlightEnd = Math.max(this.cursorPos, this.highlightPos);
        int k = this.maxLength - this.text.length() - (highlightStart - highlightEnd);
        String textFiltered = textTransformer.apply(text);
        int l = textFiltered.length();
        if (k < l) {
            textFiltered = textFiltered.substring(0, k);
            l = k;
        }
        
        String result = new StringBuilder(this.text)
                .replace(highlightStart, highlightEnd, textFiltered)
                .toString();
        if (this.filter.test(result)) {
            this.text = result;
            this.setCursorPosition(highlightStart + l);
            this.setHighlightPos(this.cursorPos);
            this.onChanged(this.text);
        }
    }
    
    public void onChanged(String newText) {
        this.responder.accept(newText);
    }
    
    private void erase(int offset) {
        if (Screen.hasControlDown()) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }
    }
    
    public void eraseWords(int wordOffset) {
        if (!this.text.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.addText("");
            } else {
                this.eraseCharacters(this.getWordPosition(wordOffset) - this.cursorPos);
            }
        }
    }
    
    public void eraseCharacters(int characterOffset) {
        if (!this.text.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.addText("");
            } else {
                int offsetCursorPos = this.getCursorPos(characterOffset);
                int from = Math.min(offsetCursorPos, this.cursorPos);
                int to = Math.max(offsetCursorPos, this.cursorPos);
                if (from != to) {
                    String string = new StringBuilder(this.text).delete(from, to).toString();
                    if (this.filter.test(string)) {
                        this.text = string;
                        this.moveCursorTo(from);
                    }
                }
                this.onChanged(this.text);
            }
        }
    }
    
    public int getWordPosition(int wordOffset) {
        return this.getWordPosition(wordOffset, this.getCursor());
    }
    
    public int getWordPosition(int wordOffset, int cursorPosition) {
        return this.getWordPosition(wordOffset, cursorPosition, true);
    }
    
    public int getWordPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
        int cursor = cursorPosition;
        boolean backwards = wordOffset < 0;
        int absoluteOffset = Math.abs(wordOffset);
        
        for (int k = 0; k < absoluteOffset; ++k) {
            if (!backwards) {
                int l = this.text.length();
                cursor = this.text.indexOf(32, cursor);
                if (cursor == -1) {
                    cursor = l;
                } else {
                    while (skipOverSpaces && cursor < l && this.text.charAt(cursor) == ' ') {
                        ++cursor;
                    }
                }
            } else {
                while (skipOverSpaces && cursor > 0 && this.text.charAt(cursor - 1) == ' ') {
                    --cursor;
                }
                
                while (cursor > 0 && this.text.charAt(cursor - 1) != ' ') {
                    --cursor;
                }
            }
        }
        
        return cursor;
    }
    
    public void moveCursor(int by) {
        this.moveCursorTo(this.cursorPos + by);
    }
    
    private int getCursorPos(int i) {
        return Util.offsetByCodepoints(this.text, this.cursorPos, i);
    }
    
    @Override
    public void moveCursorTo(int cursor) {
        this.setCursorPosition(cursor);
        if (!selecting) {
            this.setHighlightPos(this.cursorPos);
        }
    }
    
    @Override
    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }
    
    @Override
    public void moveCursorToEnd() {
        this.moveCursorTo(this.text.length());
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isFocused()) {
            this.selecting = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(keyCode)) {
                minecraft.keyboardHandler.setClipboard(this.getSelectedText());
                return true;
            } else if (Screen.isPaste(keyCode)) {
                if (this.editable) {
                    this.addText(minecraft.keyboardHandler.getClipboard());
                }
                
                return true;
            } else if (Screen.isCut(keyCode)) {
                minecraft.keyboardHandler.setClipboard(this.getSelectedText());
                if (this.editable) {
                    this.addText("");
                }
                
                return true;
            } else {
                switch (keyCode) {
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
                        return keyCode != 256;
                    case 261:
                        if (this.editable) {
                            this.selecting = false;
                            this.erase(1);
                            this.selecting = Screen.hasShiftDown();
                        }
                        
                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }
                        
                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }
                        
                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                }
            }
        } else {
            return false;
        }
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        if (this.isFocused()) {
            if (SharedConstants.isAllowedChatCharacter(character) && !(
                    Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown() && (
                            character == 'a' || character == 'c' || character == 'v'
                    )
            )) {
                if (this.editable) {
                    this.addText(Character.toString(character));
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean hovered = mouseX >= (double) this.bounds.x && mouseX < (double) (this.bounds.x + this.bounds.width) && mouseY >= (double) this.bounds.y && mouseY < (double) (this.bounds.y + this.bounds.height);
        if (this.focusUnlocked) {
            this.setFocused(hovered);
        }
        
        if (this.focused && hovered && button == 0) {
            int int_2 = Mth.floor(mouseX) - this.bounds.x;
            if (this.hasBorder) {
                int_2 -= 4;
            }
            
            String string_1 = this.font.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), this.getWidth());
            this.moveCursorTo(this.font.plainSubstrByWidth(string_1, int_2).length() + this.firstCharacterIndex);
            return true;
        } else {
            return false;
        }
    }
    
    public void renderBorder(PoseStack matrices) {
        if (this.hasBorder()) {
            int borderColor = this.borderColorProvider.getBorderColor(this);
            fill(matrices, this.bounds.x - 1, this.bounds.y - 1, this.bounds.x + this.bounds.width + 1, this.bounds.y + this.bounds.height + 1, 0xff000000);
            fill(matrices, this.bounds.x, this.bounds.y, this.bounds.x + this.bounds.width, this.bounds.y + this.bounds.height, borderColor);
            fill(matrices, this.bounds.x + 1, this.bounds.y + 1, this.bounds.x + this.bounds.width - 1, this.bounds.y + this.bounds.height - 1, 0xff000000);
        }
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBorder(matrices);
        
        int color = this.editable ? this.editableColor : this.notEditableColor;
        int int_4 = this.cursorPos - this.firstCharacterIndex;
        int int_5 = this.highlightPos - this.firstCharacterIndex;
        String textClipped = this.font.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), this.getWidth());
        boolean boolean_1 = int_4 >= 0 && int_4 <= textClipped.length();
        boolean boolean_2 = this.focused && this.frame / 6 % 2 == 0 && boolean_1;
        int x = this.hasBorder ? this.bounds.x + 4 : this.bounds.x;
        int y = this.hasBorder ? this.bounds.y + (this.bounds.height - 8) / 2 : this.bounds.y;
        int int_8 = x;
        int_5 = Math.min(textClipped.length(), int_5);
        
        if (!textClipped.isEmpty()) {
            String string_2 = boolean_1 ? textClipped.substring(0, int_4) : textClipped;
            int_8 = this.font.drawShadow(matrices, this.formatter.format(string_2, this.firstCharacterIndex), (float) x, (float) y, color);
        }
        
        boolean isCursorInsideText = this.cursorPos < this.text.length() || this.text.length() >= this.getMaxLength();
        int selectionLeft = int_8;
        if (!boolean_1) {
            selectionLeft = int_4 > 0 ? x + this.bounds.width : x;
        } else if (isCursorInsideText) {
            --int_8;
        }
        selectionLeft--;
        
        if (!textClipped.isEmpty() && boolean_1 && int_4 < textClipped.length()) {
            this.font.drawShadow(matrices, this.formatter.format(textClipped.substring(int_4), this.cursorPos), (float) int_8, (float) y, color);
        }
        
        if (!isCursorInsideText && text.isEmpty() && this.suggestion != null) {
            renderSuggestion(matrices, x, y);
        }
        
        if (boolean_2) {
            fill(matrices, selectionLeft + 1, y, selectionLeft + 2, y + 9, ((0xFF) << 24) | ((((color >> 16 & 255) / 4) & 0xFF) << 16) | ((((color >> 8 & 255) / 4) & 0xFF) << 8) | ((((color & 255) / 4) & 0xFF)));
            fill(matrices, selectionLeft, y - 1, selectionLeft + 1, y + 8, ((0xFF) << 24) | color);
        }
        
        // Render selection overlay
        if (int_5 != int_4) {
            int selectionRight = x + this.font.width(textClipped.substring(0, int_5));
            this.renderSelection(matrices, selectionLeft, y - 1, selectionRight - 1, y + 9, color);
        }
    }
    
    private void renderSuggestion(PoseStack matrices, int x, int y) {
        this.suggestionRenderer.renderSuggestion(matrices, x, y, -8355712);
    }
    
    private void renderSelection(PoseStack matrices, int x1, int y1, int x2, int y2, int color) {
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
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        Matrix4f matrix = matrices.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, x1, y2, getBlitOffset() + 50f).color(r, g, b, 120).endVertex();
        buffer.vertex(matrix, x2, y2, getBlitOffset() + 50f).color(r, g, b, 120).endVertex();
        buffer.vertex(matrix, x2, y1, getBlitOffset() + 50f).color(r, g, b, 120).endVertex();
        buffer.vertex(matrix, x1, y1, getBlitOffset() + 50f).color(r, g, b, 120).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }
    
    @Override
    public int getMaxLength() {
        return this.maxLength;
    }
    
    @Override
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (this.text.length() > maxLength) {
            this.text = this.text.substring(0, maxLength);
            this.onChanged(this.text);
        }
    }
    
    @Override
    public int getCursor() {
        return this.cursorPos;
    }
    
    @Override
    public void setCursorPosition(int cursor) {
        this.cursorPos = Mth.clamp(cursor, 0, this.text.length());
    }
    
    @Override
    public boolean hasBorder() {
        return this.hasBorder;
    }
    
    @Override
    public void setHasBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
    }
    
    @Override
    public void setEditableColor(int editableColor) {
        this.editableColor = editableColor;
    }
    
    @Override
    public void setNotEditableColor(int notEditableColor) {
        this.notEditableColor = notEditableColor;
    }
    
    @Override
    public boolean changeFocus(boolean next) {
        if (this.editable) {
            this.setFocused(!this.focused);
            return this.focused;
        }
        return false;
    }
    
    @Override
    public boolean isFocused() {
        return this.focused;
    }
    
    @Override
    public void setFocused(boolean focused) {
        if (focused && !this.focused)
            this.frame = 0;
        this.focused = focused;
        this.focusedResponder.accept(focused);
    }
    
    @Override
    public void setFocusedFromKey(boolean focused, InputConstants.Key key) {
        setFocused(focused);
    }
    
    @Override
    public WidgetWithBounds asWidget() {
        return this;
    }
    
    public void setIsEditable(boolean isEditable) {
        this.editable = isEditable;
    }
    
    public int getWidth() {
        return this.hasBorder() ? this.bounds.width - 8 : this.bounds.width;
    }
    
    public void setHighlightPos(int highlightPos) {
        int j = this.text.length();
        this.highlightPos = Mth.clamp(highlightPos, 0, j);
        if (this.font != null) {
            if (this.firstCharacterIndex > j) {
                this.firstCharacterIndex = j;
            }
            
            int width = this.getWidth();
            String clippedText = this.font.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), width);
            int int_4 = clippedText.length() + this.firstCharacterIndex;
            if (this.highlightPos == this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.font.plainSubstrByWidth(this.text, width, true).length();
            }
            
            if (this.highlightPos > int_4) {
                this.firstCharacterIndex += this.highlightPos - int_4;
            } else if (this.highlightPos <= this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.firstCharacterIndex - this.highlightPos;
            }
            
            this.firstCharacterIndex = Mth.clamp(this.firstCharacterIndex, 0, j);
        }
        
    }
    
    public void setFocusUnlocked(boolean focusUnlocked) {
        this.focusUnlocked = focusUnlocked;
    }
    
    public int getCharacterX(int index) {
        return index > this.text.length() ? this.bounds.x : this.bounds.x + this.font.width(this.text.substring(0, index));
    }
}
