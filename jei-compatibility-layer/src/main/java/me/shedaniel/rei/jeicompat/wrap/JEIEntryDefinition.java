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

package me.shedaniel.rei.jeicompat.wrap;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.utils.EnvExecutor;
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

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrapContext;

public class JEIEntryDefinition<T> implements EntryDefinition<T> {
    private final EntryType<T> type;
    private final IIngredientType<T> ingredientType;
    private final IIngredientHelper<T> ingredientHelper;
    private final IIngredientRenderer<T> ingredientRenderer;
    private final Renderer renderer = new Renderer();
    
    public JEIEntryDefinition(EntryType<T> type, IIngredientType<T> ingredientType, IIngredientHelper<T> ingredientHelper, IIngredientRenderer<T> ingredientRenderer) {
        this.type = type;
        this.ingredientType = ingredientType;
        this.ingredientHelper = ingredientHelper;
        this.ingredientRenderer = ingredientRenderer;
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
        String resourceId = ingredientHelper.getResourceId(value);
        if (resourceId == null) return null;
        return ResourceLocation.tryParse(resourceId);
    }
    
    @Override
    public boolean isEmpty(EntryStack<T> entry, T value) {
        return ingredientHelper.isValidIngredient(value);
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
    public int hash(EntryStack<T> entry, T value, ComparisonContext context) {
        return ingredientHelper.getUniqueId(value, wrapContext(context)).hashCode();
    }
    
    @Override
    public boolean equals(T o1, T o2, ComparisonContext context) {
        return Objects.equals(ingredientHelper.getUniqueId(o1, wrapContext(context)), ingredientHelper.getUniqueId(o2, wrapContext(context)));
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
    
    private class Renderer implements EntryRenderer<T> {
        @Override
        public void render(EntryStack<T> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            ingredientRenderer.render(matrices, bounds.x, bounds.y, entry.getValue());
        }
        
        @Override
        public @Nullable Tooltip getTooltip(EntryStack<T> entry, Point mouse) {
            List<Component> components = ingredientRenderer.getTooltip(entry.getValue(), Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
            if (components != null && !components.isEmpty()) {
                return Tooltip.create(mouse, components);
            }
            return null;
        }
    }
}
