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

package me.shedaniel.rei;

import com.google.common.collect.Lists;
import com.mojang.serialization.DataResult;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientRecipeUpdateEvent;
import dev.architectury.event.events.client.ClientScreenInputEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.transformers.SplitPacketTransformer;
import dev.architectury.utils.value.BooleanValue;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleTypeRegistry;
import me.shedaniel.rei.api.client.gui.config.ForceLocalRecipesMode;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.addon.ConfigAddonRegistryImpl;
import me.shedaniel.rei.impl.client.entry.filtering.rules.FilteringRuleTypeRegistryImpl;
import me.shedaniel.rei.impl.client.entry.renderer.EntryRendererRegistryImpl;
import me.shedaniel.rei.impl.client.favorites.DelegatingFavoriteEntryProviderImpl;
import me.shedaniel.rei.impl.client.favorites.FavoriteEntryTypeRegistryImpl;
import me.shedaniel.rei.impl.client.gui.modules.entries.SubMenuEntry;
import me.shedaniel.rei.impl.client.gui.modules.entries.ToggleMenuEntry;
import me.shedaniel.rei.impl.client.gui.widget.InternalWidgets;
import me.shedaniel.rei.impl.client.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.client.gui.widget.TooltipContextImpl;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
import me.shedaniel.rei.impl.client.registry.category.CategoryRegistryImpl;
import me.shedaniel.rei.impl.client.registry.display.ClientRecipeFallback;
import me.shedaniel.rei.impl.client.registry.display.DisplayRegistryImpl;
import me.shedaniel.rei.impl.client.registry.screen.ScreenRegistryImpl;
import me.shedaniel.rei.impl.client.search.SearchProviderImpl;
import me.shedaniel.rei.impl.client.search.SearchRuntime;
import me.shedaniel.rei.impl.client.search.method.InputMethodRegistryImpl;
import me.shedaniel.rei.impl.client.subsets.SubsetsRegistryImpl;
import me.shedaniel.rei.impl.client.transfer.SimpleTransferHandlerImpl;
import me.shedaniel.rei.impl.client.transfer.TransferHandlerRegistryImpl;
import me.shedaniel.rei.impl.client.view.ViewsImpl;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsibleEntryRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.types.EmptyEntryDefinition;
import me.shedaniel.rei.impl.common.networking.DisplaySyncPacket;
import me.shedaniel.rei.impl.common.plugins.PluginManagerImpl;
import me.shedaniel.rei.impl.common.plugins.ReloadManagerImpl;
import me.shedaniel.rei.impl.common.registry.displays.ServerDisplayRegistryImpl;
import me.shedaniel.rei.impl.common.util.InstanceHelper;
import me.shedaniel.rei.impl.common.util.IssuesDetector;
import me.shedaniel.rei.plugin.test.REITestCommonPlugin;
import me.shedaniel.rei.plugin.test.REITestPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class RoughlyEnoughItemsCoreClient {
    public static final Event<BiConsumer<RecipeAccess, RegistryAccess>> PRE_UPDATE_RECIPES = EventFactory.createLoop();
    public static final Event<Runnable> POST_UPDATE_TAGS = EventFactory.createLoop();
    public static boolean isLeftMousePressed = false;
    
    public static void attachClientInternals() {
        InternalWidgets.attach();
        Internals.attachInstance((Supplier<ContextMap>) () -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null) {
                return SlotDisplayContext.fromLevel(client.level);
            }
            ContextMap.Builder builder = new ContextMap.Builder()
                    .withParameter(SlotDisplayContext.REGISTRIES, Internals.getRegistryAccess());
            ClientPacketListener connection = client.getConnection();
            if (connection != null) {
                builder.withParameter(SlotDisplayContext.FUEL_VALUES, connection.fuelValues());
            }
            return builder.create(SlotDisplayContext.CONTEXT);
        }, "slotDisplayContext");
        EmptyEntryDefinition.EmptyRenderer emptyEntryRenderer = new EmptyEntryDefinition.EmptyRenderer();
        ClientInternals.attachInstance((Supplier<EntryRenderer<?>>) () -> emptyEntryRenderer, "emptyEntryRenderer");
        ClientInternals.attachInstance((BiFunction<Supplier<DataResult<FavoriteEntry>>, Supplier<CompoundTag>, FavoriteEntry>) DelegatingFavoriteEntryProviderImpl::new, "delegateFavoriteEntry");
        ClientInternals.attachInstance((Function<CompoundTag, DataResult<FavoriteEntry>>) (object) -> {
            String type = object.getString(FavoriteEntry.TYPE_KEY).orElseThrow();
            Identifier id = Identifier.parse(type);
            FavoriteEntryType<FavoriteEntry> entryType = FavoriteEntryType.registry().get(id);
            if (entryType == null) return DataResult.error(() -> "Unknown favorite type: " + id + ", json: " + object);
            return entryType.read(object);
        }, "favoriteEntryFromJson");
        ClientInternals.attachInstance((BiFunction<@Nullable Point, Collection<Tooltip.Entry>, Tooltip>) QueuedTooltip::impl, "tooltipProvider");
        ClientInternals.attachInstance((ClientInternals.QuadFunction<Point, @Nullable TooltipFlag, Boolean, Item.TooltipContext, TooltipContext>) TooltipContextImpl::new, "tooltipContextProvider");
        ClientInternals.attachInstance((Function<Object, Tooltip.Entry>) QueuedTooltip.TooltipEntryImpl::new, "tooltipEntryProvider");
        ClientInternals.attachInstance((BiFunction<Component, List<FavoriteMenuEntry>, FavoriteMenuEntry>) SubMenuEntry::new, "subMenuEntry");
        ClientInternals.attachInstance((BiFunction<Component, BooleanValue, FavoriteMenuEntry>) (text, value) -> ToggleMenuEntry.of(text, value::get, value), "toggleEntry");
        ClientInternals.attachInstanceSupplier(SimpleTransferHandlerImpl.INSTANCE, "simpleTransferHandler");
        ClientInternals.attachInstance((Function<@Nullable Boolean, ClickArea.Result>) successful -> new ClickArea.Result() {
            private List<CategoryIdentifier<?>> categories = Lists.newArrayList();
            private BooleanSupplier execute = () -> {
                return false;
            };
            private Supplier<Component @Nullable []> tooltip = () -> {
                if (categories != null && !categories.isEmpty()) {
                    Component collect = CollectionUtils.mapAndJoinToComponent(categories,
                            identifier -> CategoryRegistry.getInstance().tryGet(identifier)
                                    .map(config -> config.getCategory().getTitle())
                                    .orElse(Component.literal(identifier.toString())), Component.literal(", "));
                    return new Component[]{Component.translatable("text.rei.view_recipes_for", collect)};
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
        ClientInternals.attachInstanceSupplier(new FilteringRuleTypeRegistryImpl(), "filteringRuleTypeRegistry");
        ClientInternals.attachInstanceSupplier(new PluginManagerImpl<>(
                REIClientPlugin.class,
                new EntryRendererRegistryImpl(),
                new ViewsImpl(),
                new InputMethodRegistryImpl(),
                new SearchProviderImpl(),
                new ConfigManagerImpl(),
                new EntryRegistryImpl(),
                new CollapsibleEntryRegistryImpl(),
                FilteringRuleTypeRegistry.getInstance().basic(),
                new CategoryRegistryImpl(),
                new DisplayRegistryImpl(),
                new ScreenRegistryImpl(),
                new FavoriteEntryTypeRegistryImpl(),
                new SubsetsRegistryImpl(),
                new TransferHandlerRegistryImpl(),
                new SearchRuntime(),
                new REIRuntimeImpl(),
                new ConfigAddonRegistryImpl()), "clientPluginManager");
    }
    
    public void onInitializeClient() {
        IssuesDetector.detect();
        registerEvents();
        RoughlyEnoughItemsCore.getPluginDetector().detectClientPlugins().get().run();
        loadTestPlugins();
        
        Minecraft client = Minecraft.getInstance();
        NetworkManager.registerReceiver(NetworkManager.s2c(), RoughlyEnoughItemsNetwork.CREATE_ITEMS_MESSAGE_PACKET, (buf, context) -> {
            ItemStack stack = buf.readLenientJsonWithCodec(ItemStack.OPTIONAL_CODEC);
            String player = buf.readUtf(32767);
            if (client.player != null) {
                client.player.sendSystemMessage(Component.literal(I18n.get("text.rei.cheat_items").replaceAll("\\{item_name}", EntryStacks.of(stack.copy()).asFormattedText().getString()).replaceAll("\\{item_count}", stack.copy().getCount() + "").replaceAll("\\{player_name}", player)));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.s2c(), RoughlyEnoughItemsNetwork.NOT_ENOUGH_ITEMS_PACKET, (buf, context) -> {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof CraftingScreen craftingScreen) {
                // TODO: Recipe Ghost
                /*RecipeBookComponent recipeBookGui = craftingScreen.getRecipeBookComponent();
                GhostRecipe ghostSlots = recipeBookGui.ghostRecipe;
                ghostSlots.clear();
                
                List<List<ItemStack>> input = Lists.newArrayList();
                int mapSize = buf.readInt();
                for (int i = 0; i < mapSize; i++) {
                    List<ItemStack> list = Lists.newArrayList();
                    int count = buf.readInt();
                    for (int j = 0; j < count; j++) {
                        list.add(buf.readJsonWithCodec(ItemStack.OPTIONAL_CODEC));
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
                }*/
            }
        });
        NetworkManager.registerReceiver(NetworkManager.s2c(), DisplaySyncPacket.TYPE, DisplaySyncPacket.STREAM_CODEC, List.of(new SplitPacketTransformer()), (payload, context) -> {
            ClientRecipeFallback.onServerDisplaySync();
            if (ConfigObject.getInstance().getForceLocalRecipes() == ForceLocalRecipesMode.ALWAYS && !Minecraft.getInstance().isLocalServer()) {
                // Local recipes take precedence in ALWAYS mode; ignore the server's display sync.
                InternalLogger.getInstance().info("[Local Recipes] Ignoring server display sync because local recipes mode is ALWAYS.");
                return;
            }
            payload.handle(context);
        });
    }
    
    private void loadTestPlugins() {
        if (System.getProperty("rei.test", "false").equals("true")) {
            PluginView.getClientInstance().registerPlugin(new REITestPlugin());
            PluginView.getInstance().registerPlugin(new REITestCommonPlugin());
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
            for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(screen)) {
                InteractionResult result = decider.shouldScreenBeOverlaid(screen);
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
        final Identifier recipeButtonTex = Identifier.withDefaultNamespace("textures/gui/recipe_button.png");
        MutableLong endReload = new MutableLong(-1);
        PRE_UPDATE_RECIPES.register((recipeAccess, registryAccess) -> {
            reloadPlugins(null, ReloadStage.START, registryAccess);
        });
        ClientRecipeUpdateEvent.EVENT.register(recipeManager -> {
            reloadPlugins(endReload, ReloadStage.END);
        });
        ClientRecipeUpdateEvent.ADD.register((recipeAccess, entries) -> {
            if (ClientHelperImpl.getInstance().canUsePackets()) {
                return;
            }

            InternalLogger.getInstance().debug("Received server's request to add %d recipes.", entries.size());
            DisplayRegistryImpl registry = (DisplayRegistryImpl) DisplayRegistry.getInstance();
            List<RecipeDisplayEntry> mapped = CollectionUtils.map(entries, ClientboundRecipeBookAddPacket.Entry::contents);
            // Record the server-advertised ids so the local fallback can dedup against them.
            ClientRecipeFallback.onServerRecipeBookAdd(CollectionUtils.map(mapped, RecipeDisplayEntry::id));
            registry.addJob(() -> registry.addRecipes(mapped));
        });
        ClientRecipeUpdateEvent.REMOVE.register((recipeAccess, entries) -> {
            if (ClientHelperImpl.getInstance().canUsePackets()) {
                return;
            }

            InternalLogger.getInstance().debug("Received server's request to remove %d recipes.", entries.size());
            DisplayRegistryImpl registry = (DisplayRegistryImpl) DisplayRegistry.getInstance();
            Set<RecipeDisplayId> ids = new HashSet<>(entries);
            ClientRecipeFallback.onServerRecipeBookRemove(ids);
            registry.addJob(() -> registry.removeRecipes(ids));
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            InternalLogger.getInstance().debug("Player quit, clearing reload tasks!");
            endReload.setValue(-1);
            ReloadManagerImpl.terminateReloadTasks();
            ClientRecipeFallback.reset();
        });
        ClientGuiEvent.INIT_PRE.register((screen, access) -> {
            List<ReloadStage> stages = ((PluginManagerImpl<REICommonPlugin>) PluginManager.getInstance()).getObservedStages();
            
            if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null && stages.contains(ReloadStage.START)
                    && !stages.contains(ReloadStage.END) && !PluginManager.areAnyReloading() && screen instanceof AbstractContainerScreen) {
                if (ReloadManagerImpl.countRunningReloadTasks() > 0) {
                    return EventResult.pass();
                }
                
                InternalLogger.getInstance().error("Detected missing stage: END! This is possibly due to issues during client recipe reload! REI will force a reload of the recipes now!");
                reloadPlugins(endReload, ReloadStage.END);
            }
            
            return EventResult.pass();
        });
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            REIRuntime.getInstance().getOverlay(false, true);
            if (Minecraft.getInstance().screen == screen) {
                if (REIRuntime.getInstance().getPreviousScreen() != screen) {
                    OverlaySearchField searchField = REIRuntimeImpl.getSearchField();
                    
                    if (searchField != null) {
                        searchField.setFocused(false);
                    }
                }
                
                REIRuntimeImpl.getInstance().setPreviousScreen(screen);
                ((ScreenRegistryImpl) ScreenRegistry.getInstance()).getLastRendererProvider(screen);
            }
            if (ConfigObject.getInstance().doesDisableRecipeBook() && screen instanceof AbstractContainerScreen) {
                access.getRenderables().removeIf(widget -> widget instanceof ImageButton button && button.sprites.enabled().equals(recipeButtonTex));
                access.getNarratables().removeIf(widget -> widget instanceof ImageButton button && button.sprites.enabled().equals(recipeButtonTex));
                screen.children().removeIf(widget -> widget instanceof ImageButton button && button.sprites.enabled().equals(recipeButtonTex));
            }
        });
        ClientScreenInputEvent.MOUSE_CLICKED_PRE.register((minecraftClient, screen, event, doubleClick) -> {
            isLeftMousePressed = true;
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().mouseClicked(event, doubleClick)) {
                if (event.button() == 0) {
                    screen.setDragging(true);
                }
                resetFocused(screen);
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
        ClientScreenInputEvent.MOUSE_RELEASED_PRE.register((minecraftClient, screen, event) -> {
            isLeftMousePressed = false;
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            resetFocused(screen);
            if (REIRuntime.getInstance().isOverlayVisible() && getOverlay().mouseReleased(event)
                    && resetFocused(screen)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
        ClientScreenInputEvent.MOUSE_SCROLLED_PRE.register((minecraftClient, screen, mouseX, mouseY, amountX, amountY) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            resetFocused(screen);
            if (REIRuntime.getInstance().isOverlayVisible() && getOverlay().mouseScrolled(mouseX, mouseY, amountX, amountY)
                    && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientScreenInputEvent.CHAR_TYPED_PRE.register((minecraftClient, screen, event) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            if (!REIRuntimeImpl.getSearchField().isFocused()) {
                GuiEventListener focused = screen.getFocused();
                if (focused != null) {
                    if (focused instanceof EditBox editBox && editBox.isFocused()) return EventResult.pass();
                    if (focused instanceof RecipeBookComponent book && book.searchBox != null && book.searchBox.isFocused()) return EventResult.pass();
                }
            }
            resetFocused(screen);
            if (getOverlay().charTyped(event)
                    && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientScreenInputEvent.MOUSE_DRAGGED_PRE.register((minecraftClient, screen, event, mouseX2, mouseY2) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            resetFocused(screen);
            if (getOverlay().mouseDragged(event, mouseX2, mouseY2)
                    && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientScreenInputEvent.KEY_PRESSED_PRE.register((minecraftClient, screen, event) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            if (screen instanceof AbstractContainerScreen && ConfigObject.getInstance().doesDisableRecipeBook() && ConfigObject.getInstance().doesFixTabCloseContainer()) {
                if (event.key() == 258 && minecraftClient.options.keyInventory.matches(event)) {
                    minecraftClient.player.closeContainer();
                    return EventResult.interruptFalse();
                }
            }
            if (!REIRuntimeImpl.getSearchField().isFocused()) {
                GuiEventListener focused = screen.getFocused();
                if (focused != null) {
                    if (focused instanceof EditBox editBox && editBox.isFocused()) return EventResult.pass();
                    if (focused instanceof RecipeBookComponent book && book.searchBox != null && book.searchBox.isFocused()) return EventResult.pass();
                }
            }
            resetFocused(screen);
            if (getOverlay().keyPressed(event)
                    && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
        ClientScreenInputEvent.KEY_RELEASED_PRE.register((minecraftClient, screen, event) -> {
            if (shouldReturn(screen) || screen instanceof DisplayScreen)
                return EventResult.pass();
            if (!REIRuntimeImpl.getSearchField().isFocused()) {
                GuiEventListener focused = screen.getFocused();
                if (focused != null) {
                    if (focused instanceof EditBox editBox && editBox.isFocused()) return EventResult.pass();
                    if (focused instanceof RecipeBookComponent book && book.searchBox != null && book.searchBox.isFocused()) return EventResult.pass();
                }
            }
            resetFocused(screen);
            if (getOverlay().keyReleased(event)
                    && resetFocused(screen))
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
    }
    
    public static boolean resetFocused(Screen screen) {
        if (screen.getFocused() instanceof ScreenOverlay || screen.getFocused() == screen) {
            screen.setFocused(null);
        }
        return true;
    }
    
    @ApiStatus.Internal
    public static void reloadPlugins(MutableLong lastReload, @Nullable ReloadStage start) {
        reloadPlugins(lastReload, start, null);
    }
    
    @ApiStatus.Internal
    public static void reloadPlugins(MutableLong lastReload, @Nullable ReloadStage start, @Nullable RegistryAccess registryAccess) {
        if (Minecraft.getInstance().level == null) return;
        if (lastReload != null) {
            if (lastReload.getValue() > 0 && System.currentTimeMillis() - lastReload.getValue() <= 1000) {
                InternalLogger.getInstance().warn("Suppressing Reload Plugins of stage " + start);
                return;
            }
            lastReload.setValue(System.currentTimeMillis());
        }
        ReloadManagerImpl.reloadPlugins(start, () -> InstanceHelper.connectionFromClient() == null);
    }
}
