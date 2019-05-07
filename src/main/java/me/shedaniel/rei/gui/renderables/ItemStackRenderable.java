package me.shedaniel.rei.gui.renderables;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.Renderable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public abstract class ItemStackRenderable extends DrawableHelper implements Renderable {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
        int l = x - 8, i1 = y - 6;
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.blitOffset = 100;
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.zOffset = 100.0F;
        GuiLighting.enableForItems();
        itemRenderer.renderGuiItem(getItemStack(), l, i1);
        itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, getItemStack(), l, i1);
        GlStateManager.disableLighting();
        itemRenderer.zOffset = 0.0F;
        this.blitOffset = 0;
    }
    
    protected abstract ItemStack getItemStack();
    
}
