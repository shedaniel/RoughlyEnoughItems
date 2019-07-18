package me.shedaniel.reiclothconfig2.api;

import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface ConfigCategory {
    
    @Deprecated
    List<Object> getEntries();
    
    ConfigCategory addEntry(AbstractConfigListEntry entry);
    
    ConfigCategory setCategoryBackground(ResourceLocation identifier);
    
}
