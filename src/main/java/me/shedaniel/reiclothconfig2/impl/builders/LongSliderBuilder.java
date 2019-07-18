package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.LongSliderEntry;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LongSliderBuilder extends FieldBuilder<Long, LongSliderEntry> {
    
    private Consumer<Long> saveConsumer = null;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private long value, max, min;
    private Function<Long, String> textGetter = null;
    
    public LongSliderBuilder(String resetButtonKey, String fieldNameKey, long value, long min, long max) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
        this.max = max;
        this.min = min;
    }
    
    public LongSliderBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public LongSliderBuilder setTextGetter(Function<Long, String> textGetter) {
        this.textGetter = textGetter;
        return this;
    }
    
    public LongSliderBuilder setSaveConsumer(Consumer<Long> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public LongSliderBuilder setDefaultValue(Supplier<Long> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public LongSliderBuilder setDefaultValue(long defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public LongSliderBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public LongSliderBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public LongSliderBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    @Override
    public LongSliderEntry build() {
        if (textGetter == null)
            return new LongSliderEntry(getFieldNameKey(), min, max, value, saveConsumer, getResetButtonKey(), defaultValue, tooltipSupplier, isRequireRestart());
        return new LongSliderEntry(getFieldNameKey(), min, max, value, saveConsumer, getResetButtonKey(), defaultValue, tooltipSupplier, isRequireRestart()).setTextGetter(textGetter);
    }
    
}