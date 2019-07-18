package me.shedaniel.reiclothconfig2.gui;

import me.shedaniel.reiclothconfig2.api.MouseUtils;
import net.minecraft.client.gui.GuiButton;

import java.util.Random;

public class ClothConfigTabButton extends GuiButton {
    
    private int index = -1;
    private ClothConfigScreen screen;
    
    public ClothConfigTabButton(ClothConfigScreen screen, int index, int int_1, int int_2, int int_3, int int_4, String string_1) {
        super(new Random().nextInt(), int_1, int_2, int_3, int_4, string_1);
        this.index = index;
        this.screen = screen;
    }
    
    @Override
    public void onClick(double diawd, double djwaidw) {
        super.onClick(diawd, djwaidw);
        if (index != -1)
            screen.nextTabIndex = index;
        screen.tabsScrollVelocity = 0d;
        screen.initGui();
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        enabled = index != screen.selectedTabIndex;
        super.render(int_1, int_2, float_1);
    }
    
    @Override
    protected boolean isPressable(double double_1, double double_2) {
        return visible && enabled && isMouseOver();
    }
    
    @Override
    public boolean isMouseOver() {
        double double_1 = MouseUtils.getMouseX();
        double double_2 = MouseUtils.getMouseY();
        return this.enabled && this.visible && double_1 >= this.x && double_2 >= this.y && double_1 < this.x + this.width && double_2 < this.y + this.height && double_1 >= 20 && double_1 < screen.width - 20;
    }
    
    @Override
    protected int getHoverState(boolean mouseOver) {
        return super.getHoverState(isMouseOver());
    }
    
}
