/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.shedaniel.rei.api.ObjectHolder;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ObjectHolderImpl<T> implements ObjectHolder<T> {
    private Object o;
    
    public ObjectHolderImpl(Object o) {
        this.o = o;
    }
    
    @Override
    public T value() {
        return (T) o;
    }
}
