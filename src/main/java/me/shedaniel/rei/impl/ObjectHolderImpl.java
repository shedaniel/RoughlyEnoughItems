/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.shedaniel.rei.api.ObjectHolder;

public class ObjectHolderImpl<T> implements ObjectHolder<T> {
    private Object o;
    
    public ObjectHolderImpl(Object o) {
        this.o = o;
    }
    
    @Override
    public int intValue() {
        return (int) o;
    }
    
    @Override
    public long longValue() {
        return (long) o;
    }
    
    @Override
    public boolean booleanValue() {
        return (boolean) o;
    }
    
    @Override
    public float floatValue() {
        return (float) o;
    }
    
    @Override
    public double doubleValue() {
        return (double) o;
    }
    
    @Override
    public String stringValue() {
        return (String) o;
    }
    
    @Override
    public T value() {
        return (T) o;
    }
}
