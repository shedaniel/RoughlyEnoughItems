package me.shedaniel.rei.api.entry;

import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.rei.api.EntryStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface EntryDefinition<T> {
    @NotNull
    Class<T> getValueType();
    
    @NotNull
    EntryType<T> getType();
    
    @NotNull
    EntryRenderer<T> getRenderer();
    
    @NotNull
    Optional<ResourceLocation> getIdentifier(EntryStack<T> entry, T value);
    
    @NotNull
    Fraction getAmount(EntryStack<T> entry, T value);
    
    void setAmount(EntryStack<T> entry, T value, Fraction amount);
    
    boolean isEmpty(EntryStack<T> entry, T value);
    
    @NotNull
    T copy(EntryStack<T> entry, T value);
    
    int hash(EntryStack<T> entry, T value, ComparisonContext context);
    
    boolean equals(T o1, T o2, ComparisonContext context);
    
    @NotNull
    CompoundTag toTag(EntryStack<T> entry, T value);
    
    @NotNull
    T fromTag(@NotNull CompoundTag tag);
    
    @NotNull
    Component asFormattedText(EntryStack<T> entry, T value);
    
    @ApiStatus.NonExtendable
    @NotNull
    default <O> EntryDefinition<O> cast() {
        return (EntryDefinition<O>) this;
    }
}

