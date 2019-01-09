package me.shedaniel.rei.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.GuiEventListener;

public class LabelWidget extends Drawable implements IWidget {
    
    public int x;
    public int y;
    public String text;
    
    public LabelWidget(int x, int y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawStringCentered(MinecraftClient.getInstance().fontRenderer, text, x, y, -1);
    }
    
}
