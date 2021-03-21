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

package me.shedaniel.rei.gui;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.REIOverlay;
import me.shedaniel.rei.api.config.ConfigManager;
import me.shedaniel.rei.api.config.ConfigObject;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.api.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.gui.drag.DraggableStackProvider;
import me.shedaniel.rei.api.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.gui.drag.DraggingContext;
import me.shedaniel.rei.api.gui.widgets.Button;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.api.gui.widgets.Widget;
import me.shedaniel.rei.api.gui.widgets.Widgets;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.api.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.registry.screen.ClickArea;
import me.shedaniel.rei.api.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.util.CollectionUtils;
import me.shedaniel.rei.api.util.ImmutableLiteralText;
import me.shedaniel.rei.api.view.ViewSearchBuilder;
import me.shedaniel.rei.gui.modules.Menu;
import me.shedaniel.rei.gui.modules.entries.GameModeMenuEntry;
import me.shedaniel.rei.gui.modules.entries.WeatherMenuEntry;
import me.shedaniel.rei.gui.widget.EntryListWidget;
import me.shedaniel.rei.gui.widget.FavoritesListWidget;
import me.shedaniel.rei.gui.widget.LateRenderable;
import me.shedaniel.rei.impl.ClientHelperImpl;
import me.shedaniel.rei.impl.InternalWidgets;
import me.shedaniel.rei.impl.REIHelperImpl;
import me.shedaniel.rei.impl.Weather;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ApiStatus.Internal
public class ContainerScreenOverlay extends REIOverlay {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final List<Tooltip> TOOLTIPS = Lists.newArrayList();
    private static final List<Runnable> AFTER_RENDER = Lists.newArrayList();
    private static final EntryListWidget ENTRY_LIST_WIDGET = new EntryListWidget();
    private static FavoritesListWidget favoritesListWidget = null;
    private final List<Widget> widgets = Lists.newLinkedList();
    public boolean shouldReload = false;
    private Rectangle bounds;
    private Window window;
    private Button leftButton, rightButton;
    private CurrentDraggingStack draggingStack = new CurrentDraggingStack();
    
    @Nullable
    private ContainerScreenOverlay.OverlayMenu overlayMenu = null;
    
    public Set<EntryStack<?>> inventoryStacks = Sets.newHashSet();
    
    public static EntryListWidget getEntryListWidget() {
        return ENTRY_LIST_WIDGET;
    }
    
    @Nullable
    public static FavoritesListWidget getFavoritesListWidget() {
        return favoritesListWidget;
    }
    
    public static ContainerScreenOverlay getInstance() {
        return (ContainerScreenOverlay) REIHelper.getInstance().getOverlay().get();
    }
    
    private static class OverlayMenu {
        @NotNull
        private UUID uuid;
        @NotNull
        private Menu menu;
        @NotNull
        private Widget wrappedMenu;
        @NotNull
        private Predicate<Point> inBounds;
        
        public OverlayMenu(@NotNull UUID uuid, @NotNull Menu menu, @NotNull Widget wrappedMenu, @NotNull Predicate<Point> inBounds) {
            this.uuid = uuid;
            this.menu = menu;
            this.wrappedMenu = wrappedMenu;
            this.inBounds = inBounds.or(point -> menu.getBounds().contains(point));
        }
    }
    
    public boolean isMenuOpened(UUID uuid) {
        return overlayMenu != null && overlayMenu.uuid.equals(uuid);
    }
    
    public boolean isAnyMenuOpened() {
        return overlayMenu != null;
    }
    
    public boolean isMenuInBounds(UUID uuid) {
        return isMenuOpened(uuid) && overlayMenu.inBounds.test(PointHelper.ofMouse());
    }
    
    private void proceedOpenMenu(UUID uuid, Runnable runnable) {
        proceedOpenMenuOrElse(uuid, runnable, menu -> {});
    }
    
    private void proceedOpenMenuOrElse(UUID uuid, Runnable runnable, Consumer<OverlayMenu> orElse) {
        if (overlayMenu == null || !overlayMenu.uuid.equals(uuid)) {
            closeOverlayMenu();
            runnable.run();
        } else {
            orElse.accept(this.overlayMenu);
        }
    }
    
