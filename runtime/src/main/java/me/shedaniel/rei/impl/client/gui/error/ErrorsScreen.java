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

package me.shedaniel.rei.impl.client.gui.error;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.rei.impl.client.gui.error.ErrorsEntryListWidget.TextEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Internal
public class ErrorsScreen extends Screen {
    private List<Object> components;
    private AbstractButton doneButton;
    private ErrorsEntryListWidget listWidget;
    private Screen parent;
    private boolean quitable;
    
    public ErrorsScreen(Component title, List<Object> components, Screen parent, boolean quitable) {
        super(title);
        this.components = components;
        this.parent = parent;
        this.quitable = quitable;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    @Override
    public void init() {
        addWidget(listWidget = new ErrorsEntryListWidget(minecraft, width, height, 32, height - 32));
        listWidget._clearItems();
        for (Object component : components) {
            if (component instanceof Component) {
                listWidget._addEntry(new TextEntry((Component) component, listWidget.getItemWidth()));
            } else {
                listWidget._addEntry(((Function<Integer, ErrorsEntryListWidget.Entry>) component).apply(listWidget.getItemWidth()));
            }
        }
        listWidget._addEntry(new TextEntry(Component.empty(), listWidget.getItemWidth()));
        if (quitable) {
            addRenderableWidget(doneButton = new Button(width / 2 - 100, height - 26, 200, 20, Component.translatable("gui.done"), button -> Minecraft.getInstance().setScreen(parent), Supplier::get) {});
        } else {
            addRenderableWidget(doneButton = new Button(width / 2 - 100, height - 26, 200, 20, Component.translatable("menu.quit"), button -> exit(), Supplier::get) {});
        }
    }
    
    private void exit() {
        boolean localServer = this.minecraft.isLocalServer();
        boolean connectedToRealms = this.minecraft.isConnectedToRealms();
        this.minecraft.level.disconnect();
        
        if (localServer) {
            this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        } else {
            this.minecraft.clearLevel();
        }
        
        System.exit(-1);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (listWidget.mouseScrolled(mouseX, mouseY, amount))
            return true;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);
        this.listWidget.render(matrices, mouseX, mouseY, delta);
        drawCenteredString(matrices, this.font, getTitle(), this.width / 2, 16, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }
    
}
