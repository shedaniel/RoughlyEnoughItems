/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.renderers;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ItemStackRenderOverlayHook;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.widget.EntryListWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MatrixStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class ItemStackRenderer extends Renderer {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    /**
     * @deprecated This boolean is no longer used
     */
    @Deprecated
    public boolean drawTooltip = false;
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
        int l = x - 8, i1 = y - 6;
        ItemStack stack = getItemStack();
        ((ItemStackRenderOverlayHook) (Object) stack).rei_setRenderOverlay(renderOverlay());
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.zOffset = getBlitOffset();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableLighting();
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableDepthTest();
        itemRenderer.renderGuiItem(stack, l, i1);
        itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, stack, l, i1, getCounts());
        itemRenderer.zOffset = 0.0F;
        setBlitOffset(0);
    }
    
    /**
     * Queue a tooltip to the REI overlay
     *
     * @param itemStack the stack to queue
     * @param delta     the delta
     * @deprecated Use {@link Renderer#getQueuedTooltip(float)} instead and queue manually
     */
    @Deprecated
    protected void queueTooltip(ItemStack itemStack, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(itemStack)));
    }
    
    @Nullable
    @Override
    public QueuedTooltip getQueuedTooltip(float delta) {
        return QueuedTooltip.create(getTooltip(getItemStack()));
    }
    
    protected boolean renderCounts() {
        return true;
    }
    
    protected boolean renderOverlay() {
        return true;
    }
    
    protected String getCounts() {
        return renderCounts() ? null : "";
    }
    
    protected List<String> getTooltip(ItemStack itemStack) {
        List<String> toolTip = Lists.newArrayList(EntryListWidget.tryGetItemStackToolTip(itemStack, true));
        toolTip.addAll(getExtraToolTips(itemStack));
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().shouldAppendModNames()) {
            final String modString = ClientHelper.getInstance().getFormattedModFromItem(itemStack.getItem());
            boolean alreadyHasMod = false;
            for (String s : toolTip)
                if (s.equalsIgnoreCase(modString)) {
                    alreadyHasMod = true;
                    break;
                }
            if (!alreadyHasMod)
                toolTip.add(modString);
        }
        return toolTip;
    }
    
    protected List<String> getExtraToolTips(ItemStack stack) {
        return Collections.emptyList();
    }
    
    public abstract ItemStack getItemStack();
    
}
