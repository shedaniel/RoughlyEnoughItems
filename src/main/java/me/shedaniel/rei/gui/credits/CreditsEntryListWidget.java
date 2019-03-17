package me.shedaniel.rei.gui.credits;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

public class CreditsEntryListWidget extends GuiListExtended<CreditsEntry> {
    
    public CreditsEntryListWidget(Minecraft client, int width, int height, int startY, int endY, int entryHeight) {
        super(client, width, height, startY, endY, entryHeight);
        setShowSelectionBox(false);
    }
    
    public void creditsClearEntries() {
        clearEntries();
    }
    
    private CreditsEntry getEntry(int int_1) {
        return this.getChildren().get(int_1);
    }
    
    public void creditsAddEntry(CreditsEntry entry) {
        addEntry(entry);
    }
    
    @Override
    public int getListWidth() {
        return width - 80;
    }
    
    @Override
    protected int getScrollBarX() {
        return width - 40;
    }
    
}
