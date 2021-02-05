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

package me.shedaniel.rei.api;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public interface AutoTransferHandler extends Comparable<AutoTransferHandler> {
    
    /**
     * @return the priority of this handler, higher priorities will be called first.
     */
    default double getPriority() {
        return 0d;
    }
    
    @NotNull
    Result handle(@NotNull Context context);
    
    @Override
    default int compareTo(@NotNull AutoTransferHandler o) {
        return Double.compare(getPriority(), o.getPriority());
    }
    
    @ApiStatus.NonExtendable
    interface Result {
        /**
         * Creates a successful result, no further handlers will be called.
         */
        static Result createSuccessful() {
            return new ResultImpl();
        }
        
        /**
         * Creates a passing result, further handlers will be called.
         * This will also mark the handler as not applicable.
         */
        static Result createNotApplicable() {
            return new ResultImpl(false);
        }
        
        /**
         * Creates a passing result, further handlers will be called.
         *
         * @param errorKey The error itself
         */
        static Result createFailed(String errorKey) {
            return new ResultImpl(errorKey, new IntArrayList(), 1744764928);
        }
        
        /**
         * Creates a passing result, further handlers will be called.
         * The special color will be applied if this is the last handler.
         *
         * @param errorKey The error itself
         * @param color    A special color for the button
         */
        static Result createFailedCustomButtonColor(String errorKey, int color) {
            return new ResultImpl(errorKey, new IntArrayList(), color);
        }
        
        /**
         * Creates a passing result, further handlers will be called.
         *
         * @param errorKey The error itself
         * @param redSlots A list of slots to be marked as red. Will be passed to {@link TransferDisplayCategory}.
         */
        static Result createFailed(String errorKey, IntList redSlots) {
            return new ResultImpl(errorKey, redSlots, 1744764928);
        }
        
        /**
         * Creates a passing result, further handlers will be called.
         * The special color will be applied if this is the last handler.
         *
         * @param errorKey The error itself
         * @param color    A special color for the button
         * @param redSlots A list of slots to be marked as red. Will be passed to {@link TransferDisplayCategory}.
         */
        static Result createFailedCustomButtonColor(String errorKey, IntList redSlots, int color) {
            return new ResultImpl(errorKey, redSlots, color);
        }
        
        /**
         * Forces this handler to be the last handler, no further handlers will be called.
         */
        default Result blocksFurtherHandling() {
            return blocksFurtherHandling(true);
        }
        
        /**
         * Forces this handler to be the last handler, no further handlers will be called.
         */
        Result blocksFurtherHandling(boolean returnsToScreen);
        
        /**
         * @return the color in which the button should be displayed in.
         */
        int getColor();
        
        /**
         * @return whether this handler has successfully handled the transfer.
         */
        boolean isSuccessful();
        
        /**
         * @return whether this handler should be the last handler.
         */
        boolean isBlocking();
        
        /**
         * Applicable if {@link #isSuccessful()} is true.
         *
         * @return whether to return to the previous screen rather than staying open
         */
        boolean isReturningToScreen();
        
        /**
         * @return whether the handler is applicable.
         */
        boolean isApplicable();
        
        /**
         * Applicable if {@link #isSuccessful()} is false.
         *
         * @return the error message
         */
        String getErrorKey();
        
        /**
         * @return a list of slots to be marked as red. Will be passed to {@link TransferDisplayCategory}.
         */
        IntList getIntegers();
    }
    
    @ApiStatus.NonExtendable
    interface Context {
        static Context create(boolean actuallyCrafting, AbstractContainerScreen<?> containerScreen, Display display) {
            return new ContextImpl(actuallyCrafting, containerScreen, () -> display);
        }
        
        default Minecraft getMinecraft() {
            return Minecraft.getInstance();
        }
        
        boolean isActuallyCrafting();
        
        @Nullable
        AbstractContainerScreen<?> getContainerScreen();
        
        Display getRecipe();
        
        @Nullable
        default AbstractContainerMenu getContainer() {
            return getContainerScreen() == null ? null : getContainerScreen().getMenu();
        }
    }
    
    @ApiStatus.Internal
    final class ResultImpl implements Result {
        private boolean successful, applicable, returningToScreen, blocking;
        private String errorKey;
        private IntList integers = new IntArrayList();
        private int color;
        
        private ResultImpl() {
            this(true, true);
        }
        
        public ResultImpl(boolean applicable) {
            this(false, applicable);
        }
        
        public ResultImpl(boolean successful, boolean applicable) {
            this.successful = successful;
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
        public Result blocksFurtherHandling(boolean returningToScreen) {
            this.blocking = true;
            this.returningToScreen = returningToScreen;
            return this;
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
        public boolean isBlocking() {
            return successful || blocking;
        }
        
        @Override
        public boolean isApplicable() {
            return applicable;
        }
        
        @Override
        public boolean isReturningToScreen() {
            return returningToScreen;
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
    
    @ApiStatus.Internal
    final class ContextImpl implements Context {
        private boolean actuallyCrafting;
        private AbstractContainerScreen<?> containerScreen;
        private Supplier<Display> recipeDisplaySupplier;
        
        private ContextImpl(boolean actuallyCrafting, AbstractContainerScreen<?> containerScreen, Supplier<Display> recipeDisplaySupplier) {
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
        public Display getRecipe() {
            return recipeDisplaySupplier.get();
        }
    }
    
}
