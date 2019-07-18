package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.gui.entries.TextListEntry;

import java.util.Optional;
import java.util.function.Supplier;

public class TextDescriptionBuilder extends FieldBuilder<String, TextListEntry> {
    
    private int color = -1;
    private Supplier<Optional<String[]>> tooltipSupplier = null;
    private String value;
    
    public TextDescriptionBuilder(String resetButtonKey, String fieldNameKey, String value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    @Override
    public void requireRestart(boolean requireRestart) {
        throw new UnsupportedOperationException();
    }
    
    public TextDescriptionBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public TextDescriptionBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = () -> tooltip;
        return this;
    }
    
    public TextDescriptionBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = () -> Optional.ofNullable(tooltip);
        return this;
    }
    
    public TextDescriptionBuilder setColor(int color) {
        this.color = color;
        return this;
    }
    
    @Override
    public TextListEntry build() {
        return new TextListEntry(getFieldNameKey(), value, color, tooltipSupplier);
    }
    
}