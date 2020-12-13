package me.shedaniel.rei.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;
import me.shedaniel.rei.api.entry.EntryDefinition;
import me.shedaniel.rei.api.entry.EntryType;
import me.shedaniel.rei.api.entry.EntryTypeBridge;
import me.shedaniel.rei.api.entry.EntryTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class EntryTypeRegistryImpl implements EntryTypeRegistry {
    private static final EntryTypeRegistryImpl INSTANCE = new EntryTypeRegistryImpl();
    private final BiMap<ResourceLocation, EntryDefinition<?>> entryTypes = HashBiMap.create();
    private final Table<ResourceLocation, ResourceLocation, List<EntryTypeBridge<?, ?>>> typeBridges = HashBasedTable.create();
    
    public static EntryTypeRegistryImpl getInstance() {
        return INSTANCE;
    }
    
    @Override
    public <T> void register(ResourceLocation id, EntryDefinition<T> definition) {
        this.entryTypes.put(id, definition);
    }
    
    @Override
    public <A, B> void registerBridge(EntryType<A> original, EntryType<B> destination, EntryTypeBridge<A, B> bridge) {
        List<EntryTypeBridge<?, ?>> list = this.typeBridges.get(original.getId(), destination.getId());
        if (list == null) {
            this.typeBridges.put(original.getId(), destination.getId(), list = new ArrayList<>());
        }
        list.add(bridge);
    }
    
    @Override
    public EntryDefinition<?> get(ResourceLocation id) {
        return this.entryTypes.get(id);
    }
    
    @Override
    public <A, B> Iterable<EntryTypeBridge<A, B>> getBridgesFor(EntryType<A> original, EntryType<B> destination) {
        List<? extends EntryTypeBridge<?, ?>> list = this.typeBridges.get(original.getId(), destination.getId());
        if (list == null) {
            return Collections.emptyList();
        }
        return (Iterable<EntryTypeBridge<A, B>>) list;
    }
    
    public void reset() {
        entryTypes.clear();
        typeBridges.clear();
    }
}
