package mezz.jei.api.constants;

import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Built-in {@link IIngredientType} for vanilla Minecraft.
 */
public final class VanillaTypes {
	public static final IIngredientType<ItemStack> ITEM = () -> ItemStack.class;
	public static final IIngredientType<FluidStack> FLUID = () -> FluidStack.class;

	private VanillaTypes() {

	}
}
