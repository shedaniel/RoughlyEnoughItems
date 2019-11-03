/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.impl.RecipeHelperImpl;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.Weather;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.LayeredVertexConsumerStorage;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.*;

public class ContainerScreenOverlay extends Widget {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final List<QueuedTooltip> QUEUED_TOOLTIPS = Lists.newArrayList();
    public static String searchTerm = "";
    private static int page = 0;
    private static EntryListWidget entryListWidget;
    private final List<Widget> widgets = Lists.newLinkedList();
    public boolean shouldReInit = false;
    private int tooltipWidth;
    private int tooltipHeight;
    private List<String> tooltipLines;
    public final TriConsumer<Integer, Integer, Float> renderTooltipCallback = (x, y, aFloat) -> {
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        setBlitOffset(1000);
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
        LayeredVertexConsumerStorage.Drawer drawer = LayeredVertexConsumerStorage.makeDrawer(Tessellator.getInstance().getBufferBuilder());
        matrixStack_1.translate(0.0D, 0.0D, getBlitOffset());
        Matrix4f matrix4f_1 = matrixStack_1.peek();
        for (int lineIndex = 0; lineIndex < tooltipLines.size(); lineIndex++) {
            font.method_22942(tooltipLines.get(lineIndex), (float) x, (float) currentY, -1, true, matrix4f_1, drawer, false, 0, 15728880);
            currentY += lineIndex == 0 ? 12 : 10;
        }
        drawer.draw();
        setBlitOffset(0);
        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
    };
    private Rectangle rectangle;
    private Window window;
    private CraftableToggleButtonWidget toggleButtonWidget;
    private ButtonWidget buttonLeft, buttonRight;
    
    public static EntryListWidget getEntryListWidget() {
        return entryListWidget;
    }
    
    public static void setPage(int page) {
        ContainerScreenOverlay.page = page;
    }
    
    public void init() {
        init(false);
    }
    
