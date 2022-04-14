package mezz.jei.api.constants;

import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Built-in {@link IIngredientType} for vanilla Minecraft.
 */
public final class VanillaTypes {
    /**
     * @since 9.7.0
     */
    public static final IIngredientTypeWithSubtypes<Item, ItemStack> ITEM_STACK = (IIngredientTypeWithSubtypes<Item, ItemStack>) JEIPluginDetector.jeiType(ItemStack.class);
    
    /**
     * @deprecated use {@link #ITEM_STACK}
     */
    @Deprecated(forRemoval = true, since = "9.7.0")
    public static final IIngredientType<ItemStack> ITEM = ITEM_STACK;
    
    /**
     * @deprecated use {@link ForgeTypes#FLUID_STACK}
     */
    @Deprecated(forRemoval = true, since = "9.6.0")
    public static final IIngredientType<FluidStack> FLUID = ForgeTypes.FLUID_STACK;
    
    private VanillaTypes() {
    }
}
