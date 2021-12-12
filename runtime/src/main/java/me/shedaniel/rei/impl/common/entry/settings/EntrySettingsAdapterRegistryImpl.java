package me.shedaniel.rei.impl.common.entry.settings;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.EntryRendererProvider;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.settings.EntrySettingsAdapter;
import me.shedaniel.rei.api.common.entry.settings.EntrySettingsAdapterRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntrySettingsAdapterRegistryImpl implements EntrySettingsAdapterRegistry {
    private final Map<EntryStack.Settings<?>, Multimap<EntryType<?>, EntrySettingsAdapter<?, ?>>> providers = new HashMap<>();
    
    @Override
    public <T,S> void register(EntryType<T> type, EntryStack.Settings<S> settings, EntrySettingsAdapter<T,S> provider) {
        Multimap<EntryType<?>, EntrySettingsAdapter<?, ?>> multimap = this.providers.computeIfAbsent(settings, $ -> Multimaps.newMultimap(new Reference2ObjectOpenHashMap<>(), ArrayList::new));
        multimap.put(type, provider);
    }
    
    @Override
    @Nullable
    public <T,S> S adapt(EntryStack<T> stack, EntryStack.Settings<S> settings, @Nullable S value) {
        Multimap<EntryType<?>, EntrySettingsAdapter<?, ?>> multimap = providers.get(settings);
        if (multimap != null) {
            for (EntrySettingsAdapter<T, S> adapter : (Collection<EntrySettingsAdapter<T, S>>) (Collection<? extends EntrySettingsAdapter<?, ?>>) multimap.get(stack.getType())) {
                value = adapter.provide(stack, settings, value);
            }
        }
        return value;
    }
    
    @Override
    public void startReload() {
        providers.clear();
    }
    
    @Override
    public void acceptPlugin(REIPlugin<?> plugin) {
        plugin.registerEntrySettingsAdapters(this);
    }
}
