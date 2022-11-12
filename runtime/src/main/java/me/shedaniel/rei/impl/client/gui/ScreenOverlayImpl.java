/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProvider;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitor;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.craftable.CraftableFilter;
import me.shedaniel.rei.impl.client.gui.dragging.CurrentDraggingStack;
import me.shedaniel.rei.impl.client.gui.modules.MenuAccess;
import me.shedaniel.rei.impl.client.gui.modules.MenuHolder;
import me.shedaniel.rei.impl.client.gui.widget.*;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.PaginatedEntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.ScrolledEntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
import me.shedaniel.rei.impl.common.util.RectangleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

@ApiStatus.Internal
public abstract class ScreenOverlayImpl extends ScreenOverlay {
    private static final List<Tooltip> TOOLTIPS = Lists.newArrayList();
    private static EntryListWidget entryListWidget = null;
    private static FavoritesListWidget favoritesListWidget = null;
    private final List<Widget> widgets = Lists.newLinkedList();
    public boolean shouldReload = false;
    public boolean shouldReloadSearch = false;
    private Rectangle bounds;
    private Window window;
    private Widget configButton;
    private CurrentDraggingStack draggingStack = new CurrentDraggingStack();
    @Nullable
    public DefaultDisplayChoosePageWidget choosePageWidget;
    private MenuHolder menuHolder = new MenuHolder();
    
    public static EntryListWidget getEntryListWidget() {
        boolean widgetScrolled = ConfigObject.getInstance().isEntryListWidgetScrolled();
        
        if (entryListWidget != null) {
            if (widgetScrolled && entryListWidget instanceof ScrolledEntryListWidget) {
                return entryListWidget;
            } else if (!widgetScrolled && entryListWidget instanceof PaginatedEntryListWidget) {
                return entryListWidget;
            }
        }
        
        entryListWidget = widgetScrolled ? new ScrolledEntryListWidget() : new PaginatedEntryListWidget();
        
        ScreenOverlayImpl overlay = ScreenOverlayImpl.getInstance();
        Rectangle overlayBounds = overlay.bounds;
        entryListWidget.updateArea(Objects.requireNonNullElse(overlayBounds, new Rectangle()), REIRuntimeImpl.getSearchField() == null ? "" : REIRuntimeImpl.getSearchField().getText());
        entryListWidget.updateEntriesPosition();
        
        return entryListWidget;
    }
    
    @Nullable
    public static FavoritesListWidget getFavoritesListWidget() {
        return favoritesListWidget;
    }
    
    public static ScreenOverlayImpl getInstance() {
        return (ScreenOverlayImpl) REIRuntime.getInstance().getOverlay().get();
    }
    
    public void tick() {
        if (REIRuntimeImpl.getSearchField() != null) {
            REIRuntimeImpl.getSearchField().tick();
            if (Minecraft.getInstance().player != null && !PluginManager.areAnyReloading() && Minecraft.getInstance().player.tickCount % 5 == 0) {
                CraftableFilter.INSTANCE.tick();
            }
        }
    }
    
    @Override
    public void queueReloadOverlay() {
        shouldReload = true;
    }
    
    @Override
    public void queueReloadSearch() {
        shouldReloadSearch = true;
    }
    
    @Override
    public DraggingContext<?> getDraggingContext() {
        return draggingStack;
    }
    
    protected boolean hasSpace() {
        return !this.bounds.isEmpty();
    }
    
    public void init() {
        this.draggingStack.set(DraggableComponentProvider.from(ScreenRegistry.getInstance()::getDraggableComponentProviders),
                DraggableComponentVisitor.from(ScreenRegistry.getInstance()::getDraggableComponentVisitors));
        
        this.shouldReload = false;
        this.shouldReloadSearch = false;
        this.children().clear();
        this.window = Minecraft.getInstance().getWindow();
        this.bounds = calculateOverlayBounds();
        
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            if (favoritesListWidget == null) {
                favoritesListWidget = new FavoritesListWidget();
            }
            favoritesListWidget.favoritePanel.resetRows();
            this.widgets.add(favoritesListWidget);
        }
        
        OverlaySearchField searchField = REIRuntimeImpl.getSearchField();
        searchField.getBounds().setBounds(getSearchFieldArea());
        this.widgets.add(searchField);
        
        EntryListWidget entryListWidget = getEntryListWidget();
        entryListWidget.updateArea(this.bounds, searchField.getText());
        this.widgets.add(entryListWidget);
        searchField.setResponder(s -> entryListWidget.updateSearch(s, false));
        entryListWidget.init(this);
        
