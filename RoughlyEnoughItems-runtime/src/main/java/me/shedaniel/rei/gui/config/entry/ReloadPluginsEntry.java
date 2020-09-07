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

package me.shedaniel.rei.gui.config.entry;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.shedaniel.clothconfig2.forge.api.AbstractConfigListEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.gui.ConfigReloadingScreen;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Unit;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;

@ApiStatus.Internal
public class ReloadPluginsEntry extends AbstractConfigListEntry<Unit> {
    private int width;
    private Widget buttonWidget = new Button(0, 0, 0, 20, NarratorChatListener.NO_TITLE, button -> RoughlyEnoughItemsCore.syncRecipes(null)) {
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            if (RecipeHelper.getInstance().arePluginsLoading()) {
                Minecraft.getInstance().setScreen(new ConfigReloadingScreen(Minecraft.getInstance().screen));
            } else
                super.render(matrices, mouseX, mouseY, delta);
        }
    };
    private List<IGuiEventListener> children = ImmutableList.of(buttonWidget);
    
    public ReloadPluginsEntry(int width) {
        super(NarratorChatListener.NO_TITLE, false);
        this.width = width;
        buttonWidget.setMessage(new TranslationTextComponent("text.rei.reload_config"));
    }
    
    @Override
    public Unit getValue() {
        return Unit.INSTANCE;
    }
    
    @Override
    public Optional<Unit> getDefaultValue() {
        return Optional.of(Unit.INSTANCE);
    }
    
    @Override
    public void save() {
        
    }
    
    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        MainWindow window = Minecraft.getInstance().getWindow();
        this.buttonWidget.active = this.isEditable();
        this.buttonWidget.y = y;
        this.buttonWidget.x = x + entryWidth / 2 - width / 2;
        this.buttonWidget.setWidth(width);
        this.buttonWidget.render(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public List<? extends IGuiEventListener> children() {
        return children;
    }
}
