package me.shedaniel.rei.utils;

import me.shedaniel.rei.api.EntryStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntryStackCompoundList extends ArrayList<List<? extends EntryStack<?>>> {
    public EntryStackCompoundList(int initialCapacity) {
        super(initialCapacity);
    }
    
    public EntryStackCompoundList() {
    }
    
    public EntryStackCompoundList(@NotNull Collection<? extends List<EntryStack<?>>> c) {
        super(c);
    }
}
