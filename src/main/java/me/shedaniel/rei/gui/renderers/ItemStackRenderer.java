/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.renderers;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.widget.ItemListOverlay;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public abstract class ItemStackRenderer extends Renderer {
    
    public static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public boolean drawTooltip = false;
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
        int l = x - 8, i1 = y - 6;
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.zLevel = zLevel;
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepthTest();
        itemRenderer.renderItemIntoGUI(getItemStack(), l, i1);
        itemRenderer.renderItemOverlays(Minecraft.getInstance().fontRenderer, getItemStack(), l, i1);
        itemRenderer.zLevel = 0.0F;
        this.zLevel = 0;
        if (drawTooltip && mouseX >= x - 8 && mouseX <= x + 8 && mouseY >= y - 6 && mouseY <= y + 10)
            queueTooltip(getItemStack(), delta);
        this.drawTooltip = false;
    }
    
    protected void queueTooltip(ItemStack itemStack, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(itemStack)));
    }
    
    protected List<String> getTooltip(ItemStack itemStack) {
        final String modString = ClientHelper.getInstance().getFormattedModFromItem(itemStack.getItem());
        List<String> toolTip = Lists.newArrayList(ItemListOverlay.tryGetItemStackToolTip(itemStack, true));
        toolTip.addAll(getExtraToolTips(itemStack));
        boolean alreadyHasMod = false;
        for(String s : toolTip)
            if (s.equalsIgnoreCase(modString)) {
                alreadyHasMod = true;
                break;
            }
        if (!alreadyHasMod)
            toolTip.add(modString);
        return toolTip;
    }
    
    protected List<String> getExtraToolTips(ItemStack stack) {
        return Collections.emptyList();
    }
    
    public abstract ItemStack getItemStack();
    
}
