package me.shedaniel.rei.listeners;

public interface KeyBindingHooks {
    
    public boolean rei_addCategory(String keyBindingCategory, int id);
    
    public boolean rei_addCategory(String keyBindingCategory);
    
    public boolean rei_hasCategory(String keyCategory);
    
}
