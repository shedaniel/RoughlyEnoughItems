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

package me.shedaniel.rei.impl.client.gui.modules.entries;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.modules.AbstractMenuEntry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ToggleMenuEntry extends AbstractMenuEntry {
    public final Component text;
    public final BooleanSupplier supplier;
    public final BooleanUnaryOperator consumer;
    public BooleanSupplier active = () -> true;
    public Supplier<Tooltip> tooltip = () -> null;
    private int textWidth = -69;
    
    public static ToggleMenuEntry of(Component text, BooleanSupplier supplier, BooleanConsumer consumer) {
        return new ToggleMenuEntry(text, supplier, b -> {
            consumer.accept(b);
            return true;
        });
    }
    
    public static ToggleMenuEntry ofDeciding(Component text, BooleanSupplier supplier, BooleanUnaryOperator consumer) {
        return new ToggleMenuEntry(text, supplier, consumer);
    }
    
    protected ToggleMenuEntry(Component text, BooleanSupplier supplier, BooleanUnaryOperator consumer) {
        this.text = text;
        this.supplier = supplier;
        this.consumer = consumer;
    }
    
    public ToggleMenuEntry withActive(BooleanSupplier active) {
        this.active = active;
        return this;
    }
    
    public ToggleMenuEntry withTooltip(Supplier<Tooltip> tooltip) {
        this.tooltip = tooltip;
        return this;
    }
    
    @FunctionalInterface
    public interface BooleanUnaryOperator {
        boolean apply(boolean b);
    }
    
    private int getTextWidth() {
        if (textWidth == -69) {
            this.textWidth = Math.max(0, font.width(text));
        }
        return this.textWidth;
    }
    
    @Override
    public int getEntryWidth() {
        return getTextWidth() + 4 + 8;
    }
    
    @Override
    public int getEntryHeight() {
        return 12;
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (isSelected() && active.getAsBoolean()) {
            fill(matrices, getX(), getY(), getX() + getWidth(), getY() + getEntryHeight(), -12237499);
            
            Tooltip tooltip = this.tooltip.get();
            
            if (tooltip != null) {
                List<Rectangle> areas = Lists.newArrayList(ScissorsHandler.INSTANCE.getScissorsAreas());
                ScissorsHandler.INSTANCE.clearScissors();
                matrices.pushPose();
                matrices.translate(0, 0, -400);
                ScreenOverlayImpl.getInstance().renderTooltip(matrices, tooltip);
                matrices.popPose();
                for (Rectangle area : areas) {
                    ScissorsHandler.INSTANCE.scissor(area);
                }
            }
        }
        font.draw(matrices, text, getX() + 2, getY() + 2, isSelected() && active.getAsBoolean() ? 16777215 : 8947848);
        if (supplier.getAsBoolean()) {
            font.draw(matrices, "✔", getX() + getWidth() - 2 - font.width("✔"), getY() + 2, isSelected() && active.getAsBoolean() ? 16777215 : 8947848);
        }
    }
    
    @Override
    protected boolean onClick(double mouseX, double mouseY, int button) {
        if (!active.getAsBoolean()) return false;
        if (consumer.apply(!supplier.getAsBoolean())) {
            REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
        }
        ConfigManager.getInstance().saveConfig();
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return true;
    }
}
