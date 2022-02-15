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

package me.shedaniel.rei.jeicompat.wrap;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import dev.architectury.utils.EnvExecutor;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIEntryDefinition<T> implements EntryDefinition<T> {
    private final EntryType<T> type;
    private final IIngredientType<T> ingredientType;
    private final IIngredientHelper<T> ingredientHelper;
    private final Renderer<T> renderer;
    
    public JEIEntryDefinition(EntryType<T> type, IIngredientType<T> ingredientType, IIngredientHelper<T> ingredientHelper, IIngredientRenderer<T> ingredientRenderer) {
        this.type = type;
        this.ingredientType = ingredientType;
        this.ingredientHelper = ingredientHelper;
        this.renderer = new Renderer<>(ingredientRenderer);
    }
    
    @Override
    public Class<T> getValueType() {
        return (Class<T>) ingredientType.getIngredientClass();
    }
    
    @Override
    public EntryType<T> getType() {
        return type;
    }
    
    @Override
    public EntryRenderer<T> getRenderer() {
        return renderer;
    }
    
    @Override
    @Nullable
    public ResourceLocation getIdentifier(EntryStack<T> entry, T value) {
        return ingredientHelper.getResourceLocation(value);
    }
    
    @Override
    @Nullable
    public String getContainingNamespace(EntryStack<T> entry, T value) {
        return ingredientHelper.getDisplayModId(value);
    }
    
    @Override
    public boolean isEmpty(EntryStack<T> entry, T value) {
        return !ingredientHelper.isValidIngredient(value);
    }
    
    @Override
    public T copy(EntryStack<T> entry, T value) {
        return ingredientHelper.copyIngredient(value);
    }
    
    @Override
    public T normalize(EntryStack<T> entry, T value) {
        return ingredientHelper.normalizeIngredient(value);
    }
    
    @Override
    public T wildcard(EntryStack<T> entry, T value) {
        return ingredientHelper.normalizeIngredient(value);
    }
    
    @Override
    public long hash(EntryStack<T> entry, T value, ComparisonContext context) {
        return hashCode(ingredientHelper.getUniqueId(value, context.wrapContext()));
    }
    
    private static long hashCode(String id) {
        long h = 0;
        for (int i = 0; i < id.length(); i++) {
            h = 31 * h + id.charAt(i);
        }
        return h;
    }
    
    @Override
    public boolean equals(T o1, T o2, ComparisonContext context) {
        return Objects.equals(ingredientHelper.getUniqueId(o1, context.wrapContext()), ingredientHelper.getUniqueId(o2, context.wrapContext()));
    }
    
    @Override
    @Nullable
    public EntrySerializer<T> getSerializer() {
        return null;
    }
    
    @Override
    public Component asFormattedText(EntryStack<T> entry, T value) {
        return new ImmutableTextComponent(ingredientHelper.getDisplayName(value));
    }
    
    @Override
    public Collection<ResourceLocation> getTagsFor(TagContainer tagContainer, EntryStack<T> entry, T value) {
        return EnvExecutor.getEnvSpecific(() -> () -> _getTagsFor(value), () -> Collections::emptyList);
    }
    
    @OnlyIn(Dist.CLIENT)
    public Collection<ResourceLocation> _getTagsFor(T value) {
        return ingredientHelper.getTags(value);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static class Renderer<T> implements EntryRenderer<T> {
        private final IIngredientRenderer<T> ingredientRenderer;
        
        public Renderer(IIngredientRenderer<T> ingredientRenderer) {
            this.ingredientRenderer = ingredientRenderer;
        }
        
        @Override
        public void render(EntryStack<T> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            Object value = entry.getValue();
            if (value instanceof FluidStack) value = FluidStackHooksForge.toForge((FluidStack) value);
            ((IIngredientRenderer<Object>) ingredientRenderer).render(matrices, bounds.getCenterX() - ingredientRenderer.getWidth() / 2, bounds.getCenterY() - ingredientRenderer.getHeight() / 2, value);
        }
        
        @Override
        @Nullable
        public Tooltip getTooltip(EntryStack<T> entry, Point mouse) {
            Object value = entry.getValue();
            if (value instanceof FluidStack) value = FluidStackHooksForge.toForge((FluidStack) value);
            List<Component> components = ((IIngredientRenderer<Object>) ingredientRenderer).getTooltip(value, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
            if (components != null && !components.isEmpty()) {
                return Tooltip.create(mouse, components);
            }
            return null;
        }
    }
}
