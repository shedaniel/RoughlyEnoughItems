package me.shedaniel.rei.impl;

import me.shedaniel.rei.api.entry.EntryDefinition;
import me.shedaniel.rei.api.entry.EntryType;
import me.shedaniel.rei.api.entry.EntryTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

@ApiStatus.Internal
public class EntryTypeDeferred<T> implements EntryType<T> {
    private final ResourceLocation id;
    private AtomicReference<WeakReference<EntryDefinition<T>>> value = new AtomicReference<>();
    
    public EntryTypeDeferred(ResourceLocation id) {
        this.id = id;
    }
    
    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }
    
    @Override
    public @NotNull EntryDefinition<T> getDefinition() {
        WeakReference<EntryDefinition<T>> reference = value.get();
        if (reference != null) {
            EntryDefinition<T> definition = reference.get();
            if (definition != null) {
                return definition;
            }
        }
        EntryDefinition<?> d = EntryTypeRegistry.getInstance().get(id);
        if (d == null)
            throw new NullPointerException("Entry type " + id + " doesn't exist!");
        EntryDefinition<T> definition = d.cast();
        value.set(new WeakReference<>(definition));
        return definition;
    }
}
