package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.IntegerListEntry;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IntFieldBuilder extends FieldBuilder<Integer, IntegerListEntry> {
    
    private Consumer<Integer> saveConsumer = null;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private int value;
    private Integer min = null, max = null;
    
    public IntFieldBuilder(String resetButtonKey, String fieldNameKey, int value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    public IntFieldBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public IntFieldBuilder setSaveConsumer(Consumer<Integer> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public IntFieldBuilder setDefaultValue(Supplier<Integer> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public IntFieldBuilder setDefaultValue(int defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public IntFieldBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public IntFieldBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public IntFieldBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    public IntFieldBuilder setMin(int min) {
        this.min = min;
        return this;
    }
    
    public IntFieldBuilder setMax(int max) {
        this.max = max;
        return this;
    }
    
    public IntFieldBuilder removeMin() {
        this.min = null;
        return this;
    }
    
    public IntFieldBuilder removeMax() {
        this.max = null;
        return this;
    }
    
    @Override
    public IntegerListEntry build() {
        IntegerListEntry entry = new IntegerListEntry(getFieldNameKey(), value, getResetButtonKey(), defaultValue, saveConsumer, tooltipSupplier, isRequireRestart());
        if (min != null)
            entry.setMinimum(min);
        if (max != null)
            entry.setMaximum(max);
        return entry;
    }
    
}