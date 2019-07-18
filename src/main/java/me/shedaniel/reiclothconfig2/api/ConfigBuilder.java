package me.shedaniel.reiclothconfig2.api;

import me.shedaniel.reiclothconfig2.impl.ConfigBuilderImpl;
import me.shedaniel.reiclothconfig2.impl.ConfigEntryBuilderImpl;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public interface ConfigBuilder {
    
    @SuppressWarnings("deprecation")
    public static ConfigBuilder create() {
        return new ConfigBuilderImpl();
    }
    
    /**
     * @deprecated Use {@link ConfigBuilder#create()}
     */
    @Deprecated
    public static ConfigBuilder create(GuiScreen parent, String title) {
        return create().setParentScreen(parent).setTitle(title);
    }
    
    GuiScreen getParentScreen();
    
    ConfigBuilder setParentScreen(GuiScreen parent);
    
    String getTitle();
    
    ConfigBuilder setTitle(String title);
    
    boolean isEditable();
    
    ConfigBuilder setEditable(boolean editable);
    
    ConfigCategory getOrCreateCategory(String categoryKey);
    
    ConfigBuilder removeCategory(String categoryKey);
    
    ConfigBuilder removeCategoryIfExists(String categoryKey);
    
    boolean hasCategory(String category);
    
    ConfigBuilder setShouldTabsSmoothScroll(boolean shouldTabsSmoothScroll);
    
    boolean isTabsSmoothScrolling();
    
    ConfigBuilder setShouldListSmoothScroll(boolean shouldListSmoothScroll);
    
    boolean isListSmoothScrolling();
    
    ConfigBuilder setDoesConfirmSave(boolean confirmSave);
    
    boolean doesConfirmSave();
    
    ConfigBuilder setDoesProcessErrors(boolean processErrors);
    
    boolean doesProcessErrors();
    
    ResourceLocation getDefaultBackgroundTexture();
    
    ConfigBuilder setDefaultBackgroundTexture(ResourceLocation texture);
    
    Runnable getSavingRunnable();
    
    ConfigBuilder setSavingRunnable(Runnable runnable);
    
    Consumer<GuiScreen> getAfterInitConsumer();
    
    ConfigBuilder setAfterInitConsumer(Consumer<GuiScreen> afterInitConsumer);
    
    ConfigEntryBuilderImpl getEntryBuilder();
    
    GuiScreen build();
    
}
