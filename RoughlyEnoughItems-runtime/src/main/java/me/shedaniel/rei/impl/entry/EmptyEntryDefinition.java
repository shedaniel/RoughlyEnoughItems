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
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.entry.*;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.utils.ImmutableLiteralText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@ApiStatus.Internal
public enum EmptyEntryDefinition implements EntryDefinition<Unit> {
    EMPTY(BuiltinEntryTypes.EMPTY, true),
    RENDERING(BuiltinEntryTypes.RENDERING, false);
    
    private final EntryType<Unit> type;
    private final boolean empty;
    
    EmptyEntryDefinition(EntryType<Unit> type, boolean empty) {
        this.type = type;
        this.empty = empty;
    }
    
    @Override
    public @NotNull Class<Unit> getValueType() {
        return Unit.class;
    }
    
    @Override
    public @NotNull EntryType<Unit> getType() {
        return type;
    }
    
    @Override
    public @NotNull EntryRenderer<Unit> getRenderer() {
        return EmptyRenderer.INSTANCE;
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getIdentifier(EntryStack<Unit> entry, Unit value) {
        return Optional.empty();
    }
    
    @Override
    public @NotNull Fraction getAmount(EntryStack<Unit> entry, Unit value) {
        return Fraction.zero();
    }
    
    @Override
    public void setAmount(EntryStack<Unit> entry, Unit value, Fraction amount) {
        
    }
    
    @Override
    public boolean isEmpty(EntryStack<Unit> entry, Unit value) {
        return empty;
    }
    
    @Override
    public @NotNull Unit copy(EntryStack<Unit> entry, Unit value) {
        return value;
    }
    
    @Override
    public int hash(EntryStack<Unit> entry, Unit value, ComparisonContext context) {
        return ordinal();
    }
    
    @Override
    public boolean equals(Unit o1, Unit o2, ComparisonContext context) {
        return true;
    }
    
    @Override
    public @NotNull CompoundTag toTag(EntryStack<Unit> entry, Unit value) {
        return new CompoundTag();
    }
    
    @Override
    public @NotNull Unit fromTag(@NotNull CompoundTag tag) {
        return Unit.INSTANCE;
    }
    
    @Override
    public @NotNull Component asFormattedText(EntryStack<Unit> entry, Unit value) {
        return ImmutableLiteralText.EMPTY;
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
}
