package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;

public abstract class CraftableToggleButtonWidget extends ButtonWidget {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private ItemRenderer itemRenderer;
    
    public CraftableToggleButtonWidget(Rectangle rectangle) {
        super(rectangle, "");
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    }
    
    public CraftableToggleButtonWidget(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        GuiLighting.disable();
        super.render(mouseX, mouseY, partialTicks);
        
        GuiLighting.enableForItems();
        this.itemRenderer.zOffset = 0.0F;
        this.itemRenderer.renderGuiItem(new ItemStack(Blocks.CRAFTING_TABLE), getBounds().x + 2, getBounds().y + 2);
        this.itemRenderer.zOffset = 0.0F;
        GuiLighting.disable();
        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.blitOffset = 100f;
        this.blit(getBounds().x, getBounds().y, (56 + (RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() ? 0 : 20)), 202, 20, 20);
        this.blitOffset = 0f;
        if (getBounds().contains(mouseX, mouseY))
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(I18n.translate(RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all")));
    }
    
}
