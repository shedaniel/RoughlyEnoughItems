package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.EnumListEntry;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnumSelectorBuilder<T extends Enum<?>> extends FieldBuilder<T, EnumListEntry<T>> {
    
    private Consumer<T> saveConsumer = null;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private T value;
    private Class<T> clazz;
    private Function<Enum, String> enumNameProvider = EnumListEntry.DEFAULT_NAME_PROVIDER;
    
    public EnumSelectorBuilder(String resetButtonKey, String fieldNameKey, Class<T> clazz, T value) {
        super(resetButtonKey, fieldNameKey);
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(value);
        this.value = value;
        this.clazz = clazz;
    }
    
    public EnumSelectorBuilder<T> requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public EnumSelectorBuilder setSaveConsumer(Consumer<T> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public EnumSelectorBuilder setDefaultValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public EnumSelectorBuilder setDefaultValue(T defaultValue) {
        Objects.requireNonNull(defaultValue);
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public EnumSelectorBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public EnumSelectorBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public EnumSelectorBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    public EnumSelectorBuilder setEnumNameProvider(Function<Enum, String> enumNameProvider) {
        Objects.requireNonNull(enumNameProvider);
        this.enumNameProvider = enumNameProvider;
        return this;
    }
    
    @Override
    public EnumListEntry<T> build() {
        return new EnumListEntry<T>(getFieldNameKey(), clazz, value, getResetButtonKey(), defaultValue, saveConsumer, enumNameProvider, tooltipSupplier, isRequireRestart());
    }
    
}