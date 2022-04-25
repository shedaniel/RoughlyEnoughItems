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

package me.shedaniel.rei.impl.client.gui.widget;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoCraftingEvaluator {
    public static class AutoCraftingResult {
        public int tint = 0;
        public boolean successful = false;
        public TransferHandler successfulHandler;
        public boolean hasApplicable = false;
        public TransferHandlerRenderer renderer;
        public BiConsumer<Point, Consumer<Tooltip>> tooltipRenderer;
    }
    
    public static AutoCraftingResult evaluateAutoCrafting(boolean actuallyCrafting, boolean stackedCrafting, Display display, Supplier<Collection<ResourceLocation>> idsSupplier) {
        AbstractContainerScreen<?> containerScreen = REIRuntime.getInstance().getPreviousContainerScreen();
        AutoCraftingResult result = new AutoCraftingResult();
        final List<Component> errorTooltip = new ArrayList<>();
        result.tooltipRenderer = (pos, sink) -> {
            List<Component> str = new ArrayList<>(errorTooltip);
            
            if (ConfigObject.getInstance().isFavoritesEnabled()) {
                str.add(Component.literal(" "));
                str.add(Component.translatable("text.rei.save.recipes", Component.literal(ConfigObject.getInstance().getFavoriteKeyCode().getLocalizedName().getString().toUpperCase(Locale.ROOT)).withStyle(ChatFormatting.BOLD)).withStyle(ChatFormatting.GRAY));
            }
            
            if (Minecraft.getInstance().options.advancedItemTooltips && idsSupplier != null) {
                Collection<ResourceLocation> locations = idsSupplier.get();
                if (!locations.isEmpty()) {
                    str.add(Component.literal(" "));
                    for (ResourceLocation location : locations) {
                        String t = I18n.get("text.rei.recipe_id", "", location.toString());
                        if (t.startsWith("\n")) {
                            t = t.substring("\n".length());
                        }
                        str.add(Component.literal(t).withStyle(ChatFormatting.GRAY));
                    }
                }
            }
            
            sink.accept(Tooltip.create(pos, str));
        };
        
        if (containerScreen == null) {
            errorTooltip.add(Component.translatable("error.rei.not.supported.move.items").withStyle(ChatFormatting.RED));
            return result;
        }
        
        List<TransferHandler.Result> errors = new ArrayList<>();
        TransferHandler.Context context = TransferHandler.Context.create(actuallyCrafting, stackedCrafting, containerScreen, display);
        
        for (TransferHandler transferHandler : TransferHandlerRegistry.getInstance()) {
            try {
                TransferHandler.Result transferResult = transferHandler.handle(context);
                
                if (transferResult.isBlocking() && actuallyCrafting) {
                    if (transferResult.isReturningToScreen()) {
                        Minecraft.getInstance().setScreen(containerScreen);
                        REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
                    }
                    
                    break;
                }
                
                if (transferResult.isApplicable()) {
                    result.hasApplicable = true;
                    result.tint = transferResult.getColor();
                    
                    TransferHandlerRenderer transferHandlerRenderer = transferResult.getRenderer(transferHandler, context);
                    if (transferHandlerRenderer != null) {
                        result.renderer = transferHandlerRenderer;
                    }
                    
                    if (transferResult.getTooltipRenderer() != null) {
                        BiConsumer<Point, TransferHandler.Result.TooltipSink> tooltipRenderer = transferResult.getTooltipRenderer();
                        result.tooltipRenderer = (point, tooltipConsumer) -> tooltipRenderer.accept(point, tooltipConsumer::accept);
                    }
                    
                    if (transferResult.isSuccessful()) {
                        errors.clear();
                        result.successful = true;
                        result.successfulHandler = transferHandler;
                        break;
                    }
                    
                    errors.add(transferResult);
                    
                    if (transferResult.isBlocking()) {
                        break;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        
        if (!result.hasApplicable) {
            errorTooltip.clear();
            errorTooltip.add(Component.translatable("error.rei.not.supported.move.items").withStyle(ChatFormatting.RED));
            return result;
        }
        
        if (errors.isEmpty()) {
            errorTooltip.clear();
            errorTooltip.add(Component.translatable("text.auto_craft.move_items"));
        } else {
            errorTooltip.clear();
            List<Component> tooltipsFilled = new ArrayList<>();
            for (TransferHandler.Result error : errors) {
                error.fillTooltip(tooltipsFilled);
            }
            
            if (errors.size() == 1) {
                for (Component tooltipFilled : tooltipsFilled) {
                    MutableComponent colored = tooltipFilled.copy().withStyle(ChatFormatting.RED);
                    if (!CollectionUtils.anyMatch(errorTooltip, ss -> ss.getString().equalsIgnoreCase(tooltipFilled.getString()))) {
                        errorTooltip.add(colored);
                    }
                }
            } else {
                errorTooltip.add(Component.translatable("error.rei.multi.errors").withStyle(ChatFormatting.RED));
                for (Component tooltipFilled : tooltipsFilled) {
                    MutableComponent colored = Component.literal("- ").withStyle(ChatFormatting.RED)
                            .append(tooltipFilled.copy().withStyle(ChatFormatting.RED));
                    if (!CollectionUtils.anyMatch(errorTooltip, ss -> ss.getString().equalsIgnoreCase(colored.getString()))) {
                        errorTooltip.add(colored);
                    }
                }
            }
        }
        
        return result;
    }
}
