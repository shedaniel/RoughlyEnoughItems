/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
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
