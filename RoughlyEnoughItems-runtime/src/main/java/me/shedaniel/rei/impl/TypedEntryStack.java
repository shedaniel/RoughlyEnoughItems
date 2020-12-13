package me.shedaniel.rei.impl;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.entry.BuiltinEntryTypes;
import me.shedaniel.rei.api.entry.ComparisonContext;
import me.shedaniel.rei.api.entry.EntryDefinition;
import me.shedaniel.rei.api.entry.EntryType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@ApiStatus.Internal
public class TypedEntryStack<T> extends AbstractEntryStack<T> {
    public static final EntryStack<Unit> EMPTY = new TypedEntryStack<>(BuiltinEntryTypes.EMPTY, Unit.INSTANCE);
    
    private final EntryDefinition<T> definition;
    private T value;
    
    public TypedEntryStack(EntryType<T> type, T value) {
        this(type.getDefinition(), value);
    }
    
    public TypedEntryStack(EntryDefinition<T> definition, T value) {
        this.definition = definition;
        this.value = value;
    }
    
    @Override
    @NotNull
    public EntryDefinition<T> getDefinition() {
        return definition;
    }
    
    @Override
    public T getValue() {
        return value;
    }
    
    @Override
    public Optional<ResourceLocation> getIdentifier() {
        return getDefinition().getIdentifier(this, value);
    }
    
    @Override
    public Fraction getAmount() {
        return getDefinition().getAmount(this, value);
    }
    
    @Override
    public void setAmount(Fraction amount) {
        getDefinition().setAmount(this, value, amount);
    }
    
    @Override
    public boolean isEmpty() {
        return getDefinition().isEmpty(this, value);
    }
    
    @Override
    public EntryStack<T> copy() {
        TypedEntryStack<T> stack = new TypedEntryStack<>(definition, getDefinition().copy(this, value));
        for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
            stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public EntryStack<T> rewrap() {
        TypedEntryStack<T> stack = new TypedEntryStack<>(definition, value);
        for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
            stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public boolean equals(EntryStack<T> other, ComparisonContext context) {
        return this.getDefinition().equals(value, other.getValue(), context);
    }
    
    @Override
    public int hash(ComparisonContext context) {
        return getDefinition().hash(this, value, context);
    }
    
    @Override
    public @NotNull Component asFormattedText() {
        return getDefinition().asFormattedText(this, value);
    }
}
