/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.config.entry;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.rei.api.EntryStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.resource.language.I18n;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@ApiStatus.Internal
public class NoFilteringEntry extends AbstractConfigListEntry<List<EntryStack>> {
    private Consumer<List<EntryStack>> saveConsumer;
    private List<EntryStack> defaultValue;
    private List<EntryStack> configFiltered;
    
    public NoFilteringEntry(List<EntryStack> configFiltered, List<EntryStack> defaultValue, Consumer<List<EntryStack>> saveConsumer) {
        super("", false);
        this.configFiltered = configFiltered;
        this.defaultValue = defaultValue;
        this.saveConsumer = saveConsumer;
    }
    
    @Override
    public List<EntryStack> getValue() {
        return configFiltered;
    }
    
    @Override
    public Optional<List<EntryStack>> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }
    
    @Override
    public void save() {
        saveConsumer.accept(getValue());
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        ClothConfigScreen.ListWidget parent = getParent();
        drawCenteredString(MinecraftClient.getInstance().textRenderer, I18n.translate("config.roughlyenoughitems.filteredEntries.loadWorldFirst"), (parent.right - parent.left) / 2 + parent.left, (parent.bottom - parent.top) / 2 + parent.top - 5, -1);
    }
    
    @Override
    public boolean mouseScrolled(double d, double e, double amount) {
        return true;
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
}
