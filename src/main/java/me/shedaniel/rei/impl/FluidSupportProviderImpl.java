package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@ApiStatus.Experimental
@ApiStatus.Internal
public class FluidSupportProviderImpl implements FluidSupportProvider {
    private final List<FluidProvider> providers = Lists.newArrayList();
    
    public void reset() {
        providers.clear();
    }
    
    @Override
    public void registerFluidProvider(@NotNull FluidProvider provider) {
        providers.add(Objects.requireNonNull(provider, "Registered provider is null!"));
    }
    
    @Override
    public @NotNull EntryStack fluidToItem(@NotNull EntryStack fluidStack) {
        if (fluidStack.isEmpty()) return EntryStack.empty();
        if (fluidStack.getType() != EntryStack.Type.FLUID)
            throw new IllegalArgumentException("EntryStack must be fluid!");
        for (FluidProvider provider : providers) {
            EntryStack stack = Objects.requireNonNull(provider.fluidToItem(fluidStack), provider.getClass() + " is creating null objects for fluidToItem!");
            if (!stack.isEmpty()) return stack;
        }
        return EntryStack.empty();
    }
    
    @Override
    public @NotNull EntryStack itemToFluid(@NotNull EntryStack itemStack) {
        if (itemStack.isEmpty()) return EntryStack.empty();
        if (itemStack.getType() != EntryStack.Type.ITEM)
            throw new IllegalArgumentException("EntryStack must be item!");
        for (FluidProvider provider : providers) {
            EntryStack stack = Objects.requireNonNull(provider.itemToFluid(itemStack), provider.getClass() + " is creating null objects for itemToFluid!");
            if (!stack.isEmpty()) return stack;
        }
        return EntryStack.empty();
    }
}
