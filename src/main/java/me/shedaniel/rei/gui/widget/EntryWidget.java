/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.gui.Element;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class EntryWidget extends WidgetWithBounds {
    
    protected static final Identifier RECIPE_GUI = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    protected static final Identifier RECIPE_GUI_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    protected boolean highlight = true;
    protected boolean tooltips = true;
    protected boolean background = true;
    protected boolean interactable = true;
    protected boolean interactableFavorites = true;
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
        interactableFavorites = interactableFavorites && interactable;
        return this;
    }
    
    public EntryWidget disableFavoritesInteractions() {
        return interactableFavorites(false);
    }
    
    public EntryWidget interactableFavorites(boolean b) {
        interactableFavorites = b && interactable;
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
        if (entryStacks.size() == 1)
            return entryStacks.get(0);
        return entryStacks.get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) entryStacks.size()) / 1f));
    }
    
    public List<EntryStack> entries() {
        return entryStacks;
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
        entry.setZ(100);
        entry.render(getInnerBounds(), mouseX, mouseY, delta);
    }
    
    protected void queueTooltip(int mouseX, int mouseY, float delta) {
        QueuedTooltip tooltip = getCurrentTooltip(mouseX, mouseY);
        if (tooltip != null) {
            if (interactableFavorites && ConfigObject.getInstance().doDisplayFavoritesTooltip() && !ConfigObject.getInstance().getFavoriteKeyCode().isUnknown()) {
                String name = ConfigObject.getInstance().getFavoriteKeyCode().getLocalizedName();
                if (reverseFavoritesAction())
                    tooltip.getText().addAll(Arrays.asList(I18n.translate("text.rei.remove_favorites_tooltip", name).split("\n")));
                else
                    tooltip.getText().addAll(Arrays.asList(I18n.translate("text.rei.favorites_tooltip", name).split("\n")));
            }
            ScreenHelper.getLastOverlay().addTooltip(tooltip);
        }
    }
    
    public QueuedTooltip getCurrentTooltip(int mouseX, int mouseY) {
        return getCurrentEntry().getTooltip(mouseX, mouseY);
    }
    
    protected void drawHighlighted(int mouseX, int mouseY, float delta) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        int color = ScreenHelper.isDarkModeEnabled() ? -1877929711 : -2130706433;
        setZ(300);
        Rectangle bounds = getInnerBounds();
        fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), color, color);
        setZ(0);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!interactable)
            return false;
        if (containsMouse(mouseX, mouseY)) {
            if (button == 0)
                return ClientHelper.getInstance().executeRecipeKeyBind(getCurrentEntry());
            else if (button == 1)
                return ClientHelper.getInstance().executeUsageKeyBind(getCurrentEntry());
        }
        return false;
    }
    
    protected boolean reverseFavoritesAction() {
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (!interactable)
            return false;
        if (containsMouse(PointHelper.fromMouse())) {
            if (interactableFavorites && ConfigObject.getInstance().isFavoritesEnabled() && containsMouse(PointHelper.fromMouse()) && !getCurrentEntry().isEmpty()) {
                ModifierKeyCode keyCode = ConfigObject.getInstance().getFavoriteKeyCode();
                if (keyCode.matchesKey(int_1, int_2)) {
                    if (reverseFavoritesAction())
                        ConfigManager.getInstance().getFavorites().remove(getCurrentEntry());
                    else if (!CollectionUtils.anyMatchEqualsAll(ConfigManager.getInstance().getFavorites(), getCurrentEntry()))
                        ConfigManager.getInstance().getFavorites().add(getCurrentEntry());
                    ContainerScreenOverlay.getEntryListWidget().updateSearch(ScreenHelper.getSearchField().getText());
                    ConfigManager.getInstance().saveConfig();
                    minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
            if (ConfigObject.getInstance().getRecipeKeybind().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeRecipeKeyBind(getCurrentEntry());
            else if (ConfigObject.getInstance().getUsageKeybind().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeUsageKeyBind(getCurrentEntry());
        }
        return false;
    }
}
