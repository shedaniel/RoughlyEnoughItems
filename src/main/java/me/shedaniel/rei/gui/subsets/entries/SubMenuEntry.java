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

package me.shedaniel.rei.gui.subsets.entries;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.subsets.SubsetsMenu;
import me.shedaniel.rei.gui.subsets.SubsetsMenuEntry;
import me.shedaniel.rei.gui.widget.TabWidget;
import me.shedaniel.rei.impl.EntryRegistryImpl;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Experimental
@ApiStatus.Internal
public class SubMenuEntry extends SubsetsMenuEntry {
    public final String text;
    private int textWidth = -69;
    private int x, y, width;
    private boolean selected, containsMouse, rendering;
    private List<SubsetsMenuEntry> entries;
    private SubsetsMenu subsetsMenu;
    private Pair<Integer, Integer> filteredRatio = null;
    private long lastListHash = -1;
    private boolean clickedBefore = false;
    
    public SubMenuEntry(String text) {
        this(text, Collections.emptyList());
    }
    
    public SubMenuEntry(String text, Supplier<List<SubsetsMenuEntry>> entries) {
        this(text, entries.get());
    }
    
    public SubMenuEntry(String text, List<SubsetsMenuEntry> entries) {
        this.text = text;
        this.entries = entries;
    }
    
    private int getTextWidth() {
        if (textWidth == -69) {
            this.textWidth = Math.max(0, font.getStringWidth(text));
        }
        return this.textWidth;
    }
    
    public SubsetsMenu getSubsetsMenu() {
        if (subsetsMenu == null) {
            this.subsetsMenu = new SubsetsMenu(new Point(getParent().getBounds().getMaxX() - 1, y - 1), entries);
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
    public void render(int mouseX, int mouseY, float delta) {
        double filteredRatio = getFilteredRatio();
        if (filteredRatio > 0) {
            filteredRatio = filteredRatio * 0.85 + 0.15;
            fill(x, y, x + width, y + 12, (16711680 | MathHelper.ceil(filteredRatio * 255.0) << 24) + (selected ? 39321 : 0));
        } else if (selected) {
            fill(x, y, x + width, y + 12, -12237499);
        }
        if (selected) {
            if (!entries.isEmpty()) {
                SubsetsMenu menu = getSubsetsMenu();
                menu.menuStartPoint.x = getParent().getBounds().getMaxX() - 1;
                menu.menuStartPoint.y = y - 1;
                List<Rectangle> areas = Lists.newArrayList(ScissorsHandler.INSTANCE.getScissorsAreas());
                ScissorsHandler.INSTANCE.clearScissors();
                menu.render(mouseX, mouseY, delta);
                for (Rectangle area : areas) {
                    ScissorsHandler.INSTANCE.scissor(area);
                }
            } else clickedBefore = false;
            if (clickedBefore) {
                if (rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12 && !entries.isEmpty()) {
                    REIHelper.getInstance().queueTooltip(Tooltip.create("Click again to filter everything in this group."));
                } else clickedBefore = false;
            }
        } else clickedBefore = false;
        font.draw(text, x + 2, y + 2, selected ? 16777215 : 8947848);
        if (!entries.isEmpty()) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TabWidget.CHEST_GUI_TEXTURE);
            drawTexture(x + width - 15, y - 2, 0, 28, 18, 18);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12 && !entries.isEmpty()) {
            if (clickedBefore) {
                clickedBefore = false;
                List<EntryStack> filteredStacks = ConfigObject.getInstance().getFilteredStacks();
                SubsetsMenu subsetsMenu = ScreenHelper.getLastOverlay().getSubsetsMenu();
                setFiltered(filteredStacks, subsetsMenu, this, !(getFilteredRatio() > 0));
                ConfigManager.getInstance().saveConfig();
                ((EntryRegistryImpl) EntryRegistry.getInstance()).refilter();
                if (ScreenHelper.getSearchField() != null)
                    ContainerScreenOverlay.getEntryListWidget().updateSearch(ScreenHelper.getSearchField().getText(), true);
            } else {
                clickedBefore = true;
            }
            minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void setFiltered(List<EntryStack> filteredStacks, SubsetsMenu subsetsMenu, SubMenuEntry subMenuEntry, boolean filtered) {
        for (SubsetsMenuEntry entry : subMenuEntry.entries) {
            if (entry instanceof EntryStackMenuEntry) {
                if (((EntryStackMenuEntry) entry).isFiltered() != filtered) {
                    if (!filtered) {
                        filteredStacks.removeIf(next -> next.equalsIgnoreAmount(((EntryStackMenuEntry) entry).stack));
                    } else {
                        filteredStacks.add(((EntryStackMenuEntry) entry).stack.copy());
                    }
                }
                if (subsetsMenu != null)
                    ((EntryStackMenuEntry) entry).recalculateFilter(subsetsMenu);
            } else if (entry instanceof SubMenuEntry) {
                setFiltered(filteredStacks, subsetsMenu, (SubMenuEntry) entry, filtered);
            }
        }
    }
    
    public double getFilteredRatio() {
        Pair<Integer, Integer> pair = getFilteredRatioPair();
        return pair.getRight() == 0 ? 0 : pair.getLeft() / (double) pair.getRight();
    }
    
    public Pair<Integer, Integer> getFilteredRatioPair() {
        List<EntryStack> filteredStacks = ConfigObject.getInstance().getFilteredStacks();
        if (lastListHash != filteredStacks.hashCode()) {
            int size = 0;
            int filtered = 0;
            for (SubsetsMenuEntry entry : entries) {
                if (entry instanceof EntryStackMenuEntry) {
                    size++;
                    if (((EntryStackMenuEntry) entry).isFiltered())
                        filtered++;
                } else if (entry instanceof SubMenuEntry) {
                    Pair<Integer, Integer> pair = ((SubMenuEntry) entry).getFilteredRatioPair();
                    filtered += pair.getLeft();
                    size += pair.getRight();
                }
            }
            filteredRatio = new Pair<>(filtered, size);
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
    public List<? extends Element> children() {
        if (subsetsMenu != null && !subsetsMenu.children().isEmpty() && selected) {
            return Collections.singletonList(subsetsMenu);
        }
        return Collections.emptyList();
    }
}
