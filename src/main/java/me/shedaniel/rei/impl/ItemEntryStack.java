/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.ItemStackRenderOverlayHook;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Deprecated
public class ItemEntryStack extends AbstractEntryStack {
    
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
        int result = 1;
        result = 31 * result + getType().ordinal();
        result = 31 * result + itemStack.getItem().hashCode();
        result = 31 * result + itemStack.getCount();
        result = 31 * result + (itemStack.hasTag() ? itemStack.getTag().hashCode() : 0);
        return result;
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
            ((ItemStackRenderOverlayHook) (Object) stack).rei_setRenderEnchantmentGlint(getSetting(Settings.Item.RENDER_ENCHANTMENT_GLINT).value().get());
            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            itemRenderer.zOffset = getZ();
            int i1 = bounds.getCenterX() - 8;
            int i2 = bounds.getCenterY() - 8;
            itemRenderer.renderGuiItemIcon(stack, i1, i2);
            itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, stack, i1, i2, getSetting(Settings.RENDER_COUNTS).value().get() ? getSetting(Settings.COUNTS).value().apply(this) : "");
            itemRenderer.zOffset = 0.0F;
            ((ItemStackRenderOverlayHook) (Object) stack).rei_setRenderEnchantmentGlint(true);
        }
    }
}
