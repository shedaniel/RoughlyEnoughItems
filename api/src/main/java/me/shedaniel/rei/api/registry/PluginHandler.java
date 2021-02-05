package me.shedaniel.rei.api.registry;

import me.shedaniel.rei.api.REIPlugin;
import me.shedaniel.rei.api.util.Identifiable;

public interface PluginHandler extends Identifiable {
    void acceptPlugin(REIPlugin plugin);
    
    boolean isConcurrent();
}
