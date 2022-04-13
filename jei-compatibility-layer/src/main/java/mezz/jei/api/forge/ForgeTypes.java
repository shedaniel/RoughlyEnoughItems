package mezz.jei.api.forge;

import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Built-in {@link IIngredientType} for Forge Minecraft.
 *
 * @since 9.6.0
 */
public final class ForgeTypes {
    /**
     * @since 9.7.0
     */
    public static final IIngredientTypeWithSubtypes<Fluid, FluidStack> FLUID_STACK = (IIngredientTypeWithSubtypes<Fluid, FluidStack>) JEIPluginDetector.jeiType(FluidStack.class);
    
    /**
     * @since 9.6.0
     * @deprecated use {@link #FLUID_STACK}
     */
    @Deprecated(forRemoval = true, since = "9.7.0")
    public static final IIngredientType<FluidStack> FLUID = FLUID_STACK;
    
    private ForgeTypes() {}
}
