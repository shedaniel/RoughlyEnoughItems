/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.api;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.fractions.Fraction;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public interface EntryStack extends TextRepresentable {
    
    static EntryStack empty() {
        return Internals.getEntryStackProvider().empty();
    }
    
    static EntryStack create(Fluid fluid) {
        return Internals.getEntryStackProvider().fluid(fluid);
    }
    
    static EntryStack create(Fluid fluid, int amount) {
        return create(fluid, Fraction.ofWhole(amount));
    }
    
    static EntryStack create(Fluid fluid, double amount) {
        return create(fluid, Fraction.from(amount));
    }
    
    static EntryStack create(Fluid fluid, Fraction amount) {
        return Internals.getEntryStackProvider().fluid(fluid, amount);
    }
    
    static EntryStack create(ItemStack stack) {
        return Internals.getEntryStackProvider().item(stack);
    }
    
    static EntryStack create(ItemLike item) {
        return create(new ItemStack(item));
    }
    
    static List<EntryStack> ofItems(Collection<ItemLike> stacks) {
        if (stacks.size() == 0) return Collections.emptyList();
        if (stacks.size() == 1) return Collections.singletonList(create(stacks.iterator().next()));
        EntryStack[] result = new EntryStack[stacks.size()];
        int i = 0;
        for (ItemLike stack : stacks) {
            result[i] = create(stack);
            i++;
        }
        return Arrays.asList(result);
    }
    
    static List<EntryStack> ofItemStacks(Collection<ItemStack> stacks) {
        if (stacks.size() == 0) return Collections.emptyList();
        if (stacks.size() == 1) {
            ItemStack stack = stacks.iterator().next();
            if (stack.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(create(stack));
        }
        List<EntryStack> result = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            result.add(create(stack));
        }
        return ImmutableList.copyOf(result);
    }
    
    static List<EntryStack> ofIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) return Collections.emptyList();
        ItemStack[] matchingStacks = ingredient.getItems();
        if (matchingStacks.length == 0) return Collections.emptyList();
        if (matchingStacks.length == 1) return Collections.singletonList(create(matchingStacks[0]));
        List<EntryStack> result = new ArrayList<>(matchingStacks.length);
        for (ItemStack matchingStack : matchingStacks) {
            if (!matchingStack.isEmpty())
                result.add(create(matchingStack));
        }
        return ImmutableList.copyOf(result);
    }
    
    static List<List<EntryStack>> ofIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() == 0) return Collections.emptyList();
        if (ingredients.size() == 1) {
            Ingredient ingredient = ingredients.get(0);
            if (ingredient.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(ofIngredient(ingredient));
        }
        boolean emptyFlag = true;
        List<List<EntryStack>> result = new ArrayList<>(ingredients.size());
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);
            if (emptyFlag && ingredient.isEmpty()) continue;
            result.add(0, ofIngredient(ingredient));
            emptyFlag = false;
        }
        return ImmutableList.copyOf(result);
    }
    
    @ApiStatus.Internal
    static EntryStack readFromJson(JsonElement jsonElement) {
        try {
            JsonObject obj = jsonElement.getAsJsonObject();
            switch (GsonHelper.getAsString(obj, "type")) {
                case "stack":
                    return EntryStack.create(ItemStack.of(TagParser.parseTag(obj.get("nbt").getAsString())));
                case "item":
                    return EntryStack.create(ItemStack.of((CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, obj)));
                case "fluid":
                    return EntryStack.create(Registry.FLUID.get(ResourceLocation.tryParse(obj.get("id").getAsString())));
                case "empty":
                    return EntryStack.empty();
                default:
                    throw new IllegalArgumentException("Invalid Entry Type!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return EntryStack.empty();
        }
    }
    
    @ApiStatus.Internal
    @Nullable
    default JsonElement toJson() {
        try {
            switch (getType()) {
                case ITEM:
                    JsonObject obj1 = Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, getItemStack().save(new CompoundTag())).getAsJsonObject();
                    obj1.addProperty("type", "item");
                    return obj1;
                case FLUID:
                    Optional<ResourceLocation> optionalIdentifier = getIdentifier();
                    if (!optionalIdentifier.isPresent())
                        throw new NullPointerException("Invalid Fluid: " + toString());
                    JsonObject obj2 = new JsonObject();
                    obj2.addProperty("type", "fluid");
                    obj2.addProperty("id", optionalIdentifier.get().toString());
                    return obj2;
                case EMPTY:
                    JsonObject obj3 = new JsonObject();
                    obj3.addProperty("type", "empty");
                    return obj3;
                default:
                    throw new IllegalArgumentException("Invalid Entry Type!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static Stream<EntryStack> copyItemToFluids(EntryStack stack) {
        return FluidSupportProvider.getInstance().itemToFluids(stack);
    }
    
    Optional<ResourceLocation> getIdentifier();
    
    EntryStack.Type getType();
    
    default int getAmount() {
        return getAccurateAmount().intValue();
    }
    
    Fraction getAccurateAmount();
    
    default double getFloatingAmount() {
        return getAccurateAmount().doubleValue();
    }
    
    default void setAmount(int amount) {
        setAmount(Fraction.ofWhole(amount));
    }
    
    default void setFloatingAmount(double amount) {
        setAmount(Fraction.from(amount));
    }
    
    void setAmount(Fraction amount);
    
    boolean isEmpty();
    
    EntryStack copy();
    
    @ApiStatus.Internal
    default EntryStack rewrap() {
        return copy();
    }
    
    Object getObject();
    
    boolean equals(EntryStack stack, boolean ignoreTags, boolean ignoreAmount);
    
    boolean equalsIgnoreTagsAndAmount(EntryStack stack);
    
    boolean equalsIgnoreTags(EntryStack stack);
    
    boolean equalsIgnoreAmount(EntryStack stack);
    
    boolean equalsAll(EntryStack stack);
    
    /**
     * {@link #hashCode()} for {@link #equalsAll(EntryStack)}.
     */
    default int hashOfAll() {
        return hashCode();
    }
    
    /**
     * {@link #hashCode()} for {@link #equalsIgnoreAmount(EntryStack)}
     */
    default int hashIgnoreAmount() {
        return hashCode();
    }
    
    /**
     * {@link #hashCode()} for {@link #equalsIgnoreTags(EntryStack)}
     */
    default int hashIgnoreTags() {
        return hashCode();
    }
    
    /**
     * {@link #hashCode()} for {@link #equalsIgnoreTagsAndAmount(EntryStack)}
     */
    default int hashIgnoreAmountAndTags() {
        return hashCode();
    }
    
    int getZ();
    
    void setZ(int z);
    
    default ItemStack getItemStack() {
        if (getType() == Type.ITEM)
            return (ItemStack) getObject();
        return null;
    }
    
    default Item getItem() {
        if (getType() == Type.ITEM)
            return ((ItemStack) getObject()).getItem();
        return null;
    }
    
    default Fluid getFluid() {
        if (getType() == Type.FLUID)
            return (Fluid) getObject();
        return null;
    }
    
    <T> EntryStack setting(Settings<T> settings, T value);
    
    <T> EntryStack removeSetting(Settings<T> settings);
    
    EntryStack clearSettings();
    
    default <T> EntryStack addSetting(Settings<T> settings, T value) {
        return setting(settings, value);
    }
    
    <T> T get(Settings<T> settings);
    
    @Nullable
    default Tooltip getTooltip(Point mouse) {
        return null;
    }
    
    void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta);
    
    enum Type {
        ITEM,
        FLUID,
        EMPTY,
        RENDER
    }
    
    class Settings<T> {
        @ApiStatus.Internal
        private static final List<Settings<?>> SETTINGS = new ArrayList<>();
        
        public static final Supplier<Boolean> TRUE = () -> true;
        public static final Supplier<Boolean> FALSE = () -> false;
        public static final Settings<Supplier<Boolean>> RENDER = new Settings<>(TRUE);
        public static final Settings<Supplier<Boolean>> CHECK_TAGS = new Settings<>(FALSE);
        public static final Settings<Supplier<Boolean>> CHECK_AMOUNT = new Settings<>(FALSE);
        public static final Settings<Supplier<Boolean>> TOOLTIP_ENABLED = new Settings<>(TRUE);
        public static final Settings<Supplier<Boolean>> TOOLTIP_APPEND_MOD = new Settings<>(TRUE);
        public static final Settings<Supplier<Boolean>> RENDER_COUNTS = new Settings<>(TRUE);
        public static final Settings<Function<EntryStack, List<Component>>> TOOLTIP_APPEND_EXTRA = new Settings<>(stack -> Collections.emptyList());
        public static final Settings<Function<EntryStack, String>> COUNTS = new Settings<>(stack -> null);
        
        private static short nextId;
        private T defaultValue;
        private short id;
        
        @ApiStatus.Internal
        public Settings(T defaultValue) {
            this.defaultValue = defaultValue;
            this.id = nextId++;
            SETTINGS.add(this);
        }
        
        @ApiStatus.Internal
        public static <T> Settings<T> getById(short id) {
            return (Settings<T>) SETTINGS.get(id);
        }
        
        public T getDefaultValue() {
            return defaultValue;
        }
        
        @ApiStatus.Internal
        public short getId() {
            return id;
        }
        
        public static class Fluid {
            // Return null to disable
            public static final Settings<Function<EntryStack, String>> AMOUNT_TOOLTIP = new Settings<>(stack -> I18n.get("tooltip.rei.fluid_amount", stack.simplifyAmount().getAccurateAmount()));
            
            private Fluid() {
            }
        }
    }
    
    default EntryStack simplifyAmount() {
        setAmount(getAccurateAmount().simplify());
        return this;
    }
}
