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

package me.shedaniel.rei.impl.client.gui.widget.basewidgets;

import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueProvider;
import me.shedaniel.math.Color;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import net.minecraft.client.gui.ComponentPath;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ButtonWidget extends Button {
    private static final WidgetSprites SPRITES = new WidgetSprites(Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"), Identifier.withDefaultNamespace("widget/button_highlighted"));
    private static final WidgetSprites DARK_SPRITES = new WidgetSprites(Identifier.parse("roughlyenoughitems:widget/button_dark"), Identifier.parse("roughlyenoughitems:widget/button_disabled_dark"), Identifier.parse("roughlyenoughitems:widget/button_highlighted_dark"));
    private Rectangle bounds;
    private boolean enabled = true;
    private Component text;
    @Nullable
    private Integer tint;
    @Nullable
    private Consumer<Button> onClick;
    @Nullable
    private BiConsumer<GuiGraphics, Button> onRender;
    private boolean focusable = false;
    private boolean focused = false;
    @Nullable
    private Function<Button, @Nullable Component[]> tooltipFunction;
    @Nullable
    private BiFunction<Button, Point, Integer> textColorFunction;
    private final ValueAnimator<Color> darkBackground;
    private ValueProvider<Double> alpha;
    
    public ButtonWidget(Rectangle rectangle, Component text) {
        this.bounds = Objects.requireNonNull(rectangle);
        this.text = Objects.requireNonNull(text);
        this.darkBackground = ValueAnimator.ofColor()
                .withConvention(() -> Color.ofTransparent(REIRuntime.getInstance().isDarkThemeEnabled() ? 0xFFFFFFFF : 0x00FFFFFF), ValueAnimator.typicalTransitionTime());
        this.alpha = ValueProvider.constant(1.0);
    }
    
    @Override
    public final boolean isFocused() {
        return focused;
    }
    
    @Override
    public final boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public final OptionalInt getTint() {
        return OptionalInt.empty();
    }
    
    @Override
    public final void setTint(int tint) {
        this.tint = tint;
    }
    
    @Override
    public final void removeTint() {
        this.tint = null;
    }
    
    @Override
    public final Component getText() {
        return text;
    }
    
    @Override
    public final void setText(Component text) {
        this.text = text;
    }
    
    @Override
    public final @Nullable Consumer<Button> getOnClick() {
        return onClick;
    }
    
    @Override
    public final void setOnClick(@Nullable Consumer<Button> onClick) {
        this.onClick = onClick;
    }
    
    @Nullable
    @Override
    public final BiConsumer<GuiGraphics, Button> getOnRender() {
        return onRender;
    }
    
    @Override
    public final void setOnRender(BiConsumer<GuiGraphics, Button> onRender) {
        this.onRender = onRender;
    }
    
    @Override
    public final boolean isFocusable() {
        return focusable;
    }
    
    @Override
    public final void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }
    
    public void setAlpha(ValueProvider<Double> alpha) {
        this.alpha = alpha;
    }
    
    @Override
    @Nullable
    public final Component[] getTooltip() {
        if (tooltipFunction == null)
            return null;
        return tooltipFunction.apply(this);
    }
    
    @Override
    public final void setTooltip(@Nullable Function<Button, @Nullable Component[]> tooltip) {
        this.tooltipFunction = tooltip;
    }
    
    @Override
    public final void setTextColor(@Nullable BiFunction<Button, Point, Integer> textColorFunction) {
        this.textColorFunction = textColorFunction;
    }
    
    @Override
    public final int getTextColor(Point mouse) {
        if (this.textColorFunction != null) {
            Integer apply = this.textColorFunction.apply(this, mouse);
            if (apply != null)
                return apply;
        }
        if (!this.enabled) {
            return 10526880;
        } else if (isFocused(mouse.x, mouse.y)) {
            return 16777120;
        }
        return 14737632;
    }
    
    @Override
    public final Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        darkBackground.update(delta);
        alpha.update(delta);
        if (onRender != null) {
            onRender.accept(graphics, this);
        }
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        int alphaAsInt = (int) (alpha.value() * 255);
        renderBackground(graphics, x, y, width, height, isFocused(mouseX, mouseY), false, Color.ofTransparent(0xFFFFFF | (alphaAsInt << 24)));
        Color darkBackgroundColor = darkBackground.value();
        darkBackgroundColor = Color.ofRGBA(darkBackgroundColor.getRed(), darkBackgroundColor.getGreen(), darkBackgroundColor.getBlue(), (int) Math.round(darkBackgroundColor.getAlpha() * alpha.value()));
        renderBackground(graphics, x, y, width, height, isFocused(mouseX, mouseY), true, darkBackgroundColor);
        
        int color = 0xe0e0e0;
        if (!this.enabled) {
            color = 0xa0a0a0;
        } else if (isFocused(mouseX, mouseY)) {
            color = 0xffffa0;
        }
        
        if (tint != null) {
            graphics.fillGradient(x + 1, y + 1, x + width - 1, y + height - 1, tint, tint);
        }
        
        if (alphaAsInt > 10) {
            graphics.drawCenteredString(font, getText(), x + width / 2, y + (height - 8) / 2, color | (alphaAsInt << 24));
        }
        
        Component[] tooltip = getTooltip();
        if (tooltip != null) {
            if (!focused && containsMouse(mouseX, mouseY)) {
                Tooltip.create(tooltip).queue();
            } else if (focused) {
                Tooltip.create(new Point(x + width / 2, y + height / 2), tooltip).queue();
            }
        }
    }
    
    protected boolean isFocused(int mouseX, int mouseY) {
        return containsMouse(mouseX, mouseY) || focused;
    }
    
    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return enabled && focusable ? ComponentPath.leaf(this) : null;
    }
    
    @Override
    public void onClick() {
        Consumer<Button> onClick = getOnClick();
        if (onClick != null)
            onClick.accept(this);
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (containsMouse(event.x(), event.y()) && isEnabled() && event.button() == 0) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onClick();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.isEnabled() && focused) {
            if (event.key() != 257 && event.key() != 32 && event.key() != 335) {
                return false;
            } else {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                onClick();
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    protected void renderBackground(GuiGraphics graphics, int x, int y, int width, int height, boolean focused) {
        renderBackground(graphics, x, y, width, height, focused, REIRuntime.getInstance().isDarkThemeEnabled(), Color.ofTransparent(0xFFFFFFFF));
    }
    
    protected void renderBackground(GuiGraphics graphics, int x, int y, int width, int height, boolean focused, boolean dark, Color color) {
        WidgetSprites sprites = dark ? DARK_SPRITES : SPRITES;
        Identifier texture = sprites.get(this.isEnabled(), focused);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height, color.getColor());
    }
}
