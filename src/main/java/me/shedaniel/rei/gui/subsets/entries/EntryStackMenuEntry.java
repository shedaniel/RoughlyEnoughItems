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

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.subsets.SubsetsMenu;
import me.shedaniel.rei.gui.subsets.SubsetsMenuEntry;
import me.shedaniel.rei.impl.EntryRegistryImpl;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.gui.Element;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

@ApiStatus.Experimental
@ApiStatus.Internal
public class EntryStackMenuEntry extends SubsetsMenuEntry {
    final EntryStack stack;
    private int x, y, width;
    private boolean selected, containsMouse, rendering;
    private boolean clickedLast = false;
    private Boolean isFiltered = null;
    
    public EntryStackMenuEntry(EntryStack stack) {
        this.stack = stack;
    }
    
    @Override
    public int getEntryWidth() {
        return 18;
    }
    
    @Override
    public int getEntryHeight() {
        return 18;
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
        if (isFiltered()) {
            if (selected) {
                fill(x, y, x + width, y + 18, -26215);
            } else {
                fill(x, y, x + width, y + 18, -65536);
            }
        } else if (selected) {
            fill(x, y, x + width, y + 18, 1174405119);
        }
        if (containsMouse && mouseX >= x + (width / 2) - 8 && mouseX <= x + (width / 2) + 8 && mouseY >= y + 1 && mouseY <= y + 17) {
            ScreenHelper.getLastOverlay().addTooltip(stack.getTooltip(mouseX, mouseY));
            if (RoughlyEnoughItemsCore.isLeftModePressed && !clickedLast) {
                clickedLast = true;
                if (!getParent().scrolling.draggingScrollBar) {
                    minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    List<EntryStack> filteredStacks = ConfigObject.getInstance().getFilteredStacks();
                    if (isFiltered()) {
                        filteredStacks.removeIf(next -> next.equalsIgnoreAmount(stack));
                    } else {
                        filteredStacks.add(stack.copy());
                    }
                    SubsetsMenu subsetsMenu = ScreenHelper.getLastOverlay().getSubsetsMenu();
                    if (subsetsMenu != null)
                        recalculateFilter(subsetsMenu);
                    ConfigManager.getInstance().saveConfig();
                    ((EntryRegistryImpl) EntryRegistry.getInstance()).refilter();
                    if (ScreenHelper.getSearchField() != null)
                        ContainerScreenOverlay.getEntryListWidget().updateSearch(ScreenHelper.getSearchField().getText(), true);
                }
            } else if (!RoughlyEnoughItemsCore.isLeftModePressed) clickedLast = false;
        } else clickedLast = false;
        stack.render(new Rectangle(x + (width / 2) - 8, y + 1, 16, 16), mouseX, mouseY, delta);
    }
    
    void recalculateFilter(SubsetsMenu menu) {
        for (SubsetsMenuEntry child : menu.children()) {
            if (child instanceof SubMenuEntry && ((SubMenuEntry) child).getSubsetsMenu() != null)
                recalculateFilter(((SubMenuEntry) child).getSubsetsMenu());
            else if (child instanceof EntryStackMenuEntry && ((EntryStackMenuEntry) child).stack.equalsIgnoreAmount(stack))
                ((EntryStackMenuEntry) child).isFiltered = null;
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 18;
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    public boolean isFiltered() {
        List<EntryStack> filteredStacks = ConfigObject.getInstance().getFilteredStacks();
        if (isFiltered == null) {
            isFiltered = CollectionUtils.findFirstOrNullEqualsEntryIgnoreAmount(filteredStacks, stack) != null;
        }
        return isFiltered;
    }
}
