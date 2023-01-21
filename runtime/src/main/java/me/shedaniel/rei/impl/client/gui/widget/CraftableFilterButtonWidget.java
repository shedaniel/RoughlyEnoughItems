/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

import dev.architectury.utils.value.BooleanValue;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.modules.MenuAccess;
import me.shedaniel.rei.impl.client.gui.modules.entries.SeparatorMenuEntry;
import me.shedaniel.rei.impl.client.gui.modules.entries.SubMenuEntry;
import me.shedaniel.rei.impl.client.gui.modules.entries.ToggleMenuEntry;
import me.shedaniel.rei.impl.client.gui.screen.ConfigReloadingScreen;
import me.shedaniel.rei.impl.client.search.method.DefaultInputMethod;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector4f;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CraftableFilterButtonWidget {
    public static final UUID FILTER_MENU_UUID = UUID.fromString("2839e998-1679-4f9e-a257-37411d16f1e6");
    
    public static Widget create(ScreenOverlayImpl overlay) {
        Rectangle bounds = getCraftableFilterBounds();
        MenuAccess access = overlay.menuAccess();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack icon = new ItemStack(Blocks.CRAFTING_TABLE);
        Button filterButton = Widgets.createButton(bounds, Component.empty())
                .focusable(false)
                .onClick(button -> {
                    ConfigManager.getInstance().toggleCraftableOnly();
                    ScreenOverlayImpl.getEntryListWidget().updateSearch(REIRuntimeImpl.getSearchField().getText(), true);
                })
                .onRender((matrices, button) -> {
                    button.setTint(ConfigManager.getInstance().isCraftableOnlyEnabled() ? 0x3800d907 : 0x38ff0000);
                    
                    access.openOrClose(FILTER_MENU_UUID, button.getBounds(), () -> menuEntries(access));
                })
                .containsMousePredicate((button, point) -> button.getBounds().contains(point) && overlay.isNotInExclusionZones(point.x, point.y))
                .tooltipLineSupplier(button -> Component.translatable(ConfigManager.getInstance().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all"));
        Widget overlayWidget = Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Vector4f vector = new Vector4f(bounds.x + 2, bounds.y + 2, helper.getBlitOffset() - 10, 1.0F);
            matrices.last().pose().transform(vector);
            itemRenderer.blitOffset = vector.z();
            itemRenderer.renderGuiItem(icon, (int) vector.x(), (int) vector.y());
            itemRenderer.blitOffset = 0.0F;
        });
        return Widgets.concat(filterButton, overlayWidget);
    }
    
    private static Collection<FavoriteMenuEntry> menuEntries(MenuAccess access) {
        ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
        ArrayList<FavoriteMenuEntry> entries = new ArrayList<>(List.of(
                new SubMenuEntry(Component.translatable("text.rei.config.menu.search_field.position"), Arrays.stream(SearchFieldLocation.values())
                        .<FavoriteMenuEntry>map(location -> ToggleMenuEntry.of(Component.literal(location.toString()),
                                        () -> config.getSearchFieldLocation() == location,
                                        bool -> config.setSearchFieldLocation(location))
                                .withActive(() -> config.getSearchFieldLocation() != location)
                        )
                        .toList())
        ));
        
        List<Map.Entry<ResourceLocation, InputMethod<?>>> applicableInputMethods = getApplicableInputMethods();
        if (applicableInputMethods.size() > 1) {
            entries.add(new SubMenuEntry(Component.translatable("text.rei.config.menu.search_field.input_method"), createInputMethodEntries(access, applicableInputMethods)));
        }
        
        return entries;
    }
    
    public static List<Map.Entry<ResourceLocation, InputMethod<?>>> getApplicableInputMethods() {
        String languageCode = Minecraft.getInstance().options.languageCode;
        return InputMethodRegistry.getInstance().getAll().entrySet().stream()
                .filter(entry -> CollectionUtils.anyMatch(entry.getValue().getMatchingLocales(), locale -> locale.code().equals(languageCode)))
                .toList();
    }
    
    public static List<FavoriteMenuEntry> createInputMethodEntries(MenuAccess access, List<Map.Entry<ResourceLocation, InputMethod<?>>> applicableInputMethods) {
        ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
        List<FavoriteMenuEntry> entries = applicableInputMethods.stream()
                .<FavoriteMenuEntry>map(pair -> ToggleMenuEntry.of(pair.getValue().getName(),
                                () -> Objects.equals(config.getInputMethodId(), pair.getKey()),
                                bool -> {
                                    ExecutorService service = Executors.newSingleThreadExecutor();
                                    InputMethod<?> active = InputMethod.active();
                                    active.dispose(service).whenComplete((unused, throwable) -> {
                                        if (throwable != null) {
                                            InternalLogger.getInstance().error("Failed to dispose input method", throwable);
                                        }
                                        
                                        ConfigManagerImpl.getInstance().getConfig().setInputMethodId(new ResourceLocation("rei:default"));
                                    }).join();
                                    double[] progress = {0};
                                    CompletableFuture<Void> future = pair.getValue().prepare(service, p -> progress[0] = Mth.clamp(p, 0, 1)).whenComplete((unused, throwable) -> {
                                        if (throwable != null) {
                                            InternalLogger.getInstance().error("Failed to prepare input method", throwable);
                                            ConfigManagerImpl.getInstance().getConfig().setInputMethodId(new ResourceLocation("rei:default"));
                                        } else {
                                            ConfigManagerImpl.getInstance().getConfig().setInputMethodId(pair.getKey());
                                        }
                                    });
                                    Screen screen = Minecraft.getInstance().screen;
                                    ConfigReloadingScreen reloadingScreen = new ConfigReloadingScreen(Component.translatable("text.rei.input.methods.initializing"),
                                            () -> !future.isDone(), () -> {
                                        Minecraft.getInstance().setScreen(screen);
                                    });
                                    reloadingScreen.setSubtitle(() -> Component.translatable("text.rei.input.methods.reload.progress", String.format("%.2f", progress[0] * 100)));
                                    Minecraft.getInstance().setScreen(reloadingScreen);
                                    access.close();
                                    future.whenComplete((unused, throwable) -> {
                                        service.shutdown();
                                    });
                                    ScreenOverlayImpl.getInstance().getHintsContainer().addHint(12, () -> new Point(getCraftableFilterBounds().getCenterX(), getCraftableFilterBounds().getCenterY()),
                                            "text.rei.hint.input.methods", List.of(Component.translatable("text.rei.hint.input.methods")));
                                })
                        .withActive(() -> !Objects.equals(config.getInputMethodId(), pair.getKey()))
                        .withTooltip(() -> Tooltip.create(Widget.mouse(), pair.getValue().getDescription()))
                )
                .collect(Collectors.toList());
        InputMethod<?> active = InputMethod.active();
        if (!(active instanceof DefaultInputMethod)) {
            entries.add(0, new SeparatorMenuEntry());
            entries.add(0, FavoriteMenuEntry.createToggle(Component.translatable("text.rei.input.methods.tooltip.hints"), new BooleanValue() {
                @Override
                public void accept(boolean t) {
                    ConfigManagerImpl.getInstance().getConfig().setDoDisplayIMEHints(!getAsBoolean());
                }
                
                @Override
                public boolean getAsBoolean() {
                    return ConfigObject.getInstance().doDisplayIMEHints();
                }
            }));
        }
        List<FavoriteMenuEntry> optionsMenuEntries = active.getOptionsMenuEntries();
        if (!optionsMenuEntries.isEmpty()) {
            entries.add(new SeparatorMenuEntry());
            entries.addAll(optionsMenuEntries);
        }
        return entries;
    }
    
    private static Rectangle getCraftableFilterBounds() {
        Rectangle area = REIRuntimeImpl.getSearchField().getBounds().clone();
        area.setLocation(area.x + area.width + 4, area.y - 1);
        area.setSize(20, 20);
        return area;
    }
}
