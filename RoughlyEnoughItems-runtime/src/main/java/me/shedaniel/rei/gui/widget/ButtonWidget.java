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

package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Tooltip;
import net.minecraft.client.gui.Element;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @see me.shedaniel.rei.api.widgets.Widgets#createButton(me.shedaniel.math.Rectangle, Text)
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public abstract class ButtonWidget extends WidgetWithBounds {
    
    protected static final Identifier BUTTON_LOCATION = new Identifier("roughlyenoughitems", "textures/gui/button.png");
    protected static final Identifier BUTTON_LOCATION_DARK = new Identifier("roughlyenoughitems", "textures/gui/button_dark.png");
    public boolean enabled = true;
    public boolean focused = false;
    private boolean canChangeFocuses = true;
    private String text;
    private Rectangle bounds;
    private Supplier<String> tooltipSupplier;
    
    protected ButtonWidget(Rectangle rectangle, Text text) {
        this.bounds = Objects.requireNonNull(rectangle);
        this.text = Objects.requireNonNull(text).getString();
    }
    
    public static ButtonWidget create(Rectangle point, String text, Consumer<ButtonWidget> onClick) {
        return create(point, new LiteralText(text), onClick);
    }
    
    public static ButtonWidget create(Rectangle point, Text text, Consumer<ButtonWidget> onClick) {
        ButtonWidget[] widget = {null};
        widget[0] = new ButtonWidget(point, text) {
            @Override
            public void onPressed() {
                onClick.accept(widget[0]);
            }
        };
        return widget[0];
    }
    
    public ButtonWidget tooltip(Supplier<String> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public ButtonWidget enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public ButtonWidget canChangeFocuses(boolean canChangeFocuses) {
        this.canChangeFocuses = canChangeFocuses;
        return this;
    }
    
    public boolean canChangeFocuses() {
        return canChangeFocuses;
    }
    
    @NotNull
    public Rectangle getBounds() {
        return bounds;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    protected int getTextureId(boolean boolean_1) {
        int int_1 = 1;
        if (!this.enabled) {
            int_1 = 0;
        } else if (boolean_1) {
            int_1 = 4; // 2 is the old blue highlight, 3 is the 1.15 outline, 4 is the 1.15 online + light hover
        }
        
        return int_1;
    }
    
    protected void renderBackground(MatrixStack matrices, int x, int y, int width, int height, int textureOffset) {
        minecraft.getTextureManager().bindTexture(REIHelper.getInstance().isDarkThemeEnabled() ? BUTTON_LOCATION_DARK : BUTTON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.blendFunc(770, 771);
        //Four Corners
        drawTexture(matrices, x, y, getZOffset(), 0, textureOffset * 80, 4, 4, 512, 256);
        drawTexture(matrices, x + width - 4, y, getZOffset(), 252, textureOffset * 80, 4, 4, 512, 256);
        drawTexture(matrices, x, y + height - 4, getZOffset(), 0, textureOffset * 80 + 76, 4, 4, 512, 256);
        drawTexture(matrices, x + width - 4, y + height - 4, getZOffset(), 252, textureOffset * 80 + 76, 4, 4, 512, 256);
        
        //Sides
        drawTexture(matrices, x + 4, y, getZOffset(), 4, textureOffset * 80, MathHelper.ceil((width - 8) / 2f), 4, 512, 256);
        drawTexture(matrices, x + 4, y + height - 4, getZOffset(), 4, textureOffset * 80 + 76, MathHelper.ceil((width - 8) / 2f), 4, 512, 256);
        drawTexture(matrices, x + 4 + MathHelper.ceil((width - 8) / 2f), y + height - 4, getZOffset(), 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80 + 76, MathHelper.floor((width - 8) / 2f), 4, 512, 256);
        drawTexture(matrices, x + 4 + MathHelper.ceil((width - 8) / 2f), y, getZOffset(), 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80, MathHelper.floor((width - 8) / 2f), 4, 512, 256);
        for (int i = y + 4; i < y + height - 4; i += 76) {
            drawTexture(matrices, x, i, getZOffset(), 0, 4 + textureOffset * 80, MathHelper.ceil(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76), 512, 256);
            drawTexture(matrices, x + MathHelper.ceil(width / 2f), i, getZOffset(), 256 - MathHelper.floor(width / 2f), 4 + textureOffset * 80, MathHelper.floor(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76), 512, 256);
        }
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        renderBackground(matrices, x, y, width, height, this.getTextureId(isHovered(mouseX, mouseY)));
        
        int color = 14737632;
        if (!this.enabled) {
            color = 10526880;
        } else if (isHovered(mouseX, mouseY)) {
            color = 16777120;
        }
        
        this.drawCenteredString(matrices, font, getText(), x + width / 2, y + (height - 8) / 2, color);
        
        if (getTooltips().isPresent())
            if (!focused && containsMouse(mouseX, mouseY))
                Tooltip.create(Stream.of(getTooltips().get().split("\n")).map(LiteralText::new).collect(Collectors.toList())).queue();
            else if (focused)
                Tooltip.create(new Point(x + width / 2, y + height / 2), Stream.of(getTooltips().get().split("\n")).map(LiteralText::new).collect(Collectors.toList())).queue();
    }
    
    public boolean isHovered(int mouseX, int mouseY) {
        return isMouseOver(mouseX, mouseY) || focused;
    }
    
    @Override
    public boolean changeFocus(boolean boolean_1) {
        if (!enabled || !canChangeFocuses)
            return false;
        this.focused = !this.focused;
        return true;
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && enabled && button == 0) {
            minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onPressed();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.enabled && focused) {
            if (int_1 != 257 && int_1 != 32 && int_1 != 335) {
                return false;
            } else {
                minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.onPressed();
                return true;
            }
        }
        return false;
    }
    
    public abstract void onPressed();
    
    public Optional<String> getTooltips() {
        return Optional.ofNullable(tooltipSupplier).map(Supplier::get);
    }
    
}
