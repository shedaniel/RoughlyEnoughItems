package me.shedaniel.rei.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeBaseWidget extends Gui implements IWidget {
    
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
    private Rectangle bounds;
    
    public RecipeBaseWidget(Rectangle bounds) {
        this.bounds = bounds;
    }
    
    @Override
    public List<IWidget> getListeners() {
        return new ArrayList<>();
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        drawTexturedModalRect(bounds.x, bounds.y, 106, 190, bounds.width, bounds.height);
    }
    
}
