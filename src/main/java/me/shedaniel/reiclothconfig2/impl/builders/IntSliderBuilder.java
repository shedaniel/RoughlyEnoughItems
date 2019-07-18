package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.IntegerSliderEntry;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class IntSliderBuilder extends FieldBuilder<Integer, IntegerSliderEntry> {
    
    private Consumer<Integer> saveConsumer = null;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private int value, max, min;
    private Function<Integer, String> textGetter = null;
    
    public IntSliderBuilder(String resetButtonKey, String fieldNameKey, int value, int min, int max) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
        this.max = max;
        this.min = min;
    }
    
    public IntSliderBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public IntSliderBuilder setTextGetter(Function<Integer, String> textGetter) {
        this.textGetter = textGetter;
        return this;
    }
    
    public IntSliderBuilder setSaveConsumer(Consumer<Integer> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public IntSliderBuilder setDefaultValue(Supplier<Integer> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public IntSliderBuilder setDefaultValue(int defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public IntSliderBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public IntSliderBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public IntSliderBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    public IntSliderBuilder setMax(int max) {
        this.max = max;
        return this;
    }
    
    public IntSliderBuilder setMin(int min) {
        this.min = min;
        return this;
    }
    
    @Override
    public IntegerSliderEntry build() {
        if (textGetter == null)
            return new IntegerSliderEntry(getFieldNameKey(), min, max, value, getResetButtonKey(), defaultValue, saveConsumer, tooltipSupplier, isRequireRestart());
        return new IntegerSliderEntry(getFieldNameKey(), min, max, value, getResetButtonKey(), defaultValue, saveConsumer, tooltipSupplier, isRequireRestart()).setTextGetter(textGetter);
    }
    
}