package me.shedaniel.rei.gui.credits;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ItemListWidget;
import net.minecraft.text.TextComponent;

public class CreditsItem extends ItemListWidget.Item<CreditsItem> {
    
    private String text;
    
    public CreditsItem(TextComponent textComponent) {
        this(textComponent.getFormattedText());
    }
    
    public CreditsItem(String text) {
        this.text = text;
    }
    
    @Override
    public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        MinecraftClient.getInstance().textRenderer.drawWithShadow(text, x + 5, y + 5, -1);
    }
    
}
