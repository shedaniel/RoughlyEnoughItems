package me.shedaniel.rei.api.fluid;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.FluidSupportProviderImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Experimental library, scheduled to change if needed.
 */
@ApiStatus.Experimental
public interface FluidSupportProvider {
    FluidSupportProvider INSTANCE = new FluidSupportProviderImpl();
    
    void registerFluidProvider(@NotNull FluidProvider provider);
    
    @NotNull
    EntryStack fluidToItem(@NotNull EntryStack fluidStack);
    
    @NotNull
    EntryStack itemToFluid(@NotNull EntryStack itemStack);
    
    interface FluidProvider {
        @NotNull
        default EntryStack fluidToItem(@NotNull EntryStack fluidStack) {
            return EntryStack.empty();
        }
        
        @NotNull
        default EntryStack itemToFluid(@NotNull EntryStack itemStack) {
            return EntryStack.empty();
        }
    }
}
