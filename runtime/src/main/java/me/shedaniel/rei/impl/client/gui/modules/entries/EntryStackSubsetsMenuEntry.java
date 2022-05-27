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

package me.shedaniel.rei.impl.client.gui.modules.entries;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.modules.AbstractMenuEntry;
import me.shedaniel.rei.impl.client.gui.modules.Menu;
import me.shedaniel.rei.impl.client.gui.modules.MenuEntry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

@ApiStatus.Experimental
@ApiStatus.Internal
public class EntryStackSubsetsMenuEntry extends AbstractMenuEntry {
    final EntryStack<?> stack;
    private boolean clickedLast = false;
    private Boolean isFiltered = null;
    
    public EntryStackSubsetsMenuEntry(EntryStack<?> stack) {
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
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (isFiltered()) {
            fill(matrices, getX(), getY(), getX() + getWidth(), getY() + 18, isSelected() ? -26215 : -65536);
        } else if (isSelected()) {
            fill(matrices, getX(), getY(), getX() + getWidth(), getY() + 18, 1174405119);
        }
        if (containsMouse() && mouseX >= getX() + (getWidth() / 2) - 8 && mouseX <= getX() + (getWidth() / 2) + 8 && mouseY >= getY() + 1 && mouseY <= getY() + 17) {
            REIRuntime.getInstance().queueTooltip(stack.getTooltip(TooltipContext.of(new Point(mouseX, mouseY))));
            if (RoughlyEnoughItemsCoreClient.isLeftMousePressed && !clickedLast) {
                clickedLast = true;
                if (getParent().scrolling.getScissorBounds().contains(mouseX, mouseY)) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    List<EntryStackProvider<?>> filteredStacks = ConfigObject.getInstance().getFilteredStackProviders();
                    if (isFiltered()) {
                        filteredStacks.removeIf(next -> EntryStacks.equalsExact(next.provide(), stack));
                    } else {
                        filteredStacks.add(EntryStackProvider.ofStack(stack.normalize()));
                    }
                    Menu menu = ((ScreenOverlayImpl) REIRuntime.getInstance().getOverlay().get()).getOverlayMenu();
                    if (menu != null)
                        recalculateFilter(menu);
                    ConfigManager.getInstance().saveConfig();
                    EntryRegistry.getInstance().refilter();
                    if (REIRuntimeImpl.getSearchField() != null) {
                        ScreenOverlayImpl.getEntryListWidget().updateSearch(REIRuntimeImpl.getSearchField().getText(), true);
                    }
                }
            } else if (!RoughlyEnoughItemsCoreClient.isLeftMousePressed) clickedLast = false;
        } else clickedLast = false;
        stack.render(matrices, new Rectangle(getX() + (getWidth() / 2) - 8, getY() + 1, 16, 16), mouseX, mouseY, delta);
    }
    
    void recalculateFilter(Menu menu) {
        for (MenuEntry child : menu.children()) {
            if (child instanceof SubSubsetsMenuEntry && ((SubSubsetsMenuEntry) child).getChildMenu() != null) {
                recalculateFilter(((SubSubsetsMenuEntry) child).getChildMenu());
            } else if (child instanceof EntryStackSubsetsMenuEntry && EntryStacks.equalsExact(((EntryStackSubsetsMenuEntry) child).stack, stack)) {
                ((EntryStackSubsetsMenuEntry) child).isFiltered = null;
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return isRendering() && mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getEntryHeight();
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    public boolean isFiltered() {
        if (isFiltered == null) {
            isFiltered = false;
            List<EntryStackProvider<?>> filteredStacks = ConfigObject.getInstance().getFilteredStackProviders();
            for (EntryStackProvider<?> provider : filteredStacks) {
                if (EntryStacks.equalsExact(provider.provide(), stack))
                    return isFiltered = true;
            }
        }
        return isFiltered;
    }
}
