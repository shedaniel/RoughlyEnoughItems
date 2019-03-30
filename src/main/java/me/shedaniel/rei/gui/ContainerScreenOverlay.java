package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.client.Weather;
import me.shedaniel.rei.gui.widget.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.audio.PositionedSoundInstance;
import net.minecraft.client.gui.*;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContainerScreenOverlay extends AbstractParentElement implements Drawable {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final List<QueuedTooltip> QUEUED_TOOLTIPS = Lists.newArrayList();
    public static String searchTerm = "";
    private static int page = 0;
    private static ItemListOverlay itemListOverlay;
    private final List<Widget> widgets = Lists.newArrayList();
    private Rectangle rectangle;
    private Window window;
    private ButtonWidget buttonLeft, buttonRight;
    private int lastLeft;
    
    public static ItemListOverlay getItemListOverlay() {
        return itemListOverlay;
    }
    
    public void onInitialized() {
        onInitialized(false);
    }
    
    public void onInitialized(boolean setPage) {
        //Update Variables
        this.widgets.clear();
        this.window = MinecraftClient.getInstance().window;
        this.rectangle = calculateBoundary();
        this.lastLeft = getLeft();
        widgets.add(this.itemListOverlay = new ItemListOverlay(page));
        this.itemListOverlay.updateList(getItemListArea(), page, searchTerm);
        
        widgets.add(buttonLeft = new ButtonWidget(rectangle.x, rectangle.y + 5, 16, 16, new TranslatableTextComponent("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                page--;
                if (page < 0)
                    page = getTotalPage();
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.previous_page"));
            }
            
            @Override
            public void onFocusChanged(boolean boolean_1, boolean boolean_2) {
            }
        });
        widgets.add(buttonRight = new ButtonWidget(rectangle.x + rectangle.width - 18, rectangle.y + 5, 16, 16, new TranslatableTextComponent("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                page++;
                if (page > getTotalPage())
                    page = 0;
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.next_page"));
            }
            
            @Override
            public void onFocusChanged(boolean boolean_1, boolean boolean_2) {
            }
        });
        if (setPage)
            page = MathHelper.clamp(page, 0, getTotalPage());
        widgets.add(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 30 : 10, 10, 20, 20, "") {
            @Override
            public void onPressed() {
                if (Screen.hasShiftDown()) {
                    ClientHelper.setCheating(!ClientHelper.isCheating());
                    return;
                }
                RoughlyEnoughItemsCore.getConfigManager().openConfigScreen(ScreenHelper.getLastContainerScreen());
            }
            
            @Override
            public void render(int mouseX, int mouseY, float partialTicks) {
                super.render(mouseX, mouseY, partialTicks);
                GuiLighting.disable();
                if (ClientHelper.isCheating())
                    fill(getBounds().x, getBounds().y, getBounds().x + 20, getBounds().y + 20, new Color(255, 0, 0, 42).getRGB());
                MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                blit(getBounds().x + 3, getBounds().y + 3, 0, 0, 14, 14);
            }
            
            @Override
            public Optional<String> getTooltips() {
                String tooltips = I18n.translate("text.rei.config_tooltip");
                tooltips += "\n  ";
                if (!ClientHelper.isCheating())
                    tooltips += "\n§c§m" + I18n.translate("text.rei.cheating");
                else
                    tooltips += "\n§a" + I18n.translate("text.rei.cheating");
                return Optional.ofNullable(tooltips);
            }
            
            @Override
            public void onFocusChanged(boolean boolean_1, boolean boolean_2) {
            }
        });
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons) {
            widgets.add(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 55 : 35, 10, 20, 20, "") {
                @Override
                public void onPressed() {
                    MinecraftClient.getInstance().player.sendChatMessage(RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand.replaceAll("\\{gamemode}", getNextGameMode().getName()));
                }
                
                @Override
                public void render(int mouseX, int mouseY, float partialTicks) {
                    text = getGameModeShortText(getCurrentGameMode());
                    super.render(mouseX, mouseY, partialTicks);
                }
                
                @Override
                public Optional<String> getTooltips() {
                    return Optional.ofNullable(I18n.translate("text.rei.gamemode_button.tooltip", getGameModeText(getNextGameMode())));
                }
                
                @Override
                public void onFocusChanged(boolean boolean_1, boolean boolean_2) {
                }
            });
            widgets.add(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel ? window.getScaledWidth() - 80 : 60, 10, 20, 20, "") {
                @Override
                public void onPressed() {
                    MinecraftClient.getInstance().player.sendChatMessage(RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand.replaceAll("\\{weather}", getNextWeather().name().toLowerCase()));
                }
                
                @Override
                public void render(int mouseX, int mouseY, float partialTicks) {
                    super.render(mouseX, mouseY, partialTicks);
                    GuiLighting.disable();
                    MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    blit(getBounds().x + 3, getBounds().y + 3, getCurrentWeather().getId() * 14, 14, 14, 14);
                }
                
                @Override
                public Optional<String> getTooltips() {
                    return Optional.ofNullable(I18n.translate("text.rei.weather_button.tooltip", I18n.translate(getNextWeather().getTranslateKey())));
                }
                
                @Override
                public void onFocusChanged(boolean boolean_1, boolean boolean_2) {
                }
            });
        }
        widgets.add(new ClickableLabelWidget(rectangle.x + (rectangle.width / 2), rectangle.y + 10, "") {
            @Override
            public void render(int mouseX, int mouseY, float partialTicks) {
                page = MathHelper.clamp(page, 0, getTotalPage());
                this.text = String.format("%s/%s", page + 1, getTotalPage() + 1);
                super.render(mouseX, mouseY, partialTicks);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.go_back_first_page"));
            }
            
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                page = 0;
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
            
            @Override
            public void onFocusChanged(boolean boolean_1, boolean boolean_2) {
            }
        });
        if (ScreenHelper.searchField == null)
            ScreenHelper.searchField = new TextFieldWidget(0, 0, 0, 0) {
                @Override
                public boolean mouseClicked(double double_1, double double_2, int int_1) {
                    if (isVisible() && getBounds().contains(double_1, double_2) && int_1 == 1) {
                        setText("");
                        return true;
                    }
                    return super.mouseClicked(double_1, double_2, int_1);
                }
            };
        ScreenHelper.searchField.setChangedListener(s -> {
            searchTerm = s;
            itemListOverlay.updateList(getItemListArea(), page, searchTerm);
        });
        ScreenHelper.searchField.getBounds().setBounds(getTextFieldArea());
        this.widgets.add(ScreenHelper.searchField);
        ScreenHelper.searchField.setText(searchTerm);
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton)
            this.widgets.add(new CraftableToggleButtonWidget(getCraftableToggleArea()) {
                @Override
                public void onPressed() {
                    RoughlyEnoughItemsCore.getConfigManager().toggleCraftableOnly();
                    itemListOverlay.updateList(getItemListArea(), page, searchTerm);
                }
            });
        
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
    
    private GameMode getNextGameMode() {
        try {
            GameMode current = getCurrentGameMode();
            int next = current.getId() + 1;
            if (next > 3)
                next = 0;
            return GameMode.byId(next);
        } catch (Exception e) {
            return GameMode.INVALID;
        }
    }
    
    private GameMode getCurrentGameMode() {
        return MinecraftClient.getInstance().getNetworkHandler().method_2871(MinecraftClient.getInstance().player.getGameProfile().getId()).getGameMode();
    }
    
    private Rectangle getTextFieldArea() {
        int widthRemoved = RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton ? 22 : 2;
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField)
            return new Rectangle(rectangle.x + 2, window.getScaledHeight() - 22, rectangle.width - 6 - widthRemoved, 18);
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen) {
            RecipeViewingScreen widget = (RecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
            return new Rectangle(widget.getBounds().x, window.getScaledHeight() - 22, widget.getBounds().width - widthRemoved, 18);
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
        return I18n.translate(String.format("%s%s", "text.rei.", ClientHelper.isCheating() ? "cheat" : "nocheat"));
    }
    
    private Rectangle getItemListArea() {
        return new Rectangle(rectangle.x + 2, rectangle.y + 24, rectangle.width - 4, rectangle.height - (RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField ? 27 + 22 : 27));
    }
    
    public Rectangle getRectangle() {
        return rectangle;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        List<ItemStack> currentStacks = ClientHelper.getInventoryItemsTypes();
        if (getLeft() != lastLeft)
            onInitialized(true);
        else if (RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() && (!hasSameListContent(new LinkedList<>(ScreenHelper.inventoryStacks), currentStacks) || (currentStacks.size() != ScreenHelper.inventoryStacks.size()))) {
            ScreenHelper.inventoryStacks = ClientHelper.getInventoryItemsTypes();
            itemListOverlay.updateList(getItemListArea(), page, searchTerm);
        }
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        this.renderWidgets(mouseX, mouseY, partialTicks);
        GuiLighting.disable();
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (!(currentScreen instanceof RecipeViewingScreen) || !((RecipeViewingScreen) currentScreen).choosePageActivated)
            QUEUED_TOOLTIPS.stream().filter(queuedTooltip -> queuedTooltip != null).forEach(queuedTooltip -> {
                GlStateManager.translatef(0, 0, 600);
                MinecraftClient.getInstance().currentScreen.renderTooltip(queuedTooltip.getText(), queuedTooltip.getLocation().x, queuedTooltip.getLocation().y);
                GlStateManager.translatef(0, 0, -600);
            });
        QUEUED_TOOLTIPS.clear();
        GuiLighting.disable();
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
    
    public void renderWidgets(int int_1, int int_2, float float_1) {
        if (!ScreenHelper.isOverlayVisible())
            return;
        buttonLeft.enabled = itemListOverlay.children().size() > 0;
        buttonRight.enabled = itemListOverlay.children().size() > 0;
        widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.render(int_1, int_2, float_1);
        });
        GuiLighting.disable();
    }
    
    private Rectangle calculateBoundary() {
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel) {
            int startX = ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() + ScreenHelper.getLastContainerScreenHooks().rei_getContainerWidth() + 10;
            int width = window.getScaledWidth() - startX;
            if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen) {
                RecipeViewingScreen widget = (RecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
                startX = widget.getBounds().x + widget.getBounds().width + 10;
                width = window.getScaledWidth() - startX;
            }
            return new Rectangle(startX, 0, width, window.getScaledHeight());
        }
        return new Rectangle(4, 0, getLeft() - 6, window.getScaledHeight());
    }
    
    private int getLeft() {
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeViewingScreen) {
            RecipeViewingScreen widget = (RecipeViewingScreen) MinecraftClient.getInstance().currentScreen;
            return widget.getBounds().x;
        }
        if (MinecraftClient.getInstance().player.getRecipeBook().isGuiOpen())
            return ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft() - 147 - 30;
        return ScreenHelper.getLastContainerScreenHooks().rei_getContainerLeft();
    }
    
    private int getTotalPage() {
        return MathHelper.ceil(itemListOverlay.getCurrentDisplayed().size() / itemListOverlay.getTotalSlotsPerPage());
    }
    
    @Override
    public boolean mouseScrolled(double i, double j, double amount) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        if (rectangle.contains(ClientUtils.getMouseLocation())) {
            if (amount > 0 && buttonLeft.enabled)
                buttonLeft.onPressed();
            else if (amount < 0 && buttonRight.enabled)
                buttonRight.onPressed();
            else
                return false;
            return true;
        }
        for(Widget widget : widgets)
            if (widget.mouseScrolled(i, j, amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (ScreenHelper.isOverlayVisible())
            for(Element listener : widgets)
                if (listener.keyPressed(int_1, int_2, int_3))
                    return true;
        if (ClientHelper.HIDE.matchesKey(int_1, int_2)) {
            ScreenHelper.toggleOverlayVisible();
            return true;
        }
        if (!ScreenHelper.isOverlayVisible())
            return false;
        Point point = ClientUtils.getMouseLocation();
        ItemStack itemStack = null;
        if (MinecraftClient.getInstance().currentScreen instanceof ContainerScreen)
            if (ScreenHelper.getLastContainerScreenHooks().rei_getHoveredSlot() != null && !ScreenHelper.getLastContainerScreenHooks().rei_getHoveredSlot().getStack().isEmpty())
                itemStack = ScreenHelper.getLastContainerScreenHooks().rei_getHoveredSlot().getStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            if (ClientHelper.RECIPE.matchesKey(int_1, int_2))
                return ClientHelper.executeRecipeKeyBind(itemStack);
            else if (ClientHelper.USAGE.matchesKey(int_1, int_2))
                return ClientHelper.executeUsageKeyBind(itemStack);
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        for(Element listener : children())
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public List<? extends Element> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!ScreenHelper.isOverlayVisible())
            return false;
        for(Element element : this.children()) {
            if (element.mouseClicked(double_1, double_2, int_1)) {
                this.method_20084(element);
                if (int_1 == 0)
                    this.setDragging(true);
                return true;
            }
        }
        return false;
    }
    
}
