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

package me.shedaniel.rei.impl.client.gui.overlay;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProvider;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitor;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.TooltipQueue;
import me.shedaniel.rei.impl.client.gui.dragging.CurrentDraggingStack;
import me.shedaniel.rei.impl.client.gui.menu.MenuAccess;
import me.shedaniel.rei.impl.client.gui.menu.MenuAccessImpl;
import me.shedaniel.rei.impl.client.gui.widget.EntryHighlighter;
import me.shedaniel.rei.impl.client.gui.widget.LateRenderable;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import java.util.List;

public abstract class AbstractScreenOverlay extends ScreenOverlay {
    private final List<Widget> widgets = Lists.newLinkedList();
    private final CurrentDraggingStack draggingStack = new CurrentDraggingStack();
    private final MenuAccessImpl menuAccess = new MenuAccessImpl();
    private final Rectangle bounds = new Rectangle();
    private boolean shouldReload = false;
    private boolean shouldReloadSearch = false;
    
    @Override
    public void queueReloadOverlay() {
        shouldReload = true;
    }
    
    @Override
    public void queueReloadSearch() {
        shouldReloadSearch = true;
    }
    
    @Override
    public boolean isOverlayReloadQueued() {
        return shouldReload;
    }
    
    @Override
    public boolean isSearchReloadQueued() {
        return shouldReloadSearch || shouldReload;
    }
    
    @Override
    public DraggingContext<?> getDraggingContext() {
        return draggingStack;
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    protected boolean hasSpace() {
        return !this.bounds.isEmpty();
    }
    
    public void init() {
        this.draggingStack.set(DraggableComponentProvider.from(ScreenRegistry.getInstance()::getDraggableComponentProviders),
                DraggableComponentVisitor.from(ScreenRegistry.getInstance()::getDraggableComponentVisitors));
        
        this.shouldReload = false;
        this.shouldReloadSearch = false;
        this.bounds.setBounds(InternalOverlayBounds.calculateOverlayBounds());
        this.widgets.clear();
        this.widgets.add(draggingStack);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (shouldReload || !InternalOverlayBounds.calculateOverlayBounds().equals(bounds)) {
            init();
            updateSearch();
        } else {
            for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(minecraft.screen)) {
                if (decider != null && decider.shouldRecalculateArea(ConfigObject.getInstance().getDisplayPanelLocation(), bounds)) {
                    init();
                    break;
                }
            }
        }
        if (shouldReloadSearch) {
            shouldReloadSearch = false;
            updateSearch();
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
    
    public void lateRender(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (REIRuntime.getInstance().isOverlayVisible() && hasSpace()) {
            getSearchField().asWidget().render(matrices, mouseX, mouseY, delta);
            for (Widget widget : widgets) {
                if (widget instanceof LateRenderable && widget != menuAccess.widget())
                    widget.render(matrices, mouseX, mouseY, delta);
            }
            matrices.pushPose();
            matrices.translate(0, 0, 500);
            menuAccess.lateRender(matrices, mouseX, mouseY, delta);
            matrices.popPose();
        }
        
        Tooltip tooltip = TooltipQueue.get();
        if (tooltip != null) {
            renderTooltip(matrices, tooltip);
        }
        
        REIRuntime.getInstance().clearTooltips();
        if (REIRuntime.getInstance().isOverlayVisible()) {
            menuAccess.afterRender();
        }
    }
    
    @Override
    public void renderTooltip(PoseStack matrices, Tooltip tooltip) {
        renderTooltipInner(minecraft.screen, matrices, tooltip, tooltip.getX(), tooltip.getY());
    }
    
    protected abstract void renderTooltipInner(Screen screen, PoseStack matrices, Tooltip tooltip, int mouseX, int mouseY);
    
    public void renderWidgets(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return;
        for (Widget widget : widgets) {
            if (!(widget instanceof LateRenderable))
                widget.render(matrices, mouseX, mouseY, delta);
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
    
    protected abstract void updateSearch();
    
    public MenuAccess menuAccess() {
        return menuAccess;
    }
    
    public boolean isInside(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY) && isNotInExclusionZones(mouseX, mouseY);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean visible = REIRuntime.getInstance().isOverlayVisible();
        if (!hasSpace()) return false;
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
            Widget menuWidget = menuAccess.widget();
            if (menuWidget != null && menuWidget.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(menuWidget);
                if (button == 0)
                    this.setDragging(true);
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
            if (element != menuAccess.widget() && element.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(element);
                if (button == 0)
                    this.setDragging(true);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (menuAccess.mouseScrolled(mouseX, mouseY, amount))
            return true;
        for (Widget widget : widgets)
            if (widget != menuAccess.widget()
                && widget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (!hasSpace()) return false;
        return (this.getFocused() != null && this.isDragging() && button == 0) && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!hasSpace()) return false;
        if (REIRuntime.getInstance().isOverlayVisible()) {
            for (GuiEventListener listener : widgets)
                if (listener.keyPressed(keyCode, scanCode, modifiers))
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
        return false;
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!hasSpace()) return false;
        if (REIRuntime.getInstance().isOverlayVisible()) {
            for (GuiEventListener listener : widgets)
                if (listener == getFocused() && listener.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        if (!REIRuntime.getInstance().isOverlayVisible())
            return false;
        if (!hasSpace()) return false;
        for (GuiEventListener listener : widgets)
            if (listener.charTyped(character, modifiers))
                return true;
        return false;
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
}
