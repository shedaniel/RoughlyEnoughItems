package me.shedaniel.rei.api.gui.widgets;

public interface TextField {
    String getText();
    
    void setText(String text);
    
    String getSelectedText();
    
    void addText(String text);
    
    void moveCursorTo(int cursor);
    
    void moveCursorToStart();
    
    void moveCursorToEnd();
    
    int getMaxLength();
    
    void setMaxLength(int maxLength);
    
    int getCursor();
    
    void setCursorPosition(int cursor);
    
    boolean hasBorder();
    
    void setHasBorder(boolean hasBorder);
    
    void setEditableColor(int editableColor);
    
    void setNotEditableColor(int int_1);
    
    boolean isFocused();
    
    void setFocused(boolean boolean_1);
}
