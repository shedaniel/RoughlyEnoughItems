/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public class OverlaySearchField extends TextFieldWidget {
    
    public static boolean isSearching = false;
    public long keybindFocusTime = -1;
    public int keybindFocusKey = -1;
    protected Pair<Long, Point> lastClickedDetails = null;
    private List<String> history = Lists.newArrayListWithCapacity(100);
    public boolean isMain = true;
    
    public OverlaySearchField(int x, int y, int width, int height) {
        super(x, y, width, height);
        setMaxLength(10000);
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
            history.add(text);
            if (history.size() > 100)
                history.remove(0);
        }
    }
    
    public void laterRender(int int_1, int int_2, float float_1) {
        RenderSystem.disableDepthTest();
        setEditableColor(isMain && ContainerScreenOverlay.getEntryListWidget().getAllStacks().isEmpty() && !getText().isEmpty() ? 16733525 : isSearching && isMain ? -852212 : (containsMouse(PointHelper.fromMouse()) || isFocused()) ? (ScreenHelper.isDarkModeEnabled() ? -17587 : -1) : -6250336);
        setSuggestion(!isFocused() && getText().isEmpty() ? I18n.translate("text.rei.search.field.suggestion") : null);
        super.render(int_1, int_2, float_1);
        RenderSystem.enableDepthTest();
    }
    
    @Override
    protected void renderSuggestion(int x, int y) {
        if (containsMouse(PointHelper.fromMouse()) || isFocused())
            this.font.drawWithShadow(this.font.trimToWidth(this.getSuggestion(), this.getWidth()), x, y, ScreenHelper.isDarkModeEnabled() ? 0xccddaa3d : 0xddeaeaea);
        else
            this.font.drawWithShadow(this.font.trimToWidth(this.getSuggestion(), this.getWidth()), x, y, -6250336);
    }
    
    @Override
    public void renderBorder() {
        if (isMain && isSearching) {
            fill(this.getBounds().x - 1, this.getBounds().y - 1, this.getBounds().x + this.getBounds().width + 1, this.getBounds().y + this.getBounds().height + 1, -852212);
        } else if (isMain && ContainerScreenOverlay.getEntryListWidget().getAllStacks().isEmpty() && !getText().isEmpty()) {
            fill(this.getBounds().x - 1, this.getBounds().y - 1, this.getBounds().x + this.getBounds().width + 1, this.getBounds().y + this.getBounds().height + 1, -43691);
        } else {
            super.renderBorder();
            return;
        }
        fill(this.getBounds().x, this.getBounds().y, this.getBounds().x + this.getBounds().width, this.getBounds().y + this.getBounds().height, -16777216);
    }
    
    public int getManhattanDistance(Point point1, Point point2) {
        int e = Math.abs(point1.getX() - point2.getX());
        int f = Math.abs(point1.getY() - point2.getY());
        return e + f;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        boolean contains = containsMouse(double_1, double_2);
        if (isVisible() && contains && int_1 == 1)
            setText("");
        if (contains && int_1 == 0 && isMain)
            if (lastClickedDetails == null)
                lastClickedDetails = new Pair<>(System.currentTimeMillis(), new Point(double_1, double_2));
            else if (System.currentTimeMillis() - lastClickedDetails.getLeft() > 1500)
                lastClickedDetails = null;
            else if (getManhattanDistance(lastClickedDetails.getRight(), new Point(double_1, double_2)) <= 25) {
                lastClickedDetails = null;
                isSearching = !isSearching;
                minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            } else {
                lastClickedDetails = new Pair<>(System.currentTimeMillis(), new Point(double_1, double_2));
            }
        return super.mouseClicked(double_1, double_2, int_1);
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
        if (System.currentTimeMillis() - keybindFocusTime < 1000 && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), keybindFocusKey)) {
            keybindFocusTime = -1;
            keybindFocusKey = -1;
            return true;
        }
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return (!isMain || ScreenHelper.getLastOverlay().isNotInExclusionZones(mouseX, mouseY)) && super.containsMouse(mouseX, mouseY);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
    }
    
}
