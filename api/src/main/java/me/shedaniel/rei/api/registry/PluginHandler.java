package me.shedaniel.rei.api.registry;

import me.shedaniel.rei.api.REIPluginEntry;
import me.shedaniel.rei.api.util.Identifiable;

public interface PluginHandler extends Identifiable {
    void acceptPlugin(REIPluginEntry plugin);
    
    boolean isConcurrent();
}
