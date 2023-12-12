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

package me.shedaniel.rei.impl.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.config.DisplayScreenType;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class UncertainDisplayViewingScreen extends Screen {
    private static final ResourceLocation DEFAULT = new ResourceLocation("roughlyenoughitems", "textures/gui/screenshot_default.png");
    private static final ResourceLocation COMPOSITE = new ResourceLocation("roughlyenoughitems", "textures/gui/screenshot_composite.png");
    private final List<Widget> widgets;
    protected long start;
    protected long duration;
    private boolean isSet;
    private boolean original;
    private double frame = 0;
    private double target = 0;
    private BooleanConsumer callback;
    private Button button;
    private Screen parent;
    private boolean showTips;
    
    public UncertainDisplayViewingScreen(Screen parent, DisplayScreenType type, boolean showTips, BooleanConsumer callback) {
        super(ImmutableTextComponent.EMPTY);
        this.widgets = Lists.newArrayList();
        if (type == DisplayScreenType.UNSET) {
            this.isSet = false;
            this.original = true;
        } else {
            this.isSet = true;
            this.original = type == DisplayScreenType.ORIGINAL;
            moveFrameTo(original ? 0 : 1, false, 0);
        }
        this.callback = callback;
        this.parent = parent;
        this.showTips = showTips;
    }
    
    public final double clamp(double v) {
        return clamp(v, 30);
    }
    
    public final double clamp(double v, double clampExtension) {
        return Mth.clamp(v, -clampExtension, 1 + clampExtension);
    }
    
    private void moveFrameTo(double value, boolean animated, long duration) {
        target = clamp(value);
        
        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else {
            frame = target;
        }
    }
    
    @Override
    public void init() {
        this.children().clear();
        this.widgets.clear();
        this._children().add(button = Widgets.createButton(new Rectangle(width / 2 - 100, height - 40, 200, 20), NarratorChatListener.NO_TITLE)
                .onRender((matrices, button) -> {
                    button.setEnabled(isSet);
                    button.setText(new TranslatableComponent("gui.done"));
                })
                .onClick(button -> {
                        callback.accept(original);
                }));
        this.widgets.add(new ScreenTypeSelection(width / 2 - 200 - 5, height / 2 - 112 / 2 - 10, DisplayScreenType.ORIGINAL));
        this.widgets.add(Widgets.createLabel(new Point(width / 2 - 200 - 5 + 104, height / 2 - 112 / 2 + 115), new TranslatableComponent("config.roughlyenoughitems.recipeScreenType.original")).noShadow().color(-1124073473));
        this.widgets.add(new ScreenTypeSelection(width / 2 + 5, height / 2 - 112 / 2 - 10, DisplayScreenType.COMPOSITE));
        this.widgets.add(Widgets.createLabel(new Point(width / 2 + 5 + 104, height / 2 - 112 / 2 + 115), new TranslatableComponent("config.roughlyenoughitems.recipeScreenType.composite")).noShadow().color(-1124073473));
        this._children().addAll(widgets);
    }
    
    public List<GuiEventListener> _children() {
        return (List<GuiEventListener>) children();
    }
    
    @Override
    public void render(PoseStack matrices, int int_1, int int_2, float float_1) {
        if (this.minecraft.level != null) {
            renderBackground(matrices);
        } else {
            this.fillGradient(matrices, 0, 0, this.width, this.height, -16777216, -16777216);
        }
        drawCenteredString(matrices, this.font, new TranslatableComponent("text.rei.recipe_screen_type.selection"), this.width / 2, 20, 16777215);
        ScissorsHandler.INSTANCE.scissor(new Rectangle(0, 20 + font.lineHeight + 2, width, height - 42));
        if (showTips) {
            float i = 32;
            for (FormattedCharSequence s : this.font.split(new TranslatableComponent("text.rei.recipe_screen_type.selection.sub").withStyle(ChatFormatting.GRAY), width - 30)) {
                font.drawShadow(matrices, s, width / 2 - font.width(s) / 2, i, -1);
                i += 10;
            }
        }
        super.render(matrices, int_1, int_2, float_1);
        for (Widget widget : widgets) {
            widget.render(matrices, int_1, int_2, float_1);
        }
        if (isSet) {
            matrices.pushPose();
            updateFramePosition(float_1);
            int x = (int) (width / 2 - 205 + (210 * frame));
            int y = height / 2 - 112 / 2 - 10;
            fillGradient(matrices, x - 2, y - 4, x - 6 + 208, y - 4 + 2, -1778384897, -1778384897);
            fillGradient(matrices, x - 2, y - 4 + 126 - 2, x - 6 + 208, y - 4 + 126, -1778384897, -1778384897);
            fillGradient(matrices, x - 4, y - 4, x - 4 + 2, y - 4 + 126, -1778384897, -1778384897);
            fillGradient(matrices, x - 4 + 208 - 2, y - 4, x - 4 + 208, y - 4 + 126, -1778384897, -1778384897);
            matrices.popPose();
        }
        ScissorsHandler.INSTANCE.removeLastScissor();
        button.render(matrices, int_1, int_2, float_1);
    }
    
    private void updateFramePosition(float delta) {
        target = clamp(target);
        if (!DynamicNewSmoothScrollingEntryListWidget.Precision.almostEquals(frame, target, DynamicNewSmoothScrollingEntryListWidget.Precision.FLOAT_EPSILON))
            frame = ease(frame, target, Math.min((System.currentTimeMillis() - start) / (double) duration * delta * 3.0D, 1));
        else
            frame = target;
    }
    
    private double ease(double start, double end, double amount) {
        return start + (end - start) * EasingMethod.EasingMethodImpl.LINEAR.apply(amount);
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 || this.minecraft.options.keyInventory.matches(int_1, int_2)) {
            Minecraft.getInstance().setScreen(parent);
            if (parent instanceof AbstractContainerScreen) {
                REIRuntime.getInstance().getOverlay().get().queueReloadOverlay();
            }
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    public class ScreenTypeSelection extends WidgetWithBounds {
        private final DisplayScreenType type;
        private Rectangle bounds;
        
        public ScreenTypeSelection(int x, int y, DisplayScreenType type) {
            this.type = type;
            this.bounds = new Rectangle(x - 4 + 16, y - 4, 176 + 8, 120 + 8);
        }
        
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        @Override
        public void render(PoseStack matrices, int i, int i1, float delta) {
            RenderSystem.setShaderTexture(0, type == DisplayScreenType.ORIGINAL ? DEFAULT : COMPOSITE);
            blit(matrices, bounds.x + (type == DisplayScreenType.ORIGINAL ? 8 : 4), bounds.y + 4, bounds.width - 8, bounds.height - 8, 113, type == DisplayScreenType.ORIGINAL ? 16 : 27, 854 - 113 * 2, 480 - 27 * 2, 854, 480);
        }
        
        @Override
        public boolean mouseClicked(double double_1, double double_2, int int_1) {
            if (containsMouse(double_1, double_2)) {
                original = (type == DisplayScreenType.ORIGINAL);
                if (!isSet) {
                    moveFrameTo(original ? 0 : 1, false, 0);
                }
                isSet = true;
                moveFrameTo(original ? 0 : 1, true, 500);
                return true;
            }
            return false;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
    }
}
