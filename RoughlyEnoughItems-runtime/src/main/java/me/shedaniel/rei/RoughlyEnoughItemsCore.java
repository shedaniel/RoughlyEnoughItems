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
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.api.Executor;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.fractions.Fraction;
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
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

import static me.shedaniel.rei.impl.Internals.attachInstance;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class RoughlyEnoughItemsCore implements ClientModInitializer {
    
    @ApiStatus.Internal public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final Map<ResourceLocation, REIPluginEntry> plugins = Maps.newHashMap();
    private static final ExecutorService SYNC_RECIPES = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "REI-SyncRecipes"));
    @ApiStatus.Experimental
    public static boolean isLeftModePressed = false;
    
    static {
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
            public EntryStack fluid(Fluid fluid) {
                return new FluidEntryStack(fluid);
            }
            
            @Override
            public EntryStack fluid(Fluid fluid, Fraction amount) {
                return new FluidEntryStack(fluid, amount);
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
        attachInstance((BiFunction<@Nullable Point, Collection<Component>, Tooltip>) QueuedTooltip::create, "tooltipProvider");
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
        return ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET) && ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.CREATE_ITEMS_GRAB_PACKET) && ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET);
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
    
    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeClient() {
        attachInstance(new ConfigManagerImpl(), ConfigManager.class);
        
        detectFabricLoader();
        registerClothEvents();
        discoverPluginEntries();
        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            if (modContainer.getMetadata().containsCustomElement("roughlyenoughitems:plugins"))
                RoughlyEnoughItemsCore.LOGGER.error("REI plugin from " + modContainer.getMetadata().getId() + " is not loaded because it is too old!");
        }
        
        boolean networkingLoaded = FabricLoader.getInstance().isModLoaded("fabric-networking-v0");
        if (!networkingLoaded) {
            RoughlyEnoughItemsState.error("Fabric API is not installed!", "https://www.curseforge.com/minecraft/mc-mods/fabric-api/files/all");
            return;
        }
        Executor.run(() -> () -> {
            ClientSidePacketRegistry.INSTANCE.register(RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, (packetContext, packetByteBuf) -> {
                ItemStack stack = packetByteBuf.readItem();
                String player = packetByteBuf.readUtf(32767);
                packetContext.getPlayer().displayClientMessage(new TextComponent(I18n.get("text.rei.cheat_items").replaceAll("\\{item_name}", EntryStack.create(stack.copy()).asFormattedText().getString()).replaceAll("\\{item_count}", stack.copy().getCount() + "").replaceAll("\\{player_name}", player)), false);
            });
            ClientSidePacketRegistry.INSTANCE.register(RoughlyEnoughItemsNetwork.NOT_ENOUGH_ITEMS_PACKET, (packetContext, packetByteBuf) -> {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof CraftingScreen) {
                    RecipeBookComponent recipeBookGui = ((RecipeUpdateListener) currentScreen).getRecipeBookComponent();
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
    
    private void detectFabricLoader() {
        Executor.run(() -> () -> {
            try {
                FabricLoader instance = FabricLoader.getInstance();
                for (Field field : instance.getClass().getDeclaredFields()) {
                    if (Logger.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Logger logger = (Logger) field.get(instance);
                        if (logger.getName().toLowerCase(Locale.ROOT).contains("subsystem")) {
                            File reiConfigFolder = new File(instance.getConfigDirectory(), "roughlyenoughitems");
                            File ignoreFile = new File(reiConfigFolder, ".ignoresubsystem");
                            if (!ignoreFile.exists()) {
                                RoughlyEnoughItemsState.warn("Subsystem is detected (probably though Aristois), please contact support from them if anything happens.");
                                RoughlyEnoughItemsState.onContinue(() -> {
                                    try {
                                        reiConfigFolder.mkdirs();
                                        ignoreFile.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        });
    }
    
    private void discoverPluginEntries() {
        for (REIPluginEntry reiPlugin : FabricLoader.getInstance().getEntrypoints("rei_plugins", REIPluginEntry.class)) {
            try {
                if (!REIPluginV0.class.isAssignableFrom(reiPlugin.getClass()))
                    throw new IllegalArgumentException("REI plugin is too old!");
                registerPlugin(reiPlugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("Can't load REI plugins from %s: %s", reiPlugin.getClass(), e.getLocalizedMessage());
            }
        }
        for (REIPluginV0 reiPlugin : FabricLoader.getInstance().getEntrypoints("rei_plugins_v0", REIPluginV0.class)) {
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
    }
    
    private boolean shouldReturn(Screen screen) {
        if (screen == null) return true;
        return shouldReturn(screen.getClass());
    }
    
    private boolean shouldReturn(Class<?> screen) {
        try {
            for (OverlayDecider decider : DisplayHelper.getInstance().getAllOverlayDeciders()) {
                if (!decider.isHandingScreen(screen))
                    continue;
                InteractionResult result = decider.shouldScreenBeOverlayed(screen);
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
