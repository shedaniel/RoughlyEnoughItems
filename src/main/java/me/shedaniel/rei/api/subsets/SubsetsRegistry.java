package me.shedaniel.rei.api.subsets;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.subsets.SubsetsRegistryImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@ApiStatus.Experimental
public interface SubsetsRegistry {
    SubsetsRegistry INSTANCE = new SubsetsRegistryImpl();
    
    /**
     * Gets all paths an entry is in, note that this is a really slow call as it looks through all paths.
     */
    @NotNull
    List<String> getEntryPaths(@NotNull EntryStack stack);
    
    @Nullable
    Set<EntryStack> getPathEntries(@NotNull String path);
    
    @NotNull
    Set<EntryStack> getOrCreatePathEntries(@NotNull String path);
    
    @NotNull
    Set<String> getPaths();
    
    void registerPathEntry(@NotNull String path, @NotNull EntryStack stack);
    
    void registerPathEntries(@NotNull String path, @NotNull Collection<EntryStack> stacks);
    
    default void registerPathEntries(@NotNull String path, @NotNull EntryStack... stacks) {
        registerPathEntries(path, Arrays.asList(stacks));
    }
}
