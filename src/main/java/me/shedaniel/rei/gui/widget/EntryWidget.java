package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.annotations.Experimental;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Experimental
public class EntryWidget extends WidgetWithBounds {
    
    protected static final Identifier RECIPE_GUI = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    protected static final Identifier RECIPE_GUI_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    protected boolean highlight = true;
    protected boolean tooltips = true;
    protected boolean background = true;
    protected boolean interactable = true;
    private Rectangle bounds;
    private List<EntryStack> entryStacks;
    
    protected EntryWidget(int x, int y) {
        this.bounds = new Rectangle(x - 1, y - 1, 18, 18);
        this.entryStacks = new ArrayList<>();
    }
    
    public static EntryWidget create(int x, int y) {
        return new EntryWidget(x, y);
    }
    
    public EntryWidget disableInteractions() {
        return interactable(false);
    }
    
    public EntryWidget interactable(boolean b) {
        interactable = b;
        return this;
    }
    
    public EntryWidget noHighlight() {
        return highlight(false);
    }
    
    public EntryWidget highlight(boolean b) {
        highlight = b;
        return this;
    }
    
    public EntryWidget noTooltips() {
        return tooltips(false);
    }
    
    public EntryWidget tooltips(boolean b) {
        tooltips = b;
        return this;
    }
    
    public EntryWidget noBackground() {
        return background(false);
    }
    
    public EntryWidget background(boolean b) {
        background = b;
        return this;
    }
    
    public EntryWidget clearStacks() {
        entryStacks.clear();
        return this;
    }
    
    public EntryWidget entry(EntryStack stack) {
        entryStacks.add(stack);
        return this;
    }
    
    public EntryWidget entries(Collection<EntryStack> stacks) {
        entryStacks.addAll(stacks);
        return this;
    }
    
    protected EntryStack getCurrentEntry() {
        if (entryStacks.isEmpty())
            return EntryStack.empty();
        return entryStacks.get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) entryStacks.size()) / 1f));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    protected Rectangle getInnerBounds() {
        return new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (background) {
            drawBackground(mouseX, mouseY, delta);
        }
        drawCurrentEntry(mouseX, mouseY, delta);
        
        boolean highlighted = containsMouse(mouseX, mouseY);
        if (tooltips && highlighted) {
            queueTooltip(mouseX, mouseY, delta);
        }
        if (highlight && highlighted) {
            drawHighlighted(mouseX, mouseY, delta);
        }
    }
    
    protected void drawBackground(int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bindTexture(ScreenHelper.isDarkModeEnabled() ? RECIPE_GUI_DARK : RECIPE_GUI);
        blit(bounds.x, bounds.y, 0, 222, bounds.width, bounds.height);
    }
    
    protected void drawCurrentEntry(int mouseX, int mouseY, float delta) {
        EntryStack entry = getCurrentEntry();
        entry.setZ(200);
        entry.render(getInnerBounds(), mouseX, mouseY, delta);
    }
    
    protected void queueTooltip(int mouseX, int mouseY, float delta) {
        EntryStack entry = getCurrentEntry();
        QueuedTooltip tooltip = entry.getTooltip(mouseX, mouseY);
        if (tooltip != null) {
            ScreenHelper.getLastOverlay().addTooltip(tooltip);
        }
    }
    
    protected void drawHighlighted(int mouseX, int mouseY, float delta) {
        RenderHelper.disableLighting();
        RenderHelper.disableDepthTest();
        RenderHelper.colorMask(true, true, true, false);
        int color = ScreenHelper.isDarkModeEnabled() ? -1877929711 : -2130706433;
        blitOffset = 300;
        Rectangle bounds = getInnerBounds();
        fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), color, color);
        blitOffset = 0;
        RenderHelper.colorMask(true, true, true, true);
        RenderHelper.enableLighting();
        RenderHelper.enableDepthTest();
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!interactable)
            return false;
        EntryStack entry = getCurrentEntry();
        if (containsMouse(mouseX, mouseY))
            if (button == 0)
                return ClientHelper.getInstance().executeRecipeKeyBind(entry);
            else if (button == 1)
                return ClientHelper.getInstance().executeUsageKeyBind(entry);
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (!interactable)
            return false;
        EntryStack entry = getCurrentEntry();
        if (containsMouse(PointHelper.fromMouse()))
            if (ClientHelper.getInstance().getRecipeKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeRecipeKeyBind(entry);
            else if (ClientHelper.getInstance().getUsageKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeUsageKeyBind(entry);
        return false;
    }
}
