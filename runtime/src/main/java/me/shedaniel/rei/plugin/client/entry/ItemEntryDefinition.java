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

package me.shedaniel.rei.plugin.client.entry;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.AbstractEntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.BatchedEntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ItemEntryDefinition implements EntryDefinition<ItemStack>, EntrySerializer<ItemStack> {
    @Environment(EnvType.CLIENT)
    private EntryRenderer<ItemStack> renderer;
    
    public ItemEntryDefinition() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> Client.init(this));
    }
    
    @Environment(EnvType.CLIENT)
    private static class Client {
        private static void init(ItemEntryDefinition definition) {
            definition.renderer = definition.new ItemEntryRenderer();
        }
    }
    
    @Override
    public Class<ItemStack> getValueType() {
        return ItemStack.class;
    }
    
    @Override
    public EntryType<ItemStack> getType() {
        return VanillaEntryTypes.ITEM;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public EntryRenderer<ItemStack> getRenderer() {
        return renderer;
    }
    
    @Override
    @Nullable
    public ResourceLocation getIdentifier(EntryStack<ItemStack> entry, ItemStack value) {
        return Registry.ITEM.getKey(value.getItem());
    }
    
    @Override
    public boolean isEmpty(EntryStack<ItemStack> entry, ItemStack value) {
        return value.isEmpty();
    }
    
    @Override
    public ItemStack copy(EntryStack<ItemStack> entry, ItemStack value) {
        return value.copy();
    }
    
    @Override
    public ItemStack normalize(EntryStack<ItemStack> entry, ItemStack value) {
        ItemStack copy = value.copy();
        copy.setCount(1);
        return copy;
    }
    
    @Override
    public ItemStack wildcard(EntryStack<ItemStack> entry, ItemStack value) {
        return new ItemStack(value.getItem(), 1);
    }
    
    @Override
    @Nullable
    public ItemStack cheatsAs(EntryStack<ItemStack> entry, ItemStack value) {
        return value.copy();
    }
    
    @Override
    public long hash(EntryStack<ItemStack> entry, ItemStack value, ComparisonContext context) {
        int code = 1;
        code = 31 * code + System.identityHashCode(value.getItem());
        code = 31 * code + Long.hashCode(ItemComparatorRegistry.getInstance().hashOf(context, value));
        return code;
    }
    
    @Override
    public boolean equals(ItemStack o1, ItemStack o2, ComparisonContext context) {
        if (o1.getItem() != o2.getItem())
            return false;
        return ItemComparatorRegistry.getInstance().hashOf(context, o1) == ItemComparatorRegistry.getInstance().hashOf(context, o2);
    }
    
    @Override
    @Nullable
    public EntrySerializer<ItemStack> getSerializer() {
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
    public CompoundTag save(EntryStack<ItemStack> entry, ItemStack value) {
        return value.save(new CompoundTag());
    }
    
    @Override
    public ItemStack read(CompoundTag tag) {
        return ItemStack.of(tag);
    }
    
    private static final ReferenceSet<Item> SEARCH_BLACKLISTED = new ReferenceOpenHashSet<>();
    
    @Override
    public Component asFormattedText(EntryStack<ItemStack> entry, ItemStack value) {
        if (!SEARCH_BLACKLISTED.contains(value.getItem()))
            try {
                return value.getHoverName();
            } catch (Throwable e) {
                e.printStackTrace();
                SEARCH_BLACKLISTED.add(value.getItem());
            }
        try {
            return Component.literal(I18n.get("item." + Registry.ITEM.getKey(value.getItem()).toString().replace(":", ".")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Component.literal("ERROR");
    }
    
    @Override
    public Stream<? extends TagKey<?>> getTagsFor(EntryStack<ItemStack> entry, ItemStack value) {
        return value.getTags();
    }
    
    @Environment(EnvType.CLIENT)
    private List<Component> tryGetItemStackToolTip(EntryStack<ItemStack> entry, ItemStack value, boolean careAboutAdvanced) {
        if (!SEARCH_BLACKLISTED.contains(value.getItem()))
            try {
                return value.getTooltipLines(Minecraft.getInstance().player, careAboutAdvanced && Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
            } catch (Throwable e) {
                e.printStackTrace();
                SEARCH_BLACKLISTED.add(value.getItem());
            }
        return Lists.newArrayList(asFormattedText(entry, value));
    }
    
    @Override
    public void fillCrashReport(CrashReport report, CrashReportCategory category, EntryStack<ItemStack> entry) {
        EntryDefinition.super.fillCrashReport(report, category, entry);
        ItemStack stack = entry.getValue();
        category.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
        category.setDetail("Item Damage", () -> String.valueOf(stack.getDamageValue()));
        category.setDetail("Item NBT", () -> String.valueOf(stack.getTag()));
        category.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
    }
    
    @Environment(EnvType.CLIENT)
    public class ItemEntryRenderer extends AbstractEntryRenderer<ItemStack> implements BatchedEntryRenderer<ItemStack, BakedModel> {
        private static final float SCALE = 20.0F;
        public static final int ITEM_LIGHT = 0xf000f0;
        
        @Override
        public BakedModel getExtraData(EntryStack<ItemStack> entry) {
            return Minecraft.getInstance().getItemRenderer().getModel(entry.getValue(), null, null, 0);
        }
        
        @Override
        public void render(EntryStack<ItemStack> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            BakedModel model = getExtraData(entry);
            setupGL(entry, model);
            if (!entry.isEmpty()) {
                ItemStack value = entry.getValue();
                matrices.pushPose();
                matrices.mulPoseMatrix(RenderSystem.getModelViewMatrix());
                matrices.translate(bounds.getCenterX(), bounds.getCenterY(), entry.getZ());
                matrices.scale(bounds.getWidth(), (bounds.getWidth() + bounds.getHeight()) / -2f, 1.0F);
                PoseStack modelViewStack = RenderSystem.getModelViewStack();
                modelViewStack.pushPose();
                modelViewStack.last().pose().load(matrices.last().pose());
                RenderSystem.applyModelViewMatrix();
                MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
                Minecraft.getInstance().getItemRenderer().render(value, ItemTransforms.TransformType.GUI, false, new PoseStack(), immediate,
                        ITEM_LIGHT, OverlayTexture.NO_OVERLAY, model);
                immediate.endBatch();
                matrices.popPose();
                modelViewStack.popPose();
                RenderSystem.applyModelViewMatrix();
            }
            PoseStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushPose();
            modelViewStack.mulPoseMatrix(matrices.last().pose());
            modelViewStack.translate(bounds.x, bounds.y, 0);
            modelViewStack.scale(bounds.width / 16f, (bounds.getWidth() + bounds.getHeight()) / 2f / 16f, 1.0F);
            RenderSystem.applyModelViewMatrix();
            renderOverlay(entry, bounds);
            modelViewStack.popPose();
            endGL(entry, model);
            RenderSystem.applyModelViewMatrix();
        }
        
        @Override
        public int getBatchIdentifier(EntryStack<ItemStack> entry, Rectangle bounds, BakedModel model) {
            return 1738923 + (model.usesBlockLight() ? 1 : 0);
        }
        
        @Override
        public void startBatch(EntryStack<ItemStack> entry, BakedModel model, PoseStack matrices, float delta) {
            setupGL(entry, model);
            PoseStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushPose();
            modelViewStack.scale(SCALE, -SCALE, 1.0F);
            RenderSystem.applyModelViewMatrix();
        }
        
        public void setupGL(EntryStack<ItemStack> entry, BakedModel model) {
            Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            boolean sideLit = model.usesBlockLight();
            if (!sideLit) Lighting.setupForFlatItems();
        }
        
        @Override
        public void renderBase(EntryStack<ItemStack> entry, BakedModel model, PoseStack matrices, MultiBufferSource.BufferSource immediate, Rectangle bounds, int mouseX, int mouseY, float delta) {
            if (!entry.isEmpty()) {
                ItemStack value = entry.getValue();
                matrices.pushPose();
                matrices.translate(bounds.getCenterX() / SCALE, bounds.getCenterY() / -SCALE, entry.getZ());
                matrices.scale(bounds.getWidth() / SCALE, (bounds.getWidth() + bounds.getHeight()) / 2f / SCALE, 1.0F);
                Minecraft.getInstance().getItemRenderer().render(value, ItemTransforms.TransformType.GUI, false, matrices, immediate,
                        ITEM_LIGHT, OverlayTexture.NO_OVERLAY, model);
                matrices.popPose();
            }
        }
        
        @Override
        public void afterBase(EntryStack<ItemStack> entry, BakedModel model, PoseStack matrices, float delta) {
            endGL(entry, model);
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
        }
        
        @Override
        public void renderOverlay(EntryStack<ItemStack> entry, BakedModel model, PoseStack matrices, MultiBufferSource.BufferSource immediate, Rectangle bounds, int mouseX, int mouseY, float delta) {
            PoseStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushPose();
            modelViewStack.mulPoseMatrix(matrices.last().pose());
            modelViewStack.translate(bounds.x, bounds.y, 0);
            modelViewStack.scale(bounds.width / 16f, (bounds.getWidth() + bounds.getHeight()) / 2f / 16f, 1.0F);
            RenderSystem.applyModelViewMatrix();
            renderOverlay(entry, bounds);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        
        public void renderOverlay(EntryStack<ItemStack> entry, Rectangle bounds) {
            if (!entry.isEmpty()) {
                Minecraft.getInstance().getItemRenderer().blitOffset = entry.getZ();
                Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, entry.getValue(), 0, 0, null);
                Minecraft.getInstance().getItemRenderer().blitOffset = 0.0F;
            }
        }
        
        @Override
        public void endBatch(EntryStack<ItemStack> entry, BakedModel model, PoseStack matrices, float delta) {
        }
        
        public void endGL(EntryStack<ItemStack> entry, BakedModel model) {
            RenderSystem.enableDepthTest();
            boolean sideLit = model.usesBlockLight();
            if (!sideLit) Lighting.setupFor3DItems();
        }
        
        @Override
        @Nullable
        public Tooltip getTooltip(EntryStack<ItemStack> entry, Point mouse) {
            if (entry.isEmpty())
                return null;
            Tooltip tooltip = Tooltip.create();
            Optional<TooltipComponent> component = entry.getValue().getTooltipImage();
            List<Component> components = tryGetItemStackToolTip(entry, entry.getValue(), true);
            if (!components.isEmpty()) {
                tooltip.add(components.get(0));
            }
            component.ifPresent(tooltip::add);
            for (int i = 1; i < components.size(); i++) {
                tooltip.add(components.get(i));
            }
            return tooltip;
        }
    }
}
