package me.shedaniel.rei.api.common.entry.settings;

import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.Reloadable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface EntrySettingsAdapterRegistry extends Reloadable<REIPlugin<?>> {
    static EntrySettingsAdapterRegistry getInstance() {
        return PluginManager.getInstance().get(EntrySettingsAdapterRegistry.class);
    }
    
    <T, S> void register(EntryType<T> type, EntryStack.Settings<S> settings, EntrySettingsAdapter<T, S> provider);
    
    @Nullable
    <T, S> S adapt(EntryStack<T> stack, EntryStack.Settings<S> settings, @Nullable S value);
}
