package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.BaseListEntry;
import me.shedaniel.reiclothconfig2.gui.entries.LongListListEntry;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LongListBuilder extends FieldBuilder<List<Long>, LongListListEntry> {
    
    private Consumer<List<Long>> saveConsumer = null;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private List<Long> value;
    private boolean expended = false;
    private Long min = null, max = null;
    private Function<BaseListEntry, LongListListEntry.LongListCell> createNewInstance;
    
    public LongListBuilder(String resetButtonKey, String fieldNameKey, List<Long> value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    public LongListBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public LongListBuilder setCreateNewInstance(Function<BaseListEntry, LongListListEntry.LongListCell> createNewInstance) {
        this.createNewInstance = createNewInstance;
        return this;
    }
    
    public LongListBuilder setExpended(boolean expended) {
        this.expended = expended;
        return this;
    }
    
    public LongListBuilder setSaveConsumer(Consumer<List<Long>> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public LongListBuilder setDefaultValue(Supplier<List<Long>> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public LongListBuilder setMin(long min) {
        this.min = min;
        return this;
    }
    
    public LongListBuilder setMax(long max) {
        this.max = max;
        return this;
    }
    
    public LongListBuilder removeMin() {
        this.min = null;
        return this;
    }
    
    public LongListBuilder removeMax() {
        this.max = null;
        return this;
    }
    
    public LongListBuilder setDefaultValue(List<Long> defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public LongListBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public LongListBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public LongListBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    @Override
    public LongListListEntry build() {
        LongListListEntry entry = new LongListListEntry(getFieldNameKey(), value, expended, tooltipSupplier, saveConsumer, defaultValue, getResetButtonKey(), isRequireRestart());
        if (min != null)
            entry.setMinimum(min);
        if (max != null)
            entry.setMaximum(max);
        if (createNewInstance != null)
            entry.setCreateNewInstance(createNewInstance);
        return entry;
    }
    
}