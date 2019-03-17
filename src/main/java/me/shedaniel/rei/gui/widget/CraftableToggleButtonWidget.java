package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Arrays;

public abstract class CraftableToggleButtonWidget extends ButtonWidget {
    
    protected static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final ItemStack ICON = Blocks.CRAFTING_TABLE.asItem().getDefaultInstance();
    private ItemRenderer itemRenderer;
    
    public CraftableToggleButtonWidget(Rectangle rectangle) {
        super(rectangle, "");
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }
    
    public CraftableToggleButtonWidget(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);
        
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRenderer.zLevel = 0.0F;
        this.itemRenderer.renderItemAndEffectIntoGUI(ICON, getBounds().x + 2, getBounds().y + 2);
        this.itemRenderer.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        Minecraft.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.zLevel = 100f;
        this.drawTexturedModalRect(getBounds().x, getBounds().y, (56 + (RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() ? 0 : 20)), 202, 20, 20);
        this.zLevel = 0f;
        if (getBounds().contains(mouseX, mouseY))
            drawTooltip();
    }
    
    private void drawTooltip() {
        ScreenHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.format(RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all"))));
    }
    
}
