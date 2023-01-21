/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.plugin.client.entry;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.AbstractEntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.BatchedEntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.util.SpriteRenderer;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FluidEntryDefinition implements EntryDefinition<FluidStack>, EntrySerializer<FluidStack> {
    private static final String FLUID_AMOUNT = Platform.isForge() ? "tooltip.rei.fluid_amount.forge" : "tooltip.rei.fluid_amount";
    @Environment(EnvType.CLIENT)
    private EntryRenderer<FluidStack> renderer;
    
    public FluidEntryDefinition() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> Client.init(this));
    }
    
    @Environment(EnvType.CLIENT)
    private static class Client {
        private static void init(FluidEntryDefinition definition) {
            definition.renderer = new FluidEntryRenderer();
        }
    }
    
    @Override
    public Class<FluidStack> getValueType() {
        return FluidStack.class;
    }
    
    @Override
    public EntryType<FluidStack> getType() {
        return VanillaEntryTypes.FLUID;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public EntryRenderer<FluidStack> getRenderer() {
        return renderer;
    }
    
    @Override
    @Nullable
    public ResourceLocation getIdentifier(EntryStack<FluidStack> entry, FluidStack value) {
        return BuiltInRegistries.FLUID.getKey(value.getFluid());
    }
    
    @Override
    public boolean isEmpty(EntryStack<FluidStack> entry, FluidStack value) {
        return value.isEmpty();
    }
    
    @Override
    public FluidStack copy(EntryStack<FluidStack> entry, FluidStack value) {
        return value.copy();
    }
    
    @Override
    public FluidStack normalize(EntryStack<FluidStack> entry, FluidStack value) {
        Fluid fluid = value.getFluid();
        if (fluid instanceof FlowingFluid flowingFluid) fluid = flowingFluid.getSource();
        return FluidStack.create(fluid, FluidStack.bucketAmount(), value.getTag());
    }
    
    @Override
    public FluidStack wildcard(EntryStack<FluidStack> entry, FluidStack value) {
        Fluid fluid = value.getFluid();
        if (fluid instanceof FlowingFluid) fluid = ((FlowingFluid) fluid).getSource();
        return FluidStack.create(fluid, FluidStack.bucketAmount());
    }
    
    @Override
    @Nullable
    public ItemStack cheatsAs(EntryStack<FluidStack> entry, FluidStack value) {
        if (value.isEmpty()) return ItemStack.EMPTY;
        Item bucket = value.getFluid().getBucket();
        if (bucket == null) return ItemStack.EMPTY;
        return new ItemStack(bucket);
    }
    
    @Nullable
    @Override
    public FluidStack add(FluidStack o1, FluidStack o2) {
        return o1.copyWithAmount(o1.getAmount() + o2.getAmount());
    }
    
    @Override
    public long hash(EntryStack<FluidStack> entry, FluidStack value, ComparisonContext context) {
        int code = 1;
        code = 31 * code + value.getFluid().hashCode();
        code = 31 * code + Long.hashCode(FluidComparatorRegistry.getInstance().hashOf(context, value));
        return code;
    }
    
    @Override
    public boolean equals(FluidStack o1, FluidStack o2, ComparisonContext context) {
        if (o1.getFluid() != o2.getFluid())
            return false;
        return FluidComparatorRegistry.getInstance().hashOf(context, o1) == FluidComparatorRegistry.getInstance().hashOf(context, o2);
    }
    
    @Override
    @Nullable
    public EntrySerializer<FluidStack> getSerializer() {
        return this;
    }
    
    @Override
    public boolean supportSaving() {
        return true;
    }
    
    @Override
    public boolean supportReading() {
        return true;
    }
    
    @Override
    public boolean acceptsNull() {
        return false;
    }
    
    @Override
    public CompoundTag save(EntryStack<FluidStack> entry, FluidStack value) {
        return value.write(new CompoundTag());
    }
    
    @Override
    public FluidStack read(CompoundTag tag) {
        return FluidStack.read(tag);
    }
    
    @Override
    public Component asFormattedText(EntryStack<FluidStack> entry, FluidStack value) {
        return value.getName();
    }
    
    @Override
    public Stream<? extends TagKey<?>> getTagsFor(EntryStack<FluidStack> entry, FluidStack value) {
        return value.getFluid().builtInRegistryHolder().tags();
    }
    
    @Override
    public void fillCrashReport(CrashReport report, CrashReportCategory category, EntryStack<FluidStack> entry) {
        EntryDefinition.super.fillCrashReport(report, category, entry);
        FluidStack stack = entry.getValue();
        category.setDetail("Fluid Type", () -> String.valueOf(BuiltInRegistries.FLUID.getKey(stack.getFluid())));
        category.setDetail("Fluid Amount", () -> String.valueOf(stack.getAmount()));
        category.setDetail("Fluid NBT", () -> String.valueOf(stack.getTag()));
    }
    
    @Environment(EnvType.CLIENT)
    public static class FluidEntryRenderer extends AbstractEntryRenderer<FluidStack> implements BatchedEntryRenderer<FluidStack, TextureAtlasSprite> {
        private static final Supplier<TextureAtlasSprite> MISSING_SPRITE = Suppliers.memoize(() -> {
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
            return atlas.getSprite(MissingTextureAtlasSprite.getLocation());
        });
        
        @Override
        public TextureAtlasSprite getExtraData(EntryStack<FluidStack> entry) {
            FluidStack stack = entry.getValue();
            if (stack.isEmpty()) return null;
            return FluidStackHooks.getStillTexture(stack);
        }
        
        private TextureAtlasSprite missingTexture() {
            return MISSING_SPRITE.get();
        }
        
        @Override
        public int getBatchIdentifier(EntryStack<FluidStack> entry, Rectangle bounds, TextureAtlasSprite extraData) {
            return 0;
        }
        
        @Override
        public void startBatch(EntryStack<FluidStack> entry, TextureAtlasSprite extraData, PoseStack matrices, float delta) {}
        
        @Override
        public void renderBase(EntryStack<FluidStack> entry, TextureAtlasSprite sprite, PoseStack matrices, MultiBufferSource.BufferSource immediate, Rectangle bounds, int mouseX, int mouseY, float delta) {
            TextureAtlasSprite s = sprite == null ? missingTexture() : sprite;
            SpriteRenderer.beginPass()
                    .setup(immediate, RenderType.solid())
                    .sprite(s)
                    .color(sprite == null ? 0xFFFFFF : FluidStackHooks.getColor(entry.getValue()))
                    .light(0x00f000f0)
                    .overlay(OverlayTexture.NO_OVERLAY)
                    .alpha(0xff)
                    .normal(matrices.last().normal(), 0, 0, 0)
                    .position(matrices.last().pose(), bounds.x, bounds.getMaxY() - bounds.height * Mth.clamp(entry.get(EntryStack.Settings.FLUID_RENDER_RATIO), 0, 1), bounds.getMaxX(), bounds.getMaxY(), entry.getZ())
                    .next(s.atlasLocation());
        }
        
        @Override
        public void afterBase(EntryStack<FluidStack> entry, TextureAtlasSprite extraData, PoseStack matrices, float delta) {}
        
        @Override
        public void renderOverlay(EntryStack<FluidStack> entry, TextureAtlasSprite extraData, PoseStack matrices, MultiBufferSource.BufferSource immediate, Rectangle bounds, int mouseX, int mouseY, float delta) {}
        
        @Override
        public void endBatch(EntryStack<FluidStack> entry, TextureAtlasSprite extraData, PoseStack matrices, float delta) {}
        
        @Override
        public void render(EntryStack<FluidStack> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            FluidStack stack = entry.getValue();
            if (stack.isEmpty()) return;
            TextureAtlasSprite sprite = FluidStackHooks.getStillTexture(stack);
            if (sprite == null) return;
            int color = FluidStackHooks.getColor(stack);
            
            MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
            
            SpriteRenderer.beginPass()
                    .setup(immediate, RenderType.solid())
                    .sprite(sprite)
                    .color(color)
                    .light(0x00f000f0)
                    .overlay(OverlayTexture.NO_OVERLAY)
                    .alpha(0xff)
                    .normal(matrices.last().normal(), 0, 0, 0)
                    .position(matrices.last().pose(), bounds.x, bounds.getMaxY() - bounds.height * Mth.clamp(entry.get(EntryStack.Settings.FLUID_RENDER_RATIO), 0, 1), bounds.getMaxX(), bounds.getMaxY(), entry.getZ())
                    .next(InventoryMenu.BLOCK_ATLAS);
            
            immediate.endBatch();
        }
        
        @Override
        @Nullable
        public Tooltip getTooltip(EntryStack<FluidStack> entry, TooltipContext context) {
            if (entry.isEmpty())
                return null;
            List<Component> toolTip = Lists.newArrayList(entry.asFormattedText());
            long amount = entry.getValue().getAmount();
            if (amount >= 0 && entry.get(EntryStack.Settings.FLUID_AMOUNT_VISIBLE)) {
                String amountTooltip = I18n.get(FLUID_AMOUNT, entry.getValue().getAmount());
                if (amountTooltip != null) {
                    toolTip.addAll(Stream.of(amountTooltip.split("\n")).map(Component::literal).collect(Collectors.toList()));
                }
            }
            if (Minecraft.getInstance().options.advancedItemTooltips) {
                ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(entry.getValue().getFluid());
                toolTip.add((Component.literal(fluidId.toString())).withStyle(ChatFormatting.DARK_GRAY));
            }
            return Tooltip.create(toolTip);
        }
    }
}
