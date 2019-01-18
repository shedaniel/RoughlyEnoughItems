package me.shedaniel.rei.gui.widget;

import net.minecraft.client.gui.IGuiEventListener;

import java.util.List;

public interface IWidget extends IGuiEventListener {
    
    public List<IWidget> getListeners();
    
    public void draw(int mouseX, int mouseY, float partialTicks);
    
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
    default boolean mouseScrolled(double amount) {
        if (onMouseScrolled(amount))
            return true;
        for(IWidget widget : getListeners())
            if (widget.mouseScrolled(amount))
                return true;
        return false;
    }
    
}