    public void openMenu(UUID uuid, Menu menu, Predicate<Point> inPoint) {
        this.overlayMenu = new OverlayMenu(uuid, menu, Widgets.withTranslate(menu, 0, 0, 400), inPoint);
    }
    
    @ApiStatus.Internal
    @Nullable
    public Menu getOverlayMenu() {
        if (isMenuOpened(Menu.SUBSETS))
            return this.overlayMenu.menu;
        throw new IllegalStateException("Subsets menu accessed when subsets are not opened!");
    }
    
    @ApiStatus.Internal
    @Override
    public void closeOverlayMenu() {
        OverlayMenu tmpOverlayMenu = this.overlayMenu;
        if (tmpOverlayMenu != null)
            AFTER_RENDER.add(() -> this.widgets.remove(tmpOverlayMenu.wrappedMenu));
        this.overlayMenu = null;
    }
    
    @Override
    public void queueReloadOverlay() {
        shouldReload = true;
    }
    
    @Override
    public DraggingContext getDraggingContext() {
        return draggingStack;
    }
    
    private static <T> Iterable<T> buildWidgetsTree(Iterable<? extends GuiEventListener> listeners, Class<T> type) {
        return () -> new AbstractIterator<T>() {
            Stack<Iterator<? extends GuiEventListener>> stack;
            
            {
                stack = new Stack<>();
                stack.push(listeners.iterator());
            }
            
            @Override
            protected T computeNext() {
                while (!stack.empty()) {
                    Iterator<? extends GuiEventListener> peek = stack.peek();
                    GuiEventListener listener = peek.next();
                    if (!peek.hasNext())
                        stack.pop();
                    if (type.isInstance(listener)) {
                        return (T) listener;
                    }
                    if (listener instanceof ContainerEventHandler) {
                        List<? extends GuiEventListener> children = ((ContainerEventHandler) listener).children();
                        if (!children.isEmpty()) {
                            stack.push(children.iterator());
                        }
                    }
                }
                return endOfData();
            }
        };
    }
    
    public void init(boolean useless) {
        init();
    }
    
