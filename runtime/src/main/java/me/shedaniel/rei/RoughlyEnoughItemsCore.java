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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.api.Executor;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.*;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.api.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.api.plugins.REIPlugin;
import me.shedaniel.rei.api.registry.EntryRegistry;
import me.shedaniel.rei.api.registry.screens.OverlayDecider;
import me.shedaniel.rei.api.registry.screens.ScreenRegistry;
import me.shedaniel.rei.api.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.gui.DrawableConsumer;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.gui.widgets.*;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.api.gui.widgets.Widget;
import me.shedaniel.rei.impl.*;
import me.shedaniel.rei.impl.entry.EmptyEntryDefinition;
import me.shedaniel.rei.impl.subsets.SubsetsRegistryImpl;
import me.shedaniel.rei.impl.widgets.*;
import me.shedaniel.rei.tests.plugin.REITestPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
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
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static me.shedaniel.rei.impl.Internals.attachInstance;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class RoughlyEnoughItemsCore implements ClientModInitializer {
    @ApiStatus.Internal public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final List<REIPlugin> PLUGINS = new ArrayList<>();
    private static final ExecutorService SYNC_RECIPES = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "REI-SyncRecipes"));
    @ApiStatus.Experimental
    public static boolean isLeftModePressed = false;
    
    static {
        attachInstance(EntryTypeRegistryImpl.getInstance(), EntryTypeRegistry.class);
        Map<ResourceLocation, EntryType<?>> typeCache = new ConcurrentHashMap<>();
        attachInstance((Function<ResourceLocation, EntryType<?>>) id -> {
            if (id.equals(BuiltinEntryTypes.EMPTY_ID)) {
                return BuiltinEntryTypes.EMPTY;
            } else if (id.equals(BuiltinEntryTypes.RENDERING_ID)) {
                return BuiltinEntryTypes.RENDERING;
            }
            return typeCache.computeIfAbsent(id, EntryTypeDeferred::new);
        }, "entryTypeDeferred");
        attachInstance(new PluginManager(), DisplayRegistry.class);
        attachInstance(new EntryRegistryImpl(), EntryRegistry.class);
        attachInstance(new ScreenRegistryImpl(), ScreenRegistry.class);
        attachInstance(new FluidSupportProviderImpl(), FluidSupportProvider.class);
        attachInstance(new SubsetsRegistryImpl(), SubsetsRegistry.class);
        attachInstance(new Internals.EntryStackProvider() {
            @Override
            public EntryStack<Unit> empty() {
                return TypedEntryStack.EMPTY;
            }
            
            @Override
            public <T> EntryStack<T> of(EntryDefinition<T> definition, T value) {
                return new TypedEntryStack<>(definition, value);
            }
            
            @Override
            public EntryType<Unit> emptyType(ResourceLocation id) {
                return new EntryType<Unit>() {
                    @Override
                    public @NotNull ResourceLocation getId() {
                        return id;
                    }
    
                    @SuppressWarnings("rawtypes")
                    @Override
                    public @NotNull EntryDefinition<Unit> getDefinition() {
                        return (EntryDefinition) EmptyEntryDefinition.EMPTY;
                    }
                };
            }
            
            @Override
            public EntryType<Renderer> renderingType(ResourceLocation id) {
                return new EntryType<Renderer>() {
                    @Override
                    public @NotNull ResourceLocation getId() {
                        return id;
                    }
    
                    @SuppressWarnings("rawtypes")
                    @Override
                    public @NotNull EntryDefinition<Renderer> getDefinition() {
                        return (EntryDefinition) EmptyEntryDefinition.RENDERING;
                    }
                };
            }
        }, Internals.EntryStackProvider.class);
        attachInstance(new Internals.WidgetsProvider() {
            @Override
            public boolean isRenderingPanel(Panel panel) {
                return PanelWidget.isRendering(panel);
            }
            
            @Override
            public Widget createDrawableWidget(DrawableConsumer drawable) {
                return new DrawableWidget(drawable);
            }
            
            @Override
            public me.shedaniel.rei.api.gui.widgets.Slot createSlot(Point point) {
                return new EntryWidget(point);
            }
            
            @Override
            public Button createButton(Rectangle bounds, Component text) {
                return new ButtonWidget(bounds, text);
            }
            
            @Override
            public Panel createPanelWidget(Rectangle bounds) {
                return new PanelWidget(bounds);
            }
            
            @Override
            public Label createLabel(Point point, FormattedText text) {
                return new LabelWidget(point, text);
            }
            
            @Override
            public Arrow createArrow(Rectangle rectangle) {
                return new ArrowWidget(rectangle);
            }
            
            @Override
            public BurningFire createBurningFire(Rectangle rectangle) {
                return new BurningFireWidget(rectangle);
            }
            
            @Override
            public DrawableConsumer createTexturedConsumer(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
                return new TexturedDrawableConsumer(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
            }
            
            @Override
            public DrawableConsumer createFillRectangleConsumer(Rectangle rectangle, int color) {
                return new FillRectangleDrawableConsumer(rectangle, color);
            }
        }, Internals.WidgetsProvider.class);
        attachInstance((Supplier<FavoriteEntryType.Registry>) FavoriteEntryTypeRegistryImpl::getInstance, "favoriteEntryTypeRegistry");
        attachInstance((BiFunction<Supplier<FavoriteEntry>, Supplier<JsonObject>, FavoriteEntry>) (supplier, toJson) -> new FavoriteEntry() {
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
            public @NotNull Optional<Supplier<Collection<@NotNull FavoriteMenuEntry>>> getMenuEntries() {
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
            public @NotNull JsonObject toJson(@NotNull JsonObject to) {
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
        attachInstance((Function<JsonObject, FavoriteEntry>) (object) -> {
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
        attachInstance((BiFunction<@Nullable Point, Collection<Component>, Tooltip>) QueuedTooltip::create, "tooltipProvider");
        attachInstance((Function<@Nullable Boolean, ClickAreaHandler.Result>) successful -> new ClickAreaHandler.Result() {
            private List<ResourceLocation> categories = Lists.newArrayList();
            
            @Override
            public ClickAreaHandler.Result category(ResourceLocation category) {
                this.categories.add(category);
                return this;
            }
            
            @Override
            public boolean isSuccessful() {
                return successful;
            }
            
            @Override
            public Stream<ResourceLocation> getCategories() {
                return categories.stream();
            }
        }, "clickAreaHandlerResult");
    }
    
    /**
     * Registers a REI plugin
     *
     * @param plugin the plugin instance
     * @return the plugin itself
     */
    @ApiStatus.Internal
    public static <T extends REIPlugin> T registerPlugin(T plugin) {
        PLUGINS.add(plugin);
        RoughlyEnoughItemsCore.LOGGER.debug("Registered plugin %s", plugin.getPluginName());
        return plugin;
    }
    
    public static List<REIPlugin> getPlugins() {
        return Collections.unmodifiableList(PLUGINS);
    }
    
    public static boolean hasPermissionToUsePackets() {
        try {
            Minecraft.getInstance().getConnection().getSuggestionsProvider().hasPermission(0);
            return hasOperatorPermission() && canUsePackets();
        } catch (NullPointerException e) {
            return true;
        }
    }
    
    public static boolean hasOperatorPermission() {
        try {
            return Minecraft.getInstance().getConnection().getSuggestionsProvider().hasPermission(1);
        } catch (NullPointerException e) {
            return true;
        }
    }
    
    public static boolean canUsePackets() {
        return ClientPlayNetworking.canSend(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET) && ClientPlayNetworking.canSend(RoughlyEnoughItemsNetwork.CREATE_ITEMS_GRAB_PACKET) && ClientPlayNetworking.canSend(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET);
    }
    
    @ApiStatus.Internal
    public static void syncRecipes(long[] lastSync) {
        if (lastSync != null) {
            if (lastSync[0] > 0 && System.currentTimeMillis() - lastSync[0] <= 5000) {
                RoughlyEnoughItemsCore.LOGGER.warn("Suppressing Sync Recipes!");
                return;
            }
            lastSync[0] = System.currentTimeMillis();
        }
        RecipeManager recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
        if (ConfigObject.getInstance().doesRegisterRecipesInAnotherThread()) {
            CompletableFuture.runAsync(() -> ((PluginManager) DisplayRegistry.getInstance()).tryRecipesLoaded(recipeManager), SYNC_RECIPES);
        } else {
            ((PluginManager) DisplayRegistry.getInstance()).tryRecipesLoaded(recipeManager);
        }
    }
    
    @ApiStatus.Internal
    public static boolean isDebugModeEnabled() {
        return System.getProperty("rei.test", "false").equals("true");
    }
    
    public static boolean canDeleteItems() {
        return hasPermissionToUsePackets() || Minecraft.getInstance().gameMode.hasInfiniteItems();
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeClient() {
        attachInstance(new ConfigManagerImpl(), ConfigManager.class);
        
        IssuesDetector.detect();
        registerClothEvents();
        discoverPluginEntries();
        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            if (modContainer.getMetadata().containsCustomElement("roughlyenoughitems:plugins"))
                RoughlyEnoughItemsCore.LOGGER.error("REI plugin from " + modContainer.getMetadata().getId() + " is not loaded because it is too old!");
        }
        
        RoughlyEnoughItemsState.checkRequiredFabricModules();
        Executor.run(() -> () -> {
            ClientPlayNetworking.registerGlobalReceiver(RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, (client, handler, buf, responseSender) -> {
                ItemStack stack = buf.readItem();
                String player = buf.readUtf(32767);
                if (client.player != null) {
                    client.player.displayClientMessage(new TextComponent(I18n.get("text.rei.cheat_items").replaceAll("\\{item_name}", EntryStacks.of(stack.copy()).asFormattedText().getString()).replaceAll("\\{item_count}", stack.copy().getCount() + "").replaceAll("\\{player_name}", player)), false);
                }
            });
            ClientPlayNetworking.registerGlobalReceiver(RoughlyEnoughItemsNetwork.NOT_ENOUGH_ITEMS_PACKET, (client, handler, buf, responseSender) -> {
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
        });
    }
    
    private void discoverPluginEntries() {
        for (REIPlugin plugin : Iterables.concat(
                FabricLoader.getInstance().getEntrypoints("rei_plugins", REIPlugin.class),
                FabricLoader.getInstance().getEntrypoints("rei", REIPlugin.class)
        )) {
            try {
                registerPlugin(plugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("Can't load REI plugins from %s: %s", plugin.getClass(), e.getLocalizedMessage());
            }
        }
        for (REIPlugin reiPlugin : FabricLoader.getInstance().getEntrypoints("rei_plugins_v0", REIPlugin.class)) {
            try {
                registerPlugin(reiPlugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("Can't load REI plugins from %s: %s", reiPlugin.getClass(), e.getLocalizedMessage());
            }
        }
        
        // Test Only
        loadTestPlugins();
    }
    
    private void loadTestPlugins() {
        if (isDebugModeEnabled()) {
            registerPlugin(new REITestPlugin());
        }
        if (FabricLoader.getInstance().isModLoaded("libblockattributes-fluids")) {
            try {
                registerPlugin((REIPluginEntry) Class.forName("me.shedaniel.rei.compat.LBASupportPlugin").getConstructor().newInstance());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
    
    private boolean shouldReturn(Screen screen) {
        if (screen == null) return true;
        if (screen != Minecraft.getInstance().screen) return true;
        return shouldReturn(screen.getClass());
    }
    
    private boolean shouldReturn(Class<?> screen) {
        try {
            for (OverlayDecider decider : ScreenRegistry.getInstance().getAllOverlayDeciders()) {
                if (!decider.isHandingScreen(screen))
                    continue;
                InteractionResult result = decider.shouldScreenBeOverlaid(screen);
                if (result != InteractionResult.PASS)
                    return result == InteractionResult.FAIL || REIHelper.getInstance().getPreviousContainerScreen() == null;
            }
        } catch (ConcurrentModificationException ignored) {
        }
        return true;
    }
    
    private void registerClothEvents() {
        final ResourceLocation recipeButtonTex = new ResourceLocation("textures/gui/recipe_button.png");
        long[] lastSync = {-1};
        ClothClientHooks.SYNC_RECIPES.register((minecraftClient, recipeManager, synchronizeRecipesS2CPacket) -> syncRecipes(lastSync));
        ClothClientHooks.SCREEN_ADD_BUTTON.register((minecraftClient, screen, abstractButtonWidget) -> {
            if (ConfigObject.getInstance().doesDisableRecipeBook() && screen instanceof AbstractContainerScreen && abstractButtonWidget instanceof ImageButton)
                if (((ImageButton) abstractButtonWidget).resourceLocation.equals(recipeButtonTex))
                    return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });
        ClothClientHooks.SCREEN_INIT_POST.register((minecraftClient, screen, screenHooks) -> {
            if (shouldReturn(screen))
                return;
            if (screen instanceof InventoryScreen && minecraftClient.gameMode.hasInfiniteItems())
                return;
            if (screen instanceof AbstractContainerScreen)
                ScreenHelper.setPreviousContainerScreen((AbstractContainerScreen<?>) screen);
            boolean alreadyAdded = false;
            for (GuiEventListener element : Lists.newArrayList(screenHooks.cloth$getChildren()))
                if (ContainerScreenOverlay.class.isAssignableFrom(element.getClass()))
                    if (alreadyAdded)
                        screenHooks.cloth$getChildren().remove(element);
                    else
                        alreadyAdded = true;
            if (!alreadyAdded)
                screenHooks.cloth$getChildren().add(ScreenHelper.getLastOverlay(true, false));
        });
        ClothClientHooks.SCREEN_RENDER_POST.register((matrices, minecraftClient, screen, i, i1, v) -> {
            if (shouldReturn(screen))
                return;
            ScreenHelper.getLastOverlay().render(matrices, i, i1, v);
        });
        ClothClientHooks.SCREEN_MOUSE_DRAGGED.register((minecraftClient, screen, v, v1, i, v2, v3) -> {
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseDragged(v, v1, i, v2, v3))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        });
        ClothClientHooks.SCREEN_MOUSE_CLICKED.register((minecraftClient, screen, v, v1, i) -> {
            isLeftModePressed = true;
            if (ScreenHelper.getOptionalOverlay().isPresent())
                if (screen instanceof CreativeModeInventoryScreen)
                    if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseClicked(v, v1, i)) {
                        screen.setFocused(ScreenHelper.getLastOverlay());
                        if (i == 0)
                            screen.setDragging(true);
                        return InteractionResult.SUCCESS;
                    }
            return InteractionResult.PASS;
        });
        ClothClientHooks.SCREEN_MOUSE_RELEASED.register((minecraftClient, screen, v, v1, i) -> {
            isLeftModePressed = false;
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            if (ScreenHelper.getOptionalOverlay().isPresent())
                if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseReleased(v, v1, i)) {
                    return InteractionResult.SUCCESS;
                }
            return InteractionResult.PASS;
        });
        ClothClientHooks.SCREEN_MOUSE_SCROLLED.register((minecraftClient, screen, v, v1, v2) -> {
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseScrolled(v, v1, v2))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        });
        ClothClientHooks.SCREEN_CHAR_TYPED.register((minecraftClient, screen, character, keyCode) -> {
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            if (ScreenHelper.getLastOverlay().charTyped(character, keyCode))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        });
        ClothClientHooks.SCREEN_LATE_RENDER.register((matrices, minecraftClient, screen, i, i1, v) -> {
            if (shouldReturn(screen))
                return;
            if (!ScreenHelper.isOverlayVisible())
                return;
            ScreenHelper.getLastOverlay().lateRender(matrices, i, i1, v);
        });
        ClothClientHooks.SCREEN_KEY_PRESSED.register((minecraftClient, screen, i, i1, i2) -> {
            if (shouldReturn(screen))
                return InteractionResult.PASS;
            if (screen instanceof AbstractContainerScreen && ConfigObject.getInstance().doesDisableRecipeBook() && ConfigObject.getInstance().doesFixTabCloseContainer())
                if (i == 258 && minecraftClient.options.keyInventory.matches(i, i1)) {
                    minecraftClient.player.closeContainer();
                    return InteractionResult.SUCCESS;
                }
            if (screen.getFocused() != null && screen.getFocused() instanceof EditBox || (screen.getFocused() instanceof RecipeBookComponent && ((RecipeBookComponent) screen.getFocused()).searchBox != null && ((RecipeBookComponent) screen.getFocused()).searchBox.isFocused()))
                return InteractionResult.PASS;
            if (ScreenHelper.getLastOverlay().keyPressed(i, i1, i2))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        });
    }
    
}
