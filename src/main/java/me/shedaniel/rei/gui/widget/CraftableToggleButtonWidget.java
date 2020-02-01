/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.ConfigManager;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public abstract class CraftableToggleButtonWidget extends LateRenderedButton {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private ItemRenderer itemRenderer;
    private static final ItemStack ICON = new ItemStack(Blocks.CRAFTING_TABLE);
    
    public CraftableToggleButtonWidget(Rectangle rectangle) {
        super(rectangle, "");
        this.itemRenderer = minecraft.getItemRenderer();
    }
    
    public CraftableToggleButtonWidget(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }
    
    @Override
    public void lateRender(int mouseX, int mouseY, float delta) {
        setBlitOffset(600);
        super.render(mouseX, mouseY, delta);
        
        this.itemRenderer.zOffset = getBlitOffset() - 98;
        Rectangle bounds = getBounds();
        this.itemRenderer.renderGuiItemIcon(ICON, bounds.x + 2, bounds.y + 2);
        this.itemRenderer.zOffset = 0.0F;
        int color = ConfigManager.getInstance().isCraftableOnlyEnabled() ? 939579655 : 956235776;
        setBlitOffset(getBlitOffset() + 1);
        this.fillGradient(bounds.x + 1, bounds.y + 1, bounds.getMaxX() - 1, bounds.getMaxY() - 1, color, color);
        setBlitOffset(0);
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
        return Optional.ofNullable(I18n.translate(ConfigManager.getInstance().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all"));
    }
}
