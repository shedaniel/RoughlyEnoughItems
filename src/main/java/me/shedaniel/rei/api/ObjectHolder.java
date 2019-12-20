/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.impl.ObjectHolderImpl;

public interface ObjectHolder<T> {
    @SuppressWarnings("deprecation")
    static <T> ObjectHolder<T> of(T o) {
        return new ObjectHolderImpl<>(o);
    }
    
    @Deprecated
    default int intValue() {
        return (int) (Object) value();
    }
    
    @Deprecated
    default long longValue() {
        return (long) (Object) value();
    }
    
    @Deprecated
    default boolean booleanValue() {
        return (boolean) (Object) value();
    }
    
    @Deprecated
    default float floatValue() {
        return (float) (Object) value();
    }
    
    @Deprecated
    default double doubleValue() {
        return (double) (Object) value();
    }
    
    @Deprecated
    default String stringValue() {
        return (String) value();
    }
    
    T value();
}