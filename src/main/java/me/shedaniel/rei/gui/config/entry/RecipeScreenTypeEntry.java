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
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import me.shedaniel.rei.gui.PreRecipeViewingScreen;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RecipeScreenTypeEntry extends TooltipListEntry<RecipeScreenType> {
    private int width;
    private RecipeScreenType type;
    private RecipeScreenType defaultValue;
    private Consumer<RecipeScreenType> save;
    private AbstractButtonWidget buttonWidget = new AbstractPressableButtonWidget(0, 0, 0, 20, "") {
        @Override
        public void onPress() {
            MinecraftClient.getInstance().openScreen(new PreRecipeViewingScreen(getScreen(), type, false, original -> {
                MinecraftClient.getInstance().openScreen(getScreen());
                type = original ? RecipeScreenType.ORIGINAL : RecipeScreenType.VILLAGER;
                getScreen().setEdited(true, isRequiresRestart());
            }));
        }
        
        @Override
        public void render(int mouseX, int mouseY, float delta) {
            setMessage(I18n.translate("config.roughlyenoughitems.recipeScreenType.config", type.toString()));
            super.render(mouseX, mouseY, delta);
        }
    };
    private List<Element> children = ImmutableList.of(buttonWidget);
    
    @SuppressWarnings("deprecation")
    public RecipeScreenTypeEntry(int width, String fieldName, RecipeScreenType type, RecipeScreenType defaultValue, Consumer<RecipeScreenType> save) {
        super(fieldName, null);
        this.width = width;
        this.type = type;
        this.defaultValue = defaultValue;
        this.save = save;
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
    public List<? extends Element> children() {
        return children;
    }
    
    @Override
    public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        Window window = MinecraftClient.getInstance().getWindow();
        this.buttonWidget.active = this.isEditable();
        this.buttonWidget.y = y;
        this.buttonWidget.x = x + entryWidth / 2 - width / 2;
        this.buttonWidget.setWidth(width);
        this.buttonWidget.render(mouseX, mouseY, delta);
    }
}
