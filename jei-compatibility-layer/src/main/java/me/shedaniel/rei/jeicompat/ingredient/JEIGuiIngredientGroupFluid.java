package me.shedaniel.rei.jeicompat.ingredient;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class JEIGuiIngredientGroupFluid extends JEIGuiIngredientGroup<FluidStack> implements IGuiFluidStackGroup {
    public JEIGuiIngredientGroupFluid(IIngredientType<FluidStack> type) {
        super(type);
    }
    
    @Override
    public void init(int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay) {
        init(slotIndex, input, xPosition, yPosition);
        SlotWrapper slot = getSlot(slotIndex);
        slot.slot.getBounds().setSize(width, height);
    }
}
