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

package me.shedaniel.rei.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.config.ConfigObject;
import me.shedaniel.rei.api.gui.AbstractRenderer;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public abstract class AbstractEntryStack<A> extends AbstractRenderer implements EntryStack<A> {
    private static final Short2ObjectMap<Object> EMPTY_SETTINGS = Short2ObjectMaps.emptyMap();
    private Short2ObjectMap<Object> settings = null;
    
    @Override
    public <T> EntryStack<A> setting(Settings<T> settings, T value) {
        short settingsId = settings.getId();
        if (this.settings == null)
            this.settings = Short2ObjectMaps.singleton(settingsId, value);
        else {
            if (this.settings.size() == 1) {
                if (this.settings.containsKey(settingsId)) {
                    this.settings = Short2ObjectMaps.singleton(settingsId, value);
                    return this;
                } else {
                    Short2ObjectMap<Object> singletonSettings = this.settings;
                    this.settings = new Short2ObjectOpenHashMap<>(2);
                    this.settings.putAll(singletonSettings);
                }
            }
            this.settings.put(settingsId, value);
        }
        return this;
    }
    
    @Override
    public <T> EntryStack<A> removeSetting(Settings<T> settings) {
        if (this.settings != null) {
            short settingsId = settings.getId();
            if (this.settings.size() == 1) {
                if (this.settings.containsKey(settingsId)) {
                    this.settings = null;
                }
            } else if (this.settings.remove(settingsId) != null && this.settings.isEmpty()) {
                this.settings = null;
            }
        }
        return this;
    }
    
    @Override
    public EntryStack<A> clearSettings() {
        this.settings = null;
        return this;
    }
    
    protected Short2ObjectMap<Object> getSettings() {
        return this.settings == null ? EMPTY_SETTINGS : this.settings;
    }
    
    @Override
    public <T> T get(Settings<T> settings) {
        Object o = this.settings == null ? null : this.settings.get(settings.getId());
        if (o == null) {
            return settings.getDefaultValue();
        }
        return (T) o;
    }
    
    @Override
    public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        this.getRenderer().render(this, matrices, bounds, mouseX, mouseY, delta);
    }
    
    @Override
    @Nullable
    public Tooltip getTooltip(Point mouse) {
        Tooltip[] tooltip = {this.get(Settings.RENDER).apply(this).<A>cast().getTooltip(this, mouse)};
        if (tooltip[0] == null) return null;
        tooltip[0].getText().addAll(get(EntryStack.Settings.TOOLTIP_APPEND_EXTRA).apply(this));
        tooltip[0] = get(EntryStack.Settings.TOOLTIP_PROCESSOR).apply(this, tooltip[0]);
        if (tooltip[0] == null) return null;
        if (ConfigObject.getInstance().shouldAppendModNames()) {
            ResourceLocation location = getIdentifier();
            if (location != null) {
                ClientHelper.getInstance().appendModIdToTooltips(tooltip[0].getText(), location.getNamespace());
            }
        }
        return tooltip[0];
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEntryStack)) return false;
        AbstractEntryStack<?> that = (AbstractEntryStack<?>) o;
        return EntryStacks.equalsExact(this, that);
    }
    
    @Override
    public int hashCode() {
        return hash(ComparisonContext.EXACT);
    }
}
