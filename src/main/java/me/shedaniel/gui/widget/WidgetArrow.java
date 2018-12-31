package me.shedaniel.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

public class WidgetArrow extends Control {
    private static final ResourceLocation RECIPE_GUI = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private int progress = 0;
    private int updateTick = 0;
    private boolean animated;
    
    public WidgetArrow(int x, int y, boolean animated) {
        super(x, y, 22, 18);
        this.animated = animated;
    }
    
    @Override
    public void draw() {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getInstance().getTextureManager().bindTexture(RECIPE_GUI);
        this.drawTexturedModalRect(rect.x, rect.y, 18, 222, 22, 18);
        if (animated) {
            int width = (int) ((progress / 10f) * 22);
            this.drawTexturedModalRect(rect.x - 1, rect.y - 1, 40, 222, width, 18);
        }
    }
    
    @Override
    public void tick() {
        updateTick++;
        if (updateTick >= 20) {
            updateTick = 0;
            
            progress++;
            if (progress > 10)
                progress = 0;
        }
    }
}
