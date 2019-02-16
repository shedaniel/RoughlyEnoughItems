package me.shedaniel.rei.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

public class ConfigEntryListWidget extends GuiListExtended<ConfigEntry> {
    
    public ConfigEntryListWidget(Minecraft client, int width, int height, int startY, int endY, int entryHeight) {
        super(client, width, height, startY, endY, entryHeight);
        setShowSelectionBox(false); //toggleShowSelection
    }
    
    public void configClearEntries() {
        clearEntries();
    }
    
    private ConfigEntry getEntry(int int_1) {
        return this.getChildren().get(int_1);
    }
    
    public void configAddEntry(ConfigEntry entry) {
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
