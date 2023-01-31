/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.client.config.entries;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@ApiStatus.Internal
public class ButtonsConfigEntry extends AbstractConfigListEntry<Unit> {
    private final int width;
    private final List<AbstractWidget> children;
    private boolean edited;
    private Runnable saveRunnable = () -> {};
    
    public ButtonsConfigEntry(int width, Triple<Component, Consumer<Button>, Consumer<Runnable>>... buttons) {
        super(NarratorChatListener.NO_TITLE, false);
        this.width = width;
        this.children = Arrays.stream(buttons).map(pair -> {
            return (AbstractWidget) new Button(0, 0, 0, 20, pair.getLeft(), button -> {
                pair.getRight().accept(() -> this.edited = true);
            }) {
                @Override
                public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                    pair.getMiddle().accept(this);
                    super.render(poses, mouseX, mouseY, delta);
                }
            };
        }).toList();
    }
    
    public ButtonsConfigEntry withSaveRunnable(Runnable runnable) {
        this.saveRunnable = runnable;
        return this;
    }
    
    @Override
    public Unit getValue() {
        return Unit.INSTANCE;
    }
    
    @Override
    public Optional<Unit> getDefaultValue() {
        return Optional.of(Unit.INSTANCE);
    }
    
    @Override
    public void save() {
        this.saveRunnable.run();
    }
    
    @Override
    public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        Window window = Minecraft.getInstance().getWindow();
        for (AbstractWidget widget : this.children) {
            widget.active = this.isEditable();
            widget.y = y;
            widget.setWidth(width / this.children.size() - (this.children.size() == 1 ? 0 : 2));
            widget.x = x + entryWidth / 2 - width / 2 + (widget.getWidth() + 2) * this.children.indexOf(widget);
            widget.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
    
    @Override
    public List<? extends NarratableEntry> narratables() {
        return children;
    }
    
    @Override
    public boolean isEdited() {
        return edited;
    }
}
