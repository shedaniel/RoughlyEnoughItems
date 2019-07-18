package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.StringListEntry;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextFieldBuilder extends FieldBuilder<String, StringListEntry> {
    
    private Consumer<String> saveConsumer = null;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private String value;
    
    public TextFieldBuilder(String resetButtonKey, String fieldNameKey, String value) {
        super(resetButtonKey, fieldNameKey);
        Objects.requireNonNull(value);
        this.value = value;
    }
    
    public TextFieldBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public TextFieldBuilder setSaveConsumer(Consumer<String> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public TextFieldBuilder setDefaultValue(Supplier<String> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public TextFieldBuilder setDefaultValue(String defaultValue) {
        this.defaultValue = () -> Objects.requireNonNull(defaultValue);
        return this;
    }
    
    public TextFieldBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public TextFieldBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public TextFieldBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    @Override
    public StringListEntry build() {
        return new StringListEntry(getFieldNameKey(), value, getResetButtonKey(), defaultValue, saveConsumer, tooltipSupplier, isRequireRestart());
    }
    
}