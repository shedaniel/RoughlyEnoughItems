/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.client.gui.modules.entries;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIHelper;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.REIHelperImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.modules.Menu;
import me.shedaniel.rei.impl.client.gui.modules.MenuEntry;
import me.shedaniel.rei.impl.client.gui.widget.TabWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Experimental
@ApiStatus.Internal
public class SubSubsetsMenuEntry extends MenuEntry {
    public final String text;
    private int textWidth = -69;
    private int x, y, width;
    private boolean selected, containsMouse, rendering;
    private List<MenuEntry> entries;
    private Menu subsetsMenu;
    private Tuple<Integer, Integer> filteredRatio = null;
    private long lastListHash = -1;
    private boolean clickedBefore = false;
    
    public SubSubsetsMenuEntry(String text) {
        this(text, Collections.emptyList());
    }
    
    public SubSubsetsMenuEntry(String text, Supplier<List<MenuEntry>> entries) {
        this(text, entries.get());
    }
    
    public SubSubsetsMenuEntry(String text, List<MenuEntry> entries) {
        this.text = text;
        this.entries = entries;
    }
    
    private int getTextWidth() {
        if (textWidth == -69) {
            this.textWidth = Math.max(0, font.width(text));
        }
        return this.textWidth;
    }
    
    public Menu getSubsetsMenu() {
        if (subsetsMenu == null) {
            this.subsetsMenu = new Menu(new Point(getParent().getBounds().getMaxX() - 1, y - 1), entries);
        }
        return subsetsMenu;
    }
    
    @Override
    public int getEntryWidth() {
        return 12 + getTextWidth() + 4;
    }
    
    @Override
    public int getEntryHeight() {
        return 12;
    }
    
    @Override
    public void updateInformation(int xPos, int yPos, boolean selected, boolean containsMouse, boolean rendering, int width) {
        this.x = xPos;
        this.y = yPos;
        this.selected = selected;
        this.containsMouse = containsMouse;
        this.rendering = rendering;
        this.width = width;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        double filteredRatio = getFilteredRatio();
        if (filteredRatio > 0) {
            filteredRatio = filteredRatio * 0.85 + 0.15;
            fill(matrices, x, y, x + width, y + 12, (16711680 | Mth.ceil(filteredRatio * 255.0) << 24) + (selected ? 39321 : 0));
        } else if (selected) {
            fill(matrices, x, y, x + width, y + 12, -12237499);
        }
        if (selected) {
            if (!entries.isEmpty()) {
                Menu menu = getSubsetsMenu();
                menu.menuStartPoint.x = getParent().getBounds().getMaxX() - 1;
                menu.menuStartPoint.y = y - 1;
                List<Rectangle> areas = Lists.newArrayList(ScissorsHandler.INSTANCE.getScissorsAreas());
                ScissorsHandler.INSTANCE.clearScissors();
                menu.render(matrices, mouseX, mouseY, delta);
                for (Rectangle area : areas) {
                    ScissorsHandler.INSTANCE.scissor(area);
                }
            } else clickedBefore = false;
            if (clickedBefore) {
                if (rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12 && !entries.isEmpty()) {
                    REIHelper.getInstance().queueTooltip(Tooltip.create(new TextComponent("Click again to filter everything in this group.")));
                } else clickedBefore = false;
            }
        } else clickedBefore = false;
        font.draw(matrices, text, x + 2, y + 2, selected ? 16777215 : 8947848);
        if (!entries.isEmpty()) {
            RenderSystem.setShaderTexture(0, TabWidget.CHEST_GUI_TEXTURE);
            blit(matrices, x + width - 15, y - 2, 0, 28, 18, 18);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12 && !entries.isEmpty()) {
            if (clickedBefore) {
                clickedBefore = false;
                List<EntryStack<?>> filteredStacks = ConfigObject.getInstance().getFilteredStacks();
                Menu overlay = ((ScreenOverlayImpl) REIHelper.getInstance().getOverlay().get()).getOverlayMenu();
                setFiltered(filteredStacks, overlay, this, !(getFilteredRatio() > 0));
                ConfigManager.getInstance().saveConfig();
                EntryRegistry.getInstance().refilter();
                if (REIHelperImpl.getSearchField() != null) {
                    ScreenOverlayImpl.getEntryListWidget().updateSearch(REIHelperImpl.getSearchField().getText(), true);
                }
            } else {
                clickedBefore = true;
            }
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void setFiltered(List<EntryStack<?>> filteredStacks, Menu subsetsMenu, SubSubsetsMenuEntry subSubsetsMenuEntry, boolean filtered) {
        for (MenuEntry entry : subSubsetsMenuEntry.entries) {
            if (entry instanceof EntryStackSubsetsMenuEntry) {
                if (((EntryStackSubsetsMenuEntry) entry).isFiltered() != filtered) {
                    if (!filtered) {
                        filteredStacks.removeIf(next -> EntryStacks.equalsExact(next, ((EntryStackSubsetsMenuEntry) entry).stack));
                    } else {
                        filteredStacks.add(((EntryStackSubsetsMenuEntry) entry).stack.normalize());
                    }
                }
                if (subsetsMenu != null)
                    ((EntryStackSubsetsMenuEntry) entry).recalculateFilter(subsetsMenu);
            } else if (entry instanceof SubSubsetsMenuEntry) {
                setFiltered(filteredStacks, subsetsMenu, (SubSubsetsMenuEntry) entry, filtered);
            }
        }
    }
    
    public double getFilteredRatio() {
        Tuple<Integer, Integer> pair = getFilteredRatioPair();
        return pair.getB() == 0 ? 0 : pair.getA() / (double) pair.getB();
    }
    
    public Tuple<Integer, Integer> getFilteredRatioPair() {
        List<EntryStack<?>> filteredStacks = ConfigObject.getInstance().getFilteredStacks();
        if (lastListHash != filteredStacks.hashCode()) {
            int size = 0;
            int filtered = 0;
            for (MenuEntry entry : entries) {
                if (entry instanceof EntryStackSubsetsMenuEntry) {
                    size++;
                    if (((EntryStackSubsetsMenuEntry) entry).isFiltered())
                        filtered++;
                } else if (entry instanceof SubSubsetsMenuEntry) {
                    Tuple<Integer, Integer> pair = ((SubSubsetsMenuEntry) entry).getFilteredRatioPair();
                    filtered += pair.getA();
                    size += pair.getB();
                }
            }
            filteredRatio = new Tuple<>(filtered, size);
            lastListHash = filteredStacks.hashCode();
        }
        return filteredRatio;
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        if (super.containsMouse(mouseX, mouseY))
            return true;
        if (subsetsMenu != null && !subsetsMenu.children().isEmpty() && selected)
            return subsetsMenu.containsMouse(mouseX, mouseY);
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return subsetsMenu != null && !subsetsMenu.children().isEmpty() && selected && subsetsMenu.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        if (subsetsMenu != null && !subsetsMenu.children().isEmpty() && selected) {
            return Collections.singletonList(subsetsMenu);
        }
        return Collections.emptyList();
    }
}
