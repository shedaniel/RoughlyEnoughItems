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

package me.shedaniel.rei.gui.modules.entries;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.gui.modules.MenuEntry;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GameModeMenuEntry extends MenuEntry {
    public final String text;
    public final GameType gameMode;
    private int x, y, width;
    private boolean selected, containsMouse, rendering;
    private int textWidth = -69;
    
    public GameModeMenuEntry(GameType gameMode) {
        this.text = gameMode.getDisplayName().getString();
        this.gameMode = gameMode;
    }
    
    private int getTextWidth() {
        if (textWidth == -69) {
            this.textWidth = Math.max(0, font.width(text));
        }
        return this.textWidth;
    }
    
    @Override
    public int getEntryWidth() {
        return getTextWidth() + 4;
    }
    
    @Override
    public int getEntryHeight() {
        return 12;
    }
    
    @Override
    public List<? extends IGuiEventListener> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void updateInformation(int xPos, int yPos, boolean selected, boolean containsMouse, boolean rendering, int width) {
        this.x = xPos;
        this.y = yPos;
        this.selected = selected;
        this.containsMouse = containsMouse;
        this.rendering = rendering;
        this.width = width;
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (selected) {
            fill(matrices, x, y, x + width, y + 12, -12237499);
        }
        if (selected && containsMouse) {
            REIHelper.getInstance().queueTooltip(Tooltip.create(new TranslationTextComponent("text.rei.gamemode_button.tooltip.entry", text)));
        }
        font.draw(matrices, text, x + 2, y + 2, selected ? 16777215 : 8947848);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12) {
            Minecraft.getInstance().player.chat(ConfigObject.getInstance().getGamemodeCommand().replaceAll("\\{gamemode}", gameMode.name().toLowerCase(Locale.ROOT)));
            minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ScreenHelper.getLastOverlay().removeGameModeMenu();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
