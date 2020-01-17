/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.shedaniel.rei.api.EntryStack;
import net.minecraft.client.gui.DrawableHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public abstract class AbstractEntryStack extends DrawableHelper implements EntryStack {
    private Map<Settings<?>, Object> settings = new HashMap<>();
    
    @Override
    public <T> EntryStack setting(Settings<T> settings, T value) {
        this.settings.put(settings, value);
        return this;
    }
    
    @Override
    public <T> EntryStack removeSetting(Settings<T> settings) {
        this.settings.remove(settings);
        return this;
    }
    
    @Override
    public EntryStack clearSettings() {
        this.settings.clear();
        return this;
    }
    
    protected Map<Settings<?>, Object> getSettings() {
        return settings;
    }
    
    @Override
    public <T> T get(Settings<T> settings) {
        Object o = this.settings.get(settings);
        if (o == null)
            return settings.getDefaultValue();
        return (T) o;
    }
    
    @Override
    public boolean equals(EntryStack stack, boolean ignoreTags, boolean ignoreAmount) {
        if (ignoreTags && ignoreAmount)
            return equalsIgnoreTagsAndAmount(stack);
        if (ignoreAmount)
            return equalsIgnoreAmount(stack);
        if (ignoreTags)
            return equalsIgnoreTags(stack);
        return equalsAll(stack);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntryStack))
            return false;
        EntryStack stack = (EntryStack) obj;
        boolean checkTags = get(Settings.CHECK_TAGS).get() || stack.get(Settings.CHECK_TAGS).get();
        return equals(stack, !checkTags, true);
    }
    
    @Override
    public int getZ() {
        return getBlitOffset();
    }
    
    @Override
    public void setZ(int z) {
        setBlitOffset(z);
    }
}