    public void init(boolean setPage) {
        this.shouldReInit = false;
        //Update Variables
        this.children().clear();
        this.window = MinecraftClient.getInstance().getWindow();
        DisplayHelper.DisplayBoundsHandler boundsHandler = RoughlyEnoughItemsCore.getDisplayHelper().getResponsibleBoundsHandler(MinecraftClient.getInstance().currentScreen.getClass());
        this.rectangle = RoughlyEnoughItemsCore.getConfigManager().getConfig().isLeftHandSidePanel() ? boundsHandler.getLeftBounds(MinecraftClient.getInstance().currentScreen) : boundsHandler.getRightBounds(MinecraftClient.getInstance().currentScreen);
        widgets.add(entryListWidget = new EntryListWidget(page));
        entryListWidget.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
        
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().isEntryListWidgetScrolled()) {
            widgets.add(buttonLeft = new ButtonWidget(rectangle.x, rectangle.y + (RoughlyEnoughItemsCore.getConfigManager().getConfig().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16, new TranslatableText("text.rei.left_arrow")) {
                @Override
                public void onPressed() {
                    page--;
                    if (page < 0)
                        page = getTotalPage();
                    entryListWidget.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
                }
                
                @Override
                public Optional<String> getTooltips() {
                    return Optional.ofNullable(I18n.translate("text.rei.previous_page"));
                }
                
                @Override
                public boolean changeFocus(boolean boolean_1) {
                    return false;
                }
                
                @Override
                public boolean containsMouse(double mouseX, double mouseY) {
                    return isNotInExclusionZones(mouseX, mouseY) && super.containsMouse(mouseX, mouseY);
                }
            });
            widgets.add(buttonRight = new ButtonWidget(rectangle.x + rectangle.width - 18, rectangle.y + (RoughlyEnoughItemsCore.getConfigManager().getConfig().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16, new TranslatableText("text.rei.right_arrow")) {
                @Override
                public void onPressed() {
                    page++;
                    if (page > getTotalPage())
                        page = 0;
                    entryListWidget.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
                }
                
                @Override
                public Optional<String> getTooltips() {
                    return Optional.ofNullable(I18n.translate("text.rei.next_page"));
                }
                
                @Override
                public boolean changeFocus(boolean boolean_1) {
                    return false;
                }
                
                @Override
                public boolean containsMouse(double mouseX, double mouseY) {
                    return isNotInExclusionZones(mouseX, mouseY) && super.containsMouse(mouseX, mouseY);
                }
            });
        }
        
        if (setPage)
            page = MathHelper.clamp(page, 0, getTotalPage());
        
        widgets.add(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().isLeftHandSidePanel() ? window.getScaledWidth() - 30 : 10, 10, 20, 20, "") {
            @Override
            public void onPressed() {
                if (Screen.hasShiftDown()) {
                    ClientHelper.getInstance().setCheating(!ClientHelper.getInstance().isCheating());
                    return;
                }
                RoughlyEnoughItemsCore.getConfigManager().openConfigScreen(ScreenHelper.getLastContainerScreen());
            }
            
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                GuiLighting.disable();
                if (ClientHelper.getInstance().isCheating() && RoughlyEnoughItemsCore.hasOperatorPermission()) {
                    if (RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                        fill(getBounds().x, getBounds().y, getBounds().x + 20, getBounds().y + 20, 721354752);
                    else
                        fill(getBounds().x, getBounds().y, getBounds().x + 20, getBounds().y + 20, 1476440063);
                }
                MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                blit(getBounds().x + 3, getBounds().y + 3, 0, 0, 14, 14);
            }
            
            @Override
            public Optional<String> getTooltips() {
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
                return Optional.ofNullable(tooltips);
            }
            
            @Override
            public boolean changeFocus(boolean boolean_1) {
                return false;
            }
            
            @Override
            public boolean containsMouse(double mouseX, double mouseY) {
                return isNotInExclusionZones(mouseX, mouseY) && super.containsMouse(mouseX, mouseY);
            }
        });
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().doesShowUtilsButtons()) {
            widgets.add(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().isLeftHandSidePanel() ? window.getScaledWidth() - 55 : 35, 10, 20, 20, "") {
                @Override
                public void onPressed() {
                    MinecraftClient.getInstance().player.sendChatMessage(RoughlyEnoughItemsCore.getConfigManager().getConfig().getGamemodeCommand().replaceAll("\\{gamemode}", getNextGameMode(Screen.hasShiftDown()).getName()));
                }
                
                @Override
                public void render(int mouseX, int mouseY, float delta) {
                    text = getGameModeShortText(getCurrentGameMode());
                    super.render(mouseX, mouseY, delta);
                }
                
                @Override
                public Optional<String> getTooltips() {
                    return Optional.ofNullable(I18n.translate("text.rei.gamemode_button.tooltip", getGameModeText(getNextGameMode(Screen.hasShiftDown()))));
                }
                
                @Override
                public boolean changeFocus(boolean boolean_1) {
                    return false;
                }
                
                @Override
                public boolean containsMouse(double mouseX, double mouseY) {
                    return isNotInExclusionZones(mouseX, mouseY) && super.containsMouse(mouseX, mouseY);
                }
            });
            int xxx = RoughlyEnoughItemsCore.getConfigManager().getConfig().isLeftHandSidePanel() ? window.getScaledWidth() - 30 : 10;
            for (Weather weather : Weather.values()) {
                widgets.add(new ButtonWidget(xxx, 35, 20, 20, "") {
                    @Override
                    public void onPressed() {
                        MinecraftClient.getInstance().player.sendChatMessage(RoughlyEnoughItemsCore.getConfigManager().getConfig().getWeatherCommand().replaceAll("\\{weather}", weather.name().toLowerCase(Locale.ROOT)));
                    }
                    
                    @Override
                    public void render(int mouseX, int mouseY, float delta) {
                        super.render(mouseX, mouseY, delta);
                        GuiLighting.disable();
                        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                        blit(getBounds().x + 3, getBounds().y + 3, weather.getId() * 14, 14, 14, 14);
                    }
                    
                    @Override
                    public Optional<String> getTooltips() {
                        return Optional.ofNullable(I18n.translate("text.rei.weather_button.tooltip", I18n.translate(weather.getTranslateKey())));
                    }
                    
                    @Override
                    public boolean changeFocus(boolean boolean_1) {
                        return false;
                    }
                    
                    @Override
                    public boolean containsMouse(double mouseX, double mouseY) {
                        return isNotInExclusionZones(mouseX, mouseY) && super.containsMouse(mouseX, mouseY);
                    }
                });
                xxx += RoughlyEnoughItemsCore.getConfigManager().getConfig().isLeftHandSidePanel() ? -25 : 25;
            }
        }
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().isEntryListWidgetScrolled()) {
            widgets.add(new ClickableLabelWidget(rectangle.x + (rectangle.width / 2), rectangle.y + (RoughlyEnoughItemsCore.getConfigManager().getConfig().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 10, "", getTotalPage() > 0) {
                @Override
                public void render(int mouseX, int mouseY, float delta) {
                    page = MathHelper.clamp(page, 0, getTotalPage());
                    this.text = String.format("%s/%s", page + 1, getTotalPage() + 1);
                    super.render(mouseX, mouseY, delta);
                }
                
                @Override
                public Optional<String> getTooltips() {
                    return Optional.ofNullable(I18n.translate("text.rei.go_back_first_page"));
                }
                
                @Override
                public void onLabelClicked() {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    page = 0;
                    entryListWidget.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
                }
                
                @Override
                public boolean changeFocus(boolean boolean_1) {
                    return false;
                }
            });
            buttonLeft.enabled = buttonRight.enabled = getTotalPage() > 0;
        }
        if (ScreenHelper.searchField == null)
            ScreenHelper.setSearchField(new OverlaySearchField(0, 0, 0, 0));
        ScreenHelper.searchField.getBounds().setBounds(getTextFieldArea());
        this.widgets.add(ScreenHelper.searchField);
        ScreenHelper.searchField.setText(searchTerm);
        ScreenHelper.searchField.setChangedListener(s -> {
            searchTerm = s;
            entryListWidget.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, true);
        });
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().isCraftableFilterEnabled())
            this.widgets.add(toggleButtonWidget = new CraftableToggleButtonWidget(getCraftableToggleArea()) {
                @Override
                public void onPressed() {
                    RoughlyEnoughItemsCore.getConfigManager().toggleCraftableOnly();
                    entryListWidget.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, true);
                }
                
                @Override
                public void lateRender(int mouseX, int mouseY, float delta) {
                    setBlitOffset(300);
                    super.lateRender(mouseX, mouseY, delta);
                }
                
                @Override
                public boolean containsMouse(double mouseX, double mouseY) {
                    return isNotInExclusionZones(mouseX, mouseY) && super.containsMouse(mouseX, mouseY);
                }
            });
        else
            toggleButtonWidget = null;
        this.entryListWidget.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
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
    
    private Rectangle getTextFieldArea() {
        int widthRemoved = RoughlyEnoughItemsCore.getConfigManager().getConfig().isCraftableFilterEnabled() ? 22 : 2;
        SearchFieldLocation searchFieldLocation = RoughlyEnoughItemsCore.getConfigManager().getConfig().getSearchFieldLocation();
        if (searchFieldLocation == SearchFieldLocation.BOTTOM_SIDE)
            return new Rectangle(rectangle.x + 2, window.getScaledHeight() - 22, rectangle.width - 6 - widthRemoved, 18);
        if (searchFieldLocation == SearchFieldLocation.TOP_SIDE)
            return new Rectangle(rectangle.x + 2, 4, rectangle.width - 6 - widthRemoved, 18);
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen) {
            RecipeViewingScreen widget = (RecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
            return new Rectangle(widget.getBounds().x, window.getScaledHeight() - 22, widget.getBounds().width - widthRemoved, 18);
        }
        if (MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen) {
            VillagerRecipeViewingScreen widget = (VillagerRecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
            return new Rectangle(widget.bounds.x, window.getScaledHeight() - 22, widget.bounds.width - widthRemoved, 18);
        }
        return new Rectangle(ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft(), window.getScaledHeight() - 22, ScreenHelper.getLastContainerScreenHooks().rei_getContainerWidth() - widthRemoved, 18);
    }
    
    private Rectangle getCraftableToggleArea() {
        Rectangle searchBoxArea = getTextFieldArea();
        searchBoxArea.setLocation(searchBoxArea.x + searchBoxArea.width + 4, searchBoxArea.y - 1);
        searchBoxArea.setSize(20, 20);
        return searchBoxArea;
    }
    
    private String getCheatModeText() {
        return I18n.translate(String.format("%s%s", "text.rei.", ClientHelper.getInstance().isCheating() ? "cheat" : "nocheat"));
    }
    
    public Rectangle getRectangle() {
        return rectangle;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        List<ItemStack> currentStacks = ClientHelper.getInstance().getInventoryItemsTypes();
        if (RoughlyEnoughItemsCore.getDisplayHelper().getBaseBoundsHandler() != null && RoughlyEnoughItemsCore.getDisplayHelper().getBaseBoundsHandler().shouldRecalculateArea(!RoughlyEnoughItemsCore.getConfigManager().getConfig().isLeftHandSidePanel(), rectangle))
            shouldReInit = true;
        if (shouldReInit)
            init(true);
        else if (RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() && ((currentStacks.size() != ScreenHelper.inventoryStacks.size()) || !hasSameListContent(new LinkedList<>(ScreenHelper.inventoryStacks), currentStacks))) {
            ScreenHelper.inventoryStacks = currentStacks;
            DisplayHelper.DisplayBoundsHandler<?> boundsHandler = RoughlyEnoughItemsCore.getDisplayHelper().getResponsibleBoundsHandler(MinecraftClient.getInstance().currentScreen.getClass());
            entryListWidget.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, true);
        }
        if (OverlaySearchField.isSearching) {
            GuiLighting.disable();
            setBlitOffset(200);
            if (MinecraftClient.getInstance().currentScreen instanceof AbstractContainerScreen) {
                ContainerScreenHooks hooks = (ContainerScreenHooks) MinecraftClient.getInstance().currentScreen;
                int left = hooks.rei_getContainerLeft(), top = hooks.rei_getContainerTop();
                for (Slot slot : ((AbstractContainerScreen<?>) MinecraftClient.getInstance().currentScreen).getContainer().slotList)
                    if (!slot.hasStack() || !entryListWidget.filterEntry(EntryStack.create(slot.getStack()), entryListWidget.getLastSearchArgument()))
                        fillGradient(left + slot.xPosition, top + slot.yPosition, left + slot.xPosition + 16, top + slot.yPosition + 16, -601874400, -601874400);
            }
            setBlitOffset(0);
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        this.renderWidgets(mouseX, mouseY, delta);
        if (MinecraftClient.getInstance().currentScreen instanceof AbstractContainerScreen && RoughlyEnoughItemsCore.getConfigManager().getConfig().areClickableRecipeArrowsEnabled()) {
            ContainerScreenHooks hooks = (ContainerScreenHooks) MinecraftClient.getInstance().currentScreen;
            for (RecipeHelperImpl.ScreenClickArea area : RecipeHelper.getInstance().getScreenClickAreas())
                if (area.getScreenClass().equals(MinecraftClient.getInstance().currentScreen.getClass()))
                    if (area.getRectangle().contains(mouseX - hooks.rei_getContainerLeft(), mouseY - hooks.rei_getContainerTop())) {
                        String collect = CollectionUtils.mapAndJoinToString(area.getCategories(), identifier -> RecipeHelper.getInstance().getCategory(identifier).getCategoryName(), ", ");
                        QUEUED_TOOLTIPS.add(QueuedTooltip.create(I18n.translate("text.rei.view_recipes_for", collect)));
                        break;
                    }
        }
    }
    
    public void lateRender(int mouseX, int mouseY, float delta) {
        if (ScreenHelper.isOverlayVisible()) {
            ScreenHelper.searchField.laterRender(mouseX, mouseY, delta);
            if (toggleButtonWidget != null)
                toggleButtonWidget.lateRender(mouseX, mouseY, delta);
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if (!(currentScreen instanceof RecipeViewingScreen) || !((RecipeViewingScreen) currentScreen).choosePageActivated)
                for (QueuedTooltip queuedTooltip : QUEUED_TOOLTIPS) {
                    if (queuedTooltip != null)
                        renderTooltip(queuedTooltip);
                }
        }
        QUEUED_TOOLTIPS.clear();
    }
    
    @SuppressWarnings("deprecation")
    public void renderTooltip(QueuedTooltip tooltip) {
        if (tooltip.getConsumer() == null)
            renderTooltip(tooltip.getText(), tooltip.getX(), tooltip.getY());
        else
            tooltip.getConsumer().accept(tooltip);
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
    
    public void addTooltip(@Nullable QueuedTooltip queuedTooltip) {
        if (queuedTooltip != null)
            QUEUED_TOOLTIPS.add(queuedTooltip);
    }
    
    public void renderWidgets(int int_1, int int_2, float float_1) {
        if (!ScreenHelper.isOverlayVisible())
            return;
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().isEntryListWidgetScrolled())
            buttonLeft.enabled = buttonRight.enabled = getTotalPage() > 0;
        widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.render(int_1, int_2, float_1);
        });
        GuiLighting.disable();
    }
    
    private int getTotalPage() {
        return entryListWidget.getTotalPage();
    }
    
    @Override
    public boolean mouseScrolled(double i, double j, double amount) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (isInside(PointHelper.fromMouse())) {
            if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().isEntryListWidgetScrolled()) {
                if (amount > 0 && buttonLeft.enabled)
                    buttonLeft.onPressed();
                else if (amount < 0 && buttonRight.enabled)
                    buttonRight.onPressed();
                else
                    return false;
                return true;
            } else {
                return entryListWidget.mouseScrolled(i, j, amount);
            }
        }
        for (Widget widget : widgets)
            if (widget.mouseScrolled(i, j, amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (ScreenHelper.isOverlayVisible())
            for (Element listener : widgets)
                if (listener.keyPressed(int_1, int_2, int_3))
                    return true;
        if (ClientHelper.getInstance().getHideKeyBinding().matchesKey(int_1, int_2)) {
            ScreenHelper.toggleOverlayVisible();
            return true;
        }
        ItemStack itemStack = null;
        if (MinecraftClient.getInstance().currentScreen instanceof AbstractContainerScreen)
            if (ScreenHelper.getLastContainerScreenHooks().rei_getHoveredSlot() != null && !ScreenHelper.getLastContainerScreenHooks().rei_getHoveredSlot().getStack().isEmpty())
                itemStack = ScreenHelper.getLastContainerScreenHooks().rei_getHoveredSlot().getStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            if (ClientHelper.getInstance().getRecipeKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeRecipeKeyBind(itemStack);
            else if (ClientHelper.getInstance().getUsageKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeUsageKeyBind(itemStack);
        }
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (ClientHelper.getInstance().getFocusSearchFieldKeyBinding().matchesKey(int_1, int_2)) {
            ScreenHelper.searchField.setFocused(true);
            setFocused(ScreenHelper.searchField);
            ScreenHelper.searchField.keybindFocusTime = System.currentTimeMillis();
            ScreenHelper.searchField.keybindFocusKey = int_1;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        for (Element listener : widgets)
            if (listener.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Override
    public List<? extends Element> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (MinecraftClient.getInstance().currentScreen instanceof AbstractContainerScreen && RoughlyEnoughItemsCore.getConfigManager().getConfig().areClickableRecipeArrowsEnabled()) {
            ContainerScreenHooks hooks = (ContainerScreenHooks) MinecraftClient.getInstance().currentScreen;
            for (RecipeHelperImpl.ScreenClickArea area : RecipeHelper.getInstance().getScreenClickAreas())
                if (area.getScreenClass().equals(MinecraftClient.getInstance().currentScreen.getClass()))
                    if (area.getRectangle().contains(double_1 - hooks.rei_getContainerLeft(), double_2 - hooks.rei_getContainerTop())) {
                        ClientHelper.getInstance().executeViewAllRecipesFromCategories(Arrays.asList(area.getCategories()));
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
        }
        for (Element element : widgets)
            if (element.mouseClicked(double_1, double_2, int_1)) {
                this.setFocused(element);
                if (int_1 == 0)
                    this.setDragging(true);
                return true;
            }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        return this.getFocused() != null && this.isDragging() && int_1 == 0 ? this.getFocused().mouseDragged(double_1, double_2, int_1, double_3, double_4) : false;
    }
    
    public boolean isInside(double mouseX, double mouseY) {
        if (!rectangle.contains(mouseX, mouseY))
            return false;
        for (DisplayHelper.DisplayBoundsHandler<?> handler : RoughlyEnoughItemsCore.getDisplayHelper().getSortedBoundsHandlers(MinecraftClient.getInstance().currentScreen.getClass())) {
            ActionResult in = handler.isInZone(!RoughlyEnoughItemsCore.getConfigManager().getConfig().isLeftHandSidePanel(), mouseX, mouseY);
            if (in != ActionResult.PASS)
                return in == ActionResult.SUCCESS;
        }
        return true;
    }
    
    public boolean isNotInExclusionZones(double mouseX, double mouseY) {
        for (DisplayHelper.DisplayBoundsHandler<?> handler : RoughlyEnoughItemsCore.getDisplayHelper().getSortedBoundsHandlers(MinecraftClient.getInstance().currentScreen.getClass())) {
            ActionResult in = handler.isInZone(true, mouseX, mouseY);
            if (in != ActionResult.PASS)
                return in == ActionResult.SUCCESS;
            in = handler.isInZone(false, mouseX, mouseY);
            if (in != ActionResult.PASS)
                return in == ActionResult.SUCCESS;
        }
        return true;
    }
    
    public boolean isInside(Point point) {
        return isInside(point.getX(), point.getY());
    }
    
}
