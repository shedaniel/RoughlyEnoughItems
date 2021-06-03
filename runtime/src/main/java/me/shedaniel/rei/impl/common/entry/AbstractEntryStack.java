/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.common.entry;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.TagContainer;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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
    @Nullable
    public ResourceLocation getIdentifier() {
        return getDefinition().getIdentifier(this, getValue());
    }
    
    @Override
    public boolean isEmpty() {
        return getDefinition().isEmpty(this, getValue());
    }
    
    @Override
    public EntryStack<A> copy() {
        return wrap(getDefinition().copy(this, getValue()), true);
    }
    
    @Override
    public EntryStack<A> rewrap() {
        return wrap(getValue(), true);
    }
    
    @Override
    public EntryStack<A> normalize() {
        return wrap(getDefinition().normalize(this, getValue()), false);
    }
    
    protected EntryStack<A> wrap(A value, boolean copySettings) {
        TypedEntryStack<A> stack = new TypedEntryStack<>(getDefinition(), value);
        if (copySettings) {
            for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
                stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
            }
        }
        return stack;
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
    public Tooltip getTooltip(Point mouse, boolean appendModName) {
        Mutable<Tooltip> tooltip = new MutableObject<>(getRenderer().<A>cast().getTooltip(this, mouse));
        if (tooltip.getValue() == null) return null;
        tooltip.getValue().getText().addAll(get(Settings.TOOLTIP_APPEND_EXTRA).apply(this));
        tooltip.setValue(get(Settings.TOOLTIP_PROCESSOR).apply(this, tooltip.getValue()));
        if (tooltip.getValue() == null) return null;
        if (appendModName) {
            ResourceLocation location = getIdentifier();
            if (location != null) {
                ClientHelper.getInstance().appendModIdToTooltips(tooltip.getValue().getText(), location.getNamespace());
            }
        }
        return tooltip.getValue();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEntryStack<?> that)) return false;
        return EntryStacks.equalsExact(this, that);
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(EntryStacks.hashExact(this));
    }
    
    @Override
    public Collection<ResourceLocation> getTagsFor() {
        TagContainer container;
        if (Platform.getEnvironment() == Env.CLIENT) {
            container = getClientTagContainer();
        } else {
            container = SerializationTags.getInstance();
        }
        return getDefinition().getTagsFor(container, this, getValue());
    }
    
    @Environment(EnvType.CLIENT)
    private static TagContainer getClientTagContainer() {
        return Minecraft.getInstance().getConnection().getTags();
    }
    
    @Override
    public Component asFormattedText() {
        return getDefinition().asFormattedText(this, getValue());
    }
}
