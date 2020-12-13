package me.shedaniel.rei.api.entry;

import me.shedaniel.rei.impl.Internals;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface EntryTypeRegistry {
    @NotNull
    static EntryTypeRegistry getInstance() {
        return Internals.getEntryTypeRegistry();
    }
    
    default <T> void register(EntryType<T> id, EntryDefinition<T> definition) {
        register(id.getId(), definition);
    }
    
    <T> void register(ResourceLocation id, EntryDefinition<T> definition);
    
    <A, B> void registerBridge(EntryType<A> original, EntryType<B> destination, EntryTypeBridge<A, B> bridge);
    
    EntryDefinition<?> get(ResourceLocation id);
    
    <A, B> Iterable<EntryTypeBridge<A, B>> getBridgesFor(EntryType<A> original, EntryType<B> destination);
}