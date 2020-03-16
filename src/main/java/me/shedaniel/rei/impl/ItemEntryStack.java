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
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.math.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.widgets.Tooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@ApiStatus.Internal
public class ItemEntryStack extends AbstractEntryStack implements OptimalEntryStack {
    
    private static final Predicate<BakedModel> IS_SIDE_LIT = BakedModel::isSideLit;
    private static final MatrixStack MATRICES = new MatrixStack();
    
    private ItemStack itemStack;
    
    public ItemEntryStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    @Override
    public Optional<Identifier> getIdentifier() {
        return Optional.ofNullable(Registry.ITEM.getId(getItem()));
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
        EntryStack stack = EntryStack.create(getItemStack().copy());
        for (Map.Entry<Settings<?>, Object> entry : getSettings().entrySet()) {
            stack.setting((Settings<? super Object>) entry.getKey(), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public Object getObject() {
        return itemStack;
    }
    
    @Override
    public boolean equalsIgnoreTagsAndAmount(EntryStack stack) {
        if (stack.getType() != Type.ITEM)
            return false;
        return itemStack.getItem() == stack.getItem();
    }
    
    @Override
    public boolean equalsAll(EntryStack stack) {
        if (stack.getType() != Type.ITEM)
            return false;
        if (itemStack.getItem() != stack.getItem() || getAmount() != stack.getAmount())
            return false;
        return ItemStack.areTagsEqual(itemStack, stack.getItemStack());
    }
    
    @Override
    public boolean equalsIgnoreAmount(EntryStack stack) {
        if (stack.getType() != Type.ITEM)
            return false;
        if (itemStack.getItem() != stack.getItem())
            return false;
        ItemStack otherStack = stack.getItemStack();
        CompoundTag o1 = itemStack.getTag();
        CompoundTag o2 = otherStack.getTag();
        return o1 == o2 || ((o1 != null && o2 != null) && equals(o1, o2));
    }
    
    public boolean equals(CompoundTag o1, CompoundTag o2) {
        int o1Size = 0;
        int o2Size = 0;
        for (String key : o1.getKeys()) {
            if (key.equals("Count"))
                continue;
            o1Size++;
        }
        for (String key : o2.getKeys()) {
            if (key.equals("Count"))
                continue;
            o2Size++;
            if (o2Size > o1Size)
                return false;
        }
        if (o1Size != o2Size)
            return false;
        
        try {
            for (String key : o1.getKeys()) {
                if (key.equals("Count"))
                    continue;
                Tag value = o1.get(key);
                Tag otherValue = o2.get(key);
                if (value == null) {
                    if (!(otherValue == null && o2.contains(key)))
                        return false;
                } else if (value instanceof CompoundTag && otherValue instanceof CompoundTag) {
                    if (!(value == otherValue || (value != null && otherValue != null) && equals((CompoundTag) value, (CompoundTag) otherValue)))
                        return false;
                } else {
                    if (!value.asString().equals(otherValue.asString()))
                        return false;
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean equalsIgnoreTags(EntryStack stack) {
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
        int result = 1;
        result = 31 * result + getType().hashCode();
        result = 31 * result + itemStack.getItem().hashCode();
        if (itemStack.hasTag()) {
            result = 31 * result + itemStack.getTag().asString().hashCode();
        } else {
            result = 31 * result;
        }
        return result;
    }
    
    @Override
    public int hashIgnoreAmountAndTags() {
        int result = 1;
        result = 31 * result + getType().hashCode();
        result = 31 * result + itemStack.getItem().hashCode();
        return result;
    }
    
    @Nullable
    @Override
    public Tooltip getTooltip(Point point) {
        if (isEmpty() || !get(Settings.TOOLTIP_ENABLED).get())
            return null;
        List<String> toolTip = Lists.newArrayList(SearchArgument.tryGetItemStackToolTip(getItemStack(), true));
        toolTip.addAll(get(Settings.TOOLTIP_APPEND_EXTRA).apply(this));
        if (get(Settings.TOOLTIP_APPEND_MOD).get() && ConfigObject.getInstance().shouldAppendModNames()) {
            final String modString = ClientHelper.getInstance().getFormattedModFromItem(getItem());
            boolean alreadyHasMod = false;
            for (String s : toolTip)
                if (s.equalsIgnoreCase(modString)) {
                    alreadyHasMod = true;
                    break;
                }
            if (!alreadyHasMod)
                toolTip.add(modString);
        }
        return Tooltip.create(toolTip);
    }
    
    @Override
    public void render(Rectangle bounds, int mouseX, int mouseY, float delta) {
        optimisedRenderStart(delta);
        optimisedRenderBase(bounds, mouseX, mouseY, delta);
        optimisedRenderOverlay(bounds, mouseX, mouseY, delta);
        optimisedRenderEnd(delta);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void optimisedRenderStart(float delta) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
        GlStateManager.enableRescaleNormal();
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void optimisedRenderEnd(float delta) {
        GlStateManager.disableRescaleNormal();
    }
    
    private BakedModel getModelFromStack(ItemStack stack) {
        BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(stack);
        if (stack.getItem().hasPropertyGetters())
            model = model.getItemPropertyOverrides().apply(model, stack, null, null);
        if (model != null)
            return model;
        return MinecraftClient.getInstance().getItemRenderer().getModels().getModelManager().getMissingModel();
    }
    
    @Override
    public void optimisedRenderBase(Rectangle bounds, int mouseX, int mouseY, float delta) {
        if (!isEmpty() && get(Settings.RENDER).get()) {
            ItemStack stack = getItemStack();
            ((ItemStackHook) (Object) stack).rei_setRenderEnchantmentGlint(get(Settings.Item.RENDER_ENCHANTMENT_GLINT).get());
            MATRICES.push();
            MATRICES.translate(bounds.getCenterX(), bounds.getCenterY(), 100.0F + getZ());
            MATRICES.scale(bounds.getWidth(), (bounds.getWidth() + bounds.getHeight()) / -2f, bounds.getHeight());
            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            BakedModel model = getModelFromStack(stack);
            boolean bl = !IS_SIDE_LIT.test(model);
            if (bl)
                GlStateManager.method_24221();
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, false, MATRICES, immediate, 15728880, OverlayTexture.DEFAULT_UV, model);
            immediate.draw();
            if (bl)
                GlStateManager.method_24222();
            MATRICES.pop();
            ((ItemStackHook) (Object) stack).rei_setRenderEnchantmentGlint(false);
        }
    }
    
    @Override
    public void optimisedRenderOverlay(Rectangle bounds, int mouseX, int mouseY, float delta) {
        if (!isEmpty() && get(Settings.RENDER).get()) {
            MinecraftClient.getInstance().getItemRenderer().zOffset = getZ();
            MinecraftClient.getInstance().getItemRenderer().renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, getItemStack(), bounds.x, bounds.y, get(Settings.RENDER_COUNTS).get() ? get(Settings.COUNTS).apply(this) : "");
            MinecraftClient.getInstance().getItemRenderer().zOffset = 0.0F;
        }
    }
}
