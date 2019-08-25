/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Optional;

public abstract class CraftableToggleButtonWidget extends ButtonWidget {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private ItemRenderer itemRenderer;
    
    public CraftableToggleButtonWidget(Rectangle rectangle) {
        super(rectangle, "");
        this.itemRenderer = minecraft.getItemRenderer();
    }
    
    public CraftableToggleButtonWidget(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }
    
    public void lateRender(int mouseX, int mouseY, float delta) {
        GuiLighting.disable();
        super.render(mouseX, mouseY, delta);
        
        GuiLighting.enableForItems();
        this.itemRenderer.zOffset = this.blitOffset;
        this.itemRenderer.renderGuiItem(new ItemStack(Blocks.CRAFTING_TABLE), getBounds().x + 2, getBounds().y + 2);
        this.itemRenderer.zOffset = 0.0F;
        GuiLighting.disable();
        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        RenderHelper.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int color = RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() ? 939579655 : 956235776;
        this.blitOffset += 10f;
        this.fillGradient(getBounds().x, getBounds().y, getBounds().x + getBounds().width, getBounds().y + getBounds().height, color, color);
        this.blitOffset = 0;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
    }
    
    @Override
    public boolean changeFocus(boolean boolean_1) {
        return false;
    }
    
    @Override
    public Optional<String> getTooltips() {
        return Optional.ofNullable(I18n.translate(RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all"));
    }
}
