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

package me.shedaniel.rei.impl.client.gui.widget.search;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Color;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.config.SyntaxHighlightingMode;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.hints.HintProvider;
import me.shedaniel.rei.impl.client.gui.text.TextTransformations;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentType;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentTypesRegistry;
import me.shedaniel.rei.impl.client.search.argument.type.TextArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Consumer;

@ApiStatus.Internal
public class OverlaySearchField extends TextFieldWidget implements TextFieldWidget.TextFormatter {
    public static boolean isHighlighting = false;
    private static final Style SPLITTER_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private static final Style QUOTES_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD);
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xff5555));
    private boolean previouslyClicking = false;
    private final OverlaySearchFieldSyntaxHighlighter highlighter = new OverlaySearchFieldSyntaxHighlighter(getText());
    public long keybindFocusTime = -1;
    public int keybindFocusKey = -1;
    public boolean isMain = true;
    protected Tuple<Long, Point> lastClickedDetails = null;
    private final List<String> history = Lists.newArrayListWithCapacity(100);
    private final NumberAnimator<Double> progress = ValueAnimator.ofDouble();
    
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
            if (isMain && ScreenOverlayImpl.getEntryListWidget().isEmpty() && !getText().isEmpty()) {
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
    public void setFocused(boolean focused) {
        if (isFocused() != focused && isMain)
            addToHistory(getText());
        super.setFocused(focused);
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
    
    public void laterRender(PoseStack matrices, int mouseX, int mouseY, float delta) {
        progress.update(delta);
        RenderSystem.disableDepthTest();
        if (isMain) drawHint(matrices, mouseX, mouseY);
        RenderSystem.enableDepthTest();
    }
    
    private void drawHint(PoseStack poses, int mouseX, int mouseY) {
        boolean mouseDown = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != 0;
        boolean clicking = false;
        if (mouseDown != previouslyClicking) {
            previouslyClicking = mouseDown;
            clicking = mouseDown;
        }
        
        List<HintProvider> hintProviders = REIRuntimeImpl.getInstance().getHintProviders();
        List<Pair<HintProvider, Component>> hints = CollectionUtils.flatMap(hintProviders, provider ->
                CollectionUtils.map(provider.provide(), component -> new Pair<>(provider, component)));
        if (hints.isEmpty()) return;
        int width = getBounds().getWidth() - 4;
        List<Pair<HintProvider, FormattedCharSequence>> sequences = CollectionUtils.flatMap(hints, pair ->
                CollectionUtils.map(font.split(pair.getSecond(), width - 6), sequence -> new Pair<>(pair.getFirst(), sequence)));
        OptionalDouble progress = hintProviders.stream().map(HintProvider::getProgress).filter(Objects::nonNull).mapToDouble(Double::doubleValue)
                .average();
        List<HintProvider.HintButton> buttons = hints.stream().map(Pair::getFirst).distinct()
                .map(HintProvider::getButtons)
                .flatMap(List::stream)
                .toList();
        boolean hasProgress = progress.isPresent();
        if (!hasProgress) {
            this.progress.setAs(0);
        } else {
            this.progress.setTo(progress.getAsDouble(), 200);
        }
        Color color = hints.stream()
                .map(Pair::getFirst)
                .distinct()
                .map(HintProvider::getColor)
                .reduce((color1, color2) -> {
                    int r = color1.getRed() - (color1.getRed() - color2.getRed()) / 2;
                    int g = color1.getGreen() - (color1.getGreen() - color2.getGreen()) / 2;
                    int b = color1.getBlue() - (color1.getBlue() - color2.getBlue()) / 2;
                    return Color.ofRGBA(r, g, b, (color1.getAlpha() + color2.getAlpha()) / 2);
                }).orElse(Color.ofTransparent(0x50000000));
        int height = 6 + font.lineHeight * sequences.size() + (hasProgress ? 2 : 0) + (buttons.isEmpty() ? 0 : (int) Math.ceil(buttons.size() / 3.0) * 20);
        boolean top = ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE;
        int x = getBounds().getX() + 2;
        int y = getBounds().getY() + (top ? getBounds().getHeight() : -height);
        if (new Rectangle(x - 1, y - 1, width + 2, height + 2).contains(mouseX, mouseY))
            ScreenOverlayImpl.getInstance().clearTooltips();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f pose = poses.last().pose();
        int background = 0xf0100010;
        int color1 = color.getColor();
        int color2 = color.darker(2).getColor();
        if (!top) fillGradient(pose, bufferBuilder, x, y - 1, x + width, y, 400, background, background);
        if (top) fillGradient(pose, bufferBuilder, x, y + height, x + width, y + height + 1, 400, background, background);
        fillGradient(pose, bufferBuilder, x, y, x + width, y + height, 400, background, background);
        fillGradient(pose, bufferBuilder, x - 1, y, x, y + height, 400, background, background);
        fillGradient(pose, bufferBuilder, x + width, y, x + width + 1, y + height, 400, background, background);
        fillGradient(pose, bufferBuilder, x, y + 1, x + 1, y + height - 1, 400, color1, color2);
        fillGradient(pose, bufferBuilder, x + width - 1, y + 1, x + width, y + height - 1, 400, color1, color2);
        if (!top) fillGradient(pose, bufferBuilder, x, y, x + width, y + 1, 400, color1, color1);
        if (top) fillGradient(pose, bufferBuilder, x, y + height - 1, x + width, y + height, 400, color2, color2);
        
        if (hasProgress) {
            int progressWidth = (int) Math.round(width * this.progress.doubleValue());
            fillGradient(pose, bufferBuilder, x + 1, y + height - 3, x + progressWidth - 1, y + height - 1, 400, 0xffffffff, 0xffffffff);
        }
        
        BufferUploader.drawWithShader(bufferBuilder.end());
        poses.pushPose();
        poses.translate(0.0D, 0.0D, 450.0D);
        for (int i = 0; i < sequences.size(); i++) {
            Pair<HintProvider, FormattedCharSequence> pair = sequences.get(i);
            int lineWidth = font.drawShadow(poses, pair.getSecond(), x + 3, y + 3 + font.lineHeight * i, -1);
            if (new Rectangle(x + 3, y + 3 + font.lineHeight * i, lineWidth, font.lineHeight).contains(mouseX, mouseY)) {
                Tooltip tooltip = pair.getFirst().provideTooltip(new Point(mouseX, mouseY));
                if (tooltip != null) {
                    ScreenOverlayImpl.getInstance().clearTooltips();
                    ScreenOverlayImpl.getInstance().renderTooltip(poses, tooltip);
                }
            }
        }
        
        int split = 2;
        for (HintProvider.HintButton button : buttons) {
            int x1 = x + 4 + ((width - 8 - 8) / split) * (buttons.indexOf(button) % split);
            int y1 = y + height - 20 - 20 * (int) Math.floor(buttons.indexOf(button) / (float) split);
            int x2 = x1 + (width - 8 - 8) / split;
            int y2 = y1 + 16;
            Rectangle bounds = new Rectangle(x1, y1, x2 - x1 - 4, y2 - y1);
            int buttonColor = bounds.contains(mouseX, mouseY) ? 0x8f8f8f8f : 0x66666666;
            fillGradient(poses, x1, y1, x2 - 4, y2, buttonColor, buttonColor);
            font.drawShadow(poses, button.name(), (x1 + x2 - 4 - font.width(button.name())) / 2, y1 + 4, -1);
            
            if (bounds.contains(mouseX, mouseY) && clicking) {
                Widgets.produceClickSound();
                button.action().accept(bounds);
            }
        }
        
        poses.popPose();
    }
    
    @Override
    protected void renderSuggestion(PoseStack matrices, int x, int y) {
        int color;
        if (containsMouse(PointHelper.ofMouse()) || isFocused()) {
            color = 0xddeaeaea;
        } else {
            color = -6250336;
        }
        this.font.drawShadow(matrices, this.font.plainSubstrByWidth(this.getSuggestion(), this.getWidth()), x, y, color);
    }
    
    @Override
    public void renderBorder(PoseStack matrices) {
        isHighlighting = isHighlighting && ConfigObject.getInstance().isInventoryHighlightingAllowed();
        int borderColor;
        if (isMain && isHighlighting) {
            borderColor = 0xfff2ff0c;
        } else if (isMain && ScreenOverlayImpl.getEntryListWidget().isEmpty() && !getText().isEmpty()) {
            borderColor = 0xffff5555;
        } else {
            super.renderBorder(matrices);
            return;
        }
        fill(matrices, this.getBounds().x - 1, this.getBounds().y - 1, this.getBounds().x + this.getBounds().width + 1, this.getBounds().y + this.getBounds().height + 1, 0xff000000);
        fill(matrices, this.getBounds().x, this.getBounds().y, this.getBounds().x + this.getBounds().width, this.getBounds().y + this.getBounds().height, borderColor);
        fill(matrices, this.getBounds().x + 1, this.getBounds().y + 1, this.getBounds().x + this.getBounds().width - 1, this.getBounds().y + this.getBounds().height - 1, 0xff000000);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isVisible() && this.isFocused() && isMain)
            if (keyCode == 257 || keyCode == 335) {
                addToHistory(getText());
                setFocused(false);
                return true;
            } else if (keyCode == 265) {
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
            } else if (keyCode == 264) {
                int i = history.indexOf(getText()) + 1;
                if (i > 0) {
                    setText(i < history.size() ? history.get(i) : "");
                    return true;
                }
            }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.isVisible() && this.isFocused() && isMain && keybindFocusKey != -1) {
            keybindFocusTime = -1;
            keybindFocusKey = -1;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        if (isMain && System.currentTimeMillis() - keybindFocusTime < 1000 && keybindFocusKey != -1 && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybindFocusKey)) {
            keybindFocusTime = -1;
            keybindFocusKey = -1;
            return true;
        }
        return super.charTyped(character, modifiers);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return (!isMain || REIRuntime.getInstance().getOverlay().get().isNotInExclusionZones(mouseX, mouseY)) && super.containsMouse(mouseX, mouseY);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.disableDepthTest();
        setSuggestion(!isFocused() && getText().isEmpty() ? I18n.get("text.rei.search.field.suggestion") : null);
        super.render(matrices, mouseX, mouseY, delta);
        RenderSystem.enableDepthTest();
    }
}
