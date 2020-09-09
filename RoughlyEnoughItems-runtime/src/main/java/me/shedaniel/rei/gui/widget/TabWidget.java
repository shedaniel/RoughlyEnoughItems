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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Tooltip;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@ApiStatus.Internal
public class TabWidget extends WidgetWithBounds {
    
    public static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final ResourceLocation CHEST_GUI_TEXTURE_DARK = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    public boolean shown = false, selected = false;
    public EntryStack logo;
    public int id;
    public String categoryName;
    public Rectangle bounds;
    public RecipeCategory<?> category;
    public int u, v;
    @Nullable
    private Predicate<TabWidget> onClick;
    
    private TabWidget(int id, Rectangle bounds, int u, int v, @Nullable Predicate<TabWidget> onClick) {
        this.id = id;
        this.bounds = bounds;
        this.u = u;
        this.v = v;
        this.onClick = onClick;
    }
    
    @ApiStatus.Internal
    public static TabWidget create(int id, int tabSize, int leftX, int bottomY, int u, int v, @Nullable Predicate<TabWidget> onClick) {
        return new TabWidget(id, new Rectangle(leftX + id * tabSize, bottomY - tabSize, tabSize, tabSize), u, v, onClick);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return button == 0 && containsMouse(mouseX, mouseY) && onClick.test(this);
    }
    
    public void setRenderer(RecipeCategory<?> category, EntryStack logo, String categoryName, boolean selected) {
        if (logo == null) {
            shown = false;
            this.logo = null;
        } else {
            shown = true;
            this.logo = logo;
        }
        this.category = category;
        this.selected = selected;
        this.categoryName = categoryName;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isShown() {
        return shown;
    }
    
    @Override
    public List<Widget> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (shown) {
            minecraft.getTextureManager().bind(REIHelper.getInstance().isDarkThemeEnabled() ? CHEST_GUI_TEXTURE_DARK : CHEST_GUI_TEXTURE);
            this.blit(matrices, bounds.x, bounds.y + 2, u + (selected ? bounds.width : 0), v, bounds.width, (selected ? bounds.height + 2 : bounds.height - 1));
            logo.setZ(100);
            logo.render(matrices, new Rectangle(bounds.getCenterX() - 8, bounds.getCenterY() - 5, 16, 16), mouseX, mouseY, delta);
            if (containsMouse(mouseX, mouseY)) {
                drawTooltip();
            }
        }
    }
    
    private void drawTooltip() {
        if (this.minecraft.options.advancedItemTooltips)
            Tooltip.create(new StringTextComponent(categoryName), new StringTextComponent(category.getIdentifier().toString()).withStyle(TextFormatting.DARK_GRAY), ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())).queue();
        else
            Tooltip.create(new StringTextComponent(categoryName), ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())).queue();
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
}
