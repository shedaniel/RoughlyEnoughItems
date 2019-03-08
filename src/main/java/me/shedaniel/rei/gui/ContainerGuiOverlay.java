package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.KeyBindHelper;
import me.shedaniel.rei.client.Weather;
import me.shedaniel.rei.gui.credits.CreditsGui;
import me.shedaniel.rei.gui.widget.*;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.GuiEventHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ContainerGuiOverlay extends GuiEventHandler {
    
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final List<QueuedTooltip> QUEUED_TOOLTIPS = Lists.newArrayList();
    public static String searchTerm = "";
    private static int page = 0;
    private static ItemListOverlay itemListOverlay;
    private final List<IWidget> widgets = Lists.newArrayList();
    private Rectangle rectangle;
    private MainWindow window;
    private ButtonWidget buttonLeft, buttonRight;
    private int lastLeft;
    
    public void init() {
        init(false);
    }
    
    public void init(boolean setPage) {
        //Update Variables
        this.widgets.clear();
        this.window = Minecraft.getInstance().mainWindow;
        this.rectangle = calculateBoundary();
        this.lastLeft = getLeft();
        
        widgets.add(this.itemListOverlay = new ItemListOverlay(page));
        this.itemListOverlay.updateList(getItemListArea(), page, searchTerm);
        
        widgets.add(buttonLeft = new ButtonWidget(rectangle.x, rectangle.y + 5, 16, 16, new TextComponentTranslation("text.rei.left_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page--;
                if (page < 0)
                    page = getTotalPage();
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
        });
        widgets.add(buttonRight = new ButtonWidget(rectangle.x + rectangle.width - 18, rectangle.y + 5, 16, 16, new TextComponentTranslation("text.rei.right_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page++;
                if (page > getTotalPage())
                    page = 0;
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
        });
        widgets.add(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 30 : 10, 10, 20, 20, "") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                if (GuiScreen.isShiftKeyDown()) {
                    ClientHelper.setCheating(!ClientHelper.isCheating());
                    return;
                }
                ClientHelper.openConfigWindow(GuiHelper.getLastGuiContainer());
            }
            
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                super.draw(mouseX, mouseY, partialTicks);
                RenderHelper.disableStandardItemLighting();
                if (ClientHelper.isCheating())
                    drawRect(getBounds().x, getBounds().y, getBounds().x + 20, getBounds().y + 20, new Color(255, 0, 0, 42).getRGB());
                Minecraft.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                drawTexturedModalRect(getBounds().x + 3, getBounds().y + 3, 0, 0, 14, 14);
                if (isHighlighted(mouseX, mouseY)) {
                    List<String> list = new LinkedList<>(Arrays.asList(I18n.format("text.rei.config_tooltip").split("\n")));
                    list.add(" ");
                    if (!ClientHelper.isCheating())
                        list.add("§c§m" + I18n.format("text.rei.cheating"));
                    else
                        list.add("§a" + I18n.format("text.rei.cheating"));
                    addTooltip(QueuedTooltip.create(list));
                }
            }
        });
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().disableCreditsButton)
            widgets.add(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 50 : 10, window.getScaledHeight() - 30, 40, 20, I18n.format("text.rei.credits")) {
                @Override
                public void onPressed(int button, double mouseX, double mouseY) {
                    Minecraft.getInstance().displayGuiScreen(new CreditsGui(GuiHelper.getLastGuiContainer()));
                }
            });
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons) {
            widgets.add(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 55 : 35, 10, 20, 20, "") {
                @Override
                public void onPressed(int button, double mouseX, double mouseY) {
                    Minecraft.getInstance().player.sendChatMessage(RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand.replaceAll("\\{gamemode}", getNextGameMode().getName()));
                }
                
                @Override
                public void draw(int mouseX, int mouseY, float partialTicks) {
                    text = getGameModeShortText(getCurrentGameMode());
                    super.draw(mouseX, mouseY, partialTicks);
                    if (isHighlighted(mouseX, mouseY)) {
                        List<String> list = Arrays.asList(I18n.format("text.rei.gamemode_button.tooltip", getGameModeText(getNextGameMode())).split("\n"));
                        addTooltip(QueuedTooltip.create(list));
                    }
                }
            });
            widgets.add(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 80 : 60, 10, 20, 20, "") {
                @Override
                public void onPressed(int button, double mouseX, double mouseY) {
                    Minecraft.getInstance().player.sendChatMessage(RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand.replaceAll("\\{weather}", getNextWeather().getName().toLowerCase()));
                }
                
                @Override
                public void draw(int mouseX, int mouseY, float partialTicks) {
                    super.draw(mouseX, mouseY, partialTicks);
                    RenderHelper.disableStandardItemLighting();
                    Minecraft.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    drawTexturedModalRect(getBounds().x + 3, getBounds().y + 3, getCurrentWeather().getId() * 14, 14, 14, 14);
                    if (isHighlighted(mouseX, mouseY)) {
                        List<String> list = Arrays.asList(I18n.format("text.rei.weather_button.tooltip", getNextWeather().getName()).split("\n"));
                        addTooltip(QueuedTooltip.create(list));
                    }
                }
            });
        }
        widgets.add(new ClickableLabelWidget(rectangle.x + (rectangle.width / 2), rectangle.y + 10, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                page = MathHelper.clamp(page, 0, getTotalPage());
                this.text = String.format("%s/%s", page + 1, getTotalPage() + 1);
                super.draw(mouseX, mouseY, partialTicks);
                if (isHighlighted(mouseX, mouseY))
                    addTooltip(QueuedTooltip.create(Arrays.asList(I18n.format("text.rei.go_back_first_page").split("\n"))));
            }
            
            @Override
            public void onLabelClicked() {
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                page = 0;
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
        });
        if (GuiHelper.searchField == null)
            GuiHelper.searchField = new TextFieldWidget(0, 0, 0, 0) {
                @Override
                public boolean mouseClicked(double double_1, double double_2, int int_1) {
                    if (isVisible() && getBounds().contains(double_1, double_2) && int_1 == 1) {
                        setText("");
                        return true;
                    }
                    return super.mouseClicked(double_1, double_2, int_1);
                }
            };
        GuiHelper.searchField.setChangedListener(s -> {
            searchTerm = s;
            itemListOverlay.updateList(getItemListArea(), page, searchTerm);
        });
        GuiHelper.searchField.getBounds().setBounds(getTextFieldArea());
        this.widgets.add(GuiHelper.searchField);
        GuiHelper.searchField.setText(searchTerm);
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton)
            this.widgets.add(new CraftableToggleButtonWidget(getCraftableToggleArea()) {
                @Override
                public void onPressed(int button, double mouseX, double mouseY) {
                    RoughlyEnoughItemsCore.getConfigManager().toggleCraftableOnly();
                    itemListOverlay.updateList(getItemListArea(), page, searchTerm);
                }
            });
        
        if (setPage)
            page = MathHelper.clamp(page, 0, getTotalPage());
        this.itemListOverlay.updateList(getItemListArea(), page, searchTerm);
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
        switch (gameMode) {
            case CREATIVE:
                return "C";
            case SURVIVAL:
                return "S";
            case ADVENTURE:
                return "A";
            case SPECTATOR:
                return "SP";
        }
        return gameMode.name();
    }
    
    private String getGameModeText(GameType gameMode) {
        switch (gameMode) {
            case CREATIVE:
                return "Creative";
            case SURVIVAL:
                return "Survival";
            case ADVENTURE:
                return "Adventure";
            case SPECTATOR:
                return "Spectator";
        }
        return gameMode.name();
    }
    
    private GameType getNextGameMode() {
        try {
            GameType current = getCurrentGameMode();
            int next = current.getID() + 1;
            if (next >= 3)
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
        int widthRemoved = RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton ? 22 : 2;
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField)
            return new Rectangle(rectangle.x + 2, window.getScaledHeight() - 22, rectangle.width - 6 - widthRemoved, 18);
        if (Minecraft.getInstance().currentScreen instanceof RecipeViewingGui) {
            RecipeViewingGui widget = (RecipeViewingGui) Minecraft.getInstance().currentScreen;
            return new Rectangle(widget.getBounds().x, window.getScaledHeight() - 22, widget.getBounds().width - widthRemoved, 18);
        }
        return new Rectangle(GuiHelper.getLastGuiContainer().getGuiLeft(), window.getScaledHeight() - 22, GuiHelper.getLastGuiContainer().getXSize() - widthRemoved, 18);
    }
    
    private Rectangle getCraftableToggleArea() {
        Rectangle searchBoxArea = getTextFieldArea();
        searchBoxArea.setLocation(searchBoxArea.x + searchBoxArea.width + 4, searchBoxArea.y - 1);
        searchBoxArea.setSize(20, 20);
        return searchBoxArea;
    }
    
    private String getCheatModeText() {
        return I18n.format(String.format("%s%s", "text.rei.", ClientHelper.isCheating() ? "cheat" : "nocheat"));
    }
    
    private Rectangle getItemListArea() {
        return new Rectangle(rectangle.x + 2, rectangle.y + 24, rectangle.width - 4, rectangle.height - (RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField ? 27 + 22 : 27));
    }
    
    public Rectangle getRectangle() {
        return rectangle;
    }
    
    public void renderOverlay(int mouseX, int mouseY, float partialTicks) {
        List<ItemStack> currentStacks = ClientHelper.getInventoryItemsTypes();
        if (getLeft() != lastLeft)
            init(true);
        else if (RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() && (!hasSameListContent(new LinkedList<>(GuiHelper.inventoryStacks), currentStacks) || (currentStacks.size() != GuiHelper.inventoryStacks.size()))) {
            GuiHelper.inventoryStacks = ClientHelper.getInventoryItemsTypes();
            itemListOverlay.updateList(getItemListArea(), page, searchTerm);
        }
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        this.render(mouseX, mouseY, partialTicks);
        RenderHelper.disableStandardItemLighting();
        GuiScreen currentScreen = Minecraft.getInstance().currentScreen;
        if (!(currentScreen instanceof RecipeViewingGui) || !((RecipeViewingGui) currentScreen).choosePageActivated)
            QUEUED_TOOLTIPS.stream().filter(queuedTooltip -> queuedTooltip != null).forEach(queuedTooltip -> Minecraft.getInstance().currentScreen.drawHoveringText(queuedTooltip.getText(), queuedTooltip.getLocation().x, queuedTooltip.getLocation().y));
        QUEUED_TOOLTIPS.clear();
        RenderHelper.disableStandardItemLighting();
    }
    
    private boolean hasSameListContent(List<ItemStack> list1, List<ItemStack> list2) {
        Collections.sort(list1, (itemStack, t1) -> {
            return ItemListOverlay.tryGetItemStackName(itemStack).compareToIgnoreCase(ItemListOverlay.tryGetItemStackName(t1));
        });
        Collections.sort(list2, (itemStack, t1) -> {
            return ItemListOverlay.tryGetItemStackName(itemStack).compareToIgnoreCase(ItemListOverlay.tryGetItemStackName(t1));
        });
        String lastString = String.join("", list1.stream().map(itemStack -> {
            return ItemListOverlay.tryGetItemStackName(itemStack);
        }).collect(Collectors.toList())), currentString = String.join("", list2.stream().map(itemStack -> {
            return ItemListOverlay.tryGetItemStackName(itemStack);
        }).collect(Collectors.toList()));
        return lastString.equals(currentString);
    }
    
    public void addTooltip(QueuedTooltip queuedTooltip) {
        QUEUED_TOOLTIPS.add(queuedTooltip);
    }
    
    public void render(int int_1, int int_2, float float_1) {
        if (!GuiHelper.isOverlayVisible())
            return;
        widgets.forEach(widget -> {
            RenderHelper.disableStandardItemLighting();
            widget.draw(int_1, int_2, float_1);
        });
        RenderHelper.disableStandardItemLighting();
    }
    
    private Rectangle calculateBoundary() {
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel) {
            int startX = GuiHelper.getLastGuiContainer().getGuiLeft() + GuiHelper.getLastGuiContainer().getXSize() + 10;
            int width = window.getScaledWidth() - startX;
            if (Minecraft.getInstance().currentScreen instanceof RecipeViewingGui) {
                RecipeViewingGui widget = (RecipeViewingGui) Minecraft.getInstance().currentScreen;
                startX = widget.getBounds().x + widget.getBounds().width + 10;
                width = window.getScaledWidth() - startX;
            }
            return new Rectangle(startX, 0, width, window.getScaledHeight());
        }
        return new Rectangle(4, 0, getLeft() - 6, window.getScaledHeight());
    }
    
    private int getLeft() {
        if (Minecraft.getInstance().currentScreen instanceof RecipeViewingGui) {
            RecipeViewingGui widget = (RecipeViewingGui) Minecraft.getInstance().currentScreen;
            return widget.getBounds().x;
        }
        if (Minecraft.getInstance().player.getRecipeBook().isGuiOpen())
            return GuiHelper.getLastGuiContainer().getGuiLeft() - 147 - 30;
        return GuiHelper.getLastGuiContainer().getGuiLeft();
    }
    
    private int getTotalPage() {
        return MathHelper.ceil(itemListOverlay.getCurrentDisplayed().size() / itemListOverlay.getTotalSlotsPerPage());
    }
    
    @Override
    public boolean mouseScrolled(double amount) {
        if (!GuiHelper.isOverlayVisible())
            return false;
        if (rectangle.contains(ClientHelper.getMouseLocation())) {
            if (amount > 0 && buttonLeft.enabled)
                buttonLeft.onPressed(0, 0, 0);
            else if (amount < 0 && buttonRight.enabled)
                buttonRight.onPressed(0, 0, 0);
            else
                return false;
            return true;
        }
        for(IWidget widget : widgets)
            if (widget.mouseScrolled(amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (GuiHelper.isOverlayVisible())
            for(IGuiEventListener listener : widgets)
                if (listener.keyPressed(int_1, int_2, int_3))
                    return true;
        if (KeyBindHelper.HIDE.matchesKey(int_1, int_2)) {
            GuiHelper.toggleOverlayVisible();
            return true;
        }
        if (!GuiHelper.isOverlayVisible())
            return false;
        Point point = ClientHelper.getMouseLocation();
        ItemStack itemStack = null;
        if (Minecraft.getInstance().currentScreen instanceof GuiContainer)
            if (GuiHelper.getLastGuiContainer().getSlotUnderMouse() != null && !GuiHelper.getLastGuiContainer().getSlotUnderMouse().getStack().isEmpty())
                itemStack = GuiHelper.getLastGuiContainer().getSlotUnderMouse().getStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            if (KeyBindHelper.RECIPE.matchesKey(int_1, int_2))
                return ClientHelper.executeRecipeKeyBind(itemStack);
            else if (KeyBindHelper.USAGE.matchesKey(int_1, int_2))
                return ClientHelper.executeUsageKeyBind(itemStack);
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!GuiHelper.isOverlayVisible())
            return false;
        for(IGuiEventListener listener : widgets)
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    protected List<? extends IGuiEventListener> getChildren() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!GuiHelper.isOverlayVisible())
            return false;
        return super.mouseClicked(double_1, double_2, int_1);
    }
    
}
