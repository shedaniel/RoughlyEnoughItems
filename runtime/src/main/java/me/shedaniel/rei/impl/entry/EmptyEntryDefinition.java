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

package me.shedaniel.rei.impl.entry;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.*;
import me.shedaniel.rei.api.util.ImmutableLiteralText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

@ApiStatus.Internal
public enum EmptyEntryDefinition implements EntryDefinition<Object> {
    EMPTY(BuiltinEntryTypes.EMPTY, true, () -> Unit.INSTANCE, EmptyRenderer.INSTANCE),
    RENDERING(BuiltinEntryTypes.RENDERING, false, EmptyEntryDefinition::throwRendering, DeferredRenderer.INSTANCE);
    
    private static <T> T throwRendering() {
        throw new IllegalStateException("Can not create rendering type from NBT tag!");
    }
    
    private final EntryType<Object> type;
    private final boolean empty;
    private final Supplier<Object> defaultValue;
    private final EntryRenderer<Object> renderer;
    
    <T> EmptyEntryDefinition(EntryType<T> type, boolean empty, Supplier<T> defaultValue, EntryRenderer<T> renderer) {
        this.type = (EntryType<Object>) type;
        this.empty = empty;
        this.defaultValue = (Supplier<Object>) defaultValue;
        this.renderer = (EntryRenderer<Object>) renderer;
    }
    
    @Override
    public Class<Object> getValueType() {
        return Object.class;
    }
    
    @Override
    public EntryType<Object> getType() {
        return type;
    }
    
    @Override
    public EntryRenderer<Object> getRenderer() {
        return renderer;
    }
    
    @Override
    public Optional<ResourceLocation> getIdentifier(EntryStack<Object> entry, Object value) {
        return Optional.empty();
    }
    
    @Override
    public Fraction getAmount(EntryStack<Object> entry, Object value) {
        return Fraction.zero();
    }
    
    @Override
    public void setAmount(EntryStack<Object> entry, Object value, Fraction amount) {
        
    }
    
    @Override
    public boolean isEmpty(EntryStack<Object> entry, Object value) {
        return empty;
    }
    
    @Override
    public Object copy(EntryStack<Object> entry, Object value) {
        return value;
    }
    
    @Override
    public int hash(EntryStack<Object> entry, Object value, ComparisonContext context) {
        return ordinal();
    }
    
    @Override
    public boolean equals(Object o1, Object o2, ComparisonContext context) {
        return true;
    }
    
    @Override
    public CompoundTag toTag(EntryStack<Object> entry, Object value) {
        return new CompoundTag();
    }
    
    @Override
    public Object fromTag(CompoundTag tag) {
        return defaultValue.get();
    }
    
    @Override
    public Component asFormattedText(EntryStack<Object> entry, Object value) {
        if (value instanceof Renderer) {
            Tooltip tooltip = ((Renderer) value).getTooltip(PointHelper.ofMouse());
            if (tooltip != null && !tooltip.getText().isEmpty()) {
                return tooltip.getText().get(0);
            }
        }
        return ImmutableLiteralText.EMPTY;
    }
    
    @Override
    public Collection<ResourceLocation> getTagsFor(EntryStack<Object> entry, Object value) {
        return Collections.emptyList();
    }
    
    private enum EmptyRenderer implements EntryRenderer<Unit> {
        INSTANCE;
        
        @Override
        public void render(EntryStack<Unit> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            
        }
        
        @Override
        public @Nullable Tooltip getTooltip(EntryStack<Unit> entry, Point mouse) {
            return null;
        }
    }
    
    private enum DeferredRenderer implements EntryRenderer<Renderer> {
        INSTANCE;
        
        @Override
        public void render(EntryStack<Renderer> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            entry.getValue().render(matrices, bounds, mouseX, mouseY, delta);
        }
        
        @Override
        public @Nullable Tooltip getTooltip(EntryStack<Renderer> entry, Point mouse) {
            return entry.getValue().getTooltip(mouse);
        }
    }
}
