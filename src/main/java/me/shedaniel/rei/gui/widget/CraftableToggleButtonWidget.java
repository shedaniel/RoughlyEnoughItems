/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.RoughlyEnoughItemsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Optional;

public abstract class CraftableToggleButtonWidget extends ButtonWidget {
    
    public static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private ItemRenderer itemRenderer;
    
    public CraftableToggleButtonWidget(Rectangle rectangle) {
        super(rectangle, "");
        this.itemRenderer = minecraft.getItemRenderer();
    }
    
    public CraftableToggleButtonWidget(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }
    
    public void lateRender(int mouseX, int mouseY, float delta) {
        RenderHelper.disableStandardItemLighting();
        super.render(mouseX, mouseY, delta);
        
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRenderer.zLevel = this.zLevel;
        this.itemRenderer.renderItemIntoGUI(new ItemStack(Blocks.CRAFTING_TABLE), getBounds().x + 2, getBounds().y + 2);
        this.itemRenderer.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        Minecraft.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int color = RoughlyEnoughItemsClient.getConfigManager().isCraftableOnlyEnabled() ? 939579655 : 956235776;
        this.zLevel += 10f;
        this.drawGradientRect(getBounds().x, getBounds().y, getBounds().x + getBounds().width, getBounds().y + getBounds().height, color, color);
        this.zLevel = 0;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
    }
    
    @Override
    public Optional<String> getTooltips() {
        return Optional.ofNullable(I18n.format(RoughlyEnoughItemsClient.getConfigManager().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all"));
    }
}
