package me.shedaniel.reiclothconfig2.impl.builders;

import me.shedaniel.reiclothconfig2.api.AbstractConfigListEntry;

import java.util.function.Supplier;

public abstract class FieldBuilder<T, A extends AbstractConfigListEntry> {
    private final String fieldNameKey;
    private final String resetButtonKey;
    protected boolean requireRestart = false;
    protected Supplier<T> defaultValue = null;
    
    protected FieldBuilder(String resetButtonKey, String fieldNameKey) {
        this.resetButtonKey = resetButtonKey;
        this.fieldNameKey = fieldNameKey;
    }
    
    public final Supplier<T> getDefaultValue() {
        return defaultValue;
    }
    
    public final AbstractConfigListEntry buildEntry() {
        return build();
    }
    
    public abstract A build();
    
    public final String getFieldNameKey() {
        return fieldNameKey;
    }
    
    public final String getResetButtonKey() {
        return resetButtonKey;
    }
    
    public boolean isRequireRestart() {
        return requireRestart;
    }
    
    public void requireRestart(boolean requireRestart) {
        this.requireRestart = requireRestart;
    }
    
}