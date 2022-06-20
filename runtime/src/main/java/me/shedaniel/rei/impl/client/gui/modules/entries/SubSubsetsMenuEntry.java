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
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.modules.Menu;
import me.shedaniel.rei.impl.client.gui.modules.MenuEntry;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Internal
public class SubSubsetsMenuEntry extends SubMenuEntry {
    private Tuple<Integer, Integer> filteredRatio = null;
    private long lastListHash = -1;
    private boolean clickedBefore = false;
    
    public SubSubsetsMenuEntry(Component text) {
        this(text, Collections.emptyList());
    }
    
    public SubSubsetsMenuEntry(Component text, Supplier<List<MenuEntry>> entries) {
        this(text, entries.get());
    }
    
    public SubSubsetsMenuEntry(Component text, List<MenuEntry> entries) {
        super(text, entries);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if (isSelected()) {
            if (entries.isEmpty()) {
                clickedBefore = false;
            }
            if (clickedBefore) {
                if (isRendering() && mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + 12 && !entries.isEmpty()) {
                    REIRuntime.getInstance().queueTooltip(Tooltip.create(Component.literal("Click again to filter everything in this group.")));
                } else clickedBefore = false;
            }
        } else clickedBefore = false;
    }
    
    @Override
    protected void renderBackground(PoseStack poses, int x, int y, int width, int height) {
        double filteredRatio = getFilteredRatio();
        if (filteredRatio > 0) {
            filteredRatio = filteredRatio * 0.85 + 0.15;
            fill(poses, x, y, x + width, y + 12, (16711680 | Mth.ceil(filteredRatio * 255.0) << 24) + (isSelected() ? 39321 : 0));
        } else if (isSelected()) {
            fill(poses, x, y, x + width, y + 12, -12237499);
        }
    }
    
    @Override
    protected boolean onClick(double mouseX, double mouseY, int button) {
        if (clickedBefore) {
            clickedBefore = false;
            List<EntryStackProvider<?>> filteredStacks = ConfigObject.getInstance().getFilteredStackProviders();
            Menu overlay = ((ScreenOverlayImpl) REIRuntime.getInstance().getOverlay().get()).getOverlayMenu();
            setFiltered(filteredStacks, overlay, this, !(getFilteredRatio() > 0));
            ConfigManager.getInstance().saveConfig();
            EntryRegistry.getInstance().refilter();
            if (REIRuntimeImpl.getSearchField() != null) {
                ScreenOverlayImpl.getEntryListWidget().updateSearch(REIRuntimeImpl.getSearchField().getText(), true);
            }
        } else {
            clickedBefore = true;
        }
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return true;
    }
    
    private void setFiltered(List<EntryStackProvider<?>> filteredStacks, Menu subsetsMenu, SubSubsetsMenuEntry subSubsetsMenuEntry, boolean filtered) {
        for (MenuEntry entry : subSubsetsMenuEntry.entries) {
            if (entry instanceof EntryStackSubsetsMenuEntry menuEntry) {
                if (menuEntry.isFiltered() != filtered) {
                    if (!filtered) {
                        filteredStacks.removeIf(next -> EntryStacks.equalsExact(next.provide(), menuEntry.stack));
                    } else {
                        filteredStacks.add(EntryStackProvider.ofStack(menuEntry.stack.normalize()));
                    }
                }
                if (subsetsMenu != null)
                    menuEntry.recalculateFilter(subsetsMenu);
            } else if (entry instanceof SubSubsetsMenuEntry menuEntry) {
                setFiltered(filteredStacks, subsetsMenu, menuEntry, filtered);
            }
        }
    }
    
    public double getFilteredRatio() {
        Tuple<Integer, Integer> pair = getFilteredRatioPair();
        return pair.getB() == 0 ? 0 : pair.getA() / (double) pair.getB();
    }
    
    public Tuple<Integer, Integer> getFilteredRatioPair() {
        List<EntryStackProvider<?>> filteredStacks = ConfigObject.getInstance().getFilteredStackProviders();
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
}
