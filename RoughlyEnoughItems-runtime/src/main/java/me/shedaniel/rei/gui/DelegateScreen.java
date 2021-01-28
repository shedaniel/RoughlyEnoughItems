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

package me.shedaniel.rei.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.rei.utils.ImmutableLiteralText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApiStatus.Internal
@ApiStatus.Experimental
public class DelegateScreen extends Screen {
    protected Screen parent;
    
    public DelegateScreen(Screen parent) {
        super(parent == null ? null : parent.getTitle());
        this.parent = parent;
    }
    
    @Override
    public Component getTitle() {
        return parent == null ? ImmutableLiteralText.EMPTY : parent.getTitle();
    }
    
    @Override
    public String getNarrationMessage() {
        return parent == null ? "" : parent.getNarrationMessage();
    }
    
    @Override
    public boolean keyPressed(int i, int j, int k) {
        return parent != null && parent.keyPressed(i, j, k);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return parent == null || parent.shouldCloseOnEsc();
    }
    
    @Override
    public void onClose() {
        if (parent != null) {
            parent.onClose();
        }
    }
    
    @Override
    public <T extends AbstractWidget> T addButton(T abstractWidget) {
        if (parent != null) {
            return parent.addButton(abstractWidget);
        }
        return abstractWidget;
    }
    
    @Override
    public <T extends GuiEventListener> T addWidget(T guiEventListener) {
        if (parent != null) {
            return parent.addWidget(guiEventListener);
        }
        return guiEventListener;
    }
    
    @Override
    public List<Component> getTooltipFromItem(ItemStack itemStack) {
        if (parent == null) {
            return super.getTooltipFromItem(itemStack);
        }
        return parent.getTooltipFromItem(itemStack);
    }
    
    @Override
    public void insertText(String string, boolean bl) {
        if (parent != null) {
            parent.insertText(string, bl);
        }
    }
    
    @Override
    public boolean handleComponentClicked(@Nullable Style style) {
        return parent != null && parent.handleComponentClicked(style);
    }
    
    @Override
    public void sendMessage(String string) {
        if (parent != null) {
            parent.sendMessage(string);
        }
    }
    
    @Override
    public void sendMessage(String string, boolean bl) {
        if (parent != null) {
            parent.sendMessage(string, bl);
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return parent == null ? Collections.emptyList() : parent.children();
    }
    
    @Override
    public void tick() {
        if (parent != null) {
            parent.tick();
        }
    }
    
    @Override
    public void removed() {
        if (parent != null) {
            parent.removed();
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return parent != null && parent.isPauseScreen();
    }
    
    @Override
    public boolean isValidCharacterForName(String string, char c, int i) {
        return parent != null && parent.isValidCharacterForName(string, c, i);
    }
    
    @Override
    public boolean isMouseOver(double d, double e) {
        return parent != null && parent.isMouseOver(d, e);
    }
    
    @Override
    public void onFilesDrop(List<Path> list) {
        if (parent != null) {
            parent.onFilesDrop(list);
        }
    }
    
    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return parent == null ? null : parent.getFocused();
    }
    
    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        if (parent != null) {
            parent.setFocused(guiEventListener);
        }
    }
    
    @Override
    public int getBlitOffset() {
        return parent == null ? 0 : parent.getBlitOffset();
    }
    
    @Override
    public void setBlitOffset(int i) {
        if (parent != null) {
            parent.setBlitOffset(i);
        }
    }
    
    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return parent != null && parent.mouseClicked(d, e, i);
    }
    
    @Override
    public boolean mouseReleased(double d, double e, int i) {
        return parent != null && parent.mouseReleased(d, e, i);
    }
    
    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        return parent != null && parent.mouseDragged(d, e, i, f, g);
    }
    
    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        return parent != null && parent.mouseScrolled(d, e, f);
    }
    
    @Override
    public boolean keyReleased(int i, int j, int k) {
        return parent != null && parent.keyReleased(i, j, k);
    }
    
    @Override
    public boolean charTyped(char c, int i) {
        return parent != null && parent.charTyped(c, i);
    }
    
    @Override
    public void setInitialFocus(@Nullable GuiEventListener guiEventListener) {
        if (parent != null) {
            parent.setInitialFocus(guiEventListener);
        }
    }
    
    @Override
    public void magicalSpecialHackyFocus(@Nullable GuiEventListener guiEventListener) {
        if (parent != null) {
            parent.magicalSpecialHackyFocus(guiEventListener);
        }
    }
    
    @Override
    public boolean changeFocus(boolean bl) {
        return parent != null && parent.changeFocus(bl);
    }
    
    @Override
    public void mouseMoved(double d, double e) {
        if (parent != null) {
            parent.mouseMoved(d, e);
        }
    }
    
    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        if (parent != null) {
            parent.resize(minecraft, i, j);
        }
    }
    
    private boolean init = true;
    
    @Override
    public void init(Minecraft minecraft, int i, int j) {
        if (parent != null) {
            parent.init(minecraft, i, j);
        }
        init = false;
        super.init(minecraft, i, j);
    }
    
    @Override
    public void init() {
        if (parent != null) {
            if (init) {
                parent.init();
            } else {
                init = true;
            }
        }
    }
    
    @Override
    public Optional<GuiEventListener> getChildAt(double d, double e) {
        return parent == null ? Optional.empty() : parent.getChildAt(d, e);
    }
    
    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (parent != null) {
            parent.render(poseStack, i, j, f);
        }
    }
}
