package me.shedaniel.rei.gui.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;

public class ConfigEntryListWidget extends EntryListWidget<ConfigEntry> {
    
    public ConfigEntryListWidget(MinecraftClient client, int width, int height, int startY, int endY, int entryHeight) {
        super(client, width, height, startY, endY, entryHeight);
        method_1943(false); //toggleShowSelection
    }
    
    public void configClearEntries() {
        clearEntries();
    }
    
    private ConfigEntry getEntry(int int_1) {
        return this.getEntries().get(int_1);
    }
    
    public void configAddEntry(ConfigEntry entry) {
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
