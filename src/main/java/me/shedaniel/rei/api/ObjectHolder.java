/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.impl.ObjectHolderImpl;
import org.jetbrains.annotations.ApiStatus;

public interface ObjectHolder<T> {
    @SuppressWarnings("deprecation")
    static <T> ObjectHolder<T> of(T o) {
        return new ObjectHolderImpl<>(o);
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default int intValue() {
        return (int) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default long longValue() {
        return (long) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default boolean booleanValue() {
        return (boolean) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default float floatValue() {
        return (float) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default double doubleValue() {
        return (double) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default String stringValue() {
        return (String) value();
    }
    
    T value();
}