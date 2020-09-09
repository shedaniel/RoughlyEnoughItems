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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.utils.FormattingUtils;
import me.shedaniel.rei.utils.ImmutableLiteralText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ApiStatus.Internal
public class ItemEntryStack extends AbstractEntryStack implements OptimalEntryStack {
    private ItemStack itemStack;
    
    public ItemEntryStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    @Override
    public Optional<ResourceLocation> getIdentifier() {
        return Optional.ofNullable(Registry.ITEM.getKey(getItem()));
    }
    
    @Override
    public Type getType() {
        return Type.ITEM;
    }
    
    @Override
    public int getAmount() {
        return itemStack.getCount();
    }
    
    @Override
    public void setAmount(int amount) {
        itemStack.setCount(amount);
    }
    
    @Override
    public boolean isEmpty() {
        return itemStack.isEmpty();
    }
    
    @Override
    public EntryStack copy() {
        EntryStack stack = EntryStack.create(itemStack.copy());
        for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
            stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public EntryStack rewrap() {
        EntryStack stack = EntryStack.create(itemStack);
        for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
            stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public Object getObject() {
        return itemStack;
    }
    
    /**
     * type:
     * 0: ignore tags and amount
     * 1: ignore tags
     * 2: ignore amount
     * 3: all
     */
    private boolean compareIfFluid(EntryStack stack, int type) {
        Stream<EntryStack> fluids = EntryStack.copyItemToFluids(this);
        Stream<EntryStack> stacks = Stream.empty();
        if (stack.getType() == Type.ITEM)
            stacks = EntryStack.copyItemToFluids(stack);
        switch (type) {
            case 0:
                return stacks.anyMatch(entryStack -> fluids.anyMatch(entryStack::equalsIgnoreTagsAndAmount));
            case 1:
                return stacks.anyMatch(entryStack -> fluids.anyMatch(entryStack::equalsIgnoreTags));
            case 2:
                return stacks.anyMatch(entryStack -> fluids.anyMatch(entryStack::equalsIgnoreAmount));
            case 3:
                return stacks.anyMatch(entryStack -> fluids.anyMatch(entryStack::equalsAll));
        }
        
        return false;
    }
    
    @Override
    public boolean equalsIgnoreTagsAndAmount(EntryStack stack) {
        if (compareIfFluid(stack, 0)) return true;
        if (stack.getType() != Type.ITEM)
            return false;
        return itemStack.getItem() == stack.getItem();
    }
    
    @Override
    public boolean equalsAll(EntryStack stack) {
        if (stack.getType() != Type.ITEM)
            return false;
        return itemStack.getItem() == stack.getItem() && getAmount() != stack.getAmount() && ItemStack.tagMatches(itemStack, stack.getItemStack());
    }
    
    @Override
    public boolean equalsIgnoreAmount(EntryStack stack) {
        if (compareIfFluid(stack, 2)) return true;
        if (stack.getType() != Type.ITEM)
            return false;
        if (itemStack.getItem() != stack.getItem())
            return false;
        ItemStack otherStack = stack.getItemStack();
        CompoundNBT o1 = itemStack.getTag();
        CompoundNBT o2 = otherStack.getTag();
        return o1 == o2 || ((o1 != null && o2 != null) && equalsTagWithoutCount(o1, o2));
    }
    
    private boolean equalsTagWithoutCount(CompoundNBT o1, CompoundNBT o2) {
        int o1Size = 0;
        int o2Size = 0;
        for (String key : o1.getAllKeys()) {
            if (key.equals("Count"))
                continue;
            o1Size++;
        }
        for (String key : o2.getAllKeys()) {
            if (key.equals("Count"))
                continue;
            o2Size++;
            if (o2Size > o1Size)
                return false;
        }
        if (o1Size != o2Size)
            return false;
        
        try {
            for (String key : o1.getAllKeys()) {
                if (key.equals("Count"))
                    continue;
                INBT value = o1.get(key);
                INBT otherValue = o2.get(key);
                if (!equalsTag(value, otherValue))
                    return false;
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
        
        return true;
    }
    
    private boolean equalsTag(INBT tag, INBT otherTag) {
        if (tag == null || otherTag == null) {
            return tag == otherTag;
        }
        if (tag instanceof ListNBT && otherTag instanceof ListNBT)
            return equalsList((ListNBT) tag, (ListNBT) otherTag);
        return tag.equals(otherTag);
    }
    
    private boolean equalsList(ListNBT listTag, ListNBT otherListTag) {
        if (listTag.size() != otherListTag.size())
            return false;
        for (int i = 0; i < listTag.size(); i++) {
            INBT value = listTag.get(i);
            INBT otherValue = otherListTag.get(i);
            if (!equalsTag(value, otherValue))
                return false;
        }
        return true;
    }
    
    @Override
    public boolean equalsIgnoreTags(EntryStack stack) {
        if (compareIfFluid(stack, 1)) return true;
        if (stack.getType() != Type.ITEM)
            return false;
        if (itemStack.getItem() != stack.getItem())
            return false;
        return getAmount() == stack.getAmount();
    }
    
    @Override
    public int hashOfAll() {
        int result = hashIgnoreAmount();
        result = 31 * result + itemStack.getCount();
        return result;
    }
    
    @Override
    public int hashIgnoreTags() {
        int result = hashIgnoreAmountAndTags();
        result = 31 * result + itemStack.getCount();
        return result;
    }
    
    @Override
    public int hashIgnoreAmount() {
        int result = hashIgnoreAmountAndTags();
        if (itemStack.hasTag()) {
            result = 31 * result + itemStack.getTag().toString().hashCode();
        } else {
            result = 31 * result;
        }
        return result;
    }
    
    @Override
    public int hashIgnoreAmountAndTags() {
        int result = 1;
        result = 31 * result + itemStack.getItem().hashCode();
        return result;
    }
    
    @Nullable
    @Override
    public Tooltip getTooltip(Point point) {
        if (isEmpty() || !get(Settings.TOOLTIP_ENABLED).get())
            return null;
        List<ITextComponent> toolTip = tryGetItemStackToolTip(true);
        toolTip.addAll(get(Settings.TOOLTIP_APPEND_EXTRA).apply(this));
        if (get(Settings.TOOLTIP_APPEND_MOD).get() && ConfigObject.getInstance().shouldAppendModNames()) {
            final String modId = ClientHelper.getInstance().getModFromItem(getItem());
            boolean alreadyHasMod = false;
            for (ITextComponent s : toolTip)
                if (FormattingUtils.stripFormatting(s.getString()).equalsIgnoreCase(modId)) {
                    alreadyHasMod = true;
                    break;
                }
            if (!alreadyHasMod)
                toolTip.add(ClientHelper.getInstance().getFormattedModFromItem(getItem()));
        }
        return Tooltip.create(toolTip);
    }
    
    @Override
    public void render(MatrixStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        optimisedRenderStart(matrices, delta);
        IRenderTypeBuffer.Impl immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        optimisedRenderBase(matrices, immediate, bounds, mouseX, mouseY, delta);
        immediate.endBatch();
        optimisedRenderOverlay(matrices, bounds, mouseX, mouseY, delta);
        optimisedRenderEnd(matrices, delta);
    }
    
    @Override
    public void optimisedRenderStart(MatrixStack matrices, float delta) {
        optimisedRenderStart(matrices, delta, true);
    }
    
    @SuppressWarnings("deprecation")
    public void optimisedRenderStart(MatrixStack matrices, float delta, boolean isOptimised) {
        Minecraft.getInstance().getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
        Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.pushMatrix();
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (isOptimised) {
            boolean sideLit = getModelFromStack(itemStack).usesBlockLight();
            if (!sideLit)
                RenderHelper.setupForFlatItems();
        }
    }
    
    @Override
    public void optimisedRenderEnd(MatrixStack matrices, float delta) {
        optimisedRenderEnd(matrices, delta, true);
    }
    
    @SuppressWarnings("deprecation")
    public void optimisedRenderEnd(MatrixStack matrices, float delta, boolean isOptimised) {
        RenderSystem.enableDepthTest();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        if (isOptimised) {
            boolean sideLit = getModelFromStack(itemStack).usesBlockLight();
            if (!sideLit)
                RenderHelper.setupFor3DItems();
        }
        RenderSystem.popMatrix();
    }
    
    private IBakedModel getModelFromStack(ItemStack stack) {
        return Minecraft.getInstance().getItemRenderer().getModel(stack, null, null);
    }
    
    @Override
    public int groupingHash() {
        return 1738923 + (getModelFromStack(itemStack).usesBlockLight() ? 1 : 0);
    }
    
    @Override
    public void optimisedRenderBase(MatrixStack matrices, IRenderTypeBuffer.Impl immediate, Rectangle bounds, int mouseX, int mouseY, float delta) {
        if (!isEmpty() && get(Settings.RENDER).get()) {
            ItemStack stack = getItemStack();
            matrices.pushPose();
            matrices.translate(bounds.getCenterX(), bounds.getCenterY(), 100.0F + getZ());
            matrices.scale(bounds.getWidth(), (bounds.getWidth() + bounds.getHeight()) / -2f, bounds.getHeight());
            Minecraft.getInstance().getItemRenderer().render(stack, ItemCameraTransforms.TransformType.GUI, false, matrices, immediate, 15728880, OverlayTexture.NO_OVERLAY, getModelFromStack(stack));
            matrices.popPose();
        }
    }
    
    @Override
    public void optimisedRenderOverlay(MatrixStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        if (!isEmpty() && get(Settings.RENDER).get()) {
            Minecraft.getInstance().getItemRenderer().blitOffset = getZ();
            Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, getItemStack(), bounds.x, bounds.y, get(Settings.RENDER_COUNTS).get() ? get(Settings.COUNTS).apply(this) : "");
            Minecraft.getInstance().getItemRenderer().blitOffset = 0.0F;
        }
    }
    
    private static final ReferenceSet<Item> SEARCH_BLACKLISTED = new ReferenceOpenHashSet<>();
    
    @Override
    public @NotNull ITextComponent asFormattedText() {
        if (!SEARCH_BLACKLISTED.contains(itemStack.getItem()))
            try {
                return itemStack.getHoverName();
            } catch (Throwable e) {
                e.printStackTrace();
                SEARCH_BLACKLISTED.add(itemStack.getItem());
            }
        try {
            return new ImmutableLiteralText(I18n.get("item." + Registry.ITEM.getKey(itemStack.getItem()).toString().replace(":", ".")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new ImmutableLiteralText("ERROR");
    }
    
    private List<ITextComponent> tryGetItemStackToolTip(boolean careAboutAdvanced) {
        if (!SEARCH_BLACKLISTED.contains(itemStack.getItem()))
            try {
                return itemStack.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips && careAboutAdvanced ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
            } catch (Throwable e) {
                e.printStackTrace();
                SEARCH_BLACKLISTED.add(itemStack.getItem());
            }
        return Lists.newArrayList(asFormattedText());
    }
}
