package me.shedaniel.rei.gui.widget;

import net.minecraft.client.gui.DrawableHelper;

import java.awt.*;

public class RecipePageLabelWidget extends TextFieldWidget {
    
    private Point middlePoint;
    private int maximumPage;
    
    public RecipePageLabelWidget(int x, int y, int currentPage, int maximumPage) {
        super(new Rectangle(0, 0, 40, 20));
        this.middlePoint = new Point(x, y);
        this.maximumPage = maximumPage;
        this.setHasBorder(false);
        setChangedListener(s -> updateSize());
        this.setText(String.valueOf(currentPage + 1));
        stripInvaild = s -> {
            StringBuilder stringBuilder_1 = new StringBuilder();
            char[] var2 = s.toCharArray();
            int var3 = var2.length;
            
            for(int var4 = 0; var4 < var3; ++var4) {
                char char_1 = var2[var4];
                if (Character.isDigit(char_1))
                    stringBuilder_1.append(char_1);
            }
            
            return stringBuilder_1.toString();
        };
        setMaxLength(String.valueOf(maximumPage).length());
    }
    
    public void updateSize() {
        this.getBounds().setLocation(middlePoint.x - 40, middlePoint.y);
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        setEditableColor(isInvaildPage(getText()) ? -1 : Color.RED.getRGB());
        if (this.isVisible()) {
            if (this.hasBorder()) {
                drawRect(this.getBounds().x - 1, this.getBounds().y - 1, this.getBounds().x + this.getBounds().width + 1, this.getBounds().y + this.getBounds().height + 1, -6250336);
                drawRect(this.getBounds().x, this.getBounds().y, this.getBounds().x + this.getBounds().width, this.getBounds().y + this.getBounds().height, -16777216);
            }
            
            int color = this.editable ? this.editableColor : this.notEditableColor;
            int int_4 = this.cursorMax - this.field_2103;
            int int_5 = this.cursorMin - this.field_2103;
            String string_1 = this.getText(); //this.textRenderer.trimToWidth(this.getText().substring(this.field_2103), this.getWidth());
            boolean boolean_1 = int_4 >= 0 && int_4 <= string_1.length();
            boolean boolean_2 = this.isFocused() && this.focusedTicks / 6 % 2 == 0 && boolean_1;
            int int_6 = this.hasBorder() ? this.getBounds().x + 4 : this.getBounds().x;
            int int_7 = this.hasBorder() ? this.getBounds().y + (this.getBounds().height - 8) / 2 : this.getBounds().y;
            int int_8 = int_6;
            if (int_5 > string_1.length()) {
                int_5 = string_1.length();
            }
            
            if (!string_1.isEmpty()) {
                String string_2 = boolean_1 ? string_1.substring(0, int_4) : string_1;
                int_8 = this.textRenderer.drawWithShadow(this.renderTextProvider.apply(string_2, this.field_2103), (float) getBounds().x + getBounds().width - textRenderer.getStringWidth(string_2.substring(1)), (float) int_7, color);
            }
            
            boolean boolean_3 = this.cursorMax < this.getText().length() || this.getText().length() >= this.getMaxLength();
            int int_9 = int_8;
            if (!boolean_1) {
                int_9 = int_4 > 0 ? int_6 + this.getBounds().width : int_6;
            } else if (boolean_3) {
                int_9 = int_8 - 1;
                --int_8;
            }
            
            if (!string_1.isEmpty() && boolean_1 && int_4 < string_1.length()) {
                this.textRenderer.drawWithShadow((String) this.renderTextProvider.apply(string_1.substring(int_4), this.cursorMax), (float) int_8, (float) int_7, color);
            }
            
            if (!boolean_3 && getText().isEmpty() && this.getSuggestion() != null) {
                this.textRenderer.drawWithShadow(this.textRenderer.trimToWidth(this.getSuggestion(), this.getWidth()), (float) int_6, (float) int_7, -8355712);
            }
            
            if (boolean_2)
                if (boolean_3) {
                    int var10001 = int_7 - 1;
                    int var10002 = int_9 + 1;
                    int var10003 = int_7 + 1;
                    this.textRenderer.getClass();
                    DrawableHelper.drawRect(int_9, var10001, var10002, var10003 + 9, -3092272);
                }
            
            if (int_5 != int_4) {
                int int_10 = int_6 + this.textRenderer.getStringWidth(string_1.substring(0, int_5));
                int var10002 = int_7 - 1;
                int var10003 = int_10 - 1;
                int var10004 = int_7 + 1;
                this.textRenderer.getClass();
                this.method_1886(int_9, var10002, var10003, var10004 + 9);
            }
        }
        textRenderer.drawWithShadow(" /" + maximumPage, getBounds().x + getBounds().width + 2, getBounds().y, -1);
    }
    
    private boolean isInvaildPage(String text) {
        try {
            int page = Integer.valueOf(text);
            return page >= 1 && page <= maximumPage;
        } catch (Exception e) {
        }
        return false;
    }
    
}
