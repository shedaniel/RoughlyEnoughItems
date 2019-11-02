package me.shedaniel.rei.api;

public interface ObjectHolder<T> {
    int intValue();
    
    long longValue();
    
    boolean booleanValue();
    
    float floatValue();
    
    double doubleValue();
    
    String stringValue();
    
    T value();
}