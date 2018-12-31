package me.shedaniel.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.util.Identifier;

public class WidgetArrow extends Control {
    private static final Identifier RECIPE_GUI = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private int progress = 0;
    private int updateTick = 0;
    private final int speed;
    private boolean animated;
    
    public WidgetArrow(int x, int y, boolean animated) {
        this(x, y, animated, 20);
    }
    
    public WidgetArrow(int x, int y, boolean animated, int speed) {
        super(x, y, 22, 18);
        this.animated = animated;
        this.speed = speed;
    }
    
    @Override
    public void draw() {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        MinecraftClient.getInstance().getTextureManager().bindTexture(RECIPE_GUI);
        this.drawTexturedModalRect(rect.x, rect.y, 18, 222, 22, 18);
        if (animated) {
            int width = (int) ((progress / 10f) * 22);
            this.drawTexturedModalRect(rect.x - 1, rect.y - 1, 40, 222, width, 18);
        }
    }
    
    @Override
    public void tick() {
        updateTick++;
        if (updateTick >= speed) {
            updateTick = 0;
            
            progress++;
            if (progress > 10)
                progress = 0;
        }
    }
}
