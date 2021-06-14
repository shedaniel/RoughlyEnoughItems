/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientRecipeUpdateEvent;
import dev.architectury.event.events.client.ClientScreenInputEvent;
import dev.architectury.networking.NetworkManager;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.favorites.DelegatingFavoriteEntryProviderImpl;
import me.shedaniel.rei.impl.client.favorites.FavoriteEntryTypeRegistryImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.InternalWidgets;
import me.shedaniel.rei.impl.client.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.client.registry.category.CategoryRegistryImpl;
import me.shedaniel.rei.impl.client.registry.display.DisplayRegistryImpl;
import me.shedaniel.rei.impl.client.registry.screen.ScreenRegistryImpl;
import me.shedaniel.rei.impl.client.search.SearchProviderImpl;
import me.shedaniel.rei.impl.client.subsets.SubsetsRegistryImpl;
import me.shedaniel.rei.impl.client.transfer.TransferHandlerRegistryImpl;
import me.shedaniel.rei.impl.client.view.ViewsImpl;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.types.EmptyEntryDefinition;
import me.shedaniel.rei.impl.common.plugins.PluginManagerImpl;
import me.shedaniel.rei.impl.common.util.IssuesDetector;
import me.shedaniel.rei.plugin.test.REITestPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class RoughlyEnoughItemsCoreClient {
    @ApiStatus.Experimental
    public static boolean isLeftMousePressed = false;
    private static final ExecutorService RELOAD_PLUGINS = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "REI-ReloadPlugins");
        thread.setDaemon(true);
        return thread;
    });
    
    public static void attachClientInternals() {
        InternalWidgets.attach();
        EmptyEntryDefinition.EmptyRenderer emptyEntryRenderer = new EmptyEntryDefinition.EmptyRenderer();
        ClientInternals.attachInstance((Supplier<EntryRenderer<?>>) () -> emptyEntryRenderer, "emptyEntryRenderer");
        ClientInternals.attachInstance((BiFunction<Supplier<FavoriteEntry>, Supplier<CompoundTag>, FavoriteEntry>) DelegatingFavoriteEntryProviderImpl::new, "delegateFavoriteEntry");
        ClientInternals.attachInstance((Function<CompoundTag, FavoriteEntry>) (object) -> {
            String type = object.getString(FavoriteEntry.TYPE_KEY);
            ResourceLocation id = new ResourceLocation(type);
            return Objects.requireNonNull(Objects.requireNonNull(FavoriteEntryType.registry().get(id)).read(object));
        }, "favoriteEntryFromJson");
        ClientInternals.attachInstance((BiFunction<@Nullable Point, Collection<Component>, Tooltip>) QueuedTooltip::create, "tooltipProvider");
        ClientInternals.attachInstance((Function<@Nullable Boolean, ClickArea.Result>) successful -> new ClickArea.Result() {
            private List<CategoryIdentifier<?>> categories = Lists.newArrayList();
            
            @Override
            public ClickArea.Result category(CategoryIdentifier<?> category) {
                this.categories.add(category);
                return this;
            }
            
            @Override
            public boolean isSuccessful() {
                return successful;
            }
            
            @Override
            public Stream<CategoryIdentifier<?>> getCategories() {
                return categories.stream();
            }
        }, "clickAreaHandlerResult");
        ClientInternals.attachInstanceSupplier(new PluginManagerImpl<>(
                REIClientPlugin.class,
                view -> view.then(PluginView.getInstance()),
                usedTime -> {
                    RoughlyEnoughItemsCore.LOGGER.info("Reloaded Plugin Manager [%s] with %d entries, %d displays, %d display visibility predicates, %d categories (%s), %d exclusion zones and %d overlay deciders in %dms.",
                            REIClientPlugin.class.getSimpleName(),
                            EntryRegistry.getInstance().size(),
                            DisplayRegistry.getInstance().displaySize(),
                            DisplayRegistry.getInstance().getVisibilityPredicates().size(),
                            CategoryRegistry.getInstance().size(),
                            CategoryRegistry.getInstance().stream()
                                    .map(CategoryRegistry.CategoryConfiguration::getCategory)
                                    .map(DisplayCategory::getTitle)
                                    .map(Component::getString).collect(Collectors.joining(", ")),
                            ScreenRegistry.getInstance().exclusionZones().getZonesCount(),
                            ScreenRegistry.getInstance().getDeciders().size(),
                            usedTime
                    );
                },
                new ViewsImpl(),
                new SearchProviderImpl(),
                new ConfigManagerImpl(),
                new EntryRegistryImpl(),
                new CategoryRegistryImpl(),
                new DisplayRegistryImpl(),
                new ScreenRegistryImpl(),
                new FavoriteEntryTypeRegistryImpl(),
                new SubsetsRegistryImpl(),
                new TransferHandlerRegistryImpl(),
                new REIRuntimeImpl()), "clientPluginManager");
    }
    
    public void onInitializeClient() {
        IssuesDetector.detect();
        registerEvents();
        PluginDetector.detectClientPlugins();
        loadTestPlugins();
        
        Minecraft client = Minecraft.getInstance();
        NetworkManager.registerReceiver(NetworkManager.s2c(), RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, (buf, context) -> {
            ItemStack stack = buf.readItem();
            String player = buf.readUtf(32767);
            if (client.player != null) {
                client.player.displayClientMessage(new TextComponent(I18n.get("text.rei.cheat_items").replaceAll("\\{item_name}", EntryStacks.of(stack.copy()).asFormattedText().getString()).replaceAll("\\{item_count}", stack.copy().getCount() + "").replaceAll("\\{player_name}", player)), false);
            }
        });
        NetworkManager.registerReceiver(NetworkManager.s2c(), RoughlyEnoughItemsNetwork.NOT_ENOUGH_ITEMS_PACKET, (buf, context) -> {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof CraftingScreen craftingScreen) {
                RecipeBookComponent recipeBookGui = craftingScreen.getRecipeBookComponent();
                GhostRecipe ghostSlots = recipeBookGui.ghostRecipe;
                ghostSlots.clear();
                
                List<List<ItemStack>> input = Lists.newArrayList();
                int mapSize = buf.readInt();
                for (int i = 0; i < mapSize; i++) {
                    List<ItemStack> list = Lists.newArrayList();
                    int count = buf.readInt();
                    for (int j = 0; j < count; j++) {
                        list.add(buf.readItem());
                    }
                    input.add(list);
                }
                
                ghostSlots.addIngredient(Ingredient.of(Items.STONE), 381203812, 12738291);
                CraftingMenu container = craftingScreen.getMenu();
                for (int i = 0; i < input.size(); i++) {
                    List<ItemStack> stacks = input.get(i);
                    if (!stacks.isEmpty()) {
                        Slot slot = container.getSlot(i + container.getResultSlotIndex() + 1);
                        ghostSlots.addIngredient(Ingredient.of(stacks.toArray(new ItemStack[0])), slot.x, slot.y);
                    }
                }
            }
        });
    }
    
    private void loadTestPlugins() {
        if (System.getProperty("rei.test", "false").equals("true")) {
            PluginView.getClientInstance().registerPlugin(new REITestPlugin());
        }
    }
    
    public static boolean shouldReturn(Screen screen) {
        if (REIRuntime.getInstance().getOverlay().isEmpty()) return true;
        if (screen == null) return true;
        if (screen != Minecraft.getInstance().screen) return true;
        return _shouldReturn(screen);
    }
    
    private static ScreenOverlay getOverlay() {
        return REIRuntime.getInstance().getOverlay().orElseThrow(() -> new IllegalStateException("Overlay not initialized!"));
    }
    
    private static boolean _shouldReturn(Screen screen) {
        try {
            Class<? extends Screen> screenClass = screen.getClass();
            for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders()) {
                if (!decider.isHandingScreen(screen.getClass()))
                    continue;
                InteractionResult result = decider.shouldScreenBeOverlaid(screenClass);
                if (result != InteractionResult.PASS) {
                    return result == InteractionResult.FAIL || REIRuntime.getInstance().getPreviousScreen() == null;
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }
        return true;
    }
    
    private void registerEvents() {
        Minecraft client = Minecraft.getInstance();
        final ResourceLocation recipeButtonTex = new ResourceLocation("textures/gui/recipe_button.png");
        MutableLong lastReload = new MutableLong(-1);
        ClientRecipeUpdateEvent.EVENT.register(recipeManager -> reloadPlugins(lastReload));
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            REIRuntimeImpl.getInstance().setPreviousScreen(screen);
            if (ConfigObject.getInstance().doesDisableRecipeBook() && screen instanceof AbstractContainerScreen) {
                access.getRenderables().removeIf(widget -> widget instanceof ImageButton button && button.resourceLocation.equals(recipeButtonTex));
                access.getNarratables().removeIf(widget -> widget instanceof ImageButton button && button.resourceLocation.equals(recipeButtonTex));
                screen.children().removeIf(widget -> widget instanceof ImageButton button && button.resourceLocation.equals(recipeButtonTex));
            }
        });
        ClientScreenInputEvent.MOUSE_CLICKED_PRE.register((minecraftClient, screen, mouseX, mouseY, button) -> {
            isLeftMousePressed = true;
            if (shouldReturn(screen))
                return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().mouseClicked(mouseX, mouseY, button)) {
                if (button == 0) {
                    screen.setDragging(true);
                }
                resetFocused(screen);
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
        ClientScreenInputEvent.MOUSE_RELEASED_PRE.register((minecraftClient, screen, mouseX, mouseY, button) -> {
            isLeftMousePressed = false;
            if (shouldReturn(screen))
                return EventResult.pass();
            resetFocused(screen);
            if (REIRuntime.getInstance().isOverlayVisible() && getOverlay().mouseReleased(mouseX, mouseY, button)
                && resetFocused(screen)) {
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
        ClientScreenInputEvent.MOUSE_SCROLLED_PRE.register((minecraftClient, screen, mouseX, mouseY, amount) -> {
            if (shouldReturn(screen))
                return EventResult.pass();
            resetFocused(screen);
            if (REIRuntime.getInstance().isOverlayVisible() && getOverlay().mouseScrolled(mouseX, mouseY, amount)
                && resetFocused(screen))
                return EventResult.interruptTrue();
            return EventResult.pass();
        });
        ClientScreenInputEvent.CHAR_TYPED_PRE.register((minecraftClient, screen, character, keyCode) -> {
            if (shouldReturn(screen))
                return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().charTyped(character, keyCode)
                && resetFocused(screen))
                return EventResult.interruptTrue();
            return EventResult.pass();
        });
        ClientGuiEvent.RENDER_POST.register((screen, matrices, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen))
                return;
            resetFocused(screen);
            getOverlay().render(matrices, mouseX, mouseY, delta);
            ((ScreenOverlayImpl) getOverlay()).lateRender(matrices, mouseX, mouseY, delta);
            resetFocused(screen);
        });
        ClientScreenInputEvent.MOUSE_DRAGGED_PRE.register((minecraftClient, screen, mouseX1, mouseY1, button, mouseX2, mouseY2) -> {
            if (shouldReturn(screen))
                return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().mouseDragged(mouseX1, mouseY1, button, mouseX2, mouseY2)
                && resetFocused(screen))
                return EventResult.interruptTrue();
            return EventResult.pass();
        });
        ClientScreenInputEvent.KEY_PRESSED_PRE.register((minecraftClient, screen, i, i1, i2) -> {
            if (shouldReturn(screen))
                return EventResult.pass();
            if (screen instanceof AbstractContainerScreen && ConfigObject.getInstance().doesDisableRecipeBook() && ConfigObject.getInstance().doesFixTabCloseContainer()) {
                if (i == 258 && minecraftClient.options.keyInventory.matches(i, i1)) {
                    minecraftClient.player.closeContainer();
                    return EventResult.interruptTrue();
                }
            }
            if (screen.getFocused() != null && screen.getFocused() instanceof EditBox || (screen.getFocused() instanceof RecipeBookComponent && ((RecipeBookComponent) screen.getFocused()).searchBox != null && ((RecipeBookComponent) screen.getFocused()).searchBox.isFocused()))
                return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().keyPressed(i, i1, i2)
                && resetFocused(screen))
                return EventResult.interruptTrue();
            return EventResult.pass();
        });
    }
    
    private boolean resetFocused(Screen screen) {
        if (screen.getFocused() instanceof ScreenOverlay || screen.getFocused() == screen) {
            screen.setFocused(null);
        }
        return true;
    }
    
    @ApiStatus.Internal
    public static void reloadPlugins(MutableLong lastReload) {
        if (lastReload != null) {
            if (lastReload.getValue() > 0 && System.currentTimeMillis() - lastReload.getValue() <= 5000) {
                RoughlyEnoughItemsCore.LOGGER.warn("Suppressing Reload Plugins!");
                return;
            }
            lastReload.setValue(System.currentTimeMillis());
        }
        if (ConfigObject.getInstance().doesRegisterRecipesInAnotherThread()) {
            CompletableFuture.runAsync(RoughlyEnoughItemsCore::_reloadPlugins, RELOAD_PLUGINS);
        } else {
            RoughlyEnoughItemsCore._reloadPlugins();
        }
    }
}
