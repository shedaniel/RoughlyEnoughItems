/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.container.Container;

import java.util.function.Supplier;

public interface AutoTransferHandler {
    
    default double getPriority() {
        return 0d;
    }
    
    Result handle(Context context);
    
    public interface Result {
        static Result createSuccessful() {
            return new ResultImpl();
        }
        
        static Result createNotApplicable() {
            return new ResultImpl(false);
        }
        
        static Result createFailed(String errorKey) {
            return new ResultImpl(errorKey, new IntArrayList());
        }
        
        static Result createFailed(String errorKey, IntList redSlots) {
            return new ResultImpl(errorKey, redSlots);
        }
        
        boolean isSuccessful();
        
        boolean isApplicable();
        
        String getErrorKey();
    }
    
    public interface Context {
        static Context create(boolean actuallyCrafting, AbstractContainerScreen<?> containerScreen, RecipeDisplay recipeDisplay) {
            return new ContextImpl(actuallyCrafting, containerScreen, () -> recipeDisplay);
        }
        
        default MinecraftClient getMinecraft() {
            return MinecraftClient.getInstance();
        }
        
        boolean isActuallyCrafting();
        
        AbstractContainerScreen<?> getContainerScreen();
        
        RecipeDisplay getRecipe();
        
        default Container getContainer() {
            return getContainerScreen().getContainer();
        }
        
        default ContainerScreenOverlay getOverlay() {
            return ScreenHelper.getLastOverlay();
        }
    }
    
    public final class ResultImpl implements Result {
        private boolean successful, applicable;
        private String errorKey;
        private IntList integers = new IntArrayList();
        
        private ResultImpl() {
            this.successful = true;
            this.applicable = true;
        }
        
        public ResultImpl(boolean applicable) {
            this.successful = false;
            this.applicable = applicable;
        }
        
        public ResultImpl(String errorKey, IntList integers) {
            this.successful = false;
            this.applicable = true;
            this.errorKey = errorKey;
            if (integers != null)
                this.integers = integers;
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
    }
    
    public final class ContextImpl implements Context {
        boolean actuallyCrafting;
        AbstractContainerScreen<?> containerScreen;
        Supplier<RecipeDisplay> recipeDisplaySupplier;
        
        private ContextImpl(boolean actuallyCrafting, AbstractContainerScreen<?> containerScreen, Supplier<RecipeDisplay> recipeDisplaySupplier) {
            this.actuallyCrafting = actuallyCrafting;
            this.containerScreen = containerScreen;
            this.recipeDisplaySupplier = recipeDisplaySupplier;
        }
        
        @Override
        public boolean isActuallyCrafting() {
            return actuallyCrafting;
        }
        
        @Override
        public AbstractContainerScreen<?> getContainerScreen() {
            return containerScreen;
        }
        
        @Override
        public RecipeDisplay getRecipe() {
            return recipeDisplaySupplier.get();
        }
    }
    
}
