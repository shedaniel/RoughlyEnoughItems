package me.shedaniel.rei.listeners;

public interface IMixinKeyBinding {
    
    public boolean addCategory(String keyBindingCategory, int id);
    
    public boolean addCategory(String keyBindingCategory);
    
    public boolean hasCategory(String keyCategory);
    
}
