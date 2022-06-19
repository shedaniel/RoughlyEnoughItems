/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.settings.EntrySettingsAdapterRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.api.common.util.FormattingUtils;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.stream.Stream;

@ApiStatus.Internal
public abstract class AbstractEntryStack<A> implements EntryStack<A>, Renderer {
    private static final Short2ObjectMap<Object> EMPTY_SETTINGS = Short2ObjectMaps.emptyMap();
    private Short2ObjectMap<Object> settings = null;
    @Environment(EnvType.CLIENT)
    private int blitOffset;
    
    @Override
    @Environment(EnvType.CLIENT)
    public int getZ() {
        return blitOffset;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void setZ(int z) {
        this.blitOffset = z;
    }
    
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
    @Nullable
    public String getContainingNamespace() {
        return get(Settings.CONTAINING_NS).apply(this, getDefinition().getContainingNamespace(this, getValue()));
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
    
    @Override
    public EntryStack<A> wildcard() {
        return wrap(getDefinition().wildcard(this, getValue()), false);
    }
    
    @Override
    public EntryStack<ItemStack> cheatsAs() {
        ItemStack stack = getDefinition().cheatsAs(this, getValue());
        
        if (stack == null) {
            return EntryStacks.of(ItemStack.EMPTY);
        }
        
        return EntryStacks.of(stack);
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
        T o = getNullable(settings);
        if (o == null) {
            o = settings.getDefaultValue();
        }
        return o;
    }
    
    @Override
    @Nullable
    public <T> T getNullable(Settings<T> settings) {
        T o = this.settings == null ? null : (T) this.settings.get(settings.getId());
        o = EntrySettingsAdapterRegistry.getInstance().adapt(this, settings, o);
        return o;
    }
    
    @Override
    public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        try {
            this.getRenderer().render(this, matrices, bounds, mouseX, mouseY, delta);
        } catch (Throwable throwable) {
            CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry");
            CrashReportUtils.renderer(report, this);
            throw CrashReportUtils.throwReport(report);
        }
    }
    
    @Override
    @Nullable
    public Tooltip getTooltip(TooltipContext context, boolean appendModName) {
        try {
            Mutable<Tooltip> tooltip = new MutableObject<>(getRenderer().<A>cast().getTooltip(this, context));
            if (tooltip.getValue() == null) return null;
            tooltip.getValue().withContextStack(this);
            tooltip.getValue().addAllTexts(get(Settings.TOOLTIP_APPEND_EXTRA).apply(this));
            tooltip.setValue(get(Settings.TOOLTIP_PROCESSOR).apply(this, tooltip.getValue()));
            if (tooltip.getValue() == null) return null;
            String containingNs = getContainingNamespace();
            if (appendModName) {
                if (containingNs != null) {
                    ClientHelper.getInstance().appendModIdToTooltips(tooltip.getValue(), containingNs);
                }
            } else if (containingNs != null) {
                final String modName = ClientHelper.getInstance().getModFromModId(containingNs);
                Iterator<Tooltip.Entry> iterator = tooltip.getValue().entries().iterator();
                while (iterator.hasNext()) {
                    Tooltip.Entry s = iterator.next();
                    if (s.isText() && FormattingUtils.stripFormatting(s.getAsText().getString()).equalsIgnoreCase(modName)) {
                        iterator.remove();
                    }
                }
            }
            return tooltip.getValue();
        } catch (Throwable throwable) {
            CrashReport report = CrashReportUtils.essential(throwable, "Getting tooltips");
            CrashReportUtils.renderer(report, this);
            throw CrashReportUtils.throwReport(report);
        }
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
    public Stream<TagKey<?>> getTagsFor() {
        return (Stream<TagKey<?>>) getDefinition().getTagsFor(this, getValue());
    }
    
    @Override
    public Component asFormattedText() {
        return getDefinition().asFormattedText(this, getValue(), TooltipContext.of());
    }
    
    @Override
    public Component asFormattedText(TooltipContext context) {
        return getDefinition().asFormattedText(this, getValue(), context);
    }
    
    @Override
    public void fillCrashReport(CrashReport report, CrashReportCategory category) {
        EntryStack.super.fillCrashReport(report, category);
        category.setDetail("Entry type", () -> String.valueOf(getType().getId()));
        category.setDetail("Is empty", () -> String.valueOf(isEmpty()));
        category.setDetail("Entry identifier", () -> String.valueOf(getIdentifier()));
        
        CrashReportCategory rendererCategory = report.addCategory("Entry Renderer");
        try {
            getDefinition().fillCrashReport(report, rendererCategory, this);
        } catch (Throwable throwable) {
            rendererCategory.setDetailError("Filling Report", throwable);
        }
    }
}
