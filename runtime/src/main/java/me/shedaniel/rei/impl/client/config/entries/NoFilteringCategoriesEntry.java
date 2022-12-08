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

package me.shedaniel.rei.impl.client.config.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public class NoFilteringCategoriesEntry extends AbstractConfigListEntry<Map<CategoryIdentifier<?>, Boolean>> {
    private Consumer<Map<CategoryIdentifier<?>, Boolean>> saveConsumer;
    private Map<CategoryIdentifier<?>, Boolean> defaultValue;
    private Map<CategoryIdentifier<?>, Boolean> configFiltered;
    private final AbstractWidget buttonWidget = new Button(0, 0, 150, 20, Component.translatable("config.roughlyenoughitems.filteredEntries.loadWorldFirst"), button -> {}, Supplier::get) {};
    private final List<AbstractWidget> children = ImmutableList.of(buttonWidget);
    
    public NoFilteringCategoriesEntry(Component fieldName, Map<CategoryIdentifier<?>, Boolean> configFiltered, Map<CategoryIdentifier<?>, Boolean> defaultValue, Consumer<Map<CategoryIdentifier<?>, Boolean>> saveConsumer) {
        super(fieldName, false);
        this.configFiltered = configFiltered;
        this.defaultValue = defaultValue;
        this.saveConsumer = saveConsumer;
    }
    
    @Override
    public Map<CategoryIdentifier<?>, Boolean> getValue() {
        return configFiltered;
    }
    
    @Override
    public Optional<Map<CategoryIdentifier<?>, Boolean>> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }
    
    @Override
    public void save() {
        saveConsumer.accept(getValue());
    }
    
    @Override
    public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        Window window = Minecraft.getInstance().getWindow();
        this.buttonWidget.active = false;
        this.buttonWidget.setY(y);
        
        Component displayedFieldName = this.getDisplayedFieldName();
        if (Minecraft.getInstance().font.isBidirectional()) {
            Minecraft.getInstance().font.drawShadow(matrices, displayedFieldName.getVisualOrderText(), (float) (window.getGuiScaledWidth() - x - Minecraft.getInstance().font.width(displayedFieldName)), (float) (y + 6), 16777215);
            this.buttonWidget.setX(x + 2);
        } else {
            Minecraft.getInstance().font.drawShadow(matrices, displayedFieldName.getVisualOrderText(), (float) x, (float) (y + 6), this.getPreferredTextColor());
            this.buttonWidget.setX(x + entryWidth - 150);
        }
        
        this.buttonWidget.render(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
    
    @Override
    public List<? extends NarratableEntry> narratables() {
        return children;
    }
}
