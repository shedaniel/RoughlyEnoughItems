package me.shedaniel.rei.gui.credits;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ItemListWidget;

public class CreditsEntryListWidget extends ItemListWidget<CreditsItem> {
    
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
    
}
