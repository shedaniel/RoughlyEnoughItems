/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.container.Container;

import java.util.function.Supplier;

public interface AutoTransferHandler {
    
    /**
     * @return the priority of this handler, higher priorities will be called first.
     */
    default double getPriority() {
        return 0d;
    }
    
    Result handle(Context context);
    
    interface Result {
        static Result createSuccessful() {
            return new ResultImpl();
        }
        
        static Result createNotApplicable() {
            return new ResultImpl(false);
        }
        
        static Result createFailed(String errorKey) {
            return new ResultImpl(errorKey, new IntArrayList(), 1744764928);
        }
        
        static Result createFailedCustomButtonColor(String errorKey, int color) {
            return new ResultImpl(errorKey, new IntArrayList(), color);
        }
        
        static Result createFailed(String errorKey, IntList redSlots) {
            return new ResultImpl(errorKey, redSlots, 1744764928);
        }
        
        static Result createFailedCustomButtonColor(String errorKey, IntList redSlots, int color) {
            return new ResultImpl(errorKey, redSlots, color);
        }
        
        int getColor();
        
        boolean isSuccessful();
        
        boolean isApplicable();
        
        String getErrorKey();
        
        IntList getIntegers();
    }
    
    interface Context {
        static Context create(boolean actuallyCrafting, ContainerScreen<?> containerScreen, RecipeDisplay recipeDisplay) {
            return new ContextImpl(actuallyCrafting, containerScreen, () -> recipeDisplay);
        }
        
        default MinecraftClient getMinecraft() {
            return MinecraftClient.getInstance();
        }
        
        boolean isActuallyCrafting();
        
        ContainerScreen<?> getContainerScreen();
        
        RecipeDisplay getRecipe();
        
        default Container getContainer() {
            return getContainerScreen().getContainer();
        }
        
        default ContainerScreenOverlay getOverlay() {
            return ScreenHelper.getLastOverlay();
        }
    }
    
    final class ResultImpl implements Result {
        private boolean successful, applicable;
        private String errorKey;
        private IntList integers = new IntArrayList();
        private int color;
        
        private ResultImpl() {
            this.successful = true;
            this.applicable = true;
        }
        
        public ResultImpl(boolean applicable) {
            this.successful = false;
            this.applicable = applicable;
        }
        
        public ResultImpl(String errorKey, IntList integers, int color) {
            this.successful = false;
            this.applicable = true;
            this.errorKey = errorKey;
            if (integers != null)
                this.integers = integers;
            this.color = color;
        }
        
        @Override
        public int getColor() {
            return color;
        }
        
        @Override
        public boolean isSuccessful() {
            return successful;
        }
        
        @Override
        public boolean isApplicable() {
            return applicable;
        }
        
        @Override
        public String getErrorKey() {
            return errorKey;
        }
        
        @Override
        public IntList getIntegers() {
            return integers;
        }
    }
    
    final class ContextImpl implements Context {
        boolean actuallyCrafting;
        ContainerScreen<?> containerScreen;
        Supplier<RecipeDisplay> recipeDisplaySupplier;
        
        private ContextImpl(boolean actuallyCrafting, ContainerScreen<?> containerScreen, Supplier<RecipeDisplay> recipeDisplaySupplier) {
            this.actuallyCrafting = actuallyCrafting;
            this.containerScreen = containerScreen;
            this.recipeDisplaySupplier = recipeDisplaySupplier;
        }
        
        @Override
        public boolean isActuallyCrafting() {
            return actuallyCrafting;
        }
        
        @Override
        public ContainerScreen<?> getContainerScreen() {
            return containerScreen;
        }
        
        @Override
        public RecipeDisplay getRecipe() {
            return recipeDisplaySupplier.get();
        }
    }
    
}
