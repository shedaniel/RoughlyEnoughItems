package me.shedaniel.rei.gui.credits;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;

public class CreditsEntryListWidget extends EntryListWidget<CreditsEntry> {
    
    public CreditsEntryListWidget(MinecraftClient client, int width, int height, int startY, int endY, int entryHeight) {
        super(client, width, height, startY, endY, entryHeight);
        method_1943(false); //toggleShowSelection
    }
    
    public void creditsClearEntries() {
        clearEntries();
    }
    
    private CreditsEntry getEntry(int int_1) {
        return this.getInputListeners().get(int_1);
    }
    
    public void creditsAddEntry(CreditsEntry entry) {
        addEntry(entry);
    }
    
    @Override
    public int getEntryWidth() {
        return width - 80;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return width - 40;
    }
    
}
