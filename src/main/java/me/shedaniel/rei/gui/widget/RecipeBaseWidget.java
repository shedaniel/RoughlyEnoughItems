package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class RecipeBaseWidget extends HighlightableWidget {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final Color INNER_COLOR = new Color(198, 198, 198);
    
    private Rectangle bounds;
    
    public RecipeBaseWidget(Rectangle bounds) {
        this.bounds = bounds;
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public List<Widget> children() {
        return Collections.emptyList();
    }
    
    public void render() {
        render(0, 0, 0);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        blit(bounds.x, bounds.y, 106, 190, bounds.width / 2, bounds.height / 2);
        blit(bounds.x + bounds.width / 2, bounds.y, 256 - bounds.width / 2, 190, bounds.width / 2, bounds.height / 2);
        blit(bounds.x, bounds.y + bounds.height / 2, 106, 190 + 66 - bounds.height / 2, bounds.width / 2, bounds.height / 2);
        blit(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, 256 - bounds.width / 2, 190 + 66 - bounds.height / 2, bounds.width / 2, bounds.height / 2);
        if (bounds.height > 40)
            for(int i = 20; i < bounds.height - 20; i += MathHelper.clamp(20, 0, bounds.height - 20 - i)) {
                int height = MathHelper.clamp(20, 0, bounds.height - 20 - i);
                blit(bounds.x, bounds.y + i, 106, 230, bounds.width / 2, height);
                blit(bounds.x + bounds.width / 2, bounds.y + i, 256 - bounds.width / 2, 210, bounds.width / 2, height);
            }
        if (bounds.width > 40)
            for(int i = 20; i < bounds.width - 20; i += MathHelper.clamp(40, 0, bounds.width - 20 - i)) {
                int width = MathHelper.clamp(40, 0, bounds.width - 20 - i);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                blit(bounds.x + i, bounds.y, 113, 190, width, MathHelper.clamp(4, 0, bounds.height / 2));
                blit(bounds.x + i, bounds.y + bounds.height - 4, 113, 252, width, MathHelper.clamp(4, 0, bounds.height / 2));
                fill(bounds.x + i, bounds.y + 4, bounds.x + i + width, bounds.y + bounds.height - 4, INNER_COLOR.getRGB());
            }
    }
    
}
