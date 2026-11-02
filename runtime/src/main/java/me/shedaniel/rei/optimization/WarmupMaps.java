package me.shedaniel.rei.optimization;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/** Keep tiny chunks compact; avoid repeated linear duplicate searches in larger chunks. */
public final class WarmupMaps {
    private WarmupMaps() {}
    public static <T> Long2ObjectMap<T> create(int size) {
        // Reserve a free probe slot explicitly. A dense table avoids the default load
        // factor's capacity doubling for the normal 100-entry partition.
        return size <= 16 ? new Long2ObjectArrayMap<>(size + 1) : new Long2ObjectOpenHashMap<>(size + 1, 0.99f);
    }
}
