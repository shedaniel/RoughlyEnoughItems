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

package me.shedaniel.rei.impl.client.gui.widget.basewidgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueProvider;
import me.shedaniel.math.Color;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    private static final ResourceLocation BUTTON_LOCATION = new ResourceLocation("roughlyenoughitems", "textures/gui/button.png");
    private static final ResourceLocation BUTTON_LOCATION_DARK = new ResourceLocation("roughlyenoughitems", "textures/gui/button_dark.png");
    private Rectangle bounds;
    private boolean enabled = true;
    private Component text;
    @Nullable
    private Integer tint;
    @Nullable
    private Consumer<Button> onClick;
    @Nullable
    private BiConsumer<PoseStack, Button> onRender;
    private boolean focusable = false;
    private boolean focused = false;
    @Nullable
    private Function<Button, @Nullable Component[]> tooltipFunction;
    @Nullable
    private BiFunction<Button, Point, Integer> textColorFunction;
    @Nullable
    private BiFunction<Button, Point, Integer> textureIdFunction;
    private final ValueAnimator<Color> darkBackground;
    private ValueProvider<Double> alpha;
    
    public ButtonWidget(Rectangle rectangle, Component text) {
        this.bounds = new Rectangle(Objects.requireNonNull(rectangle));
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
    public final BiConsumer<PoseStack, Button> getOnRender() {
        return onRender;
    }
    
    @Override
    public final void setOnRender(BiConsumer<PoseStack, Button> onRender) {
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
    public final void setTextureId(@Nullable BiFunction<Button, Point, Integer> textureIdFunction) {
        this.textureIdFunction = textureIdFunction;
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
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        darkBackground.update(delta);
        alpha.update(delta);
        if (onRender != null) {
            onRender.accept(matrices, this);
        }
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        int alphaAsInt = (int) (alpha.value() * 255);
        renderBackground(matrices, x, y, width, height, this.getTextureId(new Point(mouseX, mouseY)), false, Color.ofTransparent(0xFFFFFF | (alphaAsInt << 24)));
        Color darkBackgroundColor = darkBackground.value();
        darkBackgroundColor = Color.ofRGBA(darkBackgroundColor.getRed(), darkBackgroundColor.getGreen(), darkBackgroundColor.getBlue(), (int) Math.round(darkBackgroundColor.getAlpha() * alpha.value()));
        renderBackground(matrices, x, y, width, height, this.getTextureId(new Point(mouseX, mouseY)), true, darkBackgroundColor);
        
        int color = 0xe0e0e0;
        if (!this.enabled) {
            color = 0xa0a0a0;
        } else if (isFocused(mouseX, mouseY)) {
            color = 0xffffa0;
        }
        
        if (tint != null) {
            fillGradient(matrices, x + 1, y + 1, x + width - 1, y + height - 1, tint, tint);
        }
        
        if (alphaAsInt > 10) {
            drawCenteredString(matrices, font, getText(), x + width / 2, y + (height - 8) / 2, color | (alphaAsInt << 24));
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
    
    @Override
    public boolean changeFocus(boolean boolean_1) {
        if (!enabled || !focusable)
            return false;
        this.focused = !this.focused;
        return true;
    }
    
    @Override
    public void onClick() {
        Consumer<Button> onClick = getOnClick();
        if (onClick != null)
            onClick.accept(this);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (containsMouse(mouseX, mouseY) && isEnabled() && button == 0) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onClick();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.isEnabled() && focused) {
            if (int_1 != 257 && int_1 != 32 && int_1 != 335) {
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
    
    @Override
    public final int getTextureId(Point mouse) {
        if (this.textureIdFunction != null) {
            Integer apply = this.textureIdFunction.apply(this, mouse);
            if (apply != null)
                return apply;
        }
        if (!this.isEnabled()) {
            return 0;
        } else if (containsMouse(mouse) || focused) {
            return 4; // 2 is the old blue highlight, 3 is the 1.15 outline, 4 is the 1.15 online + light hover
        }
        return 1;
    }
    
    protected void renderBackground(PoseStack matrices, int x, int y, int width, int height, int textureOffset) {
        renderBackground(matrices, x, y, width, height, textureOffset, REIRuntime.getInstance().isDarkThemeEnabled(), Color.ofTransparent(0xFFFFFFFF));
    }
    
    protected void renderBackground(PoseStack matrices, int x, int y, int width, int height, int textureOffset, boolean dark, Color color) {
        RenderSystem.setShaderTexture(0, dark ? BUTTON_LOCATION_DARK : BUTTON_LOCATION);
        RenderSystem.setShaderColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.blendFunc(770, 771);
        
        // 9 Patch Texture
        
        // Four Corners
        blit(matrices, x, y, getBlitOffset(), 0, textureOffset * 80, 8, 8, 256, 512);
        blit(matrices, x + width - 8, y, getBlitOffset(), 248, textureOffset * 80, 8, 8, 256, 512);
        blit(matrices, x, y + height - 8, getBlitOffset(), 0, textureOffset * 80 + 72, 8, 8, 256, 512);
        blit(matrices, x + width - 8, y + height - 8, getBlitOffset(), 248, textureOffset * 80 + 72, 8, 8, 256, 512);
        
        Matrix4f matrix = matrices.last().pose();
        // Sides
        GuiComponent.innerBlit(matrix, x + 8, x + width - 8, y, y + 8, getZ(), (8) / 256f, (248) / 256f, (textureOffset * 80) / 512f, (textureOffset * 80 + 8) / 512f);
        GuiComponent.innerBlit(matrix, x + 8, x + width - 8, y + height - 8, y + height, getZ(), (8) / 256f, (248) / 256f, (textureOffset * 80 + 72) / 512f, (textureOffset * 80 + 80) / 512f);
        GuiComponent.innerBlit(matrix, x, x + 8, y + 8, y + height - 8, getZ(), (0) / 256f, (8) / 256f, (textureOffset * 80 + 8) / 512f, (textureOffset * 80 + 72) / 512f);
        GuiComponent.innerBlit(matrix, x + width - 8, x + width, y + 8, y + height - 8, getZ(), (248) / 256f, (256) / 256f, (textureOffset * 80 + 8) / 512f, (textureOffset * 80 + 72) / 512f);
        
        // Center
        GuiComponent.innerBlit(matrix, x + 8, x + width - 8, y + 8, y + height - 8, getZ(), (8) / 256f, (248) / 256f, (textureOffset * 80 + 8) / 512f, (textureOffset * 80 + 72) / 512f);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
