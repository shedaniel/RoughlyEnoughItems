package me.shedaniel.rei.gui.widget;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TextFieldWidget extends DrawableHelper implements HighlightableWidget {
    
    private final TextRenderer textRenderer;
    public Function<String, String> stripInvaild;
    private Rectangle bounds;
    private String text;
    private int maxLength;
    private int focusedTicks;
    private boolean hasBorder;
    private boolean field_2096;
    private boolean focused;
    private boolean editable;
    private boolean field_17037;
    private int field_2103;
    private int cursorMax;
    private int cursorMin;
    private int field_2100;
    private int field_2098;
    private boolean visible;
    private String suggestion;
    private Consumer<String> changedListener;
    private Predicate<String> textPredicate;
    private BiFunction<String, Integer, String> renderTextProvider;
    
    public TextFieldWidget(Rectangle rectangle) {
        this.text = "";
        this.maxLength = 32;
        this.hasBorder = true;
        this.field_2096 = true;
        this.editable = true;
        this.field_2100 = 14737632;
        this.field_2098 = 7368816;
        this.visible = true;
        this.textPredicate = Predicates.alwaysTrue();
        this.renderTextProvider = (string_1, integer_1) -> {
            return string_1;
        };
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.bounds = rectangle;
        this.stripInvaild = s -> SharedConstants.stripInvalidChars(s);
    }
    
    public TextFieldWidget(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
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
            this.method_1872();
        }
    }
    
    public String getSelectedText() {
        int int_1 = this.cursorMax < this.cursorMin ? this.cursorMax : this.cursorMin;
        int int_2 = this.cursorMax < this.cursorMin ? this.cursorMin : this.cursorMax;
        return this.text.substring(int_1, int_2);
    }
    
    public void method_1890(Predicate<String> predicate_1) {
        this.textPredicate = predicate_1;
    }
    
    public void addText(String string_1) {
        String string_2 = "";
        String string_3 = stripInvaild.apply(string_1);
        int int_1 = this.cursorMax < this.cursorMin ? this.cursorMax : this.cursorMin;
        int int_2 = this.cursorMax < this.cursorMin ? this.cursorMin : this.cursorMax;
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
        if (Screen.isControlPressed()) {
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
                        this.moveCursor(int_1);
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
        
        for(int int_5 = 0; int_5 < int_4; ++int_5) {
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
    
    public void moveCursor(int int_1) {
        this.method_1883(this.cursorMax + int_1);
    }
    
    public void method_1883(int int_1) {
        this.setCursor(int_1);
        if (!this.field_17037) {
            this.method_1884(this.cursorMax);
        }
        
        this.onChanged(this.text);
    }
    
    public void method_1870() {
        this.method_1883(0);
    }
    
    public void method_1872() {
        this.method_1883(this.text.length());
    }
    
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.isVisible() && this.isFocused()) {
            this.field_17037 = Screen.isShiftPressed();
            if (Screen.isSelectAllShortcutPressed(int_1)) {
                this.method_1872();
                this.method_1884(0);
                return true;
            } else if (Screen.isCopyShortcutPressed(int_1)) {
                MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                return true;
            } else if (Screen.isPasteShortcutPressed(int_1)) {
                if (this.editable) {
                    this.addText(MinecraftClient.getInstance().keyboard.getClipboard());
                }
                
                return true;
            } else if (Screen.isCutShortcutPressed(int_1)) {
                MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
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
                        if (Screen.isControlPressed()) {
                            this.method_1883(this.method_1853(1));
                        } else {
                            this.moveCursor(1);
                        }
                        
                        return true;
                    case 263:
                        if (Screen.isControlPressed()) {
                            this.method_1883(this.method_1853(-1));
                        } else {
                            this.moveCursor(-1);
                        }
                        
                        return true;
                    case 268:
                        this.method_1870();
                        return true;
                    case 269:
                        this.method_1872();
                        return true;
                }
            }
        } else {
            return false;
        }
    }
    
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
    public List<IWidget> getListeners() {
        return new ArrayList<>();
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
                
                String string_1 = this.textRenderer.trimToWidth(this.text.substring(this.field_2103), this.method_1859());
                this.method_1883(this.textRenderer.trimToWidth(string_1, int_2).length() + this.field_2103);
                return true;
            } else {
                return false;
            }
        }
    }
    
    public void draw(int int_1, int int_2, float float_1) {
        if (this.isVisible()) {
            if (this.hasBorder()) {
                drawRect(this.bounds.x - 1, this.bounds.y - 1, this.bounds.x + this.bounds.width + 1, this.bounds.y + this.bounds.height + 1, -6250336);
                drawRect(this.bounds.x, this.bounds.y, this.bounds.x + this.bounds.width, this.bounds.y + this.bounds.height, -16777216);
            }
            
            int int_3 = this.editable ? this.field_2100 : this.field_2098;
            int int_4 = this.cursorMax - this.field_2103;
            int int_5 = this.cursorMin - this.field_2103;
            String string_1 = this.textRenderer.trimToWidth(this.text.substring(this.field_2103), this.method_1859());
            boolean boolean_1 = int_4 >= 0 && int_4 <= string_1.length();
            boolean boolean_2 = this.focused && this.focusedTicks / 6 % 2 == 0 && boolean_1;
            int int_6 = this.hasBorder ? this.bounds.x + 4 : this.bounds.x;
            int int_7 = this.hasBorder ? this.bounds.y + (this.bounds.height - 8) / 2 : this.bounds.y;
            int int_8 = int_6;
            if (int_5 > string_1.length()) {
                int_5 = string_1.length();
            }
            
            if (!string_1.isEmpty()) {
                String string_2 = boolean_1 ? string_1.substring(0, int_4) : string_1;
                int_8 = this.textRenderer.drawWithShadow((String) this.renderTextProvider.apply(string_2, this.field_2103), (float) int_6, (float) int_7, int_3);
            }
            
            boolean boolean_3 = this.cursorMax < this.text.length() || this.text.length() >= this.getMaxLength();
            int int_9 = int_8;
            if (!boolean_1) {
                int_9 = int_4 > 0 ? int_6 + this.bounds.width : int_6;
            } else if (boolean_3) {
                int_9 = int_8 - 1;
                --int_8;
            }
            
            if (!string_1.isEmpty() && boolean_1 && int_4 < string_1.length()) {
                this.textRenderer.drawWithShadow((String) this.renderTextProvider.apply(string_1.substring(int_4), this.cursorMax), (float) int_8, (float) int_7, int_3);
            }
            
            if (!boolean_3 && text.isEmpty() && this.suggestion != null) {
                this.textRenderer.drawWithShadow(this.textRenderer.trimToWidth(this.suggestion, this.method_1859()), (float) int_6, (float) int_7, -8355712);
            }
            
            int var10002;
            int var10003;
            if (boolean_2) {
                if (boolean_3) {
                    int var10001 = int_7 - 1;
                    var10002 = int_9 + 1;
                    var10003 = int_7 + 1;
                    this.textRenderer.getClass();
                    DrawableHelper.drawRect(int_9, var10001, var10002, var10003 + 9, -3092272);
                } else {
                    this.textRenderer.drawWithShadow("_", (float) int_9, (float) int_7, int_3);
                }
            }
            
            if (int_5 != int_4) {
                int int_10 = int_6 + this.textRenderer.getStringWidth(string_1.substring(0, int_5));
                var10002 = int_7 - 1;
                var10003 = int_10 - 1;
                int var10004 = int_7 + 1;
                this.textRenderer.getClass();
                this.method_1886(int_9, var10002, var10003, var10004 + 9);
            }
            
        }
    }
    
    private void method_1886(int int_1, int int_2, int int_3, int int_4) {
        int int_6;
        if (int_1 < int_3) {
            int_6 = int_1;
            int_1 = int_3;
            int_3 = int_6;
        }
        
        if (int_2 < int_4) {
            int_6 = int_2;
            int_2 = int_4;
            int_4 = int_6;
        }
        
        if (int_3 > this.bounds.x + this.bounds.width) {
            int_3 = this.bounds.x + this.bounds.width;
        }
        
        if (int_1 > this.bounds.x + this.bounds.width) {
            int_1 = this.bounds.x + this.bounds.width;
        }
        
        Tessellator tessellator_1 = Tessellator.getInstance();
        BufferBuilder bufferBuilder_1 = tessellator_1.getBufferBuilder();
        GlStateManager.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture();
        GlStateManager.enableColorLogicOp();
        GlStateManager.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder_1.begin(7, VertexFormats.POSITION);
        bufferBuilder_1.vertex((double) int_1, (double) int_4, 0.0D).next();
        bufferBuilder_1.vertex((double) int_3, (double) int_4, 0.0D).next();
        bufferBuilder_1.vertex((double) int_3, (double) int_2, 0.0D).next();
        bufferBuilder_1.vertex((double) int_1, (double) int_2, 0.0D).next();
        tessellator_1.draw();
        GlStateManager.disableColorLogicOp();
        GlStateManager.enableTexture();
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
    
    public void method_1868(int int_1) {
        this.field_2100 = int_1;
    }
    
    public void method_1860(int int_1) {
        this.field_2098 = int_1;
    }
    
    public void setHasFocus(boolean boolean_1) {
        this.setFocused(boolean_1);
    }
    
    public boolean hasFocus() {
        return true;
    }
    
    public boolean isFocused() {
        return this.focused;
    }
    
    public void setFocused(boolean boolean_1) {
        if (boolean_1 && !this.focused) {
            this.focusedTicks = 0;
        }
        
        this.focused = boolean_1;
    }
    
    public void setIsEditable(boolean boolean_1) {
        this.editable = boolean_1;
    }
    
    public int method_1859() {
        return this.hasBorder() ? this.bounds.width - 8 : this.bounds.width;
    }
    
    public void method_1884(int int_1) {
        int int_2 = this.text.length();
        this.cursorMin = MathHelper.clamp(int_1, 0, int_2);
        if (this.textRenderer != null) {
            if (this.field_2103 > int_2) {
                this.field_2103 = int_2;
            }
            
            int int_3 = this.method_1859();
            String string_1 = this.textRenderer.trimToWidth(this.text.substring(this.field_2103), int_3);
            int int_4 = string_1.length() + this.field_2103;
            if (this.cursorMin == this.field_2103) {
                this.field_2103 -= this.textRenderer.trimToWidth(this.text, int_3, true).length();
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
    
    public void setSuggestion(String string_1) {
        this.suggestion = string_1;
    }
    
    public int method_1889(int int_1) {
        return int_1 > this.text.length() ? this.bounds.x : this.bounds.x + this.textRenderer.getStringWidth(this.text.substring(0, int_1));
    }
    
}
