/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.renderers;

import com.google.common.collect.Lists;
import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.widget.EntryListWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public abstract class ItemStackRenderer extends Renderer {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public boolean drawTooltip = false;
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
        int l = x - 8, i1 = y - 6;
        RenderHelper.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.zOffset = blitOffset;
        GuiLighting.enableForItems();
        RenderHelper.colorMask(true, true, true, true);
        RenderHelper.enableLighting();
        RenderHelper.enableRescaleNormal();
        RenderHelper.enableDepthTest();
        itemRenderer.renderGuiItem(getItemStack(), l, i1);
        itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, getItemStack(), l, i1, renderCounts() ? null : "");
        itemRenderer.zOffset = 0.0F;
        this.blitOffset = 0;
        if (drawTooltip && mouseX >= x - 8 && mouseX <= x + 8 && mouseY >= y - 6 && mouseY <= y + 10)
            queueTooltip(getItemStack(), delta);
        this.drawTooltip = false;
    }
    
    protected void queueTooltip(ItemStack itemStack, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(itemStack)));
    }
    
    protected boolean renderCounts() {
        return true;
    }
    
    protected List<String> getTooltip(ItemStack itemStack) {
        List<String> toolTip = Lists.newArrayList(EntryListWidget.tryGetItemStackToolTip(itemStack, true));
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().shouldAppendModNames()) {
            final String modString = ClientHelper.getInstance().getFormattedModFromItem(itemStack.getItem());
            toolTip.addAll(getExtraToolTips(itemStack));
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
