package me.shedaniel.rei.impl.client;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.CodepointMap;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public class CodepointMapWrapper<T> extends CodepointMap<T> {
    private final CodepointMap<T> delegate;
    protected transient IntSet keys;
    
    public CodepointMapWrapper(CodepointMap<T> delegate) {
        super(delegate.blockConstructor, i -> delegate.blockMap);
        this.empty = delegate.empty;
        this.blockMap = delegate.blockMap;
        this.delegate = delegate;
    }
    
    @Override
    public void clear() {
        synchronized (this) {
            delegate.clear();
        }
    }
    
    @Nullable
    @Override
    public T put(int i, T object) {
        synchronized (this) {
            return delegate.put(i, object);
        }
    }
    
    @Nullable
    @Override
    public T get(int i) {
        synchronized (this) {
            return delegate.get(i);
        }
    }
    
    @Override
    public T computeIfAbsent(int i, IntFunction<T> intFunction) {
        synchronized (this) {
            return delegate.computeIfAbsent(i, intFunction);
        }
    }
    
    @Nullable
    @Override
    public T remove(int i) {
        synchronized (this) {
            return delegate.remove(i);
        }
    }
    
    @Override
    public void forEach(Output<T> arg) {
        synchronized (this) {
            delegate.forEach(arg);
        }
    }
    
    @Override
    public IntSet keySet() {
        return delegate.keySet();
    }
}
