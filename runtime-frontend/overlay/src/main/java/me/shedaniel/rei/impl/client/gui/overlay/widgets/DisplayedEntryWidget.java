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

package me.shedaniel.rei.impl.client.gui.overlay.widgets;

import com.google.common.base.Suppliers;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.ItemCheatingMode;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.view.Views;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.impl.client.ClientInternals;
import me.shedaniel.rei.impl.client.provider.AutoCraftingEvaluator;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class DisplayedEntryWidget extends GuiComponent implements UnaryOperator<Tooltip>, Slot.ActionPredicate {
    public final Slot slot;
    private long lastCheckTime = -1;
    private long lastCheckedTime = -1;
    private Display display;
    private Supplier<DisplayTooltipComponent> displayTooltipComponent;
    
    public int backupY;
    
    protected DisplayedEntryWidget(Slot slot) {
        this.slot = slot;
        slot.tooltipProcessor(this);
        slot.noHighlightIfEmpty();
        slot.tooltipsEnabled(s -> !ClientHelper.getInstance().isCheating() || Minecraft.getInstance().screen instanceof DisplayScreen || Minecraft.getInstance().player.containerMenu.getCarried().isEmpty());
        slot.action(this);
        this.backupY = slot.getBounds().y;
    }
    
    @Override
    public boolean doMouse(Slot slot, double mouseX, double mouseY, int button) {
        if (ClientHelper.getInstance().isCheating() && !Screen.hasControlDown() && !(Minecraft.getInstance().screen instanceof DisplayScreen)) {
            EntryStack<?> entry = slot.getCurrentEntry().copy();
            if (!entry.isEmpty()) {
                if (entry.getType() != VanillaEntryTypes.ITEM) {
                    EntryStack<ItemStack> cheatsAs = entry.cheatsAs();
                    entry = cheatsAs.isEmpty() ? entry : cheatsAs;
                }
                if (entry.getValueType() == ItemStack.class) {
                    boolean all;
                    if (ConfigObject.getInstance().getItemCheatingMode() == ItemCheatingMode.REI_LIKE) {
                        all = button == 1 || Screen.hasShiftDown();
                    } else {
                        all = button != 1 || Screen.hasShiftDown();
                    }
                    entry.<ItemStack>castValue().setCount(!all ? 1 : entry.<ItemStack>castValue().getMaxStackSize());
                }
                return ClientHelper.getInstance().tryCheatingEntry(entry);
            }
        }
        
        if (!(Minecraft.getInstance().screen instanceof DisplayScreen) && Screen.hasControlDown()) {
            try {
                TransferHandler handler = getTransferHandler(true);
                
                if (handler != null) {
                    AbstractContainerScreen<?> containerScreen = REIRuntime.getInstance().getPreviousContainerScreen();
                    TransferHandler.Context context = TransferHandler.Context.create(true, Screen.hasShiftDown() || button == 1, containerScreen, display);
                    TransferHandler.Result transferResult = handler.handle(context);
                    
                    if (transferResult.isBlocking()) {
                        Widgets.produceClickSound();
                        if (transferResult.isReturningToScreen() && Minecraft.getInstance().screen != containerScreen) {
                            Minecraft.getInstance().setScreen(containerScreen);
                            REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
                        }
                        return true;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        
        return false;
    }
    
    @Override
    public boolean doKey(Slot slot, int keyCode, int scanCode, int modifiers) {
        if (ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen)) {
            EntryStack<?> entry = slot.getCurrentEntry().copy();
            if (!entry.isEmpty()) {
                if (entry.getType() != VanillaEntryTypes.ITEM) {
                    EntryStack<ItemStack> cheatsAs = entry.cheatsAs();
                    entry = cheatsAs.isEmpty() ? entry : cheatsAs;
                }
                if (entry.getValueType() == ItemStack.class) {
                    entry.<ItemStack>castValue().setCount(entry.<ItemStack>castValue().getMaxStackSize());
                    
                    KeyMapping[] keyHotbarSlots = Minecraft.getInstance().options.keyHotbarSlots;
                    for (int i = 0; i < keyHotbarSlots.length; i++) {
                        if (keyHotbarSlots[i].matches(keyCode, scanCode)) {
                            return ClientHelper.getInstance().tryCheatingEntryTo(entry, i);
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public Tooltip apply(Tooltip tooltip) {
        if (!(Minecraft.getInstance().screen instanceof DisplayScreen)) {
            boolean exists = getTransferHandler(false) != null;
            
            if (!exists) {
                if (lastCheckedTime == -1 || Util.getMillis() - lastCheckedTime > 400) {
                    lastCheckedTime = Util.getMillis();
                }
                
                if (Util.getMillis() - lastCheckedTime > 200) {
                    lastCheckedTime = -1;
                    exists = getTransferHandler(true) != null;
                }
            } else {
                lastCheckedTime = -1;
            }
            
            if (exists) {
                tooltip.add(new TranslatableComponent("text.auto_craft.move_items.tooltip").withStyle(ChatFormatting.YELLOW));
                tooltip.add((ClientTooltipComponent) displayTooltipComponent.get());
            }
        }
        
        return tooltip;
    }
    
    @Nullable
    private TransferHandler _getTransferHandler() {
        lastCheckTime = Util.getMillis();
        
        if (PluginManager.areAnyReloading()) {
            return null;
        }
        
        try {
            DisplayRegistry displayRegistry = DisplayRegistry.getInstance();
            CategoryRegistry categoryRegistry = CategoryRegistry.getInstance();
            Map<CategoryIdentifier<?>, Boolean> filteringQuickCraftCategories = ConfigObject.getInstance().getFilteringQuickCraftCategories();
            for (Map.Entry<CategoryIdentifier<?>, List<Display>> entry : displayRegistry.getAll().entrySet()) {
                Optional<? extends CategoryRegistry.CategoryConfiguration<?>> configuration;
                if ((configuration = categoryRegistry.tryGet(entry.getKey())).isEmpty()
                    || categoryRegistry.isCategoryInvisible(configuration.get().getCategory())) continue;
                if (!filteringQuickCraftCategories.getOrDefault(entry.getKey(), configuration.get().isQuickCraftingEnabledByDefault())) continue;
                for (Display display : entry.getValue()) {
                    if ((!ConfigObject.getInstance().shouldFilterDisplays() || displayRegistry.isDisplayVisible(display))
                        && Views.getInstance().isRecipesFor(slot.getEntries(), display)) {
                        AutoCraftingEvaluator.Result result = ClientInternals.getAutoCraftingEvaluator(display).get();
                        
                        if (result.isSuccessful()) {
                            this.display = display;
                            this.displayTooltipComponent = Suppliers.memoize(() -> new DisplayTooltipComponent(display));
                            return result.getSuccessfulHandler();
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException ignored) {
            display = null;
            displayTooltipComponent = null;
            lastCheckTime = -1;
        }
        
        return null;
    }
    
    private TransferHandler getTransferHandler(boolean query) {
        if (PluginManager.areAnyReloading()) {
            return null;
        }
        
        if (display != null) {
            if (Views.getInstance().isRecipesFor(slot.getEntries(), display)) {
                AutoCraftingEvaluator.Result result = ClientInternals.getAutoCraftingEvaluator(display).get();
                
                if (result.isSuccessful()) {
                    return result.getSuccessfulHandler();
                }
            }
            
            display = null;
            displayTooltipComponent = null;
            lastCheckTime = -1;
        }
        
        if (lastCheckTime != -1 && Util.getMillis() - lastCheckTime < 2000) {
            return null;
        }
        
        return query ? _getTransferHandler() : null;
    }
}
