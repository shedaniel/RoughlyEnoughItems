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

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.fractions.Fraction;
import me.shedaniel.rei.api.widgets.Tooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
public class FluidEntryStack extends AbstractEntryStack {
    private static final Fraction IGNORE_AMOUNT = Fraction.of(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)).simplify();
    private Fluid fluid;
    private Fraction amount;
    private int hashIgnoreAmount;
    private int hash;
    
    public FluidEntryStack(Fluid fluid) {
        this(fluid, IGNORE_AMOUNT);
    }
    
    public FluidEntryStack(Fluid fluid, Fraction amount) {
        this.fluid = fluid;
        this.amount = amount;
        
        rehash();
    }
    
    private void rehash() {
        hashIgnoreAmount = 31 + getType().ordinal();
        hashIgnoreAmount = 31 * hashIgnoreAmount + fluid.hashCode();
        
        hash = 31 * hashIgnoreAmount + amount.hashCode();
    }
    
    @Override
    public Optional<ResourceLocation> getIdentifier() {
        return Optional.ofNullable(Registry.FLUID.getKey(getFluid()));
    }
    
    @Override
    public Type getType() {
        return Type.FLUID;
    }
    
    @Override
    public Fraction getAccurateAmount() {
        return amount;
    }
    
    @Override
    public void setAmount(Fraction amount) {
        this.amount = amount.equals(IGNORE_AMOUNT) ? IGNORE_AMOUNT : max(amount, Fraction.empty());
        if (isEmpty()) {
            fluid = Fluids.EMPTY;
        }
        
        rehash();
    }
    
    private <T extends Comparable<T>> T max(T o1, T o2) {
        return o1.compareTo(o2) > 0 ? o1 : o2;
    }
    
    @Override
    public boolean isEmpty() {
        return (!amount.equals(IGNORE_AMOUNT) && !amount.isGreaterThan(Fraction.empty())) || fluid == Fluids.EMPTY;
    }
    
    @Override
    public EntryStack copy() {
        EntryStack stack = EntryStack.create(fluid, amount);
        for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
            stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public Object getObject() {
        return fluid;
    }
    
    @Override
    public boolean equalsIgnoreTagsAndAmount(EntryStack stack) {
        if (stack.getType() == Type.ITEM)
            return EntryStack.copyItemToFluids(stack).anyMatch(this::equalsIgnoreTagsAndAmount);
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid();
    }
    
    @Override
    public boolean equalsIgnoreTags(EntryStack stack) {
        if (stack.getType() == Type.ITEM)
            return EntryStack.copyItemToFluids(stack).anyMatch(this::equalsIgnoreTags);
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid() && (amount.equals(IGNORE_AMOUNT) || stack.getAccurateAmount().equals(IGNORE_AMOUNT) || amount.equals(stack.getAccurateAmount()));
    }
    
    @Override
    public boolean equalsIgnoreAmount(EntryStack stack) {
        if (stack.getType() == Type.ITEM)
            return EntryStack.copyItemToFluids(stack).anyMatch(this::equalsIgnoreAmount);
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid();
    }
    
    @Override
    public boolean equalsAll(EntryStack stack) {
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid() && (amount.equals(IGNORE_AMOUNT) || stack.getAccurateAmount().equals(IGNORE_AMOUNT) || amount.equals(stack.getAccurateAmount()));
    }
    
    @Override
    public int hashOfAll() {
        return hash;
    }
    
    @Override
    public int hashIgnoreAmountAndTags() {
        return hashIgnoreAmount;
    }
    
    @Override
    public int hashIgnoreTags() {
        return hash;
    }
    
    @Override
    public int hashIgnoreAmount() {
        return hashIgnoreAmount;
    }
    
    @Nullable
    @Override
    public Tooltip getTooltip(Point point) {
        if (!get(Settings.TOOLTIP_ENABLED).get() || isEmpty())
            return null;
        List<Component> toolTip = Lists.newArrayList(asFormattedText());
        if (!amount.isLessThan(Fraction.empty()) && !amount.equals(IGNORE_AMOUNT)) {
            String amountTooltip = get(Settings.Fluid.AMOUNT_TOOLTIP).apply(this);
            if (amountTooltip != null)
                toolTip.addAll(Stream.of(amountTooltip.split("\n")).map(TextComponent::new).collect(Collectors.toList()));
        }
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            toolTip.add((new TextComponent(Registry.FLUID.getKey(this.getFluid()).toString())).withStyle(ChatFormatting.DARK_GRAY));
        }
        toolTip.addAll(get(Settings.TOOLTIP_APPEND_EXTRA).apply(this));
        if (get(Settings.TOOLTIP_APPEND_MOD).get() && ConfigObject.getInstance().shouldAppendModNames()) {
            ClientHelper.getInstance().appendModIdToTooltips(toolTip, Registry.FLUID.getKey(getFluid()).getNamespace());
        }
        return Tooltip.create(toolTip);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        if (get(Settings.RENDER).get()) {
            SimpleFluidRenderer.FluidRenderingData renderingData = SimpleFluidRenderer.fromFluid(fluid);
            if (renderingData != null) {
                TextureAtlasSprite sprite = renderingData.getSprite();
                int color = renderingData.getColor();
                int a = 255;
                int r = (color >> 16 & 255);
                int g = (color >> 8 & 255);
                int b = (color & 255);
                Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
                Tesselator tess = Tesselator.getInstance();
                BufferBuilder bb = tess.getBuilder();
                Matrix4f matrix = matrices.last().pose();
                bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bb.vertex(matrix, bounds.getMaxX(), bounds.y, getZ()).uv(sprite.getU1(), sprite.getV0()).color(r, g, b, a).endVertex();
                bb.vertex(matrix, bounds.x, bounds.y, getZ()).uv(sprite.getU0(), sprite.getV0()).color(r, g, b, a).endVertex();
                bb.vertex(matrix, bounds.x, bounds.getMaxY(), getZ()).uv(sprite.getU0(), sprite.getV1()).color(r, g, b, a).endVertex();
                bb.vertex(matrix, bounds.getMaxX(), bounds.getMaxY(), getZ()).uv(sprite.getU1(), sprite.getV1()).color(r, g, b, a).endVertex();
                tess.end();
            }
        }
    }
    
    @NotNull
    @Override
    public Component asFormattedText() {
        return fluid.defaultFluidState().createLegacyBlock().getBlock().getName();
    }
}
