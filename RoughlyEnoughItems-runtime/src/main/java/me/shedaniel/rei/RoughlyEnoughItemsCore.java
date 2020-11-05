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
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.clothconfig2.forge.api.LazyResettable;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.api.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.plugins.REIPlugin;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.api.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.widgets.*;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.*;
import me.shedaniel.rei.impl.subsets.SubsetsRegistryImpl;
import me.shedaniel.rei.impl.widgets.*;
import me.shedaniel.rei.tests.plugin.REITestPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CraftingScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static me.shedaniel.rei.impl.Internals.attachInstance;

@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public class RoughlyEnoughItemsCore {
    
    @ApiStatus.Internal public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final Map<ResourceLocation, REIPluginEntry> plugins = Maps.newHashMap();
    private static final ExecutorService SYNC_RECIPES = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "REI-SyncRecipes"));
    @ApiStatus.Experimental
    public static boolean isLeftModePressed = false;
    
    static {
        new ClientHelperImpl();
        attachInstance(new RecipeHelperImpl(), RecipeHelper.class);
        attachInstance(new EntryRegistryImpl(), EntryRegistry.class);
        attachInstance(new DisplayHelperImpl(), DisplayHelper.class);
        attachInstance(new FluidSupportProviderImpl(), FluidSupportProvider.class);
        attachInstance(new SubsetsRegistryImpl(), SubsetsRegistry.class);
        attachInstance(new Internals.EntryStackProvider() {
            @Override
            public EntryStack empty() {
                return EmptyEntryStack.EMPTY;
            }
            
            @Override
            public EntryStack fluid(FluidStack stack) {
                return new FluidEntryStack(stack);
            }
            
            @Override
            public EntryStack item(ItemStack stack) {
                return new ItemEntryStack(stack);
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
            public me.shedaniel.rei.api.widgets.Slot createSlot(Point point) {
                return EntryWidget.create(point);
            }
            
            @Override
            public Button createButton(Rectangle bounds, ITextComponent text) {
                return new ButtonWidget(bounds, text);
            }
            
            @Override
            public Panel createPanelWidget(Rectangle bounds) {
                return new PanelWidget(bounds);
            }
            
            @Override
            public Label createLabel(Point point, ITextProperties text) {
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
            LazyResettable<FavoriteEntry> value = new LazyResettable<>(supplier);
            
            @Override
            public FavoriteEntry getUnwrapped() {
                FavoriteEntry entry = value.get();
                if (entry == null) {
                    value.reset();
                }
                return Objects.requireNonNull(entry).getUnwrapped();
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
            public EntryStack getWidget(boolean showcase) {
                return getUnwrapped().getWidget(showcase);
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
            String type = JSONUtils.getAsString(object, FavoriteEntry.TYPE_KEY);
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
        attachInstance((BiFunction<@Nullable Point, Collection<ITextComponent>, Tooltip>) QueuedTooltip::create, "tooltipProvider");
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
    public static REIPluginEntry registerPlugin(REIPluginEntry plugin) {
        plugins.put(plugin.getPluginIdentifier(), plugin);
        RoughlyEnoughItemsCore.LOGGER.debug("Registered plugin %s from %s", plugin.getPluginIdentifier().toString(), plugin.getClass().getSimpleName());
        return plugin;
    }
    
    public static List<REIPluginEntry> getPlugins() {
        return new LinkedList<>(plugins.values());
    }
    
    public static Optional<ResourceLocation> getPluginIdentifier(REIPluginEntry plugin) {
        for (ResourceLocation identifier : plugins.keySet())
            if (identifier != null && plugins.get(identifier).equals(plugin))
                return Optional.of(identifier);
        return Optional.empty();
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
        return NetworkingManager.canServerReceive(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET) && NetworkingManager.canServerReceive(RoughlyEnoughItemsNetwork.CREATE_ITEMS_GRAB_PACKET) && NetworkingManager.canServerReceive(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET);
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
            CompletableFuture.runAsync(() -> ((RecipeHelperImpl) RecipeHelper.getInstance()).tryRecipesLoaded(recipeManager), SYNC_RECIPES);
        } else {
            ((RecipeHelperImpl) RecipeHelper.getInstance()).tryRecipesLoaded(recipeManager);
        }
    }
    
    @ApiStatus.Internal
    public static boolean isDebugModeEnabled() {
        return System.getProperty("rei.test", "false").equals("true");
    }
    
    public static boolean canDeleteItems() {
        return hasPermissionToUsePackets() || Minecraft.getInstance().gameMode.hasInfiniteItems();
    }
    
    public RoughlyEnoughItemsCore() {
        new ScreenHelper();
        attachInstance(new ConfigManagerImpl(), ConfigManager.class);
        
        discoverPluginEntries();
        registerEvents();
        
        NetworkingManager.registerS2CHandler(RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, (packetContext, packetByteBuf) -> {
            ItemStack stack = packetByteBuf.readItem();
            String player = packetByteBuf.readUtf(32767);
            Minecraft.getInstance().player.displayClientMessage(new StringTextComponent(I18n.get("text.rei.cheat_items").replaceAll("\\{item_name}", EntryStack.create(stack.copy()).asFormattedText().getString()).replaceAll("\\{item_count}", stack.copy().getCount() + "").replaceAll("\\{player_name}", player)), false);
        });
        NetworkingManager.registerS2CHandler(RoughlyEnoughItemsNetwork.NOT_ENOUGH_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof CraftingScreen) {
                RecipeBookGui recipeBookGui = ((IRecipeShownListener) currentScreen).getRecipeBookComponent();
                GhostRecipe ghostSlots = recipeBookGui.ghostRecipe;
                ghostSlots.clear();
                
                List<List<ItemStack>> input = Lists.newArrayList();
                int mapSize = packetByteBuf.readInt();
                for (int i = 0; i < mapSize; i++) {
                    List<ItemStack> list = Lists.newArrayList();
                    int count = packetByteBuf.readInt();
                    for (int j = 0; j < count; j++) {
                        list.add(packetByteBuf.readItem());
                    }
                    input.add(list);
                }
                
                ghostSlots.addIngredient(Ingredient.of(Items.STONE), 381203812, 12738291);
                WorkbenchContainer container = ((CraftingScreen) currentScreen).getMenu();
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
    
    private void discoverPluginEntries() {
        RoughlyEnoughItemsInit.<REIPluginEntry>scanAnnotation(Type.getType(REIPlugin.class), plugin -> {
            try {
                if (!REIPluginV0.class.isAssignableFrom(plugin.getClass()))
                    throw new IllegalArgumentException("REI plugin is too old!");
                registerPlugin(plugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("Can't load REI plugins from %s: %s", plugin.getClass(), e.getLocalizedMessage());
            }
        });
        
        // Test Only
        loadTestPlugins();
    }
    
    private void loadTestPlugins() {
        if (isDebugModeEnabled()) {
            registerPlugin(new REITestPlugin());
        }
    }
    
    private boolean shouldReturn(Screen screen) {
        if (screen == null) return true;
        if (screen != Minecraft.getInstance().screen) return true;
        return shouldReturn(screen.getClass());
    }
    
    private boolean shouldReturn(Class<?> screen) {
        try {
            for (OverlayDecider decider : DisplayHelper.getInstance().getAllOverlayDeciders()) {
                if (!decider.isHandingScreen(screen))
                    continue;
                ActionResultType result = decider.shouldScreenBeOverlayed(screen);
                if (result != ActionResultType.PASS)
                    return result == ActionResultType.FAIL || REIHelper.getInstance().getPreviousContainerScreen() == null;
            }
        } catch (ConcurrentModificationException ignored) {
        }
        return true;
    }
    
    private void registerEvents() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        final ResourceLocation recipeButtonTex = new ResourceLocation("textures/gui/recipe_button.png");
        long[] lastSync = {-1};
        bus.<RecipesUpdatedEvent>addListener(event -> syncRecipes(lastSync));
        bus.<GuiScreenEvent.InitGuiEvent.Post>addListener(event -> {
            Screen screen = event.getGui();
            if (ConfigObject.getInstance().doesDisableRecipeBook() && screen instanceof ContainerScreen) {
                for (net.minecraft.client.gui.widget.Widget widget : Lists.newArrayList(event.getWidgetList())) {
                    if (widget instanceof ImageButton) {
                        if (((ImageButton) widget).resourceLocation.equals(recipeButtonTex))
                            event.removeWidget(widget);
                    }
                }
            }
            if (shouldReturn(screen))
                return;
            if (screen instanceof InventoryScreen && Minecraft.getInstance().gameMode.hasInfiniteItems())
                return;
            if (screen instanceof ContainerScreen)
                ScreenHelper.setPreviousContainerScreen((ContainerScreen<?>) screen);
            boolean alreadyAdded = false;
            for (IGuiEventListener element : Lists.newArrayList(screen.children()))
                if (ContainerScreenOverlay.class.isAssignableFrom(element.getClass()))
                    if (alreadyAdded)
                        screen.children().remove(element);
                    else
                        alreadyAdded = true;
            if (!alreadyAdded)
                ((List) screen.children()).add(ScreenHelper.getLastOverlay(true, false));
        });
        bus.<GuiScreenEvent.DrawScreenEvent.Post>addListener(event -> {
            if (shouldReturn(event.getGui()))
                return;
            ScreenHelper.getLastOverlay().render(event.getMatrixStack(), event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
        });
        bus.<GuiScreenEvent.MouseDragEvent.Pre>addListener(event -> {
            if (shouldReturn(event.getGui()))
                return;
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton(), event.getDragX(), event.getDragY()))
                event.setCanceled(true);
        });
        bus.<GuiScreenEvent.MouseClickedEvent.Pre>addListener(event -> {
            isLeftModePressed = true;
            if (!(event.getGui() instanceof CreativeScreen))
                return;
            if (ScreenHelper.getOptionalOverlay().isPresent() && ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
                event.getGui().setFocused(ScreenHelper.getLastOverlay());
                if (event.getButton() == 0)
                    event.getGui().setDragging(true);
                event.setCanceled(true);
            }
        });
        bus.<GuiScreenEvent.MouseReleasedEvent.Pre>addListener(event -> {
            isLeftModePressed = false;
            if (shouldReturn(event.getGui()))
                return;
            if (ScreenHelper.getOptionalOverlay().isPresent())
                if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton()))
                    event.setCanceled(true);
        });
        bus.<GuiScreenEvent.MouseScrollEvent.Pre>addListener(event -> {
            if (shouldReturn(event.getGui()))
                return;
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta()))
                event.setCanceled(true);
        });
        bus.<GuiScreenEvent.KeyboardCharTypedEvent.Pre>addListener(event -> {
            if (shouldReturn(event.getGui()))
                return;
            if (ScreenHelper.isOverlayVisible() && ScreenHelper.getLastOverlay().charTyped(event.getCodePoint(), event.getModifiers()))
                event.setCanceled(true);
        });
        bus.<GuiScreenEvent.DrawScreenEvent.Post>addListener(event -> {
            if (shouldReturn(event.getGui()))
                return;
            if (!ScreenHelper.isOverlayVisible())
                return;
            ScreenHelper.getLastOverlay().lateRender(event.getMatrixStack(), event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
        });
        bus.<GuiScreenEvent.KeyboardKeyPressedEvent.Pre>addListener(event -> {
            if (shouldReturn(event.getGui()))
                return;
            if (event.getGui() instanceof ContainerScreen && ConfigObject.getInstance().doesDisableRecipeBook() && ConfigObject.getInstance().doesFixTabCloseContainer())
                if (event.getKeyCode() == 258 && Minecraft.getInstance().options.keyInventory.matches(event.getKeyCode(), event.getScanCode())) {
                    Minecraft.getInstance().player.closeContainer();
                    event.setCanceled(true);
                    return;
                }
            if (ScreenHelper.getLastOverlay().keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers()))
                event.setCanceled(true);
        });
    }
    
}
