package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.BaseListEntry;
import me.shedaniel.reiclothconfig2.gui.entries.FloatListListEntry;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FloatListBuilder extends FieldBuilder<List<Float>, FloatListListEntry> {
    
    private Consumer<List<Float>> saveConsumer = null;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private List<Float> value;
    private boolean expended = false;
    private Float min = null, max = null;
    private Function<BaseListEntry, FloatListListEntry.FloatListCell> createNewInstance;
    
    public FloatListBuilder(String resetButtonKey, String fieldNameKey, List<Float> value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    public FloatListBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public FloatListBuilder setCreateNewInstance(Function<BaseListEntry, FloatListListEntry.FloatListCell> createNewInstance) {
        this.createNewInstance = createNewInstance;
        return this;
    }
    
    public FloatListBuilder setExpended(boolean expended) {
        this.expended = expended;
        return this;
    }
    
    public FloatListBuilder setSaveConsumer(Consumer<List<Float>> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public FloatListBuilder setDefaultValue(Supplier<List<Float>> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public FloatListBuilder setMin(float min) {
        this.min = min;
        return this;
    }
    
    public FloatListBuilder setMax(float max) {
        this.max = max;
        return this;
    }
    
    public FloatListBuilder removeMin() {
        this.min = null;
        return this;
    }
    
    public FloatListBuilder removeMax() {
        this.max = null;
        return this;
    }
    
    public FloatListBuilder setDefaultValue(List<Float> defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public FloatListBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public FloatListBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public FloatListBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    @Override
    public FloatListListEntry build() {
        FloatListListEntry entry = new FloatListListEntry(getFieldNameKey(), value, expended, tooltipSupplier, saveConsumer, defaultValue, getResetButtonKey(), isRequireRestart());
        if (min != null)
            entry.setMinimum(min);
        if (max != null)
            entry.setMaximum(max);
        if (createNewInstance != null)
            entry.setCreateNewInstance(createNewInstance);
        return entry;
    }
    
}