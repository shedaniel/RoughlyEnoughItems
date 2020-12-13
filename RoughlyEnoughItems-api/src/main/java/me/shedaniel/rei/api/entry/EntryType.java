package me.shedaniel.rei.api.entry;

import me.shedaniel.rei.impl.Internals;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface EntryType<T> {
    static <T> EntryType<T> deferred(ResourceLocation id) {
        return Internals.deferEntryType(id).cast();
    }
    
    @NotNull
    ResourceLocation getId();
    
    @NotNull
    EntryDefinition<T> getDefinition();
    
    @ApiStatus.NonExtendable
    @NotNull
    default <O> EntryType<O> cast() {
        return (EntryType<O>) this;
    }
}
