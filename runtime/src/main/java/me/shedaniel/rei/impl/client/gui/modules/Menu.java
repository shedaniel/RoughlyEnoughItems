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

package me.shedaniel.rei.impl.client.gui.modules;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.impl.client.gui.modules.entries.SubMenuEntry;
import me.shedaniel.rei.impl.client.gui.widget.LateRenderable;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@ApiStatus.Internal
public class Menu extends WidgetWithBounds implements LateRenderable {
    public static final UUID WEATHER = UUID.randomUUID();
    public static final UUID GAME_TYPE = UUID.randomUUID();
    
    public final Point menuStartPoint;
    public final boolean facingRight;
    public final boolean facingDownwards;
    private final List<MenuEntry> entries = Lists.newArrayList();
    public final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public int getMaxScrollHeight() {
            int i = 0;
            for (MenuEntry entry : children()) {
                i += entry.getEntryHeight();
            }
            return i;
        }
        
        @Override
        public Rectangle getBounds() {
            return Menu.this.getInnerBounds();
        }
        
        @Override
        public boolean hasScrollBar() {
            return Menu.this.hasScrollBar();
        }
    };
    
    public Menu(Rectangle menuStart, Collection<MenuEntry> entries, boolean sort) {
        buildEntries(entries, sort);
        int fullWidth = Minecraft.getInstance().screen.width;
        int fullHeight = Minecraft.getInstance().screen.height;
        boolean facingRight = true;
        this.facingDownwards = fullHeight - menuStart.getMaxY() > menuStart.y;
        int y = facingDownwards ? menuStart.getMaxY() : menuStart.y - (scrolling.getMaxScrollHeight() + 2);
        boolean hasScrollBar = scrolling.getMaxScrollHeight() > getInnerHeight(y);
        int menuWidth = getMaxEntryWidth() + 2 + (hasScrollBar ? 6 : 0);
        if (facingRight && fullWidth - menuStart.getMaxX() < menuWidth + 10) {
            facingRight = false;
        } else if (!facingRight && menuStart.x < menuWidth + 10) {
            facingRight = true;
        }
        this.facingRight = facingRight;
        int x = facingRight ? menuStart.x : menuStart.getMaxX() - (getMaxEntryWidth() + 2 + (hasScrollBar ? 6 : 0));
        this.menuStartPoint = new Point(x, y);
    }
    
    @SuppressWarnings("deprecation")
    private void buildEntries(Collection<MenuEntry> entries, boolean sort) {
        this.entries.clear();
        this.entries.addAll(entries);
        if (sort) {
            this.entries.sort(Comparator.comparing(entry -> entry instanceof SubMenuEntry ? 0 : 1)
                    .thenComparing(entry -> entry instanceof SubMenuEntry menuEntry ? menuEntry.text.getString() : ""));
        }
        for (MenuEntry entry : this.entries) {
            entry.parent = this;
        }
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle(menuStartPoint.x, menuStartPoint.y, getMaxEntryWidth() + 2 + (hasScrollBar() ? 6 : 0), getInnerHeight(menuStartPoint.y) + 2);
    }
    
    public Rectangle getInnerBounds() {
        return new Rectangle(menuStartPoint.x + 1, menuStartPoint.y + 1, getMaxEntryWidth() + (hasScrollBar() ? 6 : 0), getInnerHeight(menuStartPoint.y));
    }
    
    public boolean hasScrollBar() {
        return scrolling.getMaxScrollHeight() > getInnerHeight(menuStartPoint.y);
    }
    
    public int getInnerHeight(int y) {
        return Math.min(scrolling.getMaxScrollHeight(), minecraft.screen.height - 20 - y);
    }
    
    public int getMaxEntryWidth() {
        int i = 0;
        for (MenuEntry entry : children()) {
            if (entry.getEntryWidth() > i)
                i = entry.getEntryWidth();
        }
        return Math.max(10, i);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Rectangle bounds = getBounds();
        Rectangle innerBounds = getInnerBounds();
        fill(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), containsMouse(mouseX, mouseY) ? (REIRuntime.getInstance().isDarkThemeEnabled() ? -17587 : -1) : -6250336);
        fill(matrices, innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY(), -16777216);
        boolean contains = innerBounds.contains(mouseX, mouseY);
        MenuEntry focused = getFocused() instanceof MenuEntry menuEntry ? menuEntry : null;
        int currentY = innerBounds.y - scrolling.scrollAmountInt();
        for (MenuEntry child : children()) {
            boolean containsMouse = contains && mouseY >= currentY && mouseY < currentY + child.getEntryHeight();
            if (containsMouse) {
                focused = child;
            }
            currentY += child.getEntryHeight();
        }
        currentY = innerBounds.y - scrolling.scrollAmountInt();
        ScissorsHandler.INSTANCE.scissor(scrolling.getScissorBounds());
        for (MenuEntry child : children()) {
            boolean rendering = currentY + child.getEntryHeight() >= innerBounds.y && currentY <= innerBounds.getMaxY();
            boolean containsMouse = contains && mouseY >= currentY && mouseY < currentY + child.getEntryHeight();
            child.updateInformation(innerBounds.x, currentY, focused == child || containsMouse, containsMouse, rendering, getMaxEntryWidth());
            if (rendering) {
                child.render(matrices, mouseX, mouseY, delta);
            }
            currentY += child.getEntryHeight();
        }
        ScissorsHandler.INSTANCE.removeLastScissor();
        setFocused(focused);
        scrolling.renderScrollBar(0, 1, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
        scrolling.updatePosition(delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrolling.updateDraggingState(mouseX, mouseY, button))
            return true;
        return super.mouseClicked(mouseX, mouseY, button) || getInnerBounds().contains(mouseX, mouseY);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrolling.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (getInnerBounds().contains(mouseX, mouseY)) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
            return true;
        }
        for (MenuEntry child : children()) {
            if (child instanceof SubMenuEntry) {
                if (child.mouseScrolled(mouseX, mouseY, amount))
                    return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        if (super.containsMouse(mouseX, mouseY)) return true;
        for (MenuEntry child : children()) {
            if (child.containsMouse(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<MenuEntry> children() {
        return entries;
    }
}
