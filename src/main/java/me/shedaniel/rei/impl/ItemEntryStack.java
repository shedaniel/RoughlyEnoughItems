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
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Deprecated
public class ItemEntryStack extends AbstractEntryStack {
    
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
        for (Map.Entry<Settings, Object> entry : getSettings().entrySet()) {
            stack.setting(entry.getKey(), entry.getValue());
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
        if (isEmpty() || !getSetting(Settings.TOOLTIP_ENABLED).value().get())
            return null;
        List<String> toolTip = Lists.newArrayList(SearchArgument.tryGetItemStackToolTip(getItemStack(), true));
        toolTip.addAll(getSetting(Settings.TOOLTIP_APPEND_EXTRA).value().apply(this));
        if (getSetting(Settings.TOOLTIP_APPEND_MOD).value().get() && ConfigObject.getInstance().shouldAppendModNames()) {
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
        if (!isEmpty() && getSetting(Settings.RENDER).value().get()) {
            ItemStack stack = getItemStack();
            if (ConfigObject.getInstance().doesFastEntryRendering() || true) {
                ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                itemRenderer.zOffset = getZ();
                BakedModel model = itemRenderer.getModels().getModel(stack);
                if (stack.getItem().hasPropertyGetters())
                    model = model.getItemPropertyOverrides().apply(model, stack, null, null);
                if (model == null)
                    model = itemRenderer.getModels().getModelManager().getMissingModel();
                MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
                GlStateManager.enableRescaleNormal();
                MatrixStack matrices = new MatrixStack();
                matrices.translate(bounds.getCenterX(), bounds.getCenterY(), 100.0F + getZ());
                matrices.scale(bounds.getWidth(), (bounds.getWidth() + bounds.getHeight()) / -2f, bounds.getHeight());
                VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
                boolean bl = !model.hasDepthInGui();
                if (bl)
                    GlStateManager.method_24221();
                itemRenderer.renderItem(stack, ModelTransformation.Type.GUI, false, matrices, immediate, 15728880, OverlayTexture.DEFAULT_UV, model);
                immediate.draw();
                if (bl)
                    GlStateManager.method_24222();
                GlStateManager.disableRescaleNormal();
                itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, stack, bounds.x, bounds.y, getSetting(Settings.RENDER_COUNTS).value().get() ? getSetting(Settings.COUNTS).value().apply(this) : "");
                itemRenderer.zOffset = 0.0F;
            } else {
                ((ItemStackHook) (Object) stack).rei_setRenderEnchantmentGlint(getSetting(Settings.Item.RENDER_ENCHANTMENT_GLINT).value().get());
                ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                itemRenderer.zOffset = getZ();
                int i1 = bounds.x;
                int i2 = bounds.y;
                itemRenderer.renderGuiItemIcon(stack, i1, i2);
                itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, stack, i1, i2, getSetting(Settings.RENDER_COUNTS).value().get() ? getSetting(Settings.COUNTS).value().apply(this) : "");
                itemRenderer.zOffset = 0.0F;
                ((ItemStackHook) (Object) stack).rei_setRenderEnchantmentGlint(true);
            }
        }
    }
}
