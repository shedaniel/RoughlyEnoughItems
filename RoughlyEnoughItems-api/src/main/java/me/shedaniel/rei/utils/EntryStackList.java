package me.shedaniel.rei.utils;

import me.shedaniel.rei.api.EntryStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class EntryStackList extends ArrayList<EntryStack<?>> {
    public EntryStackList(int initialCapacity) {
        super(initialCapacity);
    }
    
    public EntryStackList() {
    }
    
    public EntryStackList(@NotNull Collection<? extends EntryStack<?>> c) {
        super(c);
    }
}
