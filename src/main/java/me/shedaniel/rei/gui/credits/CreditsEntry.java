package me.shedaniel.rei.gui.credits;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.TextComponent;

public class CreditsEntry extends EntryListWidget.Entry<CreditsEntry> {
    
    private TextComponent textComponent;
    
    public CreditsEntry(TextComponent textComponent) {
        this.textComponent = textComponent;
    }
    
    @Override
    public void draw(int entryWidth, int height, int i3, int i4, boolean isSelected, float delta) {
        int x = getX();
        int y = getY();
        MinecraftClient.getInstance().fontRenderer.drawWithShadow(textComponent.getFormattedText(), x + 5, y + 5, -1);
    }
    
}
