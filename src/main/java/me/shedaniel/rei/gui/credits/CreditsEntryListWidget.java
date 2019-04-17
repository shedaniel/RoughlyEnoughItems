package me.shedaniel.rei.gui.credits;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.menu.AlwaysSelectedItemListWidget;
import net.minecraft.text.TextComponent;

public class CreditsEntryListWidget extends AlwaysSelectedItemListWidget<CreditsEntryListWidget.CreditsItem> {
    
    public CreditsEntryListWidget(MinecraftClient client, int width, int height, int startY, int endY, int entryHeight) {
        super(client, width, height, startY, endY, entryHeight);
        visible = false; // showSelection
    }
    
    public void creditsClearEntries() {
        clearItems();
    }
    
    private CreditsItem getEntry(int int_1) {
        return this.children().get(int_1);
    }
    
    public void creditsAddEntry(CreditsItem entry) {
        addItem(entry);
    }
    
    @Override
    public int getItemWidth() {
        return width - 80;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return width - 40;
    }
    
    public static class CreditsItem extends AlwaysSelectedItemListWidget.Item<CreditsItem> {
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
    
}
