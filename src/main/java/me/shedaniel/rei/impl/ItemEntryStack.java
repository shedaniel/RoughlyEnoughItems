/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Internal
public class ItemEntryStack extends AbstractEntryStack implements OptimalEntryStack {
    
    private static final MatrixStack MATRICES = new MatrixStack();
    private ItemStack itemStack;
    private int hash = -1;
    
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
        hash = -1;
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
        return ItemStack.areTagsEqual(itemStack, stack.getItemStack());
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
    public int hashCode() {
        //        if (hash == -1) {
        int result = 1;
        result = 31 * result + getType().hashCode();
        result = 31 * result + itemStack.getItem().hashCode();
        result = 31 * result + itemStack.getCount();
        result = 31 * result + (itemStack.hasTag() ? itemStack.getTag().hashCode() : 0);
        hash = result;
        //            if (hash == -1) {
        //                hash = -2;
        //            }
        //        }
        return hash;
    }
    
    @Nullable
    @Override
    public QueuedTooltip getTooltip(int mouseX, int mouseY) {
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
        return QueuedTooltip.create(toolTip);
    }
    
    @Override
    public void render(Rectangle bounds, int mouseX, int mouseY, float delta) {
        optimisedRenderStart(delta);
        optimisedRenderBase(bounds, mouseX, mouseY, delta);
        optimisedRenderOverlay(bounds, mouseX, mouseY, delta);
        optimisedRenderEnd(delta);
    }
    
    @Override
    public void optimisedRenderStart(float delta) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
        GlStateManager.enableRescaleNormal();
    }
    
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
            boolean bl = !model.isSideLit();
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
