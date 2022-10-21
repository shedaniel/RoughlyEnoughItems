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

package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.mojang.serialization.DataResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientScreenInputEvent;
import dev.architectury.platform.Platform;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import me.shedaniel.rei.impl.client.ClientInternals;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.InternalCursorState;
import me.shedaniel.rei.impl.client.gui.widget.CatchingExceptionUtils;
import me.shedaniel.rei.impl.client.gui.widget.TooltipContextImpl;
import me.shedaniel.rei.impl.client.gui.widget.TooltipImpl;
import me.shedaniel.rei.impl.common.util.IssuesDetector;
import me.shedaniel.rei.impl.init.PluginDetector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class RoughlyEnoughItemsCoreClient {
    public static void attachClientInternals() {
        CatchingExceptionUtils.attach();
        ClientInternals.attachInstance((Function<CompoundTag, DataResult<FavoriteEntry>>) (object) -> {
            String type = object.getString(FavoriteEntry.TYPE_KEY);
            ResourceLocation id = new ResourceLocation(type);
            FavoriteEntryType<FavoriteEntry> entryType = FavoriteEntryType.registry().get(id);
            if (entryType == null) return DataResult.error("Unknown favorite type: " + id + ", json: " + object);
            return entryType.read(object);
        }, "favoriteEntryFromJson");
        ClientInternals.attachInstance((BiFunction<@Nullable Point, Collection<Tooltip.Entry>, Tooltip>) TooltipImpl::impl, "tooltipProvider");
        ClientInternals.attachInstance((TriFunction<Point, @Nullable TooltipFlag, Boolean, TooltipContext>) TooltipContextImpl::new, "tooltipContextProvider");
        ClientInternals.attachInstance((Function<Object, Tooltip.Entry>) TooltipImpl.TooltipEntryImpl::new, "tooltipEntryProvider");
        ClientInternals.attachInstance((Function<Boolean, ClickArea.Result>) successful -> new ClickArea.Result() {
            private final List<CategoryIdentifier<?>> categories = Lists.newArrayList();
            private BooleanSupplier execute = () -> false;
            private Supplier<Component @Nullable []> tooltip = () -> {
                if (!categories.isEmpty()) {
                    Component collect = CollectionUtils.mapAndJoinToComponent(categories,
                            identifier -> CategoryRegistry.getInstance().tryGet(identifier)
                                    .map(config -> config.getCategory().getTitle())
                                    .orElse(new ImmutableTextComponent(identifier.toString())), new ImmutableTextComponent(", "));
                    return new Component[]{new TranslatableComponent("text.rei.view_recipes_for", collect)};
                }
                
                return null;
            };
            
            @Override
            public ClickArea.Result executor(BooleanSupplier task) {
                this.execute = task;
                return this;
            }
            
            @Override
            public ClickArea.Result category(CategoryIdentifier<?> category) {
                this.categories.add(category);
                return this;
            }
            
            @Override
            public ClickArea.Result tooltip(Supplier<Component @Nullable []> tooltip) {
                this.tooltip = tooltip;
                return this;
            }
            
            @Override
            public boolean isSuccessful() {
                return successful;
            }
            
            @Override
            public boolean execute() {
                return this.execute.getAsBoolean();
            }
            
            @Override
            public Component @Nullable [] getTooltips() {
                return tooltip.get();
            }
            
            @Override
            public Stream<CategoryIdentifier<?>> getCategories() {
                return categories.stream();
            }
        }, "clickAreaHandlerResult");
    }
    
    public void onInitializeClient() {
        IssuesDetector.detect();
        registerEvents();
        for (PluginDetector detector : RoughlyEnoughItemsCore.PLUGIN_DETECTORS) {
            detector.detectClientPlugins().get().run();
        }
        
        Platform.getMod("roughlyenoughitems").registerConfigurationScreen(ConfigManager.getInstance()::getConfigScreen);
    }
    
    public static boolean shouldReturn(Screen screen) {
        return !ScreenRegistry.getInstance().shouldDisplay(screen);
    }
    
    private static ScreenOverlay getOverlay() {
        return REIRuntime.getInstance().getOverlay().orElseThrow();
    }
    
    private void registerEvents() {
        Minecraft client = Minecraft.getInstance();
        final ResourceLocation recipeButtonTex = new ResourceLocation("textures/gui/recipe_button.png");
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            REIRuntime.getInstance().getOverlay(false, true);
            if (Minecraft.getInstance().screen == screen) {
                if (REIRuntime.getInstance().getPreviousScreen() != screen) {
                    TextField searchField = REIRuntime.getInstance().getSearchTextField();
                    
                    if (searchField != null) {
                        searchField.setFocused(false);
                    }
                }
                
                REIRuntimeImpl.getInstance().setPreviousScreen(screen);
            }
            if (ConfigObject.getInstance().doesDisableRecipeBook() && screen instanceof AbstractContainerScreen) {
                access.getRenderables().removeIf(widget -> widget instanceof ImageButton button && button.resourceLocation.equals(recipeButtonTex));
                access.getNarratables().removeIf(widget -> widget instanceof ImageButton button && button.resourceLocation.equals(recipeButtonTex));
                screen.children().removeIf(widget -> widget instanceof ImageButton button && button.resourceLocation.equals(recipeButtonTex));
            }
        });
        ClientScreenInputEvent.MOUSE_CLICKED_PRE.register((minecraftClient, screen, mouseX, mouseY, button) -> {
            if (button == 0) InternalCursorState.isLeftMousePressed = true;
            if (button == 1) InternalCursorState.isRightMousePressed = true;
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().mouseClicked(mouseX, mouseY, button)) {
                if (button == 0) {
                    screen.setDragging(true);
                }
                resetFocused(screen);
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
        ClientScreenInputEvent.MOUSE_RELEASED_PRE.register((minecraftClient, screen, mouseX, mouseY, button) -> {
            if (button == 0) InternalCursorState.isLeftMousePressed = false;
            if (button == 1) InternalCursorState.isRightMousePressed = false;
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            resetFocused(screen);
            if (REIRuntime.getInstance().isOverlayVisible() && getOverlay().mouseReleased(mouseX, mouseY, button)
                && resetFocused(screen)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
        ClientScreenInputEvent.MOUSE_SCROLLED_PRE.register((minecraftClient, screen, mouseX, mouseY, amount) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            resetFocused(screen);
            if (REIRuntime.getInstance().isOverlayVisible() && getOverlay().mouseScrolled(mouseX, mouseY, amount)
                && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientScreenInputEvent.CHAR_TYPED_PRE.register((minecraftClient, screen, character, keyCode) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            if (screen.getFocused() != null && screen.getFocused() instanceof EditBox || (screen.getFocused() instanceof RecipeBookComponent && ((RecipeBookComponent) screen.getFocused()).searchBox != null && ((RecipeBookComponent) screen.getFocused()).searchBox.isFocused()))
                if (REIRuntime.getInstance().getSearchTextField() != null && !REIRuntime.getInstance().getSearchTextField().isFocused())
                    return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().charTyped(character, keyCode)
                && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientGuiEvent.RENDER_POST.register((screen, matrices, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen))
                return;
            resetFocused(screen);
            if (!(screen instanceof DisplayScreen)) {
                getOverlay().render(matrices, mouseX, mouseY, delta);
            }
            getOverlay().lateRender(matrices, mouseX, mouseY, delta);
            resetFocused(screen);
        });
        ClientScreenInputEvent.MOUSE_DRAGGED_PRE.register((minecraftClient, screen, mouseX1, mouseY1, button, mouseX2, mouseY2) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().mouseDragged(mouseX1, mouseY1, button, mouseX2, mouseY2)
                && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientScreenInputEvent.KEY_PRESSED_PRE.register((minecraftClient, screen, i, i1, i2) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            if (screen instanceof AbstractContainerScreen && ConfigObject.getInstance().doesDisableRecipeBook() && ConfigObject.getInstance().doesFixTabCloseContainer()) {
                if (i == 258 && minecraftClient.options.keyInventory.matches(i, i1)) {
                    minecraftClient.player.closeContainer();
                    return EventResult.interruptFalse();
                }
            }
            if (screen.getFocused() != null && screen.getFocused() instanceof EditBox || (screen.getFocused() instanceof RecipeBookComponent && ((RecipeBookComponent) screen.getFocused()).searchBox != null && ((RecipeBookComponent) screen.getFocused()).searchBox.isFocused()))
                if (REIRuntime.getInstance().getSearchTextField() != null && !REIRuntime.getInstance().getSearchTextField().isFocused())
                    return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().keyPressed(i, i1, i2)
                && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientScreenInputEvent.KEY_RELEASED_PRE.register((minecraftClient, screen, i, i1, i2) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            if (screen.getFocused() != null && screen.getFocused() instanceof EditBox || (screen.getFocused() instanceof RecipeBookComponent && ((RecipeBookComponent) screen.getFocused()).searchBox != null && ((RecipeBookComponent) screen.getFocused()).searchBox.isFocused()))
                if (REIRuntime.getInstance().getSearchTextField() != null && !REIRuntime.getInstance().getSearchTextField().isFocused())
                    return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().keyReleased(i, i1, i2)
                && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
    }
    
    private boolean resetFocused(Screen screen) {
        if (screen.getFocused() instanceof ScreenOverlay || screen.getFocused() == screen) {
            screen.setFocused(null);
        }
        return true;
    }
}
