package me.shedaniel.reiclothconfig2.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.reiclothconfig2.api.ConfigBuilder;
import me.shedaniel.reiclothconfig2.api.ConfigCategory;
import me.shedaniel.reiclothconfig2.gui.ClothConfigScreen;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConfigBuilderImpl implements ConfigBuilder {
    
    private Runnable savingRunnable;
    private GuiScreen parent;
    private String title = "text.cloth-config.config";
    private boolean editable = true;
    private boolean tabsSmoothScroll = true;
    private boolean listSmoothScroll = true;
    private boolean doesProcessErrors = true;
    private boolean doesConfirmSave = true;
    private ResourceLocation defaultBackground = Gui.OPTIONS_BACKGROUND;
    private Consumer<GuiScreen> afterInitConsumer = screen -> {};
    private Map<String, ResourceLocation> categoryBackground = Maps.newHashMap();
    private Map<String, List<Pair<String, Object>>> dataMap = Maps.newLinkedHashMap();
    private ConfigEntryBuilderImpl entryBuilder = ConfigEntryBuilderImpl.create();
    
    @Deprecated
    public ConfigBuilderImpl() {
    
    }
    
    @Override
    public ConfigEntryBuilderImpl getEntryBuilder() {
        return entryBuilder;
    }
    
    @Override
    public ConfigBuilder setAfterInitConsumer(Consumer<GuiScreen> afterInitConsumer) {
        this.afterInitConsumer = afterInitConsumer;
        return this;
    }
    
    @Override
    public GuiScreen getParentScreen() {
        return parent;
    }
    
    @Override
    public ConfigBuilder setParentScreen(GuiScreen parent) {
        this.parent = parent;
        return this;
    }
    
    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public ConfigBuilder setTitle(String title) {
        this.title = title;
        return this;
    }
    
    @Override
    public boolean isEditable() {
        return editable;
    }
    
    @Override
    public ConfigBuilder setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }
    
    @Override
    public ConfigCategory getOrCreateCategory(String categoryKey) {
        if (dataMap.containsKey(categoryKey))
            return new ConfigCategoryImpl(identifier -> {
                categoryBackground.put(categoryKey, identifier);
            }, () -> dataMap.get(categoryKey));
        dataMap.put(categoryKey, Lists.newArrayList());
        return new ConfigCategoryImpl(identifier -> {
            categoryBackground.put(categoryKey, identifier);
        }, () -> dataMap.get(categoryKey));
    }
    
    @Override
    public ConfigBuilder removeCategory(String category) {
        dataMap.remove(category);
        return this;
    }
    
    @Override
    public ConfigBuilder removeCategoryIfExists(String category) {
        if (dataMap.containsKey(category))
            dataMap.remove(category);
        return this;
    }
    
    @Override
    public boolean hasCategory(String category) {
        return dataMap.containsKey(category);
    }
    
    @Override
    public ConfigBuilder setShouldTabsSmoothScroll(boolean shouldTabsSmoothScroll) {
        this.tabsSmoothScroll = shouldTabsSmoothScroll;
        return this;
    }
    
    @Override
    public boolean isTabsSmoothScrolling() {
        return tabsSmoothScroll;
    }
    
    @Override
    public ConfigBuilder setShouldListSmoothScroll(boolean shouldListSmoothScroll) {
        this.listSmoothScroll = shouldListSmoothScroll;
        return this;
    }
    
    @Override
    public boolean isListSmoothScrolling() {
        return listSmoothScroll;
    }
    
    @Override
    public ConfigBuilder setDoesConfirmSave(boolean confirmSave) {
        this.doesConfirmSave = confirmSave;
        return this;
    }
    
    @Override
    public boolean doesConfirmSave() {
        return doesConfirmSave;
    }
    
    @Override
    public ConfigBuilder setDoesProcessErrors(boolean processErrors) {
        this.doesProcessErrors = processErrors;
        return this;
    }
    
    @Override
    public boolean doesProcessErrors() {
        return doesProcessErrors;
    }
    
    @Override
    public ResourceLocation getDefaultBackgroundTexture() {
        return defaultBackground;
    }
    
    @Override
    public ConfigBuilder setDefaultBackgroundTexture(ResourceLocation texture) {
        this.defaultBackground = texture;
        return this;
    }
    
    @Override
    public ConfigBuilder setSavingRunnable(Runnable runnable) {
        this.savingRunnable = runnable;
        return this;
    }
    
    @Override
    public Consumer<GuiScreen> getAfterInitConsumer() {
        return afterInitConsumer;
    }
    
    @Override
    public GuiScreen build() {
        ClothConfigScreen screen = new ClothConfigScreen(parent, I18n.format(title), dataMap, doesConfirmSave, doesProcessErrors, listSmoothScroll, defaultBackground, categoryBackground) {
            @Override
            public void onSave(Map<String, List<Pair<String, Object>>> o) {
                if (savingRunnable != null)
                    savingRunnable.run();
            }
            
            @Override
            public boolean isEditable() {
                return editable;
            }
            
            @Override
            protected void initGui() {
                super.initGui();
                afterInitConsumer.accept(this);
            }
        };
        screen.setSmoothScrollingTabs(tabsSmoothScroll);
        return screen;
    }
    
    @Override
    public Runnable getSavingRunnable() {
        return savingRunnable;
    }
    
}
