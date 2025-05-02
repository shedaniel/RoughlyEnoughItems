package me.shedaniel.rei.plugin.common.displays.grindstone;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalDouble;

public class GrindstoneRecipe {
    @Nullable
    private final ResourceLocation id;
    private final List<ItemStack> topInput;
    private final List<ItemStack> bottomInput;
    private final List<ItemStack> outputs;
    private final OptionalDouble averageXpReward;

    public GrindstoneRecipe(@Nullable ResourceLocation id, List<ItemStack> topInput, List<ItemStack> bottomInput, List<ItemStack> outputs) {
        this(id, topInput, bottomInput, outputs, OptionalDouble.empty());
    }

    public GrindstoneRecipe(@Nullable ResourceLocation id, List<ItemStack> topInput, List<ItemStack> bottomInput, List<ItemStack> outputs, OptionalDouble averageXpReward) {
        this.id = id;
        this.topInput = topInput;
        this.bottomInput = bottomInput;
        this.outputs = outputs;
        this.averageXpReward = averageXpReward;
    }

    public @Nullable ResourceLocation getId() {
        return id;
    }

    public List<ItemStack> getTopInput() {
        return topInput;
    }

    public List<ItemStack> getBottomInput() {
        return bottomInput;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public OptionalDouble getAverageXpReward() {
        return averageXpReward;
    }
}
