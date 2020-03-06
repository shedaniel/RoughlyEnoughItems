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
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.REIHelper;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TextFieldWidget extends WidgetWithBounds implements Tickable {
    
    public Function<String, String> stripInvalid;
    protected int focusedTicks;
    protected boolean editable;
    protected int field_2103;
    protected int cursorMax;
    protected int cursorMin;
    protected int editableColor;
    protected int notEditableColor;
    protected BiFunction<String, Integer, String> renderTextProvider;
    private Rectangle bounds;
    private String text;
    private int maxLength;
    private boolean hasBorder;
    private boolean field_2096;
    private boolean focused;
    private boolean visible;
    private String suggestion;
    private Consumer<String> changedListener;
    private Predicate<String> textPredicate;
    
    public TextFieldWidget(Rectangle rectangle) {
        this.text = "";
        this.maxLength = 32;
        this.hasBorder = true;
        this.field_2096 = true;
        this.editable = true;
        this.editableColor = 14737632;
        this.notEditableColor = 7368816;
        this.visible = true;
        this.textPredicate = s -> true;
        this.renderTextProvider = (string_1, integer_1) -> {
            return string_1;
        };
        this.bounds = rectangle;
        this.stripInvalid = SharedConstants::stripInvalidChars;
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
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    public void setChangedListener(Consumer<String> biConsumer_1) {
        this.changedListener = biConsumer_1;
    }
    
    public void method_1854(BiFunction<String, Integer, String> biFunction_1) {
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
            this.moveCursorToEnd();
        }
    }
    
    public String getSelectedText() {
        int int_1 = Math.min(this.cursorMax, this.cursorMin);
        int int_2 = Math.max(this.cursorMax, this.cursorMin);
        return this.text.substring(int_1, int_2);
    }
    
    public void method_1890(Predicate<String> predicate_1) {
        this.textPredicate = predicate_1;
    }
    
    public void addText(String string_1) {
        String string_2 = "";
        String string_3 = stripInvalid.apply(string_1);
        int int_1 = Math.min(this.cursorMax, this.cursorMin);
        int int_2 = Math.max(this.cursorMax, this.cursorMin);
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
            this.setCursor(int_1 + int_5);
            this.method_1884(this.cursorMax);
            this.onChanged(this.text);
        }
    }
    
    public void onChanged(String string_1) {
        if (this.changedListener != null) {
            this.changedListener.accept(string_1);
        }
        
    }
    
    private void method_16873(int int_1) {
        if (Screen.hasControlDown()) {
            this.method_1877(int_1);
        } else {
            this.method_1878(int_1);
        }
        
    }
    
    public void method_1877(int int_1) {
        if (!this.text.isEmpty()) {
            if (this.cursorMin != this.cursorMax) {
                this.addText("");
            } else {
                this.method_1878(this.method_1853(int_1) - this.cursorMax);
            }
        }
    }
    
    public void method_1878(int int_1) {
        if (!this.text.isEmpty()) {
            if (this.cursorMin != this.cursorMax) {
                this.addText("");
            } else {
                boolean boolean_1 = int_1 < 0;
                int int_2 = boolean_1 ? this.cursorMax + int_1 : this.cursorMax;
                int int_3 = boolean_1 ? this.cursorMax : this.cursorMax + int_1;
                String string_1 = "";
                if (int_2 >= 0) {
                    string_1 = this.text.substring(0, int_2);
                }
                
                if (int_3 < this.text.length()) {
                    string_1 = string_1 + this.text.substring(int_3);
                }
                
                if (this.textPredicate.test(string_1)) {
                    this.text = string_1;
                    if (boolean_1) {
                        this.moveCursor(int_1, true);
                    }
                    
                    this.onChanged(this.text);
                }
            }
        }
    }
    
    public int method_1853(int int_1) {
        return this.method_1869(int_1, this.getCursor());
    }
    
    public int method_1869(int int_1, int int_2) {
        return this.method_1864(int_1, int_2, true);
    }
    
    public int method_1864(int int_1, int int_2, boolean boolean_1) {
        int int_3 = int_2;
        boolean boolean_2 = int_1 < 0;
        int int_4 = Math.abs(int_1);
        
        for (int int_5 = 0; int_5 < int_4; ++int_5) {
            if (!boolean_2) {
                int int_6 = this.text.length();
                int_3 = this.text.indexOf(32, int_3);
                if (int_3 == -1) {
                    int_3 = int_6;
                } else {
                    while (boolean_1 && int_3 < int_6 && this.text.charAt(int_3) == ' ') {
                        ++int_3;
                    }
                }
            } else {
                while (boolean_1 && int_3 > 0 && this.text.charAt(int_3 - 1) == ' ') {
                    --int_3;
                }
                
                while (int_3 > 0 && this.text.charAt(int_3 - 1) != ' ') {
                    --int_3;
                }
            }
        }
        
        return int_3;
    }
    
    public void moveCursor(int int_1, boolean resetSelect) {
        this.moveCursorTo(this.cursorMax + int_1, resetSelect);
    }
    
    public void moveCursorTo(int int_1, boolean resetSelect) {
        this.setCursor(int_1);
        //        if (!this.field_17037) {
        if (resetSelect) {
            this.method_1884(this.cursorMax);
        }
        
        this.onChanged(this.text);
    }
    
    public void moveCursorToHead() {
        this.moveCursorTo(0, true);
    }
    
    public void moveCursorToEnd() {
        this.moveCursorTo(this.text.length(), true);
    }
    
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.isVisible() && this.isFocused()) {
            if (Screen.isSelectAll(int_1)) {
                this.moveCursorToEnd();
                this.method_1884(0);
                return true;
            } else if (Screen.isCopy(int_1)) {
                minecraft.keyboard.setClipboard(this.getSelectedText());
                return true;
            } else if (Screen.isPaste(int_1)) {
                if (this.editable) {
                    this.addText(minecraft.keyboard.getClipboard());
                }
                
                return true;
            } else if (Screen.isCut(int_1)) {
                minecraft.keyboard.setClipboard(this.getSelectedText());
                if (this.editable) {
                    this.addText("");
                }
                
                return true;
            } else {
                switch (int_1) {
                    case 259:
                        if (this.editable) {
                            this.method_16873(-1);
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
                            this.method_16873(1);
                        }
                        
                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.method_1853(1), !Screen.hasShiftDown());
                        } else {
                            this.moveCursor(1, !Screen.hasShiftDown());
                        }
                        
                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.method_1853(-1), !Screen.hasShiftDown());
                        } else {
                            this.moveCursor(-1, !Screen.hasShiftDown());
                        }
                        
                        return true;
                    case 268:
                        this.moveCursorToHead();
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
    public boolean charTyped(char char_1, int int_1) {
        if (this.isVisible() && this.isFocused()) {
            if (SharedConstants.isValidChar(char_1)) {
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
    
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean boolean_1 = double_1 >= (double) this.bounds.x && double_1 < (double) (this.bounds.x + this.bounds.width) && double_2 >= (double) this.bounds.y && double_2 < (double) (this.bounds.y + this.bounds.height);
            if (this.field_2096) {
                this.setFocused(boolean_1);
            }
            
            if (this.focused && boolean_1 && int_1 == 0) {
                int int_2 = MathHelper.floor(double_1) - this.bounds.x;
                if (this.hasBorder) {
                    int_2 -= 4;
                }
                
                String string_1 = this.font.trimToWidth(this.text.substring(this.field_2103), this.getWidth());
                this.moveCursorTo(this.font.trimToWidth(string_1, int_2).length() + this.field_2103, true);
                return true;
            } else {
                return false;
            }
        }
    }
    
    public void renderBorder() {
        if (this.hasBorder()) {
            if (containsMouse(PointHelper.ofMouse()) || focused)
                fill(this.bounds.x - 1, this.bounds.y - 1, this.bounds.x + this.bounds.width + 1, this.bounds.y + this.bounds.height + 1, REIHelper.getInstance().isDarkThemeEnabled() ? -17587 : -1);
            else
                fill(this.bounds.x - 1, this.bounds.y - 1, this.bounds.x + this.bounds.width + 1, this.bounds.y + this.bounds.height + 1, -6250336);
            fill(this.bounds.x, this.bounds.y, this.bounds.x + this.bounds.width, this.bounds.y + this.bounds.height, -16777216);
        }
    }
    
    public void render(int mouseX, int mouseY, float delta) {
        if (this.isVisible()) {
            renderBorder();
            
            int color = this.editable ? this.editableColor : this.notEditableColor;
            int int_4 = this.cursorMax - this.field_2103;
            int int_5 = this.cursorMin - this.field_2103;
            String string_1 = this.font.trimToWidth(this.text.substring(this.field_2103), this.getWidth());
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
                int_8 = this.font.drawWithShadow(this.renderTextProvider.apply(string_2, this.field_2103), (float) x, (float) y, color);
            }
            
            boolean isCursorInsideText = this.cursorMax < this.text.length() || this.text.length() >= this.getMaxLength();
            int int_9 = int_8;
            if (!boolean_1) {
                int_9 = int_4 > 0 ? x + this.bounds.width : x;
            } else if (isCursorInsideText) {
                --int_8;
            }
            int_9--;
            
            if (!string_1.isEmpty() && boolean_1 && int_4 < string_1.length()) {
                this.font.drawWithShadow(this.renderTextProvider.apply(string_1.substring(int_4), this.cursorMax), (float) int_8, (float) y, color);
            }
            
            if (!isCursorInsideText && text.isEmpty() && this.suggestion != null) {
                renderSuggestion(x, y);
            }
            
            if (boolean_2) {
                //                if (isCursorInsideText) {
                fill(int_9 + 1, y, int_9 + 2, y + 9, ((0xFF) << 24) | ((((color >> 16 & 255) / 4) & 0xFF) << 16) | ((((color >> 8 & 255) / 4) & 0xFF) << 8) | ((((color & 255) / 4) & 0xFF)));
                //                fill(int_9, y, int_9 + 1, y + 9, 0xff343434);
                fill(int_9, y - 1, int_9 + 1, y + 8, ((0xFF) << 24) | color);
                //                fill(int_9 - 1, y - 1, int_9, y + 8, 0xffd0d0d0);
                //                } else {
                //                                    this.font.drawWithShadow("|", (float) int_9 - 2, (float) y, 0xffd0d0d0);
                //                }
            }
            
            // Render selection overlay
            if (int_5 != int_4) {
                int int_10 = x + this.font.getStringWidth(string_1.substring(0, int_5));
                this.method_1886(int_9, y - 1, int_10 - 1, y + 9, color);
            }
        }
    }
    
    protected void renderSuggestion(int x, int y) {
        this.font.drawWithShadow(this.font.trimToWidth(this.suggestion, this.getWidth()), x, y, -8355712);
    }
    
    protected void method_1886(int x1, int y1, int x2, int y2, int color) {
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
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(7425);
        buffer.begin(7, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y2, getBlitOffset() + 50d).color(r, g, b, 120).next();
        buffer.vertex(x2, y2, getBlitOffset() + 50d).color(r, g, b, 120).next();
        buffer.vertex(x2, y1, getBlitOffset() + 50d).color(r, g, b, 120).next();
        buffer.vertex(x1, y1, getBlitOffset() + 50d).color(r, g, b, 120).next();
        tessellator.draw();
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
        return this.cursorMax;
    }
    
    public void setCursor(int int_1) {
        this.cursorMax = MathHelper.clamp(int_1, 0, this.text.length());
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
    
    public void method_1884(int int_1) {
        int int_2 = this.text.length();
        this.cursorMin = MathHelper.clamp(int_1, 0, int_2);
        if (this.font != null) {
            if (this.field_2103 > int_2) {
                this.field_2103 = int_2;
            }
            
            int int_3 = this.getWidth();
            String string_1 = this.font.trimToWidth(this.text.substring(this.field_2103), int_3);
            int int_4 = string_1.length() + this.field_2103;
            if (this.cursorMin == this.field_2103) {
                this.field_2103 -= this.font.trimToWidth(this.text, int_3, true).length();
            }
            
            if (this.cursorMin > int_4) {
                this.field_2103 += this.cursorMin - int_4;
            } else if (this.cursorMin <= this.field_2103) {
                this.field_2103 -= this.field_2103 - this.cursorMin;
            }
            
            this.field_2103 = MathHelper.clamp(this.field_2103, 0, int_2);
        }
        
    }
    
    public void method_1856(boolean boolean_1) {
        this.field_2096 = boolean_1;
    }
    
    public boolean isVisible() {
        return this.visible;
    }
    
    public void setVisible(boolean boolean_1) {
        this.visible = boolean_1;
    }
    
    public int method_1889(int int_1) {
        return int_1 > this.text.length() ? this.bounds.x : this.bounds.x + this.font.getStringWidth(this.text.substring(0, int_1));
    }
    
}
