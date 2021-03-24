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

package me.shedaniel.rei.impl.client.gui.widget.search;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Color;
import me.shedaniel.math.Point;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SyntaxHighlightingMode;
import me.shedaniel.rei.impl.client.gui.ContainerScreenOverlay;
import me.shedaniel.rei.impl.client.gui.text.TextTransformations;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentType;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentTypesRegistry;
import me.shedaniel.rei.impl.client.search.argument.type.TextArgumentType;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
public class OverlaySearchField extends TextFieldWidget implements TextFieldWidget.TextFormatter {
    public static boolean isHighlighting = false;
    private static final Style SPLITTER_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private static final Style QUOTES_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD);
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xff5555));
    private final OverlaySearchFieldSyntaxHighlighter highlighter = new OverlaySearchFieldSyntaxHighlighter(this);
    public long keybindFocusTime = -1;
    public int keybindFocusKey = -1;
    public boolean isMain = true;
    protected Tuple<Long, Point> lastClickedDetails = null;
    private List<String> history = Lists.newArrayListWithCapacity(100);
    
    public OverlaySearchField(int x, int y, int width, int height) {
        super(x, y, width, height);
        setMaxLength(10000);
        setFormatter(this);
        super.setResponder(highlighter);
    }
    
    @Override
    public FormattedCharSequence format(TextFieldWidget widget, String text, int index) {
        boolean isPlain = ConfigObject.getInstance().getSyntaxHighlightingMode() == SyntaxHighlightingMode.PLAIN || ConfigObject.getInstance().getSyntaxHighlightingMode() == SyntaxHighlightingMode.PLAIN_UNDERSCORED;
        boolean hasUnderscore = ConfigObject.getInstance().getSyntaxHighlightingMode() == SyntaxHighlightingMode.PLAIN_UNDERSCORED || ConfigObject.getInstance().getSyntaxHighlightingMode() == SyntaxHighlightingMode.COLORFUL_UNDERSCORED;
        return TextTransformations.forwardWithTransformation(text, (s, charIndex, c) -> {
            byte arg = highlighter.highlighted[charIndex + index];
            Style style = Style.EMPTY;
            if (isMain && ContainerScreenOverlay.getEntryListWidget().getAllStacks().isEmpty() && !getText().isEmpty()) {
                style = ERROR_STYLE;
            }
            if (arg > 0) {
                ArgumentType<?, ?> argumentType = ArgumentTypesRegistry.ARGUMENT_TYPE_LIST.get((arg - 1) / 2);
                if (!isPlain) {
                    style = argumentType.getHighlightedStyle();
                }
                if (!(argumentType instanceof TextArgumentType) && hasUnderscore && arg % 2 == 1) {
                    style = style.withUnderlined(true);
                }
            } else if (!isPlain) {
                if (arg == -1) {
                    style = SPLITTER_STYLE;
                } else if (arg == -2) {
                    style = QUOTES_STYLE;
                }
            }
            
            if (containsMouse(PointHelper.ofMouse()) || isFocused()) {
                return style;
            }
            return style.withColor(TextColor.fromRgb(Color.ofOpaque(style.getColor() == null ? -1 : style.getColor().getValue()).brighter(0.75).getColor()));
        });
    }
    
    @Override
    public void setResponder(Consumer<String> responder) {
        super.setResponder(highlighter.andThen(responder));
    }
    
    @Override
    public void setFocused(boolean boolean_1) {
        if (isFocused() != boolean_1 && isMain)
            addToHistory(getText());
        super.setFocused(boolean_1);
    }
    
    @ApiStatus.Internal
    public void addToHistory(String text) {
        if (!text.isEmpty()) {
            history.removeIf(str -> str.equalsIgnoreCase(text));
            if (history.size() > 100)
                history.remove(0);
            history.add(text);
        }
    }
    
    public void laterRender(PoseStack matrices, int int_1, int int_2, float float_1) {
        RenderSystem.disableDepthTest();
        setSuggestion(!isFocused() && getText().isEmpty() ? I18n.get("text.rei.search.field.suggestion") : null);
        super.render(matrices, int_1, int_2, float_1);
        RenderSystem.enableDepthTest();
    }
    
    @Override
    protected void renderSuggestion(PoseStack matrices, int x, int y) {
        matrices.pushPose();
        matrices.translate(0, 0, 400);
        int color;
        if (containsMouse(PointHelper.ofMouse()) || isFocused()) {
            color = 0xddeaeaea;
        } else {
            color = -6250336;
        }
        this.font.drawShadow(matrices, this.font.plainSubstrByWidth(this.getSuggestion(), this.getWidth()), x, y, color);
        matrices.popPose();
    }
    
    @Override
    public void renderBorder(PoseStack matrices) {
        isHighlighting = isHighlighting && ConfigObject.getInstance().isInventoryHighlightingAllowed();
        if (isMain && isHighlighting) {
            fill(matrices, this.getBounds().x - 1, this.getBounds().y - 1, this.getBounds().x + this.getBounds().width + 1, this.getBounds().y + this.getBounds().height + 1, -852212);
        } else if (isMain && ContainerScreenOverlay.getEntryListWidget().getAllStacks().isEmpty() && !getText().isEmpty()) {
            fill(matrices, this.getBounds().x - 1, this.getBounds().y - 1, this.getBounds().x + this.getBounds().width + 1, this.getBounds().y + this.getBounds().height + 1, -43691);
        } else {
            super.renderBorder(matrices);
            return;
        }
        fill(matrices, this.getBounds().x, this.getBounds().y, this.getBounds().x + this.getBounds().width, this.getBounds().y + this.getBounds().height, -16777216);
    }
    
    public int getManhattanDistance(Point point1, Point point2) {
        int e = Math.abs(point1.getX() - point2.getX());
        int f = Math.abs(point1.getY() - point2.getY());
        return e + f;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean contains = containsMouse(mouseX, mouseY);
        if (isVisible() && contains && button == 1)
            setText("");
        if (contains && button == 0 && isMain && ConfigObject.getInstance().isInventoryHighlightingAllowed())
            if (lastClickedDetails == null)
                lastClickedDetails = new Tuple<>(System.currentTimeMillis(), new Point(mouseX, mouseY));
            else if (System.currentTimeMillis() - lastClickedDetails.getA() > 1500)
                lastClickedDetails = null;
            else if (getManhattanDistance(lastClickedDetails.getB(), new Point(mouseX, mouseY)) <= 25) {
                lastClickedDetails = null;
                isHighlighting = !isHighlighting;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            } else {
                lastClickedDetails = new Tuple<>(System.currentTimeMillis(), new Point(mouseX, mouseY));
            }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.isVisible() && this.isFocused() && isMain)
            if (int_1 == 257 || int_1 == 335) {
                addToHistory(getText());
                setFocused(false);
                return true;
            } else if (int_1 == 265) {
                int i = history.indexOf(getText()) - 1;
                if (i < -1 && getText().isEmpty())
                    i = history.size() - 1;
                else if (i < -1) {
                    addToHistory(getText());
                    i = history.size() - 2;
                }
                if (i >= 0) {
                    setText(history.get(i));
                    return true;
                }
            } else if (int_1 == 264) {
                int i = history.indexOf(getText()) + 1;
                if (i > 0) {
                    setText(i < history.size() ? history.get(i) : "");
                    return true;
                }
            }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (System.currentTimeMillis() - keybindFocusTime < 1000 && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybindFocusKey)) {
            keybindFocusTime = -1;
            keybindFocusKey = -1;
            return true;
        }
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return (!isMain || REIHelper.getInstance().getOverlay().get().isNotInExclusionZones(mouseX, mouseY)) && super.containsMouse(mouseX, mouseY);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
    }
}
