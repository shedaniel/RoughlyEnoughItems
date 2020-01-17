/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class TabWidget extends WidgetWithBounds {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final Identifier CHEST_GUI_TEXTURE_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    public boolean shown = false, selected = false;
    public EntryStack logo;
    public int id;
    public String categoryName;
    public Rectangle bounds;
    public RecipeCategory<?> category;
    public int u, v;
    
    public TabWidget(int id, Rectangle bounds) {
        this(id, bounds, 0, 192);
    }
    
    public TabWidget(int id, Rectangle bounds, int u, int v) {
        this.id = id;
        this.bounds = bounds;
        this.u = u;
        this.v = v;
    }
    
    public TabWidget(int id, int tabSize, int leftX, int bottomY) {
        this(id, new Rectangle(leftX + id * tabSize, bottomY - tabSize, tabSize, tabSize));
    }
    
    public TabWidget(int id, int tabSize, int leftX, int bottomY, int u, int v) {
        this(id, new Rectangle(leftX + id * tabSize, bottomY - tabSize, tabSize, tabSize), u, v);
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
    public void render(int mouseX, int mouseY, float delta) {
        if (shown) {
            minecraft.getTextureManager().bindTexture(ScreenHelper.isDarkModeEnabled() ? CHEST_GUI_TEXTURE_DARK : CHEST_GUI_TEXTURE);
            this.blit(bounds.x, bounds.y + 2, u + (selected ? bounds.width : 0), v, bounds.width, (selected ? bounds.height + 2 : bounds.height - 1));
            logo.setZ(100);
            logo.render(new Rectangle(bounds.getCenterX() - 8, bounds.getCenterY() - 5, 16, 16), mouseX, mouseY, delta);
            if (containsMouse(mouseX, mouseY)) {
                drawTooltip();
            }
        }
    }
    
    private void drawTooltip() {
        if (this.minecraft.options.advancedItemTooltips)
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(categoryName, Formatting.DARK_GRAY.toString() + category.getIdentifier().toString(), ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())));
        else
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(categoryName, ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
}
