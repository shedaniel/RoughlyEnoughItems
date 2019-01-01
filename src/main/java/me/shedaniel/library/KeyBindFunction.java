package me.shedaniel.library;

import java.util.function.Function;

public abstract class KeyBindFunction {
    
    public KeyBindFunction(int key) {
        this.key = key;
    }
    
    private int key;
    
    public void setKey(int key) {
        this.key = key;
    }
    
    public int getKey() {
        return key;
    }
    
    public abstract boolean apply(int key);
}
