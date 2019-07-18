/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsClient;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.client.RecipeHelperImpl;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.client.Weather;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.api.ContainerScreenHooks;
import me.shedaniel.rei.utils.ClothScreenRegistry;
import me.shedaniel.reiclothconfig2.api.MouseUtils;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ContainerScreenOverlay extends Widget {
    
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final List<QueuedTooltip> QUEUED_TOOLTIPS = Lists.newArrayList();
    public static String searchTerm = "";
    private static int page = 0;
    private static ItemListOverlay itemListOverlay;
    private final List<Widget> widgets = Lists.newLinkedList();
    public boolean shouldReInit = false;
    private Rectangle rectangle;
    private MainWindow window;
    private CraftableToggleButtonWidget toggleButtonWidget;
    private ButtonWidget buttonLeft, buttonRight;
    
    public static ItemListOverlay getItemListOverlay() {
        return itemListOverlay;
    }
    
    public void init() {
        init(false);
    }
    
    public void init(boolean setPage) {
        this.shouldReInit = false;
        //Update Variables
        this.getChildren().clear();
        this.window = Minecraft.getInstance().mainWindow;
        DisplayHelper.DisplayBoundsHandler boundsHandler = RoughlyEnoughItemsCore.getDisplayHelper().getResponsibleBoundsHandler(Minecraft.getInstance().currentScreen.getClass());
        this.rectangle = RoughlyEnoughItemsClient.getConfigManager().getConfig().mirrorItemPanel ? boundsHandler.getLeftBounds(Minecraft.getInstance().currentScreen) : boundsHandler.getRightBounds(Minecraft.getInstance().currentScreen);
        widgets.add(itemListOverlay = new ItemListOverlay(page));
        itemListOverlay.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
        
        widgets.add(buttonLeft = new ButtonWidget(rectangle.x, rectangle.y + 5, 16, 16, new TextComponentTranslation("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                page--;
                if (page < 0)
                    page = getTotalPage();
                itemListOverlay.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.format("text.rei.previous_page"));
            }
        });
        widgets.add(buttonRight = new ButtonWidget(rectangle.x + rectangle.width - 18, rectangle.y + 5, 16, 16, new TextComponentTranslation("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                page++;
                if (page > getTotalPage())
                    page = 0;
                itemListOverlay.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.format("text.rei.next_page"));
            }
        });
        
        if (setPage)
            page = MathHelper.clamp(page, 0, getTotalPage());
        
        widgets.add(new ButtonWidget(RoughlyEnoughItemsClient.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 30 : 10, 10, 20, 20, "") {
            @Override
            public void onPressed() {
                if (GuiScreen.isShiftKeyDown()) {
                    ClientHelper.getInstance().setCheating(!ClientHelper.getInstance().isCheating());
                    return;
                }
                ClothScreenRegistry.openConfigScreen(ScreenHelper.getLastContainerScreen());
            }
            
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                super.render(mouseX, mouseY, delta);
                RenderHelper.disableStandardItemLighting();
                if (ClientHelper.getInstance().isCheating() && Minecraft.getInstance().isSingleplayer())
                    drawRect(getBounds().x, getBounds().y, getBounds().x + 20, getBounds().y + 20, 721354752);
                Minecraft.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                drawTexturedModalRect(getBounds().x + 3, getBounds().y + 3, 0, 0, 14, 14);
            }
            
            @Override
            public Optional<String> getTooltips() {
                String tooltips = I18n.format("text.rei.config_tooltip");
                tooltips += "\n  ";
                if (!ClientHelper.getInstance().isCheating())
                    tooltips += "\n" + I18n.format("text.rei.cheating_disabled");
                else if (Minecraft.getInstance().isSingleplayer())
                    tooltips += "\n" + I18n.format("text.rei.cheating_enabled");
                else
                    tooltips += "\n" + I18n.format("text.rei.cheating_limited_enabled");
                return Optional.ofNullable(tooltips);
            }
        });
        if (RoughlyEnoughItemsClient.getConfigManager().getConfig().showUtilsButtons) {
            widgets.add(new ButtonWidget(RoughlyEnoughItemsClient.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 55 : 35, 10, 20, 20, "") {
                @Override
                public void onPressed() {
                    Minecraft.getInstance().player.sendChatMessage(RoughlyEnoughItemsClient.getConfigManager().getConfig().gamemodeCommand.replaceAll("\\{gamemode}", getNextGameMode().getName()));
                }
                
                @Override
                public void render(int mouseX, int mouseY, float delta) {
                    text = getGameModeShortText(getCurrentGameMode());
                    super.render(mouseX, mouseY, delta);
                }
                
                @Override
                public Optional<String> getTooltips() {
                    return Optional.ofNullable(I18n.format("text.rei.gamemode_button.tooltip", getGameModeText(getNextGameMode())));
                }
            });
            int xxx = RoughlyEnoughItemsClient.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 30 : 10;
            for(Weather weather : Weather.values()) {
                widgets.add(new ButtonWidget(xxx, 35, 20, 20, "") {
                    @Override
                    public void onPressed() {
                        Minecraft.getInstance().player.sendChatMessage(RoughlyEnoughItemsClient.getConfigManager().getConfig().weatherCommand.replaceAll("\\{weather}", weather.name().toLowerCase(Locale.ROOT)));
                    }
                    
                    @Override
                    public void render(int mouseX, int mouseY, float delta) {
                        super.render(mouseX, mouseY, delta);
                        RenderHelper.disableStandardItemLighting();
                        Minecraft.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                        drawTexturedModalRect(getBounds().x + 3, getBounds().y + 3, weather.getId() * 14, 14, 14, 14);
                    }
                    
                    @Override
                    public Optional<String> getTooltips() {
                        return Optional.ofNullable(I18n.format("text.rei.weather_button.tooltip", I18n.format(weather.getTranslateKey())));
                    }
                });
                xxx += RoughlyEnoughItemsClient.getConfigManager().getConfig().mirrorItemPanel ? -25 : 25;
            }
        }
        widgets.add(new ClickableLabelWidget(rectangle.x + (rectangle.width / 2), rectangle.y + 10, "", getTotalPage() > 0) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                page = MathHelper.clamp(page, 0, getTotalPage());
                this.text = String.format("%s/%s", page + 1, getTotalPage() + 1);
                super.render(mouseX, mouseY, delta);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.format("text.rei.go_back_first_page"));
            }
            
            @Override
            public void onLabelClicked() {
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                page = 0;
                itemListOverlay.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
            }
        });
        buttonLeft.enabled = buttonRight.enabled = getTotalPage() > 0;
        if (ScreenHelper.searchField == null)
            ScreenHelper.searchField = new SearchFieldWidget(0, 0, 0, 0);
        ScreenHelper.searchField.getBounds().setBounds(getTextFieldArea());
        this.widgets.add(ScreenHelper.searchField);
        ScreenHelper.searchField.setText(searchTerm);
        ScreenHelper.searchField.setChangedListener(s -> {
            searchTerm = s;
            itemListOverlay.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, true);
        });
        if (RoughlyEnoughItemsClient.getConfigManager().getConfig().enableCraftableOnlyButton)
            this.widgets.add(toggleButtonWidget = new CraftableToggleButtonWidget(getCraftableToggleArea()) {
                @Override
                public void onPressed() {
                    RoughlyEnoughItemsClient.getConfigManager().toggleCraftableOnly();
                    itemListOverlay.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, true);
                }
                
                @Override
                public void lateRender(int mouseX, int mouseY, float delta) {
                    zLevel = 300;
                    super.lateRender(mouseX, mouseY, delta);
                }
            });
        else
            toggleButtonWidget = null;
        this.itemListOverlay.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, false);
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
        WorldClient world = Minecraft.getInstance().world;
        if (world.isThundering())
            return Weather.THUNDER;
        if (world.getWorldInfo().isRaining())
            return Weather.RAIN;
        return Weather.CLEAR;
    }
    
    private String getGameModeShortText(GameType gameMode) {
        return I18n.format("text.rei.short_gamemode." + gameMode.getName());
    }
    
    private String getGameModeText(GameType gameMode) {
        return I18n.format("selectWorld.gameMode." + gameMode.getName());
    }
    
    private GameType getNextGameMode() {
        try {
            GameType current = getCurrentGameMode();
            int next = current.getID() + 1;
            if (next > 3)
                next = 0;
            return GameType.getByID(next);
        } catch (Exception e) {
            return GameType.NOT_SET;
        }
    }
    
    private GameType getCurrentGameMode() {
        return Minecraft.getInstance().getConnection().getPlayerInfo(Minecraft.getInstance().player.getGameProfile().getId()).getGameType();
    }
    
    private Rectangle getTextFieldArea() {
        int widthRemoved = RoughlyEnoughItemsClient.getConfigManager().getConfig().enableCraftableOnlyButton ? 22 : 2;
        if (RoughlyEnoughItemsClient.getConfigManager().getConfig().sideSearchField)
            return new Rectangle(rectangle.x + 2, window.getScaledHeight() - 22, rectangle.width - 6 - widthRemoved, 18);
        if (Minecraft.getInstance().currentScreen instanceof RecipeViewingScreen) {
            RecipeViewingScreen widget = (RecipeViewingScreen) Minecraft.getInstance().currentScreen;
            return new Rectangle(widget.getBounds().x, window.getScaledHeight() - 22, widget.getBounds().width - widthRemoved, 18);
        }
        if (Minecraft.getInstance().currentScreen instanceof VillagerRecipeViewingScreen) {
            VillagerRecipeViewingScreen widget = (VillagerRecipeViewingScreen) Minecraft.getInstance().currentScreen;
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
        return I18n.format(String.format("%s%s", "text.rei.", ClientHelper.getInstance().isCheating() ? "cheat" : "nocheat"));
    }
    
    public Rectangle getRectangle() {
        return rectangle;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        List<ItemStack> currentStacks = ClientHelper.getInstance().getInventoryItemsTypes();
        if (RoughlyEnoughItemsCore.getDisplayHelper().getBaseBoundsHandler() != null && RoughlyEnoughItemsCore.getDisplayHelper().getBaseBoundsHandler().shouldRecalculateArea(!RoughlyEnoughItemsClient.getConfigManager().getConfig().mirrorItemPanel, rectangle))
            shouldReInit = true;
        if (shouldReInit)
            init(true);
        else if (RoughlyEnoughItemsClient.getConfigManager().isCraftableOnlyEnabled() && (!hasSameListContent(new LinkedList<>(ScreenHelper.inventoryStacks), currentStacks) || (currentStacks.size() != ScreenHelper.inventoryStacks.size()))) {
            ScreenHelper.inventoryStacks = ClientHelper.getInstance().getInventoryItemsTypes();
            DisplayHelper.DisplayBoundsHandler<?> boundsHandler = RoughlyEnoughItemsCore.getDisplayHelper().getResponsibleBoundsHandler(Minecraft.getInstance().currentScreen.getClass());
            itemListOverlay.updateList(boundsHandler, boundsHandler.getItemListArea(rectangle), page, searchTerm, true);
        }
        if (Minecraft.getInstance().currentScreen instanceof GuiContainer && SearchFieldWidget.isSearching) {
            RenderHelper.disableStandardItemLighting();
            zLevel = 200;
            ContainerScreenHooks hooks = (ContainerScreenHooks) Minecraft.getInstance().currentScreen;
            int left = hooks.rei_getContainerLeft(), top = hooks.rei_getContainerTop();
            for(Slot slot : ((GuiContainer) Minecraft.getInstance().currentScreen).inventorySlots.inventorySlots)
                if (!slot.getHasStack() || !itemListOverlay.filterItem(slot.getStack(), itemListOverlay.getLastSearchArgument()))
                    drawGradientRect(left + slot.xPos, top + slot.yPos, left + slot.xPos + 16, top + slot.yPos + 16, -601874400, -601874400);
            zLevel = 0;
        }
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        this.renderWidgets(mouseX, mouseY, delta);
        if (Minecraft.getInstance().currentScreen instanceof GuiContainer) {
            ContainerScreenHooks hooks = (ContainerScreenHooks) Minecraft.getInstance().currentScreen;
            for(RecipeHelperImpl.ScreenClickArea area : RecipeHelper.getInstance().getScreenClickAreas())
                if (area.getScreenClass().equals(Minecraft.getInstance().currentScreen.getClass()))
                    if (area.getRectangle().contains(mouseX - hooks.rei_getContainerLeft(), mouseY - hooks.rei_getContainerTop())) {
                        String collect = Arrays.asList(area.getCategories()).stream().map(identifier -> RecipeHelper.getInstance().getCategory(identifier).getCategoryName()).collect(Collectors.joining(", "));
                        QUEUED_TOOLTIPS.add(QueuedTooltip.create(I18n.format("text.rei.view_recipes_for", collect)));
                        break;
                    }
        }
    }
    
    public void lateRender(int mouseX, int mouseY, float delta) {
        ScreenHelper.searchField.laterRender(mouseX, mouseY, delta);
        if (toggleButtonWidget != null)
            toggleButtonWidget.lateRender(mouseX, mouseY, delta);
        GuiScreen currentScreen = Minecraft.getInstance().currentScreen;
        if (!(currentScreen instanceof RecipeViewingScreen) || !((RecipeViewingScreen) currentScreen).choosePageActivated)
            QUEUED_TOOLTIPS.stream().filter(Objects::nonNull).forEach(this::renderTooltip);
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
        int width = lines.stream().map(font::getStringWidth).max(Integer::compareTo).get();
        int height = lines.size() <= 1 ? 8 : lines.size() * 10;
        ScreenHelper.drawHoveringWidget(mouseX, mouseY, (x, y, aFloat) -> {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            this.zLevel = 1000;
            this.drawGradientRect(x - 3, y - 4, x + width + 3, y - 3, -267386864, -267386864);
            this.drawGradientRect(x - 3, y + height + 3, x + width + 3, y + height + 4, -267386864, -267386864);
            this.drawGradientRect(x - 3, y - 3, x + width + 3, y + height + 3, -267386864, -267386864);
            this.drawGradientRect(x - 4, y - 3, x - 3, y + height + 3, -267386864, -267386864);
            this.drawGradientRect(x + width + 3, y - 3, x + width + 4, y + height + 3, -267386864, -267386864);
            this.drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, 1347420415, 1344798847);
            this.drawGradientRect(x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, 1347420415, 1344798847);
            this.drawGradientRect(x - 3, y - 3, x + width + 3, y - 3 + 1, 1347420415, 1347420415);
            this.drawGradientRect(x - 3, y + height + 2, x + width + 3, y + height + 3, 1344798847, 1344798847);
            int currentY = y;
            for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                GlStateManager.disableDepthTest();
                font.drawStringWithShadow(lines.get(lineIndex), x, currentY, -1);
                GlStateManager.enableDepthTest();
                currentY += lineIndex == 0 ? 12 : 10;
            }
            this.zLevel = 0;
            GlStateManager.enableLighting();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }, width, height, 0);
    }
    
    private boolean hasSameListContent(List<ItemStack> list1, List<ItemStack> list2) {
        list1.sort((itemStack, t1) -> ItemListOverlay.tryGetItemStackName(itemStack).compareToIgnoreCase(ItemListOverlay.tryGetItemStackName(t1)));
        list2.sort((itemStack, t1) -> ItemListOverlay.tryGetItemStackName(itemStack).compareToIgnoreCase(ItemListOverlay.tryGetItemStackName(t1)));
        
        return list1.stream().map(ItemListOverlay::tryGetItemStackName).collect(Collectors.joining("")).equals(list2.stream().map(ItemListOverlay::tryGetItemStackName).collect(Collectors.joining("")));
    }
    
    public void addTooltip(QueuedTooltip queuedTooltip) {
        QUEUED_TOOLTIPS.add(queuedTooltip);
    }
    
    public void renderWidgets(int int_1, int int_2, float float_1) {
        if (!ScreenHelper.isOverlayVisible())
            return;
        buttonLeft.enabled = buttonRight.enabled = getTotalPage() > 0;
        widgets.forEach(widget -> {
            RenderHelper.disableStandardItemLighting();
            widget.render(int_1, int_2, float_1);
        });
        RenderHelper.disableStandardItemLighting();
    }
    
    private int getTotalPage() {
        return itemListOverlay.getTotalPage();
    }
    
    @Override
    public boolean mouseScrolled(double amount) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (isInside(MouseUtils.getMouseLocation())) {
            if (amount > 0 && buttonLeft.enabled)
                buttonLeft.onPressed();
            else if (amount < 0 && buttonRight.enabled)
                buttonRight.onPressed();
            else
                return false;
            return true;
        }
        for(Widget widget : widgets)
            if (widget.mouseScrolled(amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (ScreenHelper.isOverlayVisible())
            for(IGuiEventListener listener : widgets)
                if (listener.keyPressed(int_1, int_2, int_3))
                    return true;
        if (ClientHelper.getInstance().getHideKeyBinding().matchesKey(int_1, int_2)) {
            ScreenHelper.toggleOverlayVisible();
            return true;
        }
        if (!ScreenHelper.isOverlayVisible())
            return false;
        ItemStack itemStack = null;
        if (Minecraft.getInstance().currentScreen instanceof GuiContainer)
            if (ScreenHelper.getLastContainerScreenHooks().rei_getHoveredSlot() != null && !ScreenHelper.getLastContainerScreenHooks().rei_getHoveredSlot().getStack().isEmpty())
                itemStack = ScreenHelper.getLastContainerScreenHooks().rei_getHoveredSlot().getStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            if (ClientHelper.getInstance().getRecipeKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeRecipeKeyBind(itemStack);
            else if (ClientHelper.getInstance().getUsageKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeUsageKeyBind(itemStack);
        }
        if (ClientHelper.getInstance().getFocusSearchFieldKeyBinding().matchesKey(int_1, int_2)) {
            ScreenHelper.searchField.setFocused(true);
            setFocused(ScreenHelper.searchField);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        for(IGuiEventListener listener : widgets)
            if (listener.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Override
    public List<? extends IGuiEventListener> getChildren() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (Minecraft.getInstance().currentScreen instanceof GuiContainer) {
            ContainerScreenHooks hooks = (ContainerScreenHooks) Minecraft.getInstance().currentScreen;
            for(RecipeHelperImpl.ScreenClickArea area : RecipeHelper.getInstance().getScreenClickAreas())
                if (area.getScreenClass().equals(Minecraft.getInstance().currentScreen.getClass()))
                    if (area.getRectangle().contains(double_1 - hooks.rei_getContainerLeft(), double_2 - hooks.rei_getContainerTop())) {
                        ClientHelper.getInstance().executeViewAllRecipesFromCategories(Arrays.asList(area.getCategories()));
                        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
        }
        for(IGuiEventListener element : widgets)
            if (element.mouseClicked(double_1, double_2, int_1)) {
                this.setFocused(element);
                if (int_1 == 0)
                    this.setDragging(true);
                return true;
            }
        return false;
    }
    
    public boolean isInside(double mouseX, double mouseY) {
        if (!rectangle.contains(mouseX, mouseY))
            return false;
        for(DisplayHelper.DisplayBoundsHandler handler : RoughlyEnoughItemsCore.getDisplayHelper().getSortedBoundsHandlers(Minecraft.getInstance().currentScreen.getClass())) {
            EnumActionResult in = handler.isInZone(!RoughlyEnoughItemsClient.getConfigManager().getConfig().mirrorItemPanel, mouseX, mouseY);
            if (in != EnumActionResult.PASS)
                return in == EnumActionResult.SUCCESS;
        }
        return true;
    }
    
    public boolean isInside(Point point) {
        return isInside(point.getX(), point.getY());
    }
    
}
