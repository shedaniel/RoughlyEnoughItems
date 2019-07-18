package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.BaseListEntry;
import me.shedaniel.reiclothconfig2.gui.entries.StringListListEntry;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class StringListBuilder extends FieldBuilder<List<String>, StringListListEntry> {
    
    private Consumer<List<String>> saveConsumer = null;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private List<String> value;
    private boolean expended = false;
    private Function<BaseListEntry, StringListListEntry.StringListCell> createNewInstance;
    
    public StringListBuilder(String resetButtonKey, String fieldNameKey, List<String> value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    public StringListBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public StringListBuilder setCreateNewInstance(Function<BaseListEntry, StringListListEntry.StringListCell> createNewInstance) {
        this.createNewInstance = createNewInstance;
        return this;
    }
    
    public StringListBuilder setExpended(boolean expended) {
        this.expended = expended;
        return this;
    }
    
    public StringListBuilder setSaveConsumer(Consumer<List<String>> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public StringListBuilder setDefaultValue(Supplier<List<String>> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public StringListBuilder setDefaultValue(List<String> defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public StringListBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public StringListBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public StringListBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    @Override
    public StringListListEntry build() {
        StringListListEntry entry = new StringListListEntry(getFieldNameKey(), value, expended, tooltipSupplier, saveConsumer, defaultValue, getResetButtonKey(), isRequireRestart());
        if (createNewInstance != null)
            entry.setCreateNewInstance(createNewInstance);
        return entry;
    }
    
}