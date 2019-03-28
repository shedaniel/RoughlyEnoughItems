package me.shedaniel.rei.gui.credits;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;

public class CreditsEntryListWidget extends EntryListWidget<CreditsEntry> {
    
    public CreditsEntryListWidget(MinecraftClient client, int width, int height, int startY, int endY, int entryHeight) {
        super(client, width, height, startY, endY, entryHeight);
        field_19091 = false; // showSelection
    }
    
    public void creditsClearEntries() {
        clearEntries();
    }
    
    private CreditsEntry getEntry(int int_1) {
        return this.children().get(int_1);
    }
    
    public void creditsAddEntry(CreditsEntry entry) {
        addEntry(entry);
    }
    
    @Override
    // getRowWidth
    public int method_20053() {
        return field_19083 - 80; // width
    }
    
    @Override
    // getScrollbarPosition
    protected int method_20078() {
        return field_19083 - 40; // width
    }
    
}
