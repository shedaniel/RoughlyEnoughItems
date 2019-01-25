package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeBaseWidget extends Drawable implements IWidget {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
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
        GuiLighting.disable();
        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        drawTexturedRect(bounds.x, bounds.y, 106, 190, bounds.width / 2, bounds.height / 2);
        drawTexturedRect(bounds.x + bounds.width / 2, bounds.y, 256 - bounds.width / 2, 190, bounds.width / 2, bounds.height / 2);
        drawTexturedRect(bounds.x, bounds.y + bounds.height / 2, 106, 190 + 66 - bounds.height / 2, bounds.width / 2, bounds.height / 2);
        drawTexturedRect(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, 256 - bounds.width / 2, 190 + 66 - bounds.height / 2, bounds.width / 2, bounds.height / 2);
        if (bounds.height > 40)
            for(int i = 20; i < bounds.height - 20; i += MathHelper.clamp(20, 0, bounds.height - 20 - i)) {
                int height = MathHelper.clamp(20, 0, bounds.height - 20 - i);
                drawTexturedRect(bounds.x, bounds.y + i, 106, 230, bounds.width / 2, height);
                drawTexturedRect(bounds.x + bounds.width / 2, bounds.y + i, 256 - bounds.width / 2, 210, bounds.width / 2, height);
            }
    }
    
}
