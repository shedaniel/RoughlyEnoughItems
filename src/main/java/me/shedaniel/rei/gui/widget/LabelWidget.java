package me.shedaniel.rei.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;

import java.util.ArrayList;
import java.util.List;

public class LabelWidget extends DrawableHelper implements IWidget {
    
    public int x;
    public int y;
    public String text;
    protected TextRenderer textRenderer;
    
    public LabelWidget(int x, int y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
    }
    
    @Override
    public List<IWidget> getListeners() {
        return new ArrayList<>();
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawStringCentered(textRenderer, text, x, y, -1);
    }
    
}
