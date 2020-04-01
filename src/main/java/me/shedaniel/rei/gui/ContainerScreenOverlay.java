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

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.widgets.Button;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.gui.subsets.SubsetsMenu;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.impl.ClientHelperImpl;
import me.shedaniel.rei.impl.InternalWidgets;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.Weather;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
public class ContainerScreenOverlay extends WidgetWithBounds {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final List<Tooltip> TOOLTIPS = Lists.newArrayList();
    private static final EntryListWidget ENTRY_LIST_WIDGET = new EntryListWidget();
    private static FavoritesListWidget favoritesListWidget = null;
    private final List<Widget> widgets = Lists.newLinkedList();
    public boolean shouldReInit = false;
    private int tooltipWidth;
    private int tooltipHeight;
    private List<String> tooltipLines;
    public final TriConsumer<Integer, Integer, Float> renderTooltipCallback = (x, y, aFloat) -> {
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        setZOffset(999);
        this.fillGradient(x - 3, y - 4, x + tooltipWidth + 3, y - 3, -267386864, -267386864);
        this.fillGradient(x - 3, y + tooltipHeight + 3, x + tooltipWidth + 3, y + tooltipHeight + 4, -267386864, -267386864);
        this.fillGradient(x - 3, y - 3, x + tooltipWidth + 3, y + tooltipHeight + 3, -267386864, -267386864);
        this.fillGradient(x - 4, y - 3, x - 3, y + tooltipHeight + 3, -267386864, -267386864);
        this.fillGradient(x + tooltipWidth + 3, y - 3, x + tooltipWidth + 4, y + tooltipHeight + 3, -267386864, -267386864);
        this.fillGradient(x - 3, y - 3 + 1, x - 3 + 1, y + tooltipHeight + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(x + tooltipWidth + 2, y - 3 + 1, x + tooltipWidth + 3, y + tooltipHeight + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(x - 3, y - 3, x + tooltipWidth + 3, y - 3 + 1, 1347420415, 1347420415);
        this.fillGradient(x - 3, y + tooltipHeight + 2, x + tooltipWidth + 3, y + tooltipHeight + 3, 1344798847, 1344798847);
        int currentY = y;
        MatrixStack matrixStack_1 = new MatrixStack();
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        matrixStack_1.translate(0.0D, 0.0D, getZOffset());
        Matrix4f matrix4f_1 = matrixStack_1.peek().getModel();
        for (int lineIndex = 0; lineIndex < tooltipLines.size(); lineIndex++) {
            font.draw(tooltipLines.get(lineIndex), x, currentY, -1, true, matrix4f_1, immediate, false, 0, 15728880);
            currentY += lineIndex == 0 ? 12 : 10;
        }
        immediate.draw();
        setZOffset(0);
        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
    };
    private Rectangle bounds;
    private Window window;
    private Button leftButton, rightButton;
    @ApiStatus.Experimental
    private Rectangle subsetsButtonBounds;
    @ApiStatus.Experimental
    @Nullable
    private SubsetsMenu subsetsMenu = null;
    private Widget wrappedSubsetsMenu = null;
    
    public static EntryListWidget getEntryListWidget() {
        return ENTRY_LIST_WIDGET;
    }
    
    @Nullable
    public static FavoritesListWidget getFavoritesListWidget() {
        return favoritesListWidget;
    }
    
    @ApiStatus.Experimental
    @Nullable
    public SubsetsMenu getSubsetsMenu() {
        return subsetsMenu;
    }
    
    public void init(boolean useless) {
        init();
    }
    
    public void init() {
        this.shouldReInit = false;
        //Update Variables
        this.children().clear();
        this.wrappedSubsetsMenu = null;
        this.subsetsMenu = null;
        this.window = MinecraftClient.getInstance().getWindow();
        @SuppressWarnings({"RawTypeCanBeGeneric", "rawtypes"})
        DisplayHelper.DisplayBoundsHandler boundsHandler = DisplayHelper.getInstance().getResponsibleBoundsHandler(MinecraftClient.getInstance().currentScreen.getClass());
        this.bounds = ConfigObject.getInstance().isLeftHandSidePanel() ? boundsHandler.getLeftBounds(MinecraftClient.getInstance().currentScreen) : boundsHandler.getRightBounds(MinecraftClient.getInstance().currentScreen);
        widgets.add(ENTRY_LIST_WIDGET);
        if (ConfigObject.getInstance().doDisplayFavoritesOnTheLeft() && ConfigObject.getInstance().isFavoritesEnabled()) {
            if (favoritesListWidget == null)
                favoritesListWidget = new FavoritesListWidget();
            widgets.add(favoritesListWidget);
        }
        ENTRY_LIST_WIDGET.updateArea(boundsHandler, ScreenHelper.getSearchField() == null ? "" : null);
        if (ScreenHelper.getSearchField() == null) {
            ScreenHelper.setSearchField(new OverlaySearchField(0, 0, 0, 0));
        }
        ScreenHelper.getSearchField().getBounds().setBounds(getSearchFieldArea());
        this.widgets.add(ScreenHelper.getSearchField());
        ScreenHelper.getSearchField().setChangedListener(s -> ENTRY_LIST_WIDGET.updateSearch(s, false));
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(leftButton = Widgets.createButton(new Rectangle(bounds.x, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), new TranslatableText("text.rei.left_arrow"))
                    .onClick(button -> {
                        ENTRY_LIST_WIDGET.previousPage();
                        if (ENTRY_LIST_WIDGET.getPage() < 0)
                            ENTRY_LIST_WIDGET.setPage(ENTRY_LIST_WIDGET.getTotalPages() - 1);
                        ENTRY_LIST_WIDGET.updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(I18n.translate("text.rei.previous_page"))
                    .focusable(false));
            widgets.add(rightButton = Widgets.createButton(new Rectangle(bounds.x + bounds.width - 18, bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), new TranslatableText("text.rei.right_arrow"))
                    .onClick(button -> {
                        ENTRY_LIST_WIDGET.nextPage();
                        if (ENTRY_LIST_WIDGET.getPage() >= ENTRY_LIST_WIDGET.getTotalPages())
                            ENTRY_LIST_WIDGET.setPage(0);
                        ENTRY_LIST_WIDGET.updateEntriesPosition();
                    })
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                    .tooltipLine(I18n.translate("text.rei.next_page"))
                    .focusable(false));
        }
        
        final Rectangle configButtonArea = getConfigButtonArea();
        Widget tmp;
        widgets.add(tmp = InternalWidgets.wrapLateRenderable(InternalWidgets.mergeWidgets(
                Widgets.createButton(configButtonArea, NarratorManager.EMPTY)
                        .onClick(button -> {
                            if (Screen.hasShiftDown()) {
                                ClientHelper.getInstance().setCheating(!ClientHelper.getInstance().isCheating());
                                return;
                            }
                            ConfigManager.getInstance().openConfigScreen(ScreenHelper.getLastHandledScreen());
                        })
                        .onRender(button -> {
                            if (ClientHelper.getInstance().isCheating() && RoughlyEnoughItemsCore.hasOperatorPermission()) {
                                button.setTint(RoughlyEnoughItemsCore.hasPermissionToUsePackets() ? 721354752 : 1476440063);
                            } else {
                                button.removeTint();
                            }
                        })
                        .focusable(false)
                        .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                        .tooltipSupplier(button -> {
                            String tooltips = I18n.translate("text.rei.config_tooltip");
                            tooltips += "\n  ";
                            if (!ClientHelper.getInstance().isCheating())
                                tooltips += "\n" + I18n.translate("text.rei.cheating_disabled");
                            else if (!RoughlyEnoughItemsCore.hasOperatorPermission())
                                tooltips += "\n" + I18n.translate("text.rei.cheating_enabled_no_perms");
                            else if (RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                                tooltips += "\n" + I18n.translate("text.rei.cheating_enabled");
                            else
                                tooltips += "\n" + I18n.translate("text.rei.cheating_limited_enabled");
                            return tooltips;
                        }),
                Widgets.createDrawableWidget((helper, mouseX, mouseY, delta) -> {
                    helper.setZOffset(helper.getZOffset() + 1);
                    MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                    helper.drawTexture(configButtonArea.x + 3, configButtonArea.y + 3, 0, 0, 14, 14);
                })
                )
        ));
        tmp.setZ(600);
        if (ConfigObject.getInstance().doesShowUtilsButtons()) {
            widgets.add(Widgets.createButton(ConfigObject.getInstance().isLowerConfigButton() ? new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getScaledWidth() - 30 : 10, 10, 20, 20) : new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getScaledWidth() - 55 : 35, 10, 20, 20), NarratorManager.EMPTY)
                    .onClick(button -> MinecraftClient.getInstance().player.sendChatMessage(ConfigObject.getInstance().getGamemodeCommand().replaceAll("\\{gamemode}", getNextGameMode(Screen.hasShiftDown()).getName())))
                    .onRender(button -> button.setText(getGameModeShortText(getCurrentGameMode())))
                    .focusable(false)
                    .tooltipLine(I18n.translate("text.rei.gamemode_button.tooltip", getGameModeText(getNextGameMode(Screen.hasShiftDown()))))
                    .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y)));
            int xxx = ConfigObject.getInstance().isLeftHandSidePanel() ? window.getScaledWidth() - 30 : 10;
            for (Weather weather : Weather.values()) {
                Button weatherButton;
                widgets.add(weatherButton = Widgets.createButton(new Rectangle(xxx, 35, 20, 20), NarratorManager.EMPTY)
                        .onClick(button -> MinecraftClient.getInstance().player.sendChatMessage(ConfigObject.getInstance().getWeatherCommand().replaceAll("\\{weather}", weather.name().toLowerCase(Locale.ROOT))))
                        .tooltipLine(I18n.translate("text.rei.weather_button.tooltip", I18n.translate(weather.getTranslateKey())))
                        .focusable(false)
                        .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y)));
                widgets.add(Widgets.createDrawableWidget((helper, mouseX, mouseY, delta) -> {
                    MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    helper.drawTexture(weatherButton.getBounds().x + 3, weatherButton.getBounds().y + 3, weather.getId() * 14, 14, 14, 14);
                }));
                xxx += ConfigObject.getInstance().isLeftHandSidePanel() ? -25 : 25;
            }
        }
        subsetsButtonBounds = getSubsetsButtonBounds();
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            widgets.add(InternalWidgets.wrapLateRenderable(Widgets.createButton(subsetsButtonBounds, ((ClientHelperImpl) ClientHelper.getInstance()).isAprilFools.get() ? I18n.translate("text.rei.tiny_potato") : I18n.translate("text.rei.subsets"))
                    .onClick(button -> {
                        if (subsetsMenu == null) {
                            wrappedSubsetsMenu = InternalWidgets.wrapTranslate(InternalWidgets.wrapLateRenderable(this.subsetsMenu = SubsetsMenu.createFromRegistry(new Point(this.subsetsButtonBounds.x, this.subsetsButtonBounds.getMaxY()))), 0, 0, 400);
                            this.widgets.add(this.wrappedSubsetsMenu);
                        } else {
                            this.widgets.remove(this.wrappedSubsetsMenu);
                            this.subsetsMenu = null;
                            this.wrappedSubsetsMenu = null;
                        }
                    })));
        }
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            widgets.add(Widgets.createClickableLabel(new Point(bounds.x + (bounds.width / 2), bounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 10), "", label -> {
                ENTRY_LIST_WIDGET.setPage(0);
                ENTRY_LIST_WIDGET.updateEntriesPosition();
            }).tooltipLine(I18n.translate("text.rei.go_back_first_page")).focusable(false).onRender(label -> {
                label.setClickable(ENTRY_LIST_WIDGET.getTotalPages() > 1);
                label.setText(String.format("%s/%s", ENTRY_LIST_WIDGET.getPage() + 1, Math.max(ENTRY_LIST_WIDGET.getTotalPages(), 1)));
            }));
        }
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) {
            Rectangle area = getCraftableToggleArea();
            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            ItemStack icon = new ItemStack(Blocks.CRAFTING_TABLE);
            this.widgets.add(tmp = InternalWidgets.wrapLateRenderable(InternalWidgets.mergeWidgets(
                    Widgets.createButton(area, NarratorManager.EMPTY)
                            .focusable(false)
                            .onClick(button -> {
                                ConfigManager.getInstance().toggleCraftableOnly();
                                ENTRY_LIST_WIDGET.updateSearch(ScreenHelper.getSearchField().getText(), true);
                            })
                            .onRender(button -> button.setTint(ConfigManager.getInstance().isCraftableOnlyEnabled() ? 939579655 : 956235776))
                            .containsMousePredicate((button, point) -> button.getBounds().contains(point) && isNotInExclusionZones(point.x, point.y))
                            .tooltipSupplier(button -> I18n.translate(ConfigManager.getInstance().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all")),
                    Widgets.createDrawableWidget((helper, mouseX, mouseY, delta) -> {
                        itemRenderer.zOffset = helper.getZOffset();
                        itemRenderer.renderGuiItemIcon(icon, area.x + 2, area.y + 2);
                        itemRenderer.zOffset = 0.0F;
                    }))
            ));
            tmp.setZ(600);
        }
    }
    
    @ApiStatus.Experimental
    private Rectangle getSubsetsButtonBounds() {
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen) {
                RecipeViewingScreen widget = (RecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
                return new Rectangle(widget.getBounds().x, 3, widget.getBounds().width, 18);
            }
            if (MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen) {
                VillagerRecipeViewingScreen widget = (VillagerRecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
                return new Rectangle(widget.bounds.x, 3, widget.bounds.width, 18);
            }
            return new Rectangle(((ContainerScreenHooks) ScreenHelper.getLastHandledScreen()).rei_getContainerLeft(), 3, ((ContainerScreenHooks) ScreenHelper.getLastHandledScreen()).rei_getContainerWidth(), 18);
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
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world.isThundering())
            return Weather.THUNDER;
        if (world.getLevelProperties().isRaining())
            return Weather.RAIN;
        return Weather.CLEAR;
    }
    
    private String getGameModeShortText(GameMode gameMode) {
        return I18n.translate("text.rei.short_gamemode." + gameMode.getName());
    }
    
    private String getGameModeText(GameMode gameMode) {
        return I18n.translate("selectWorld.gameMode." + gameMode.getName());
    }
    
    private GameMode getNextGameMode(boolean reverse) {
        try {
            GameMode current = getCurrentGameMode();
            int next = current.getId() + 1;
            if (reverse)
                next -= 2;
            if (next > 3)
                next = 0;
            if (next < 0)
                next = 3;
            return GameMode.byId(next);
        } catch (Exception e) {
            return GameMode.NOT_SET;
        }
    }
    
    private GameMode getCurrentGameMode() {
        return MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(MinecraftClient.getInstance().player.getGameProfile().getId()).getGameMode();
    }
    
    private Rectangle getSearchFieldArea() {
        int widthRemoved = 1 + (ConfigObject.getInstance().isCraftableFilterEnabled() ? 22 : 0) + (ConfigObject.getInstance().isLowerConfigButton() ? 22 : 0);
        SearchFieldLocation searchFieldLocation = ConfigObject.getInstance().getSearchFieldLocation();
        if (searchFieldLocation == SearchFieldLocation.BOTTOM_SIDE)
            return new Rectangle(bounds.x + 2, window.getScaledHeight() - 22, bounds.width - 6 - widthRemoved, 18);
        if (searchFieldLocation == SearchFieldLocation.TOP_SIDE)
            return new Rectangle(bounds.x + 2, 4, bounds.width - 6 - widthRemoved, 18);
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen) {
            RecipeViewingScreen widget = (RecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
            return new Rectangle(widget.getBounds().x, window.getScaledHeight() - 22, widget.getBounds().width - widthRemoved, 18);
        }
        if (MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen) {
            VillagerRecipeViewingScreen widget = (VillagerRecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
            return new Rectangle(widget.bounds.x, window.getScaledHeight() - 22, widget.bounds.width - widthRemoved, 18);
        }
        return new Rectangle(((ContainerScreenHooks) ScreenHelper.getLastHandledScreen()).rei_getContainerLeft(), window.getScaledHeight() - 22, ((ContainerScreenHooks) ScreenHelper.getLastHandledScreen()).rei_getContainerWidth() - widthRemoved, 18);
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
        return new Rectangle(ConfigObject.getInstance().isLeftHandSidePanel() ? window.getScaledWidth() - 30 : 10, 10, 20, 20);
    }
    
    private String getCheatModeText() {
        return I18n.translate(String.format("%s%s", "text.rei.", ClientHelper.getInstance().isCheating() ? "cheat" : "nocheat"));
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        List<ItemStack> currentStacks = ClientHelper.getInstance().getInventoryItemsTypes();
        if (shouldReInit) {
            ENTRY_LIST_WIDGET.updateSearch(ScreenHelper.getSearchField().getText(), true);
            init();
        } else {
            for (DisplayHelper.DisplayBoundsHandler<?> handler : DisplayHelper.getInstance().getSortedBoundsHandlers(minecraft.currentScreen.getClass())) {
                if (handler != null && handler.shouldRecalculateArea(!ConfigObject.getInstance().isLeftHandSidePanel(), bounds)) {
                    init();
                    break;
                }
            }
        }
        if (ConfigManager.getInstance().isCraftableOnlyEnabled() && ((currentStacks.size() != ScreenHelper.inventoryStacks.size()) || !hasSameListContent(new LinkedList<>(ScreenHelper.inventoryStacks), currentStacks))) {
            ScreenHelper.inventoryStacks = currentStacks;
            ENTRY_LIST_WIDGET.updateSearch(ScreenHelper.getSearchField().getText(), true);
        }
        if (OverlaySearchField.isSearching) {
            setZOffset(200);
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen) {
                ContainerScreenHooks hooks = (ContainerScreenHooks) MinecraftClient.getInstance().currentScreen;
                int left = hooks.rei_getContainerLeft(), top = hooks.rei_getContainerTop();
                for (Slot slot : ((HandledScreen<?>) MinecraftClient.getInstance().currentScreen).getScreenHandler().slots)
                    if (!slot.hasStack() || !ENTRY_LIST_WIDGET.canLastSearchTermsBeAppliedTo(EntryStack.create(slot.getStack())))
                        fillGradient(left + slot.x, top + slot.y, left + slot.x + 16, top + slot.y + 16, -601874400, -601874400);
            }
            setZOffset(0);
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderWidgets(mouseX, mouseY, delta);
        if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen && ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            ContainerScreenHooks hooks = (ContainerScreenHooks) MinecraftClient.getInstance().currentScreen;
            for (RecipeHelper.ScreenClickArea area : RecipeHelper.getInstance().getScreenClickAreas())
                if (area.getScreenClass().equals(MinecraftClient.getInstance().currentScreen.getClass()))
                    if (area.getRectangle().contains(mouseX - hooks.rei_getContainerLeft(), mouseY - hooks.rei_getContainerTop())) {
                        String collect = CollectionUtils.mapAndJoinToString(area.getCategories(), identifier -> RecipeHelper.getInstance().getCategory(identifier).getCategoryName(), ", ");
                        TOOLTIPS.add(Tooltip.create(I18n.translate("text.rei.view_recipes_for", collect)));
                        break;
                    }
        }
    }
    
    public void lateRender(int mouseX, int mouseY, float delta) {
        if (ScreenHelper.isOverlayVisible()) {
            ScreenHelper.getSearchField().laterRender(mouseX, mouseY, delta);
            for (Widget widget : widgets) {
                if (widget instanceof LateRenderable && wrappedSubsetsMenu != widget)
                    widget.render(mouseX, mouseY, delta);
            }
        }
        if (wrappedSubsetsMenu != null) {
            TOOLTIPS.clear();
            wrappedSubsetsMenu.render(mouseX, mouseY, delta);
        }
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (!(currentScreen instanceof RecipeViewingScreen) || !((RecipeViewingScreen) currentScreen).choosePageActivated)
            for (Tooltip tooltip : TOOLTIPS) {
                if (tooltip != null)
                    renderTooltip(tooltip);
            }
        TOOLTIPS.clear();
    }
    
    public void renderTooltip(Tooltip tooltip) {
        renderTooltip(tooltip.getText(), tooltip.getX(), tooltip.getY());
    }
    
    public void renderTooltip(List<String> lines, int mouseX, int mouseY) {
        if (lines.isEmpty())
            return;
        tooltipWidth = lines.stream().map(font::getStringWidth).max(Integer::compareTo).get();
        tooltipHeight = lines.size() <= 1 ? 8 : lines.size() * 10;
        tooltipLines = lines;
        ScreenHelper.drawHoveringWidget(mouseX, mouseY, renderTooltipCallback, tooltipWidth, tooltipHeight, 0);
    }
    
    private boolean hasSameListContent(List<ItemStack> list1, List<ItemStack> list2) {
        list1.sort(Comparator.comparing(Object::toString));
        list2.sort(Comparator.comparing(Object::toString));
        return CollectionUtils.mapAndJoinToString(list1, Object::toString, "").equals(CollectionUtils.mapAndJoinToString(list2, Object::toString, ""));
    }
    
    public void addTooltip(@Nullable Tooltip tooltip) {
        if (tooltip != null)
            TOOLTIPS.add(tooltip);
    }
    
    public void renderWidgets(int int_1, int int_2, float float_1) {
        if (!ScreenHelper.isOverlayVisible())
            return;
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            leftButton.setEnabled(ENTRY_LIST_WIDGET.getTotalPages() > 1);
            rightButton.setEnabled(ENTRY_LIST_WIDGET.getTotalPages() > 1);
        }
        for (Widget widget : widgets) {
            if (!(widget instanceof LateRenderable))
                widget.render(int_1, int_2, float_1);
        }
    }
    
    @Override
    public boolean mouseScrolled(double i, double j, double amount) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (wrappedSubsetsMenu != null && wrappedSubsetsMenu.mouseScrolled(i, j, amount))
            return true;
        if (isInside(PointHelper.ofMouse())) {
            if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) {
                if (amount > 0 && leftButton.isEnabled())
                    leftButton.onClick();
                else if (amount < 0 && rightButton.isEnabled())
                    rightButton.onClick();
                else
                    return false;
                return true;
            } else if (ENTRY_LIST_WIDGET.mouseScrolled(i, j, amount))
                return true;
        }
        if (isNotInExclusionZones(PointHelper.getMouseX(), PointHelper.getMouseY())) {
            if (favoritesListWidget != null && favoritesListWidget.mouseScrolled(i, j, amount))
                return true;
        }
        for (Widget widget : widgets)
            if (widget != ENTRY_LIST_WIDGET && (favoritesListWidget == null || widget != favoritesListWidget) && (wrappedSubsetsMenu == null || widget != wrappedSubsetsMenu) && widget.mouseScrolled(i, j, amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (ScreenHelper.isOverlayVisible()) {
            if (ScreenHelper.getSearchField().keyPressed(int_1, int_2, int_3))
                return true;
            for (Element listener : widgets)
                if (listener != ScreenHelper.getSearchField() && listener.keyPressed(int_1, int_2, int_3))
                    return true;
        }
        if (ConfigObject.getInstance().getHideKeybind().matchesKey(int_1, int_2)) {
            ScreenHelper.toggleOverlayVisible();
            return true;
        }
        ItemStack itemStack = null;
        if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen)
            if (((ContainerScreenHooks) ScreenHelper.getLastHandledScreen()).rei_getHoveredSlot() != null && !((ContainerScreenHooks) ScreenHelper.getLastHandledScreen()).rei_getHoveredSlot().getStack().isEmpty())
                itemStack = ((ContainerScreenHooks) ScreenHelper.getLastHandledScreen()).rei_getHoveredSlot().getStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            if (ConfigObject.getInstance().getRecipeKeybind().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeRecipeKeyBind(itemStack);
            else if (ConfigObject.getInstance().getUsageKeybind().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeUsageKeyBind(itemStack);
        }
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesKey(int_1, int_2)) {
            ScreenHelper.getSearchField().setFocused(true);
            setFocused(ScreenHelper.getSearchField());
            ScreenHelper.getSearchField().keybindFocusTime = System.currentTimeMillis();
            ScreenHelper.getSearchField().keybindFocusKey = int_1;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (ScreenHelper.getSearchField().charTyped(char_1, int_1))
            return true;
        for (Element listener : widgets)
            if (listener != ScreenHelper.getSearchField() && listener.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (wrappedSubsetsMenu != null && wrappedSubsetsMenu.mouseClicked(double_1, double_2, int_1)) {
            this.setFocused(wrappedSubsetsMenu);
            if (int_1 == 0)
                this.setDragging(true);
            ScreenHelper.getSearchField().setFocused(false);
            return true;
        }
        if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen && ConfigObject.getInstance().areClickableRecipeArrowsEnabled()) {
            ContainerScreenHooks hooks = (ContainerScreenHooks) MinecraftClient.getInstance().currentScreen;
            for (RecipeHelper.ScreenClickArea area : RecipeHelper.getInstance().getScreenClickAreas())
                if (area.getScreenClass().equals(MinecraftClient.getInstance().currentScreen.getClass()))
                    if (area.getRectangle().contains(double_1 - hooks.rei_getContainerLeft(), double_2 - hooks.rei_getContainerTop())) {
                        ClientHelper.getInstance().executeViewAllRecipesFromCategories(Arrays.asList(area.getCategories()));
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
        }
        for (Element element : widgets)
            if (element != wrappedSubsetsMenu && element.mouseClicked(double_1, double_2, int_1)) {
                this.setFocused(element);
                if (int_1 == 0)
                    this.setDragging(true);
                if (!(element instanceof OverlaySearchField))
                    ScreenHelper.getSearchField().setFocused(false);
                return true;
            }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        return (this.getFocused() != null && this.isDragging() && int_1 == 0) && this.getFocused().mouseDragged(double_1, double_2, int_1, double_3, double_4);
    }
    
    public boolean isInside(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY) && isNotInExclusionZones(mouseX, mouseY);
    }
    
    public boolean isNotInExclusionZones(double mouseX, double mouseY) {
        for (DisplayHelper.DisplayBoundsHandler<?> handler : DisplayHelper.getInstance().getSortedBoundsHandlers(MinecraftClient.getInstance().currentScreen.getClass())) {
            ActionResult in = handler.isInZone(mouseX, mouseY);
            if (in != ActionResult.PASS)
                return in == ActionResult.SUCCESS;
        }
        return true;
    }
    
    public boolean isInside(Point point) {
        return isInside(point.getX(), point.getY());
    }
    
}
