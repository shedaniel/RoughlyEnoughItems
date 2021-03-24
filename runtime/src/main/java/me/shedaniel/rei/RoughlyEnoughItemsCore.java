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

package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.RecipeUpdateEvent;
import me.shedaniel.architectury.event.events.client.ClientScreenInputEvent;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIHelper;
import me.shedaniel.rei.api.client.REIOverlay;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.BuiltinEntryTypes;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.impl.client.REIHelperImpl;
import me.shedaniel.rei.impl.client.gui.ContainerScreenOverlay;
import me.shedaniel.rei.impl.client.gui.widget.InternalWidgets;
import me.shedaniel.rei.impl.client.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.*;
import me.shedaniel.rei.impl.common.category.CategoryIdentifierImpl;
import me.shedaniel.rei.impl.common.display.DisplaySerializerRegistryImpl;
import me.shedaniel.rei.impl.common.plugins.PluginManagerImpl;
import me.shedaniel.rei.impl.common.transfer.MenuInfoRegistryImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.common.entry.EntryIngredientImpl;
import me.shedaniel.rei.impl.client.favorites.FavoriteEntryTypeRegistryImpl;
import me.shedaniel.rei.impl.common.entry.comparison.ItemComparatorRegistryImpl;
import me.shedaniel.rei.impl.common.entry.comparison.NbtHasherProviderImpl;
import me.shedaniel.rei.impl.common.entry.TypedEntryStack;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.EntryTypeDeferred;
import me.shedaniel.rei.impl.common.entry.type.EntryTypeRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.types.EmptyEntryDefinition;
import me.shedaniel.rei.impl.client.entry.type.types.RenderingEntryDefinition;
import me.shedaniel.rei.impl.common.fluid.FluidSupportProviderImpl;
import me.shedaniel.rei.impl.client.registry.category.CategoryRegistryImpl;
import me.shedaniel.rei.impl.client.registry.display.DisplayRegistryImpl;
import me.shedaniel.rei.impl.client.registry.screen.ScreenRegistryImpl;
import me.shedaniel.rei.impl.client.search.SearchProviderImpl;
import me.shedaniel.rei.impl.client.subsets.SubsetsRegistryImpl;
import me.shedaniel.rei.impl.common.util.IssuesDetector;
import me.shedaniel.rei.impl.client.transfer.TransferHandlerRegistryImpl;
import me.shedaniel.rei.impl.client.view.ViewsImpl;
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
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class RoughlyEnoughItemsCore {
    @ApiStatus.Internal public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final ExecutorService SYNC_RECIPES = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "REI-SyncRecipes"));
    @ApiStatus.Experimental
    public static boolean isLeftMousePressed = false;
    
    static {
        attachCommonInternals();
        if (Platform.getEnvironment() == Env.CLIENT) {
            attachClientInternals();
        }
    }
    
    public static void attachCommonInternals() {
        CategoryIdentifierImpl.attach();
        Internals.attachInstanceSupplier(new PluginManagerImpl<>(
                REIPlugin.class,
                UnaryOperator.identity(),
                new EntryTypeRegistryImpl(),
                new ItemComparatorRegistryImpl(),
                new DisplaySerializerRegistryImpl(),
                new FluidSupportProviderImpl()), "commonPluginManager");
        Internals.attachInstanceSupplier(new PluginManagerImpl<>(
                REIServerPlugin.class,
                view -> view.then(PluginView.getInstance()),
                new MenuInfoRegistryImpl()), "serverPluginManager");
        Internals.attachInstance((Function<ResourceLocation, EntryType<?>>) new Function<ResourceLocation, EntryType<?>>() {
            ResourceLocation RENDERING_ID = new ResourceLocation("rendering");
            private Map<ResourceLocation, EntryType<?>> typeCache = new ConcurrentHashMap<>();
            private EntryType<Unit> empty;
            @Environment(EnvType.CLIENT)
            private EntryType<Renderer> render;
            
            @Override
            public EntryType<?> apply(ResourceLocation id) {
                if (id.equals(BuiltinEntryTypes.EMPTY_ID)) {
                    return typeCache.computeIfAbsent(id, this::emptyType);
                } else if (id.equals(RENDERING_ID) && Platform.getEnv() == EnvType.CLIENT) {
                    return typeCache.computeIfAbsent(id, this::renderingType);
                }
                return typeCache.computeIfAbsent(id, EntryTypeDeferred::new);
            }
            
            public EntryType<Unit> emptyType(ResourceLocation id) {
                if (empty == null) {
                    empty = new EntryType<Unit>() {
                        @Override
                        public ResourceLocation getId() {
                            return id;
                        }
                        
                        @Override
                        public EntryDefinition<Unit> getDefinition() {
                            return EmptyEntryDefinition.EMPTY;
                        }
                    };
                }
                return empty;
            }
            
            @Environment(EnvType.CLIENT)
            public EntryType<Renderer> renderingType(ResourceLocation id) {
                if (render == null) {
                    render = new EntryType<Renderer>() {
                        @Override
                        public ResourceLocation getId() {
                            return id;
                        }
                        
                        @Override
                        public EntryDefinition<Renderer> getDefinition() {
                            return RenderingEntryDefinition.RENDERING;
                        }
                    };
                }
                return render;
            }
        }, "entryTypeDeferred");
        Internals.attachInstance(new Internals.EntryStackProvider() {
            @Override
            public EntryStack<Unit> empty() {
                return TypedEntryStack.EMPTY;
            }
            
            @Override
            public <T> EntryStack<T> of(EntryDefinition<T> definition, T value) {
                if (Objects.equals(definition.getType().getId(), BuiltinEntryTypes.EMPTY_ID)) {
                    return empty().cast();
                }
                
                return new TypedEntryStack<>(definition, value);
            }
        }, Internals.EntryStackProvider.class);
        Internals.attachInstance(new NbtHasherProviderImpl(), Internals.NbtHasherProvider.class);
        Internals.attachInstance(EntryIngredientImpl.provide(), Internals.EntryIngredientProvider.class);
    }
    
    @Environment(EnvType.CLIENT)
    public static void attachClientInternals() {
        ClientInternals.attachInstanceSupplier(new PluginManagerImpl<>(
                REIClientPlugin.class,
                view -> view.then(PluginView.getInstance()),
                new ViewsImpl(),
                new SearchProviderImpl(),
                new ConfigManagerImpl(),
                new CategoryRegistryImpl(),
                new DisplayRegistryImpl(),
                new ScreenRegistryImpl(),
                new EntryRegistryImpl(),
                new FavoriteEntryTypeRegistryImpl(),
                new SubsetsRegistryImpl(),
                new TransferHandlerRegistryImpl(),
                new REIHelperImpl()), "clientPluginManager");
        InternalWidgets.attach();
        ClientInternals.attachInstance((Supplier<EntryRenderer<?>>) () -> EmptyEntryDefinition.EmptyRenderer.INSTANCE, "emptyEntryRenderer");
        ClientInternals.attachInstance((BiFunction<Supplier<FavoriteEntry>, Supplier<JsonObject>, FavoriteEntry>) (supplier, toJson) -> new FavoriteEntry() {
            FavoriteEntry value = null;
            
            @Override
            public FavoriteEntry getUnwrapped() {
                if (this.value == null) {
                    this.value = supplier.get();
                }
                return Objects.requireNonNull(value).getUnwrapped();
            }
            
            @Override
            public UUID getUuid() {
                return getUnwrapped().getUuid();
            }
            
            @Override
            public boolean isInvalid() {
                try {
                    return getUnwrapped().isInvalid();
                } catch (Exception e) {
                    return true;
                }
            }
            
            @Override
            public Renderer getRenderer(boolean showcase) {
                return getUnwrapped().getRenderer(showcase);
            }
            
            @Override
            public boolean doAction(int button) {
                return getUnwrapped().doAction(button);
            }
            
            @Override
            public Optional<Supplier<Collection<FavoriteMenuEntry>>> getMenuEntries() {
                return getUnwrapped().getMenuEntries();
            }
            
            @Override
            public int hashIgnoreAmount() {
                return getUnwrapped().hashIgnoreAmount();
            }
            
            @Override
            public FavoriteEntry copy() {
                return FavoriteEntry.delegate(supplier, toJson);
            }
            
            @Override
            public ResourceLocation getType() {
                return getUnwrapped().getType();
            }
            
            @Override
            public JsonObject toJson(JsonObject to) {
                if (toJson == null) {
                    return getUnwrapped().toJson(to);
                }
                
                JsonObject object = toJson.get();
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    to.add(entry.getKey(), entry.getValue());
                }
                return to;
            }
            
            @Override
            public boolean isSame(FavoriteEntry other) {
                return getUnwrapped().isSame(other.getUnwrapped());
            }
        }, "delegateFavoriteEntry");
        ClientInternals.attachInstance((Function<JsonObject, FavoriteEntry>) (object) -> {
            String type = GsonHelper.getAsString(object, FavoriteEntry.TYPE_KEY);
            switch (type) {
                case "stack":
                case "item":
                case "fluid":
                case "empty":
                    return FavoriteEntry.fromEntryStack(EntryStack.readFromJson(object));
                default:
                    ResourceLocation id = new ResourceLocation(type);
                    return Objects.requireNonNull(Objects.requireNonNull(FavoriteEntryType.registry().get(id)).fromJson(object));
            }
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
            CompletableFuture.runAsync(RoughlyEnoughItemsCore::_reloadPlugins, SYNC_RECIPES);
        } else {
            _reloadPlugins();
        }
    }
    
    private static void _reloadPlugins() {
        for (PluginManager<? extends REIPlugin<?>> instance : PluginManager.getActiveInstances()) {
            instance.startReload();
        }
    }
    
    public void onInitialize() {
        PluginDetector.detectCommonPlugins();
        PluginDetector.detectServerPlugins();
        RoughlyEnoughItemsNetwork.onInitialize();
    }
    
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        IssuesDetector.detect();
        registerClothEvents();
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
            if (currentScreen instanceof CraftingScreen) {
                RecipeBookComponent recipeBookGui = ((RecipeUpdateListener) currentScreen).getRecipeBookComponent();
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
                CraftingMenu container = ((CraftingScreen) currentScreen).getMenu();
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
    
    @Environment(EnvType.CLIENT)
    private void loadTestPlugins() {
        if ( System.getProperty("rei.test", "false").equals("true")) {
            PluginView.getClientInstance().registerPlugin(new REITestPlugin());
        }
    }
    
    @Environment(EnvType.CLIENT)
    private boolean shouldReturn(Screen screen) {
        if (!REIHelper.getInstance().getOverlay().isPresent()) return true;
        if (screen == null) return true;
        if (screen != Minecraft.getInstance().screen) return true;
        return _shouldReturn(screen);
    }
    
    @Environment(EnvType.CLIENT)
    private boolean _shouldReturn(Screen screen) {
        try {
            Class<? extends Screen> screenClass = screen.getClass();
            for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders()) {
                if (!decider.isHandingScreen(screen.getClass()))
                    continue;
                InteractionResult result = decider.shouldScreenBeOverlaid(screenClass);
                if (result != InteractionResult.PASS) {
                    return result == InteractionResult.FAIL || REIHelper.getInstance().getPreviousScreen() == null;
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }
        return true;
    }
    
    @Environment(EnvType.CLIENT)
    private void registerClothEvents() {
        Minecraft client = Minecraft.getInstance();
        final ResourceLocation recipeButtonTex = new ResourceLocation("textures/gui/recipe_button.png");
        MutableLong lastReload = new MutableLong(-1);
        RecipeUpdateEvent.EVENT.register(recipeManager -> reloadPlugins(lastReload));
        GuiEvent.INIT_POST.register((screen, widgets, children) -> {
            REIHelperImpl.getInstance().setPreviousScreen(screen);
            if (ConfigObject.getInstance().doesDisableRecipeBook() && screen instanceof AbstractContainerScreen) {
                widgets.removeIf(widget -> widget instanceof ImageButton && ((ImageButton) widget).resourceLocation.equals(recipeButtonTex));
            }
        });
        ClientScreenInputEvent.MOUSE_CLICKED_PRE.register((minecraftClient, screen, mouseX, mouseY, button) -> {
            isLeftMousePressed = true;
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            resetFocused(screen);
            if (REIHelper.getInstance().isOverlayVisible() && REIHelper.getInstance().getOverlay().get().mouseClicked(mouseX, mouseY, button)) {
                if (button == 0) {
                    screen.setDragging(true);
                }
                resetFocused(screen);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
        ClientScreenInputEvent.MOUSE_RELEASED_PRE.register((minecraftClient, screen, mouseX, mouseY, button) -> {
            isLeftMousePressed = false;
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            resetFocused(screen);
            if (REIHelper.getInstance().isOverlayVisible() && REIHelper.getInstance().getOverlay().get().mouseReleased(mouseX, mouseY, button)
                && resetFocused(screen)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
        ClientScreenInputEvent.MOUSE_SCROLLED_PRE.register((minecraftClient, screen, mouseX, mouseY, amount) -> {
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            resetFocused(screen);
            if (REIHelper.getInstance().isOverlayVisible() && REIHelper.getInstance().getOverlay().get().mouseScrolled(mouseX, mouseY, amount)
                && resetFocused(screen))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        });
        ClientScreenInputEvent.CHAR_TYPED_PRE.register((minecraftClient, screen, character, keyCode) -> {
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            resetFocused(screen);
            if (REIHelper.getInstance().getOverlay().get().charTyped(character, keyCode)
                && resetFocused(screen))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        });
        GuiEvent.RENDER_POST.register((screen, matrices, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen))
                return;
            resetFocused(screen);
            REIHelper.getInstance().getOverlay().get().render(matrices, mouseX, mouseY, delta);
            ((ContainerScreenOverlay) REIHelper.getInstance().getOverlay().get()).lateRender(matrices, mouseX, mouseY, delta);
            resetFocused(screen);
        });
        ClientScreenInputEvent.MOUSE_DRAGGED_PRE.register((minecraftClient, screen, mouseX1, mouseY1, button, mouseX2, mouseY2) -> {
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            resetFocused(screen);
            if (REIHelper.getInstance().getOverlay().get().mouseDragged(mouseX1, mouseY1, button, mouseX2, mouseY2)
                && resetFocused(screen))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        });
        ClientScreenInputEvent.KEY_PRESSED_PRE.register((minecraftClient, screen, i, i1, i2) -> {
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            if (screen instanceof AbstractContainerScreen && ConfigObject.getInstance().doesDisableRecipeBook() && ConfigObject.getInstance().doesFixTabCloseContainer()) {
                if (i == 258 && minecraftClient.options.keyInventory.matches(i, i1)) {
                    minecraftClient.player.closeContainer();
                    return InteractionResult.SUCCESS;
                }
            }
            if (screen.getFocused() != null && screen.getFocused() instanceof EditBox || (screen.getFocused() instanceof RecipeBookComponent && ((RecipeBookComponent) screen.getFocused()).searchBox != null && ((RecipeBookComponent) screen.getFocused()).searchBox.isFocused()))
                return InteractionResult.PASS;
            resetFocused(screen);
            if (REIHelper.getInstance().getOverlay().get().keyPressed(i, i1, i2)
                && resetFocused(screen))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        });
    }
    
    @Environment(EnvType.CLIENT)
    private boolean resetFocused(Screen screen) {
        if (screen.getFocused() instanceof REIOverlay || screen.getFocused() == screen) {
            screen.setFocused(null);
        }
        return true;
    }
}
