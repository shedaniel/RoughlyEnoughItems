package me.shedaniel.rei.optimization;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;

/** Matches the original wrapper's equality: only its cached exact hash participates. */
public final class GroupHashes {
    private GroupHashes() {}
    public static LongSet create(List<? extends HashedEntryStackWrapper> entries) {
        LongSet hashes = new LongOpenHashSet(entries.size());
        for (HashedEntryStackWrapper entry : entries) if (entry != null) hashes.add(entry.hashExact());
        return hashes;
    }
}
