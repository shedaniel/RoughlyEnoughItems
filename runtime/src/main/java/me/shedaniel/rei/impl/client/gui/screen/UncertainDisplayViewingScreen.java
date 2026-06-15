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
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class UncertainDisplayViewingScreen extends REIScreen {
    private static final Identifier DEFAULT = Identifier.fromNamespaceAndPath("roughlyenoughitems", "textures/gui/screenshot_default.png");
    private static final Identifier COMPOSITE = Identifier.fromNamespaceAndPath("roughlyenoughitems", "textures/gui/screenshot_composite.png");
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
        super(Component.empty());
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
        this._children().add(button = Widgets.createButton(new Rectangle(width / 2 - 100, height - 40, 200, 20), Component.empty())
                .onRender((matrices, button) -> {
                    button.setEnabled(isSet);
                    button.setText(Component.translatable("gui.done"));
                })
                .onClick(button -> {
                    callback.accept(original);
                }));
        this.widgets.add(new ScreenTypeSelection(width / 2 - 200 - 5, height / 2 - 112 / 2 - 10, DisplayScreenType.ORIGINAL));
        this.widgets.add(Widgets.createLabel(new Point(width / 2 - 200 - 5 + 104, height / 2 - 112 / 2 + 115), Component.translatable("config.roughlyenoughitems.recipeScreenType.original")).noShadow().color(-1124073473));
        this.widgets.add(new ScreenTypeSelection(width / 2 + 5, height / 2 - 112 / 2 - 10, DisplayScreenType.COMPOSITE));
        this.widgets.add(Widgets.createLabel(new Point(width / 2 + 5 + 104, height / 2 - 112 / 2 + 115), Component.translatable("config.roughlyenoughitems.recipeScreenType.composite")).noShadow().color(-1124073473));
        this._children().addAll(widgets);
    }
    
    public List<GuiEventListener> _children() {
        return (List<GuiEventListener>) children();
    }
    
    @Override
    public void render(GuiGraphics graphics, int int_1, int int_2, float float_1) {
        super.render(graphics, int_1, int_2, float_1);
        graphics.drawCenteredString(this.font, Component.translatable("text.rei.recipe_screen_type.selection"), this.width / 2, 20, 0xFFFFFFFF);
        graphics.enableScissor(0, 20 + font.lineHeight + 2, width, 20 + font.lineHeight + 2 + height - 42);
        if (showTips) {
            int i = 32;
            for (FormattedCharSequence s : this.font.split(Component.translatable("text.rei.recipe_screen_type.selection.sub").withStyle(ChatFormatting.GRAY), width - 30)) {
                graphics.drawString(font, s, width / 2 - font.width(s) / 2, i, -1);
                i += 10;
            }
        }
        int k = 10, l = 44, m = width - 20, n = height - l - 10 - 5;
        graphics.fill( k + 1, l, k + m, l + n, -16777216);
        graphics.fill(k, l, m, n, -1);
        for (Widget widget : widgets) {
            widget.render(graphics, int_1, int_2, float_1);
        }
        if (isSet) {
            graphics.pose().pushMatrix();
            updateFramePosition(float_1);
            int x = (int) (width / 2 - 205 + (200 * frame)) + 10;
            int y = height / 2 - 112 / 2 - 10;
            graphics.fillGradient(x - 2, y - 4, x - 6 + 208- 10, y - 4 + 2, -1778384897, -1778384897);
            graphics.fillGradient(x - 2, y - 4 + 126 - 2, x - 6 + 208- 10, y - 4 + 126, -1778384897, -1778384897);
            graphics.fillGradient(x - 4, y - 4, x - 4 + 2, y - 4 + 126, -1778384897, -1778384897);
            graphics.fillGradient(x - 4 + 208 - 2 - 10, y - 4, x - 4 + 208 - 10, y - 4 + 126, -1778384897, -1778384897);
            graphics.pose().popMatrix();
        }
        graphics.disableScissor();
        button.render(graphics, int_1, int_2, float_1);
    }
    
    @Override
    public void renderBackground(GuiGraphics graphics, int i, int j, float f) {
        super.renderBackground(graphics, i, j, f);
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
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256 || this.minecraft.options.keyInventory.matches(event)) {
            Minecraft.getInstance().setScreen(parent);
            if (parent instanceof AbstractContainerScreen) {
                REIRuntime.getInstance().getOverlay().get().queueReloadOverlay();
            }
            return true;
        }
        return super.keyPressed(event);
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
        public void render(GuiGraphics graphics, int i, int i1, float delta) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, type == DisplayScreenType.ORIGINAL ? DEFAULT : COMPOSITE, bounds.x + (type == DisplayScreenType.ORIGINAL ? 8 : 4), bounds.y + 4, bounds.width - 8, bounds.height - 8, 113, type == DisplayScreenType.ORIGINAL ? 16 : 27, 854 - 113 * 2, 480 - 27 * 2, 854, 480);
        }
        
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (containsMouse(event.x(), event.y())) {
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
