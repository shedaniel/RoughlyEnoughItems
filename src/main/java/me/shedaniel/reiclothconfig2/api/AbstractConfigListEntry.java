package me.shedaniel.reiclothconfig2.api;

public abstract class AbstractConfigListEntry<T> extends AbstractConfigEntry<T> {
    private String fieldName;
    private boolean editable = true;
    private boolean requiresRestart;
    //
    //    public AbstractConfigListEntry(String fieldName) {
    //        this(fieldName, false);
    //    }
    
    public AbstractConfigListEntry(String fieldName, boolean requiresRestart) {
        this.fieldName = fieldName;
        this.requiresRestart = requiresRestart;
    }
    
    @Override
    public boolean isRequiresRestart() {
        return requiresRestart;
    }
    
    @Override
    public void setRequiresRestart(boolean requiresRestart) {
        this.requiresRestart = requiresRestart;
    }
    
    public boolean isEditable() {
        return getScreen().isEditable() && editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    @Override
    public String getFieldName() {
        return fieldName;
    }
}
