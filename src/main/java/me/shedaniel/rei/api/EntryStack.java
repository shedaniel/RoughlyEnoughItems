/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.EmptyEntryStack;
import me.shedaniel.rei.impl.FluidEntryStack;
import me.shedaniel.rei.impl.ItemEntryStack;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface EntryStack {
    
    static EntryStack empty() {
        return EmptyEntryStack.EMPTY;
    }
    
    static EntryStack create(Fluid fluid) {
        return create(fluid, 1000);
    }
    
    static EntryStack create(Fluid fluid, int amount) {
        return new FluidEntryStack(fluid, amount);
    }
    
    static EntryStack create(ItemStack stack) {
        return new ItemEntryStack(stack);
    }
    
    static EntryStack create(ItemConvertible item) {
        return new ItemEntryStack(new ItemStack(item));
    }
    
    static EntryStack create(Block block) {
        return new ItemEntryStack(new ItemStack(block));
    }
    
    Optional<Identifier> getIdentifier();
    
    EntryStack.Type getType();
    
    int getAmount();
    
    void setAmount(int amount);
    
    boolean isEmpty();
    
    EntryStack copy();
    
    Object getObject();
    
    boolean equals(EntryStack stack, boolean ignoreTags, boolean ignoreAmount);
    
    boolean equalsIgnoreTagsAndAmount(EntryStack stack);
    
    boolean equalsIgnoreTags(EntryStack stack);
    
    boolean equalsIgnoreAmount(EntryStack stack);
    
    boolean equalsAll(EntryStack stack);
    
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
    
    <T> ObjectHolder<T> getSetting(Settings<T> settings);
    
    @Nullable
    QueuedTooltip getTooltip(int mouseX, int mouseY);
    
    void render(Rectangle bounds, int mouseX, int mouseY, float delta);
    
    public static enum Type {
        ITEM, FLUID, EMPTY, RENDER
    }
    
    public static class Settings<T> {
        public static final Supplier<Boolean> TRUE = () -> true;
        public static final Supplier<Boolean> FALSE = () -> false;
        public static final Settings<Supplier<Boolean>> RENDER = new Settings(TRUE);
        public static final Settings<Supplier<Boolean>> CHECK_TAGS = new Settings(FALSE);
        public static final Settings<Supplier<Boolean>> TOOLTIP_ENABLED = new Settings(TRUE);
        public static final Settings<Supplier<Boolean>> TOOLTIP_APPEND_MOD = new Settings(TRUE);
        public static final Settings<Supplier<Boolean>> RENDER_COUNTS = new Settings(TRUE);
        public static final Settings<Function<EntryStack, List<String>>> TOOLTIP_APPEND_EXTRA = new Settings<Function<EntryStack, List<String>>>(stack -> Collections.emptyList());
        public static final Settings<Function<EntryStack, String>> COUNTS = new Settings<Function<EntryStack, String>>(stack -> null);
        
        private T defaultValue;
        
        public Settings(T defaultValue) {
            this.defaultValue = defaultValue;
        }
        
        public T getDefaultValue() {
            return defaultValue;
        }
        
        public static class Item {
            public static final Settings<Supplier<Boolean>> RENDER_OVERLAY = new Settings(TRUE);
            
            private Item() {
            }
        }
    }
}
