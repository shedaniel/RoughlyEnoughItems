package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.shedaniel.rei.api.IPluginDisabler;
import me.shedaniel.rei.api.PluginFunction;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class PluginManager implements IPluginDisabler {
    
    private static Map<Identifier, List<PluginFunction>> pluginDisabledFunctions = Maps.newHashMap();
    
    @Override
    public void disablePluginFunction(Identifier plugin, PluginFunction function) {
        List<PluginFunction> list = Lists.newArrayList();
        if (pluginDisabledFunctions.containsKey(plugin))
            list = pluginDisabledFunctions.get(plugin);
        if (!list.contains(function))
            list.add(function);
        pluginDisabledFunctions.put(plugin, list);
    }
    
    @Override
    public void enablePluginFunction(Identifier plugin, PluginFunction function) {
        List<PluginFunction> list = Lists.newArrayList();
        if (pluginDisabledFunctions.containsKey(plugin))
            list = pluginDisabledFunctions.get(plugin);
        if (list.contains(function))
            list.remove(function);
        pluginDisabledFunctions.put(plugin, list);
        if (list.size() == 0)
            pluginDisabledFunctions.remove(plugin);
    }
    
    @Override
    public boolean isFunctionEnabled(Identifier plugin, PluginFunction function) {
        return !pluginDisabledFunctions.containsKey(plugin) || !pluginDisabledFunctions.get(plugin).contains(function);
    }
    
}
