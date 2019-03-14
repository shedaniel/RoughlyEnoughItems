package me.shedaniel.rei.gui.widget;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.InputListener;

import java.util.List;

public interface IWidget extends InputListener, Drawable {
    
    public List<IWidget> getListeners();
    
    @Override
    default boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (onMouseClick(int_1, double_1, double_2))
            return true;
        for(IWidget widget : getListeners())
            if (widget.mouseClicked(double_1, double_2, int_1))
                return true;
        return false;
    }
    
    default boolean onMouseClick(int button, double mouseX, double mouseY) {
        return false;
    }
    
    default boolean onMouseScrolled(double amount) {
        return false;
    }
    
    @Override
    default boolean mouseScrolled(double i, double j, double amount) {
        if (onMouseScrolled(amount))
            return true;
        for(IWidget widget : getListeners())
            if (widget.mouseScrolled(i, j, amount))
                return true;
        return false;
    }
    
}
