/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
    private static final ItemStack ICON = new ItemStack(Blocks.CRAFTING_TABLE);
    private ItemRenderer itemRenderer;
    
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
