package me.shedaniel.rei.impl;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.ObjectHolder;
import net.minecraft.client.gui.DrawableHelper;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public abstract class AbstractEntryStack extends DrawableHelper implements EntryStack {
    private Map<Settings, Object> settings = new HashMap<>();
    
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
    
    protected Map<Settings, Object> getSettings() {
        return settings;
    }
    
    @Override
    public <T> ObjectHolder<T> getSetting(Settings<T> settings) {
        Object o = this.settings.get(settings);
        if (o == null)
            return new ObjectHolderImpl(settings.getDefaultValue());
        return new ObjectHolderImpl(o);
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
        boolean checkTags = getSetting(Settings.CHECK_TAGS).value().get() || stack.getSetting(Settings.CHECK_TAGS).value().get();
        return equals(stack, !checkTags, true);
    }
    
    @Override
    public int getZ() {
        return blitOffset;
    }
    
    @Override
    public void setZ(int z) {
        blitOffset = z;
    }
}
