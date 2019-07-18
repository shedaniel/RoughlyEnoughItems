package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.BaseListEntry;
import me.shedaniel.reiclothconfig2.gui.entries.DoubleListListEntry;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DoubleListBuilder extends FieldBuilder<List<Double>, DoubleListListEntry> {
    
    private Consumer<List<Double>> saveConsumer = null;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private List<Double> value;
    private boolean expended = false;
    private Double min = null, max = null;
    private Function<BaseListEntry, DoubleListListEntry.DoubleListCell> createNewInstance;
    
    public DoubleListBuilder(String resetButtonKey, String fieldNameKey, List<Double> value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    public DoubleListBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public DoubleListBuilder setCreateNewInstance(Function<BaseListEntry, DoubleListListEntry.DoubleListCell> createNewInstance) {
        this.createNewInstance = createNewInstance;
        return this;
    }
    
    public DoubleListBuilder setExpended(boolean expended) {
        this.expended = expended;
        return this;
    }
    
    public DoubleListBuilder setSaveConsumer(Consumer<List<Double>> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public DoubleListBuilder setDefaultValue(Supplier<List<Double>> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public DoubleListBuilder setMin(double min) {
        this.min = min;
        return this;
    }
    
    public DoubleListBuilder setMax(double max) {
        this.max = max;
        return this;
    }
    
    public DoubleListBuilder removeMin() {
        this.min = null;
        return this;
    }
    
    public DoubleListBuilder removeMax() {
        this.max = null;
        return this;
    }
    
    public DoubleListBuilder setDefaultValue(List<Double> defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public DoubleListBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public DoubleListBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public DoubleListBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    @Override
    public DoubleListListEntry build() {
        DoubleListListEntry entry = new DoubleListListEntry(getFieldNameKey(), value, expended, tooltipSupplier, saveConsumer, defaultValue, getResetButtonKey(), isRequireRestart());
        if (min != null)
            entry.setMinimum(min);
        if (max != null)
            entry.setMaximum(max);
        if (createNewInstance != null)
            entry.setCreateNewInstance(createNewInstance);
        return entry;
    }
    
}