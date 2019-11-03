/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.entries;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.ObjectHolder;
import me.shedaniel.rei.impl.ObjectHolderImpl;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.util.Identifier;

import java.util.Optional;

public abstract class RecipeEntry extends DrawableHelper implements EntryStack {
    @Override
    public Optional<Identifier> getIdentifier() {
        return Optional.empty();
    }
    
    @Override
    public Type getType() {
        return Type.RENDER;
    }
    
    @Override
    public int getAmount() {
        return 0;
    }
    
    @Override
    public void setAmount(int amount) {
    
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public EntryStack copy() {
        return this;
    }
    
    @Override
    public Object getObject() {
        return null;
    }
    
    @Override
    public boolean equals(EntryStack stack, boolean ignoreTags, boolean ignoreAmount) {
        return stack == this;
    }
    
    @Override
    public boolean equalsIgnoreTagsAndAmount(EntryStack stack) {
        return stack == this;
    }
    
    @Override
    public boolean equalsIgnoreTags(EntryStack stack) {
        return stack == this;
    }
    
    @Override
    public boolean equalsIgnoreAmount(EntryStack stack) {
        return stack == this;
    }
    
    @Override
    public boolean equalsAll(EntryStack stack) {
        return stack == this;
    }
    
    @Override
    public int getZ() {
        return getBlitOffset();
    }
    
    @Override
    public void setZ(int z) {
        setBlitOffset(z);
    }
    
    @Override
    public <T> EntryStack setting(Settings<T> settings, T value) {
        return this;
    }
    
    @Override
    public <T> EntryStack removeSetting(Settings<T> settings) {
        return this;
    }
    
    @Override
    public EntryStack clearSettings() {
        return this;
    }
    
    @Override
    public <T> EntryStack addSetting(Settings<T> settings, T value) {
        return this;
    }
    
    @Override
    public <T> ObjectHolder<T> getSetting(Settings<T> settings) {
        return new ObjectHolderImpl<>(settings.getDefaultValue());
    }
    
    public abstract int getHeight();
    
    public final int getWidth() {
        return 100;
    }
}
