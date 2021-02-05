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

package me.shedaniel.rei.api.gui.config.entry;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import me.shedaniel.rei.gui.PreRecipeViewingScreen;
import me.shedaniel.rei.api.gui.config.RecipeScreenType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RecipeScreenTypeEntry extends TooltipListEntry<RecipeScreenType> {
    private int width;
    private final RecipeScreenType original;
    private RecipeScreenType type;
    private RecipeScreenType defaultValue;
    private Consumer<RecipeScreenType> save;
    private final AbstractWidget buttonWidget = new AbstractButton(0, 0, 0, 20, NarratorChatListener.NO_TITLE) {
        @Override
        public void onPress() {
            Minecraft.getInstance().setScreen(new PreRecipeViewingScreen(getConfigScreen(), type, false, original -> {
                Minecraft.getInstance().setScreen(getConfigScreen());
                type = original ? RecipeScreenType.ORIGINAL : RecipeScreenType.VILLAGER;
            }));
        }
        
        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            setMessage(new TranslatableComponent("config.roughlyenoughitems.recipeScreenType.config", type.toString()));
            super.render(matrices, mouseX, mouseY, delta);
        }
    };
    private final List<GuiEventListener> children = ImmutableList.of(buttonWidget);
    
    @SuppressWarnings("deprecation")
    public RecipeScreenTypeEntry(int width, Component fieldName, RecipeScreenType type, RecipeScreenType defaultValue, Consumer<RecipeScreenType> save) {
        super(fieldName, null);
        this.original = type;
        this.width = width;
        this.type = type;
        this.defaultValue = defaultValue;
        this.save = save;
    }
    
    @Override
    public boolean isEdited() {
        return super.isEdited() || getValue() != original;
    }
    
    @Override
    public RecipeScreenType getValue() {
        return type;
    }
    
    @Override
    public Optional<RecipeScreenType> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }
    
    @Override
    public void save() {
        save.accept(type);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
    
    @Override
    public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        Window window = Minecraft.getInstance().getWindow();
        this.buttonWidget.active = this.isEditable();
        this.buttonWidget.y = y;
        this.buttonWidget.x = x + entryWidth / 2 - width / 2;
        this.buttonWidget.setWidth(width);
        this.buttonWidget.render(matrices, mouseX, mouseY, delta);
    }
}
