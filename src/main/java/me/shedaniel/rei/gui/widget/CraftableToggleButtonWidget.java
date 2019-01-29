package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Arrays;

public abstract class CraftableToggleButtonWidget extends ButtonWidget {
    
    protected static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private ItemRenderer itemRenderer;
    
    public CraftableToggleButtonWidget(Rectangle rectangle) {
        this(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    }
    
    public CraftableToggleButtonWidget(int x, int y, int width, int height) {
        super(x, y, width, height, "");
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);
        
        GuiLighting.enableForItems();
        this.itemRenderer.zOffset = 0.0F;
        this.itemRenderer.renderItemAndGlowInGui(new ItemStack(Blocks.CRAFTING_TABLE), x + 2, y + 2);
        this.itemRenderer.zOffset = 0.0F;
        GuiLighting.disable();
        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.zOffset = 100f;
        this.drawTexturedRect(x, y, (56 + (RoughlyEnoughItemsCore.getConfigHelper().craftableOnly() ? 0 : 20)), 202, 20, 20);
        this.zOffset = 0f;
        if (getBounds().contains(mouseX, mouseY))
            drawTooltip();
    }
    
    private void drawTooltip() {
        GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.translate(RoughlyEnoughItemsCore.getConfigHelper().craftableOnly() ? "text.rei.showing_craftable" : "text.rei.showing_all"))));
    }
    
}