    public void init() {
        Iterable<DraggableStackProvider> stackProviders = buildWidgetsTree(Iterables.concat(widgets, Minecraft.getInstance().screen.children()), DraggableStackProvider.class);
        Iterable<DraggableStackVisitor> stackVisitors = buildWidgetsTree(Iterables.concat(widgets, Minecraft.getInstance().screen.children()), DraggableStackVisitor.class);
        draggingStack.set(DraggableStackProvider.from(() -> stackProviders), DraggableStackVisitor.from(() -> stackVisitors));
        
        this.shouldReload = false;
        //Update Variables
        this.children().clear();
        this.closeOverlayMenu();
        this.window = Minecraft.getInstance().getWindow();
        this.bounds = ScreenRegistry.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation(), Minecraft.getInstance().screen);
        widgets.add(ENTRY_LIST_WIDGET);
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            if (favoritesListWidget == null) {
                favoritesListWidget = new FavoritesListWidget();
            }
            favoritesListWidget.favoritePanel.resetRows();
            widgets.add(favoritesListWidget);
        }
        ENTRY_LIST_WIDGET.updateArea(REIHelperImpl.getSearchField() == null ? "" : REIHelperImpl.getSearchField().getText());
        REIHelperImpl.getSearchField().getBounds().setBounds(getSearchFieldArea());
        this.widgets.add(REIHelperImpl.getSearchField());
        REIHelperImpl.getSearchField().setResponder(s -> ENTRY_LIST_WIDGET.updateSearch(s, false));
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(leftButton = Widgets.createButton(new Rectangle(bounds.x, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), new TranslatableComponent("text.rei.left_arrow"))
                    .onClick(button -> {
                        ENTRY_LIST_WIDGET.previousPage();
                        if (ENTRY_LIST_WIDGET.getPage() < 0)
                            ENTRY_LIST_WIDGET.setPage(ENTRY_LIST_WIDGET.getTotalPages() - 1);
                        ENTRY_LIST_WIDGET.updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(I18n.get("text.rei.previous_page"))
                    .focusable(false));
            widgets.add(rightButton = Widgets.createButton(new Rectangle(bounds.x + bounds.width - 18, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), new TranslatableComponent("text.rei.right_arrow"))
                    .onClick(button -> {
                        ENTRY_LIST_WIDGET.nextPage();
                        if (ENTRY_LIST_WIDGET.getPage() >= ENTRY_LIST_WIDGET.getTotalPages())
                            ENTRY_LIST_WIDGET.setPage(0);
                        ENTRY_LIST_WIDGET.updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(I18n.get("text.rei.next_page"))
                    .focusable(false));
        }
        
        final Rectangle configButtonArea = getConfigButtonArea();
        Widget tmp;
        widgets.add(tmp = InternalWidgets.wrapLateRenderable(InternalWidgets.concatWidgets(
                Widgets.createButton(configButtonArea, NarratorChatListener.NO_TITLE)
                        .onClick(button -> {
                            if (Screen.hasShiftDown() || Screen.hasControlDown()) {
                                ClientHelper.getInstance().setCheating(!ClientHelper.getInstance().isCheating());
                                return;
                            }
                            ConfigManager.getInstance().openConfigScreen(REIHelper.getInstance().getPreviousScreen());
                        })
                        .onRender((matrices, button) -> {
                            if (ClientHelper.getInstance().isCheating() && RoughlyEnoughItemsCore.hasOperatorPermission()) {
                                button.setTint(RoughlyEnoughItemsCore.hasPermissionToUsePackets() ? 721354752 : 1476440063);
                            } else {
                                button.removeTint();
                            }
                        })
                        .focusable(false)
                        .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                        .tooltipSupplier(button -> {
                            String tooltips = I18n.get("text.rei.config_tooltip");
                            tooltips += "\n  ";
                            if (!ClientHelper.getInstance().isCheating())
                                tooltips += "\n" + I18n.get("text.rei.cheating_disabled");
                            else if (!RoughlyEnoughItemsCore.hasOperatorPermission()) {
                                if (minecraft.gameMode.hasInfiniteItems())
                                    tooltips += "\n" + I18n.get("text.rei.cheating_limited_creative_enabled");
                                else tooltips += "\n" + I18n.get("text.rei.cheating_enabled_no_perms");
                            } else if (RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                                tooltips += "\n" + I18n.get("text.rei.cheating_enabled");
                            else
                                tooltips += "\n" + I18n.get("text.rei.cheating_limited_enabled");
                            return tooltips;
                        }),
                Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                    helper.setBlitOffset(helper.getBlitOffset() + 1);
                    Minecraft.getInstance().getTextureManager().bind(CHEST_GUI_TEXTURE);
                    helper.blit(matrices, configButtonArea.x + 3, configButtonArea.y + 3, 0, 0, 14, 14);
                })
                )
        ));
        tmp.setZ(600);
        if (ConfigObject.getInstance().doesShowUtilsButtons()) {
            widgets.add(Widgets.createButton(ConfigObject.getInstance().isLowerConfigButton() ? new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 30 : 10, 10, 20, 20) : new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 55 : 35, 10, 20, 20), NarratorChatListener.NO_TITLE)
                    .onRender((matrices, button) -> {
                        boolean isOpened = isMenuOpened(Menu.GAME_TYPE);
                        if (isOpened || !isAnyMenuOpened()) {
                            boolean inBounds = (button.isFocused() || button.containsMouse(PointHelper.ofMouse())) || isMenuInBounds(Menu.GAME_TYPE);
                            if (isOpened != inBounds) {
                                if (inBounds) {
                                    Menu menu = new Menu(new Point(button.getBounds().x, button.getBounds().getMaxY()),
                                            CollectionUtils.map(GameType.values(), GameModeMenuEntry::new));
                                    if (ConfigObject.getInstance().isLeftHandSidePanel())
                                        menu.menuStartPoint.x -= menu.getBounds().width - button.getBounds().width;
                                    openMenu(Menu.GAME_TYPE, menu, point -> button.isFocused() && button.containsMouse(PointHelper.ofMouse()));
                                } else {
                                    closeOverlayMenu();
                                }
                            }
                        }
                        button.setText(new TextComponent(getGameModeShortText(getCurrentGameMode())));
                    })
                    .focusable(false)
                    .tooltipLine(I18n.get("text.rei.gamemode_button.tooltip.all"))
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y)));
            Button weatherButton;
            widgets.add(weatherButton = Widgets.createButton(new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 30 : 10, 35, 20, 20), NarratorChatListener.NO_TITLE)
                    .onRender((matrices, button) -> {
                        boolean isOpened = isMenuOpened(Menu.WEATHER);
                        if (isOpened || !isAnyMenuOpened()) {
                            boolean inBounds = (button.isFocused() || button.containsMouse(PointHelper.ofMouse())) || isMenuInBounds(Menu.WEATHER);
                            if (isOpened != inBounds) {
                                if (inBounds) {
                                    Menu menu = new Menu(new Point(button.getBounds().x, button.getBounds().getMaxY()),
                                            CollectionUtils.map(Weather.values(), WeatherMenuEntry::new));
                                    if (ConfigObject.getInstance().isLeftHandSidePanel())
                                        menu.menuStartPoint.x -= menu.getBounds().width - button.getBounds().width;
                                    openMenu(Menu.WEATHER, menu, point -> button.isFocused() && button.containsMouse(PointHelper.ofMouse()));
                                } else {
                                    closeOverlayMenu();
                                }
                            }
                        }
                    })
                    .tooltipLine(I18n.get("text.rei.weather_button.tooltip.all"))
                    .focusable(false)
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y)));
            widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                Minecraft.getInstance().getTextureManager().bind(CHEST_GUI_TEXTURE);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                helper.blit(matrices, weatherButton.getBounds().x + 3, weatherButton.getBounds().y + 3, getCurrentWeather().getId() * 14, 14, 14, 14);
            }));
        }
        Rectangle subsetsButtonBounds = getSubsetsButtonBounds();
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            widgets.add(InternalWidgets.wrapLateRenderable(Widgets.withTranslate(Widgets.createButton(subsetsButtonBounds, ClientHelperImpl.getInstance().isAprilFools.get() ? new TranslatableComponent("text.rei.tiny_potato") : new TranslatableComponent("text.rei.subsets"))
                    .onClick(button -> {
                        proceedOpenMenuOrElse(Menu.SUBSETS, () -> {
                            openMenu(Menu.SUBSETS, Menu.createSubsetsMenuFromRegistry(new Point(subsetsButtonBounds.x, subsetsButtonBounds.getMaxY())), point -> true);
                        }, menu -> {
                            closeOverlayMenu();
                        });
                    }), 0, 0, 600)));
        }
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(Widgets.createClickableLabel(new Point(bounds.x + (bounds.width / 2), bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 10), NarratorChatListener.NO_TITLE, label -> {
                ENTRY_LIST_WIDGET.setPage(0);
                ENTRY_LIST_WIDGET.updateEntriesPosition();
            }).tooltipLine(I18n.get("text.rei.go_back_first_page")).focusable(false).onRender((matrices, label) -> {
                label.setClickable(ENTRY_LIST_WIDGET.getTotalPages() > 1);
                label.setText(new TextComponent(String.format("%s/%s", ENTRY_LIST_WIDGET.getPage() + 1, Math.max(ENTRY_LIST_WIDGET.getTotalPages(), 1))));
            }).rainbow(new Random().nextFloat() < 1.0E-4D || ClientHelperImpl.getInstance().isAprilFools.get()));
        }
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) {
            Rectangle area = getCraftableToggleArea();
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            ItemStack icon = new ItemStack(Blocks.CRAFTING_TABLE);
            this.widgets.add(tmp = InternalWidgets.wrapLateRenderable(InternalWidgets.concatWidgets(
                    Widgets.createButton(area, NarratorChatListener.NO_TITLE)
                            .focusable(false)
                            .onClick(button -> {
                                ConfigManager.getInstance().toggleCraftableOnly();
                                ENTRY_LIST_WIDGET.updateSearch(REIHelperImpl.getSearchField().getText(), true);
                            })
                            .onRender((matrices, button) -> button.setTint(ConfigManager.getInstance().isCraftableOnlyEnabled() ? 939579655 : 956235776))
                            .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                            .tooltipSupplier(button -> I18n.get(ConfigManager.getInstance().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all")),
                    Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                        Vector4f vector = new Vector4f(area.x + 2, area.y + 2, helper.getBlitOffset() - 10, 1.0F);
                        vector.transform(matrices.last().pose());
                        itemRenderer.blitOffset = vector.z();
                        itemRenderer.renderGuiItem(icon, (int) vector.x(), (int) vector.y());
                        itemRenderer.blitOffset = 0.0F;
                    }))
            ));
            tmp.setZ(600);
        }
        
        widgets.add(draggingStack);
    }
    
    private Rectangle getSubsetsButtonBounds() {
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            ScreenRegistry registry = ScreenRegistry.getInstance();
            Rectangle screenBounds = registry.getScreenBounds(minecraft.screen);
            return new Rectangle(screenBounds.x, 3, screenBounds.width, 18);
        }
        return null;
    }
    
    private Weather getNextWeather() {
        try {
            Weather current = getCurrentWeather();
            int next = current.getId() + 1;
            if (next >= 3)
                next = 0;
            return Weather.byId(next);
        } catch (Exception e) {
            return Weather.CLEAR;
        }
    }
    
    private Weather getCurrentWeather() {
        ClientLevel world = Minecraft.getInstance().level;
        if (world.isThundering())
            return Weather.THUNDER;
        if (world.getLevelData().isRaining())
            return Weather.RAIN;
        return Weather.CLEAR;
    }
    
    private String getGameModeShortText(GameType gameMode) {
        return I18n.get("text.rei.short_gamemode." + gameMode.getName());
    }
    
    private String getGameModeText(GameType gameMode) {
        return I18n.get("selectWorld.gameMode." + gameMode.getName());
    }
    
    private GameType getCurrentGameMode() {
        PlayerInfo info = Minecraft.getInstance().getConnection().getPlayerInfo(Minecraft.getInstance().player.getGameProfile().getId());
        return info == null ? GameType.SURVIVAL : info.getGameMode();
    }
    
    private Rectangle getSearchFieldArea() {
        int widthRemoved = 1;
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) widthRemoved += 22;
        if (ConfigObject.getInstance().isLowerConfigButton()) widthRemoved += 22;
        SearchFieldLocation searchFieldLocation = REIHelper.getInstance().getContextualSearchFieldLocation();
        switch (searchFieldLocation) {
            case TOP_SIDE:
                return getTopSideSearchFieldArea(widthRemoved);
            case BOTTOM_SIDE:
                return getBottomSideSearchFieldArea(widthRemoved);
            default:
            case CENTER:
                return getCenterSearchFieldArea(widthRemoved);
        }
    }
    
    private Rectangle getTopSideSearchFieldArea(int widthRemoved) {
        return new Rectangle(bounds.x + 2, 4, bounds.width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getBottomSideSearchFieldArea(int widthRemoved) {
        return new Rectangle(bounds.x + 2, window.getGuiScaledHeight() - 22, bounds.width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getCenterSearchFieldArea(int widthRemoved) {
        Rectangle screenBounds = ScreenRegistry.getInstance().getScreenBounds(minecraft.screen);
        return getBottomCenterSearchFieldArea(screenBounds, widthRemoved);
    }
    
    private Rectangle getBottomCenterSearchFieldArea(Rectangle containerBounds, int widthRemoved) {
        return new Rectangle(containerBounds.x, window.getGuiScaledHeight() - 22, containerBounds.width - widthRemoved, 18);
    }
    
    private Rectangle getCraftableToggleArea() {
        Rectangle area = getSearchFieldArea();
        area.setLocation(area.x + area.width + 4, area.y - 1);
        area.setSize(20, 20);
        return area;
    }
    
    private Rectangle getConfigButtonArea() {
        if (ConfigObject.getInstance().isLowerConfigButton()) {
            Rectangle area = getSearchFieldArea();
            area.setLocation(area.x + area.width + (ConfigObject.getInstance().isCraftableFilterEnabled() ? 26 : 4), area.y - 1);
            area.setSize(20, 20);
            return area;
        }
        return new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getGuiScaledWidth() - 30 : 10, 10, 20, 20);
    }
    
    private String getCheatModeText() {
        return I18n.get(String.format("%s%s", "text.rei.", ClientHelper.getInstance().isCheating() ? "cheat" : "nocheat"));
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (shouldReload) {
            ENTRY_LIST_WIDGET.updateSearch(REIHelperImpl.getSearchField().getText(), true);
            init();
        } else {
            for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(minecraft.screen)) {
                if (decider != null && decider.shouldRecalculateArea(ConfigObject.getInstance().getDisplayPanelLocation(), bounds)) {
                    init();
                    break;
                }
            }
        }
        if (ConfigManager.getInstance().isCraftableOnlyEnabled()) {
            Set<EntryStack<?>> currentStacks = ClientHelperImpl.getInstance()._getInventoryItemsTypes();
            if (!currentStacks.equals(this.inventoryStacks)) {
                this.inventoryStacks = currentStacks;
                ENTRY_LIST_WIDGET.updateSearch(REIHelperImpl.getSearchField().getText(), true);
            }
        }
        if (OverlaySearchField.isHighlighting) {
            matrices.pushPose();
            matrices.translate(0, 0, 200f);
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen) {
                AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) Minecraft.getInstance().screen;
                int x = containerScreen.leftPos, y = containerScreen.topPos;
                for (Slot slot : containerScreen.getMenu().slots)
                    if (!slot.hasItem() || !ENTRY_LIST_WIDGET.matches(EntryStacks.of(slot.getItem())))
                        fillGradient(matrices, x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, -601874400, -601874400);
            }
            matrices.popPose();
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderWidgets(matrices, mouseX, mouseY, delta);
        if (ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            Screen screen = Minecraft.getInstance().screen;
            ClickArea.ClickAreaContext<Screen> context = new ClickArea.ClickAreaContext<Screen>() {
                @Override
                public Screen getScreen() {
                    return screen;
                }
                
                @Override
                public Point getMousePosition() {
                    return new Point(mouseX, mouseY);
                }
            };
            Set<ResourceLocation> categories = ScreenRegistry.getInstance().handleClickArea((Class<Screen>) screen.getClass(), context);
            if (categories != null && !categories.isEmpty()) {
                Component collect = CollectionUtils.mapAndJoinToComponent(categories, identifier -> CategoryRegistry.getInstance().get(identifier).getCategory().getTitle(), new ImmutableLiteralText(", "));
                Tooltip.create(new TranslatableComponent("text.rei.view_recipes_for", collect)).queue();
            }
        }
    }
    
    public void lateRender(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (REIHelper.getInstance().isOverlayVisible()) {
            REIHelperImpl.getSearchField().laterRender(matrices, mouseX, mouseY, delta);
            for (Widget widget : widgets) {
                if (widget instanceof LateRenderable && (overlayMenu == null || overlayMenu.wrappedMenu != widget))
                    widget.render(matrices, mouseX, mouseY, delta);
            }
            if (overlayMenu != null) {
                if (overlayMenu.wrappedMenu.containsMouse(mouseX, mouseY)) {
                    TOOLTIPS.clear();
                }
                overlayMenu.wrappedMenu.render(matrices, mouseX, mouseY, delta);
            }
        }
        Screen currentScreen = Minecraft.getInstance().screen;
        if (!(currentScreen instanceof RecipeViewingScreen) || !((RecipeViewingScreen) currentScreen).choosePageActivated) {
            for (Tooltip tooltip : TOOLTIPS) {
                if (tooltip != null)
                    renderTooltip(matrices, tooltip);
            }
        }
        TOOLTIPS.clear();
        if (REIHelper.getInstance().isOverlayVisible()) {
            for (Runnable runnable : AFTER_RENDER) {
                runnable.run();
            }
            AFTER_RENDER.clear();
        }
    }
    
    public void renderTooltip(PoseStack matrices, Tooltip tooltip) {
        renderTooltip(matrices, tooltip.getText(), tooltip.getX(), tooltip.getY());
    }
    
    public void renderTooltip(PoseStack matrices, List<Component> lines, int mouseX, int mouseY) {
        if (lines.isEmpty())
            return;
        List<FormattedCharSequence> orderedTexts = CollectionUtils.map(lines, Component::getVisualOrderText);
        renderTooltipInner(matrices, orderedTexts, mouseX, mouseY);
    }
    
    public void renderTooltipInner(PoseStack matrices, List<FormattedCharSequence> lines, int mouseX, int mouseY) {
        if (lines.isEmpty())
            return;
        matrices.pushPose();
        matrices.translate(0, 0, 500);
        minecraft.screen.renderTooltip(matrices, lines, mouseX, mouseY);
        matrices.popPose();
    }
    
    public void addTooltip(@Nullable Tooltip tooltip) {
        if (tooltip != null)
            TOOLTIPS.add(tooltip);
    }
    
    public void renderWidgets(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!REIHelper.getInstance().isOverlayVisible())
            return;
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            leftButton.setEnabled(ENTRY_LIST_WIDGET.getTotalPages() > 1);
            rightButton.setEnabled(ENTRY_LIST_WIDGET.getTotalPages() > 1);
        }
        for (Widget widget : widgets) {
            if (!(widget instanceof LateRenderable))
                widget.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!REIHelper.getInstance().isOverlayVisible())
            return false;
        if (overlayMenu != null && overlayMenu.wrappedMenu.mouseScrolled(mouseX, mouseY, amount))
            return true;
        if (isInside(PointHelper.ofMouse())) {
            if (ENTRY_LIST_WIDGET.mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
            if (!Screen.hasControlDown() && !ConfigObject.getInstance().isEntryListWidgetScrolled()) {
                if (amount > 0 && leftButton.isEnabled())
                    leftButton.onClick();
                else if (amount < 0 && rightButton.isEnabled())
                    rightButton.onClick();
                else
                    return false;
                return true;
            }
        }
        if (isNotInExclusionZones(PointHelper.getMouseX(), PointHelper.getMouseY())) {
            if (favoritesListWidget != null && favoritesListWidget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        }
        for (Widget widget : widgets)
            if (widget != ENTRY_LIST_WIDGET && (favoritesListWidget == null || widget != favoritesListWidget)
                && (overlayMenu == null || widget != overlayMenu.wrappedMenu)
                && widget.mouseScrolled(mouseX, mouseY, amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (REIHelper.getInstance().isOverlayVisible()) {
            if (REIHelperImpl.getSearchField().keyPressed(keyCode, scanCode, modifiers))
                return true;
            for (GuiEventListener listener : widgets)
                if (listener != REIHelperImpl.getSearchField() && listener.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        }
        if (ConfigObject.getInstance().getHideKeybind().matchesKey(keyCode, scanCode)) {
            REIHelper.getInstance().toggleOverlayVisible();
            return true;
        }
        EntryStack<?> stack = ScreenRegistry.getInstance().getFocusedStack(Minecraft.getInstance().screen, PointHelper.ofMouse());
        if (stack != null && !stack.isEmpty()) {
            stack = stack.copy();
            if (ConfigObject.getInstance().getRecipeKeybind().matchesKey(keyCode, scanCode)) {
                return ClientHelper.getInstance().openView(ViewSearchBuilder.builder().addRecipesFor(stack).setOutputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getUsageKeybind().matchesKey(keyCode, scanCode)) {
                return ClientHelper.getInstance().openView(ViewSearchBuilder.builder().addUsagesFor(stack).setInputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(keyCode, scanCode)) {
                FavoriteEntry favoriteEntry = FavoriteEntry.fromEntryStack(stack);
                if (!ConfigObject.getInstance().getFavoriteEntries().contains(favoriteEntry))
                    ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                ConfigManager.getInstance().saveConfig();
                FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
                if (favoritesListWidget != null)
                    favoritesListWidget.updateSearch(ContainerScreenOverlay.getEntryListWidget(), REIHelperImpl.getSearchField().getText());
                return true;
            }
        }
        if (!REIHelper.getInstance().isOverlayVisible())
            return false;
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesKey(keyCode, scanCode)) {
            REIHelperImpl.getSearchField().setFocused(true);
            setFocused(REIHelperImpl.getSearchField());
            REIHelperImpl.getSearchField().keybindFocusTime = System.currentTimeMillis();
            REIHelperImpl.getSearchField().keybindFocusKey = keyCode;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!REIHelper.getInstance().isOverlayVisible())
            return false;
        if (REIHelperImpl.getSearchField().charTyped(char_1, int_1))
            return true;
        for (GuiEventListener listener : widgets)
            if (listener != REIHelperImpl.getSearchField() && listener.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ConfigObject.getInstance().getHideKeybind().matchesMouse(button)) {
            REIHelper.getInstance().toggleOverlayVisible();
            return true;
        }
        EntryStack<?> stack = ScreenRegistry.getInstance().getFocusedStack(Minecraft.getInstance().screen, PointHelper.ofMouse());
        if (stack != null && !stack.isEmpty()) {
            stack = stack.copy();
            if (ConfigObject.getInstance().getRecipeKeybind().matchesMouse(button)) {
                return ClientHelper.getInstance().openView(ViewSearchBuilder.builder().addRecipesFor(stack).setOutputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getUsageKeybind().matchesMouse(button)) {
                return ClientHelper.getInstance().openView(ViewSearchBuilder.builder().addUsagesFor(stack).setInputNotice(stack).fillPreferredOpenedCategory());
            } else if (ConfigObject.getInstance().getFavoriteKeyCode().matchesMouse(button)) {
                FavoriteEntry favoriteEntry = FavoriteEntry.fromEntryStack(stack);
                if (!ConfigObject.getInstance().getFavoriteEntries().contains(favoriteEntry))
                    ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                ConfigManager.getInstance().saveConfig();
                FavoritesListWidget favoritesListWidget = ContainerScreenOverlay.getFavoritesListWidget();
                if (favoritesListWidget != null)
                    favoritesListWidget.updateSearch(ContainerScreenOverlay.getEntryListWidget(), REIHelper.getInstance().getSearchTextField().getText());
                return true;
            }
        }
        if (!REIHelper.getInstance().isOverlayVisible())
            return false;
        if (overlayMenu != null) {
            if (overlayMenu.wrappedMenu.mouseClicked(mouseX, mouseY, button)) {
                if (overlayMenu != null) this.setFocused(overlayMenu.wrappedMenu);
                else this.setFocused(null);
                if (button == 0)
                    this.setDragging(true);
                REIHelperImpl.getSearchField().setFocused(false);
                return true;
            } else if (!overlayMenu.inBounds.test(new Point(mouseX, mouseY))) {
                closeOverlayMenu();
            }
        }
        if (ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            Screen screen = Minecraft.getInstance().screen;
            ClickArea.ClickAreaContext<Screen> context = new ClickArea.ClickAreaContext<Screen>() {
                @Override
                public Screen getScreen() {
                    return screen;
                }
                
                @Override
                public Point getMousePosition() {
                    return new Point(mouseX, mouseY);
                }
            };
            Set<ResourceLocation> categories = ScreenRegistry.getInstance().handleClickArea((Class<Screen>) screen.getClass(), context);
            if (categories != null && !categories.isEmpty()) {
                ClientHelper.getInstance().openView(ViewSearchBuilder.builder().addCategories(categories).fillPreferredOpenedCategory());
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        for (GuiEventListener element : widgets)
            if ((overlayMenu == null || element != overlayMenu.wrappedMenu) && element.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(element);
                if (button == 0)
                    this.setDragging(true);
                if (!(element instanceof OverlaySearchField))
                    REIHelperImpl.getSearchField().setFocused(false);
                return true;
            }
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesMouse(button)) {
            REIHelperImpl.getSearchField().setFocused(true);
            setFocused(REIHelperImpl.getSearchField());
            REIHelperImpl.getSearchField().keybindFocusTime = -1;
            REIHelperImpl.getSearchField().keybindFocusKey = -1;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (!REIHelper.getInstance().isOverlayVisible())
            return false;
        return (this.getFocused() != null && this.isDragging() && int_1 == 0) && this.getFocused().mouseDragged(double_1, double_2, int_1, double_3, double_4);
    }
    
    public boolean isInside(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY) && isNotInExclusionZones(mouseX, mouseY);
    }
    
    @Override
    public boolean isNotInExclusionZones(double mouseX, double mouseY) {
        for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(Minecraft.getInstance().screen)) {
            InteractionResult in = decider.isInZone(mouseX, mouseY);
            if (in != InteractionResult.PASS)
                return in == InteractionResult.SUCCESS;
        }
        return true;
    }
    
    public boolean isInside(Point point) {
        return isInside(point.getX(), point.getY());
    }
    
}