        this.widgets.add(configButton = ConfigButtonWidget.create(this));
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) {
            this.widgets.add(CraftableFilterButtonWidget.create(this));
        }
        
        this.widgets.add(draggingStack);
    }
    
    private Rectangle getSearchFieldArea() {
        int widthRemoved = 1;
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) widthRemoved += 22;
        if (ConfigObject.getInstance().isLowerConfigButton()) widthRemoved += 22;
        SearchFieldLocation searchFieldLocation = REIRuntime.getInstance().getContextualSearchFieldLocation();
        return switch (searchFieldLocation) {
            case TOP_SIDE -> getTopSideSearchFieldArea(widthRemoved);
            case BOTTOM_SIDE -> getBottomSideSearchFieldArea(widthRemoved);
            case CENTER -> getCenterSearchFieldArea(widthRemoved);
        };
    }
    
    private Rectangle getTopSideSearchFieldArea(int widthRemoved) {
        return new Rectangle(bounds.x + 2, 4, bounds.width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getBottomSideSearchFieldArea(int widthRemoved) {
        return new Rectangle(bounds.x + 2, window.getGuiScaledHeight() - 22, bounds.width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getCenterSearchFieldArea(int widthRemoved) {
        Rectangle screenBounds = ScreenRegistry.getInstance().getScreenBounds(minecraft.screen);
        return new Rectangle(screenBounds.x, window.getGuiScaledHeight() - 22, screenBounds.width - widthRemoved, 18);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (shouldReload || !calculateOverlayBounds().equals(bounds)) {
            init();
            getEntryListWidget().updateSearch(REIRuntimeImpl.getSearchField().getText(), true);
        } else {
            for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(minecraft.screen)) {
                if (decider != null && decider.shouldRecalculateArea(ConfigObject.getInstance().getDisplayPanelLocation(), bounds)) {
                    init();
                    break;
                }
            }
        }
        if (shouldReloadSearch || (ConfigManager.getInstance().isCraftableOnlyEnabled() && CraftableFilter.INSTANCE.wasDirty())) {
            shouldReloadSearch = false;
            getEntryListWidget().updateSearch(REIRuntimeImpl.getSearchField().getText(), true);
        }
        if (OverlaySearchField.isHighlighting) {
            EntryHighlighter.render(matrices);
        }
        if (!hasSpace()) return;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderWidgets(matrices, mouseX, mouseY, delta);
        if (ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            Screen screen = Minecraft.getInstance().screen;
            ClickArea.ClickAreaContext<Screen> context = createClickAreaContext(mouseX, mouseY, screen);
            List<Component> clickAreaTooltips = ScreenRegistry.getInstance().getClickAreaTooltips((Class<Screen>) screen.getClass(), context);
            if (clickAreaTooltips != null && !clickAreaTooltips.isEmpty()) {
                Tooltip.create(clickAreaTooltips).queue();
            }
        }
    }
    
    private ClickArea.ClickAreaContext<Screen> createClickAreaContext(double mouseX, double mouseY, Screen screen) {
        return new ClickArea.ClickAreaContext<>() {
            @Override
            public Screen getScreen() {
                return screen;
            }
            
            @Override
            public Point getMousePosition() {
                return new Point(mouseX, mouseY);
            }
        };
    }
    
    private static Rectangle calculateOverlayBounds() {
        Rectangle bounds = ScreenRegistry.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation(), Minecraft.getInstance().screen);
        
        int widthReduction = (int) Math.round(bounds.width * (1 - ConfigObject.getInstance().getHorizontalEntriesBoundariesPercentage()));
        if (ConfigObject.getInstance().getDisplayPanelLocation() == DisplayPanelLocation.RIGHT)
            bounds.x += widthReduction;
        bounds.width -= widthReduction;
        int maxWidth = (int) Math.ceil(entrySize() * ConfigObject.getInstance().getHorizontalEntriesBoundariesColumns() + entrySize() * 0.75);
        if (bounds.width > maxWidth) {
            if (ConfigObject.getInstance().getDisplayPanelLocation() == DisplayPanelLocation.RIGHT)
                bounds.x += bounds.width - maxWidth;
            bounds.width = maxWidth;
        }
        
        return avoidButtons(bounds);
    }
    
    private static Rectangle avoidButtons(Rectangle bounds) {
        int buttonsHeight = 2;
        if (REIRuntime.getInstance().getContextualSearchFieldLocation() == SearchFieldLocation.TOP_SIDE) buttonsHeight += 24;
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) buttonsHeight += 22;
        Rectangle area = REIRuntime.getInstance().calculateEntryListArea(bounds).clone();
        area.height = buttonsHeight;
        return RectangleUtils.excludeZones(bounds, ScreenRegistry.getInstance().exclusionZones().getExclusionZones(Minecraft.getInstance().screen).stream()
                .filter(zone -> zone.intersects(area)));
    }
    
    public void lateRender(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (REIRuntime.getInstance().isOverlayVisible() && hasSpace()) {
            for (Widget widget : widgets) {
                if (widget instanceof LateRenderable && widget != menuHolder.widget())
                    widget.render(matrices, mouseX, mouseY, delta);
            }
            matrices.pushPose();
            matrices.translate(0, 0, 500);
            menuHolder.lateRender(matrices, mouseX, mouseY, delta);
            matrices.popPose();
            if (choosePageWidget != null) {
                setBlitOffset(500);
                this.fillGradient(matrices, 0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), -1072689136, -804253680);
                setBlitOffset(0);
                choosePageWidget.render(matrices, mouseX, mouseY, delta);
            }
        }
        if (choosePageWidget == null) {
            TOOLTIPS.stream().filter(Objects::nonNull)
                    .reduce((tooltip, tooltip2) -> tooltip2)
                    .ifPresent(tooltip -> renderTooltip(matrices, tooltip));
        }
        TOOLTIPS.clear();
        if (REIRuntime.getInstance().isOverlayVisible()) {
            menuHolder.afterRender();
        }
    }
    
    public void renderTooltip(PoseStack matrices, Tooltip tooltip) {
        renderTooltipInner(minecraft.screen, matrices, tooltip, tooltip.getX(), tooltip.getY());
    }
    
    protected abstract void renderTooltipInner(Screen screen, PoseStack matrices, Tooltip tooltip, int mouseX, int mouseY);
    
    public void addTooltip(@Nullable Tooltip tooltip) {
        if (tooltip != null)
            TOOLTIPS.add(tooltip);
    }
    
    public void clearTooltips() {
        TOOLTIPS.clear();
    }
    
    public void renderWidgets(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return;
        for (Widget widget : widgets) {
            if (!(widget instanceof LateRenderable))
                widget.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (menuHolder.mouseScrolled(mouseX, mouseY, amount))
            return true;
        if (isInside(mouseX, mouseY) && getEntryListWidget().mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }
        if (isNotInExclusionZones(PointHelper.getMouseX(), PointHelper.getMouseY())) {
            if (favoritesListWidget != null && favoritesListWidget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        }
        for (Widget widget : widgets)
            if (widget != getEntryListWidget() && (favoritesListWidget == null || widget != favoritesListWidget)
                && widget != menuHolder.widget()
                && widget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!hasSpace()) return false;
        if (REIRuntime.getInstance().isOverlayVisible()) {
            if (keyCode == 256 && choosePageWidget != null) {
                choosePageWidget = null;
                return true;
            }
            if (choosePageWidget != null)
                return choosePageWidget.keyPressed(keyCode, scanCode, modifiers);
            if (REIRuntimeImpl.getSearchField().keyPressed(keyCode, scanCode, modifiers))
                return true;
            for (GuiEventListener listener : widgets)
                if (listener != REIRuntimeImpl.getSearchField() && listener.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        }
        if (ConfigObject.getInstance().getHideKeybind().matchesKey(keyCode, scanCode)) {
            REIRuntime.getInstance().toggleOverlayVisible();
            return true;
        }
        EntryStack<?> stack = ScreenRegistry.getInstance().getFocusedStack(Minecraft.getInstance().screen, PointHelper.ofMouse());
        if (stack != null && !stack.isEmpty()) {
            stack = stack.copy();
            if (ConfigObject.getInstance().getRecipeKeybind().matchesKey(keyCode, scanCode)) {
                return ViewSearchBuilder.builder().addRecipesFor(stack).open();
            } else if (ConfigObject.getInstance().getUsageKeybind().matchesKey(keyCode, scanCode)) {
                return ViewSearchBuilder.builder().addUsagesFor(stack).open();
            } else if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(keyCode, scanCode)) {
                FavoriteEntry favoriteEntry = FavoriteEntry.fromEntryStack(stack);
                ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                return true;
            }
        }
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesKey(keyCode, scanCode)) {
            REIRuntimeImpl.getSearchField().setFocused(true);
            setFocused(REIRuntimeImpl.getSearchField());
            REIRuntimeImpl.getSearchField().keybindFocusTime = System.currentTimeMillis();
            REIRuntimeImpl.getSearchField().keybindFocusKey = keyCode;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!hasSpace()) return false;
        if (REIRuntime.getInstance().isOverlayVisible()) {
            if (choosePageWidget == null) {
                if (REIRuntimeImpl.getSearchField().keyReleased(keyCode, scanCode, modifiers))
                    return true;
                for (GuiEventListener listener : widgets)
                    if (listener != REIRuntimeImpl.getSearchField() && listener == getFocused() && listener.keyPressed(keyCode, scanCode, modifiers))
                        return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (!hasSpace()) return false;
        if (choosePageWidget != null) {
            return choosePageWidget.charTyped(character, modifiers);
        }
        if (REIRuntimeImpl.getSearchField().charTyped(character, modifiers))
            return true;
        for (GuiEventListener listener : widgets)
            if (listener != REIRuntimeImpl.getSearchField() && listener.charTyped(character, modifiers))
                return true;
        return false;
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean visible = REIRuntime.getInstance().isOverlayVisible();
        if (choosePageWidget != null) {
            if (choosePageWidget.containsMouse(mouseX, mouseY)) {
                return choosePageWidget.mouseClicked(mouseX, mouseY, button);
            } else {
                choosePageWidget = null;
                init();
                return false;
            }
        }
        if (!hasSpace()) return false;
        if (visible && configButton.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(configButton);
            if (button == 0)
                this.setDragging(true);
            return true;
        }
        if (ConfigObject.getInstance().getHideKeybind().matchesMouse(button)) {
            REIRuntime.getInstance().toggleOverlayVisible();
            return REIRuntime.getInstance().isOverlayVisible();
        }
        EntryStack<?> stack = ScreenRegistry.getInstance().getFocusedStack(Minecraft.getInstance().screen, PointHelper.ofMouse());
        if (stack != null && !stack.isEmpty()) {
            stack = stack.copy();
            if (ConfigObject.getInstance().getRecipeKeybind().matchesMouse(button)) {
                return ViewSearchBuilder.builder().addRecipesFor(stack).open();
            } else if (ConfigObject.getInstance().getUsageKeybind().matchesMouse(button)) {
                return ViewSearchBuilder.builder().addUsagesFor(stack).open();
            } else if (visible && ConfigObject.getInstance().getFavoriteKeyCode().matchesMouse(button)) {
                FavoriteEntry favoriteEntry = FavoriteEntry.fromEntryStack(stack);
                ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                return true;
            }
        }
        if (visible) {
            Widget menuWidget = menuHolder.widget();
            if (menuWidget != null && menuWidget.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(menuWidget);
                if (button == 0)
                    this.setDragging(true);
                REIRuntimeImpl.getSearchField().setFocused(false);
                return true;
            }
        }
        if (ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            Screen screen = Minecraft.getInstance().screen;
            ClickArea.ClickAreaContext<Screen> context = createClickAreaContext(mouseX, mouseY, screen);
            if (ScreenRegistry.getInstance().executeClickArea((Class<Screen>) screen.getClass(), context)) {
                return true;
            }
        }
        if (!visible) {
            return false;
        }
        for (GuiEventListener element : widgets) {
            if (element != configButton && element != menuHolder.widget() && element.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(element);
                if (button == 0)
                    this.setDragging(true);
                if (!(element instanceof OverlaySearchField))
                    REIRuntimeImpl.getSearchField().setFocused(false);
                return true;
            }
        }
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesMouse(button)) {
            REIRuntimeImpl.getSearchField().setFocused(true);
            setFocused(REIRuntimeImpl.getSearchField());
            REIRuntimeImpl.getSearchField().keybindFocusTime = -1;
            REIRuntimeImpl.getSearchField().keybindFocusKey = -1;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (!hasSpace()) return false;
        if (choosePageWidget != null) {
            return choosePageWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return (this.getFocused() != null && this.isDragging() && button == 0) && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public GuiEventListener getFocused() {
        if (choosePageWidget != null)
            return choosePageWidget;
        return super.getFocused();
    }
    
    public boolean isInside(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY) && isNotInExclusionZones(mouseX, mouseY);
    }
    
    @Override
    public boolean isNotInExclusionZones(double mouseX, double mouseY) {
        for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(Minecraft.getInstance().screen)) {
            InteractionResult in = decider.isInZone(mouseX, mouseY);
            if (in != InteractionResult.PASS)
                return in == InteractionResult.SUCCESS;
        }
        return true;
    }
    
    public boolean isInside(Point point) {
        return isInside(point.getX(), point.getY());
    }
    
    @Override
    public OverlayListWidget getEntryList() {
        return getEntryListWidget();
    }
    
    @Override
    public Optional<OverlayListWidget> getFavoritesList() {
        return Optional.ofNullable(getFavoritesListWidget());
    }
    
    public MenuAccess menuAccess() {
        return menuHolder;
    }
}
