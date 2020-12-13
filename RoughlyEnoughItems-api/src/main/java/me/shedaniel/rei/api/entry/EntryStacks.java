package me.shedaniel.rei.api.entry;

import com.google.common.collect.ImmutableList;
import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.rei.api.EntryStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.stream.Stream;

public final class EntryStacks {
    private EntryStacks() {}
    
    public static EntryStack<FluidStack> of(Fluid fluid) {
        return of(fluid, FluidStack.bucketAmount());
    }
    
    public static EntryStack<FluidStack> of(Fluid fluid, int amount) {
        return of(fluid, Fraction.ofWhole(amount));
    }
    
    public static EntryStack<FluidStack> of(Fluid fluid, double amount) {
        return of(fluid, Fraction.from(amount));
    }
    
    public static EntryStack<FluidStack> of(Fluid fluid, Fraction amount) {
        return EntryStack.of(VanillaEntryTypes.FLUID, FluidStack.create(fluid, amount));
    }
    
    public static EntryStack<ItemStack> of(ItemStack stack) {
        return EntryStack.of(VanillaEntryTypes.ITEM, stack);
    }
    
    public static EntryStack<ItemStack> of(ItemLike item) {
        return of(new ItemStack(item));
    }
    
    public static List<EntryStack<ItemStack>> ofItems(Collection<ItemLike> stacks) {
        if (stacks.size() == 0) return Collections.emptyList();
        if (stacks.size() == 1) return Collections.singletonList(of(stacks.iterator().next()));
        EntryStack<ItemStack>[] result = new EntryStack[stacks.size()];
        int i = 0;
        for (ItemLike stack : stacks) {
            result[i] = of(stack);
            i++;
        }
        return Arrays.asList(result);
    }
    
    public static List<EntryStack<ItemStack>> ofItemStacks(Collection<ItemStack> stacks) {
        if (stacks.size() == 0) return Collections.emptyList();
        if (stacks.size() == 1) {
            ItemStack stack = stacks.iterator().next();
            if (stack.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(of(stack));
        }
        List<EntryStack<ItemStack>> result = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            result.add(of(stack));
        }
        return ImmutableList.copyOf(result);
    }
    
    public static List<EntryStack<ItemStack>> ofIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) return Collections.emptyList();
        ItemStack[] matchingStacks = ingredient.getItems();
        if (matchingStacks.length == 0) return Collections.emptyList();
        if (matchingStacks.length == 1) return Collections.singletonList(of(matchingStacks[0]));
        List<EntryStack<ItemStack>> result = new ArrayList<>(matchingStacks.length);
        for (ItemStack matchingStack : matchingStacks) {
            if (!matchingStack.isEmpty())
                result.add(of(matchingStack));
        }
        return ImmutableList.copyOf(result);
    }
    
    public static List<List<EntryStack<ItemStack>>> ofIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() == 0) return Collections.emptyList();
        if (ingredients.size() == 1) {
            Ingredient ingredient = ingredients.get(0);
            if (ingredient.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(ofIngredient(ingredient));
        }
        boolean emptyFlag = true;
        List<List<EntryStack<ItemStack>>> result = new ArrayList<>(ingredients.size());
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);
            if (emptyFlag && ingredient.isEmpty()) continue;
            result.add(0, ofIngredient(ingredient));
            emptyFlag = false;
        }
        return ImmutableList.copyOf(result);
    }
    
    public static <A, B> boolean equals(EntryStack<A> left, EntryStack<B> right, ComparisonContext context) {
        if (left == null) return right == null;
        if (right == null) return left == null;
        if (left == right) return true;
        EntryType<A> leftType = left.getType();
        EntryType<B> rightType = right.getType();
        if (leftType == rightType)
            return left.equals((EntryStack<A>) right, context);
        for (EntryTypeBridge<A, B> bridge : EntryTypeRegistry.getInstance().getBridgesFor(leftType, rightType)) {
            InteractionResultHolder<Stream<EntryStack<B>>> holder = bridge.bridge(left);
            if (holder.getResult().shouldSwing()) {
                Iterator<EntryStack<B>> iterator = holder.getObject().iterator();
                while (iterator.hasNext()) {
                    EntryStack<B> bridged = iterator.next();
                    if (bridged.equals(right, context))
                        return true;
                }
            }
        }
        for (EntryTypeBridge<B, A> bridge : EntryTypeRegistry.getInstance().getBridgesFor(rightType, leftType)) {
            InteractionResultHolder<Stream<EntryStack<A>>> holder = bridge.bridge(right);
            if (holder.getResult().shouldSwing()) {
                Iterator<EntryStack<A>> iterator = holder.getObject().iterator();
                while (iterator.hasNext()) {
                    EntryStack<A> bridged = iterator.next();
                    if (bridged.equals(left, context))
                        return true;
                }
            }
        }
        return false;
    }
    
    public static <A, B> boolean equalsExact(EntryStack<A> left, EntryStack<B> right) {
        return equals(left, right, ComparisonContext.EXACT);
    }
    
    public static <A, B> boolean equalsFuzzy(EntryStack<A> left, EntryStack<B> right) {
        return equals(left, right, ComparisonContext.FUZZY);
    }
    
    public static <A, B> boolean equalsIgnoreCount(EntryStack<A> left, EntryStack<B> right) {
        return equals(left, right, ComparisonContext.IGNORE_COUNT);
    }
    
    public static <A, B> boolean equalsIgnoreNbt(EntryStack<A> left, EntryStack<B> right) {
        return equals(left, right, ComparisonContext.IGNORE_NBT);
    }
    
    public static <T> int hashExact(EntryStack<T> stack) {
        return stack.hash(ComparisonContext.EXACT);
    }
    
    public static <T> int hashFuzzy(EntryStack<T> stack) {
        return stack.hash(ComparisonContext.FUZZY);
    }
    
    public static <T> int hashIgnoreCount(EntryStack<T> stack) {
        return stack.hash(ComparisonContext.IGNORE_COUNT);
    }
    
    public static <T> int hashIgnoreNbt(EntryStack<T> stack) {
        return stack.hash(ComparisonContext.IGNORE_NBT);
    }
}
