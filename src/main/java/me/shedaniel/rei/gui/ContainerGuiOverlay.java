package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ConfigHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.KeyBindHelper;
import me.shedaniel.rei.gui.widget.*;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ContainerGuiOverlay extends GuiScreen {
    
    private static final List<QueuedTooltip> QUEUED_TOOLTIPS = Lists.newArrayList();
    public static String searchTerm = "";
    private static int page = 0;
    private static ItemListOverlay itemListOverlay;
    private final List<IWidget> widgets = Lists.newArrayList();
    private Rectangle rectangle;
    private MainWindow window;
    private ButtonWidget buttonLeft, buttonRight;
    private int lastLeft;
    
    public void onInitialized() {
        //Update Variables
        this.widgets.clear();
        this.window = Minecraft.getInstance().mainWindow;
        this.rectangle = calculateBoundary();
        this.lastLeft = getLeft();
        
        widgets.add(this.itemListOverlay = new ItemListOverlay(page));
        this.itemListOverlay.updateList(getItemListArea(), page, searchTerm);
        
        widgets.add(buttonLeft = new ButtonWidget(rectangle.x, rectangle.y + 5, 16, 16, "<") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page--;
                if (page < 0)
                    page = getTotalPage();
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
        });
        widgets.add(buttonRight = new ButtonWidget(rectangle.x + rectangle.width - 18, rectangle.y + 5, 16, 16, ">") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page++;
                if (page > getTotalPage())
                    page = 0;
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
        });
        page = MathHelper.clamp(page, 0, getTotalPage());
        widgets.add(new ButtonWidget(ConfigHelper.getInstance().isMirrorItemPanel() ? window.getScaledWidth() - 50 : 10, 10, 40, 20, "") {
            @Override
            public void draw(int int_1, int int_2, float float_1) {
                this.text = getCheatModeText();
                super.draw(int_1, int_2, float_1);
            }
            
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                ClientHelper.setCheating(!ClientHelper.isCheating());
            }
        });
        widgets.add(new ButtonWidget(ConfigHelper.getInstance().isMirrorItemPanel() ? window.getScaledWidth() - 50 : 10, 35, 40, 20, I18n.format("text.rei.config")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                ClientHelper.openConfigWindow(GuiHelper.getLastGuiContainer());
            }
        });
        this.widgets.add(new LabelWidget(rectangle.x + (rectangle.width / 2), rectangle.y + 10, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                page = MathHelper.clamp(page, 0, getTotalPage());
                this.text = String.format("%s/%s", page + 1, getTotalPage() + 1);
                super.draw(mouseX, mouseY, partialTicks);
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
            itemListOverlay.updateList(page, searchTerm);
        });
        GuiHelper.searchField.setBounds(getTextFieldArea());
        this.widgets.add(GuiHelper.searchField);
        GuiHelper.searchField.setText(searchTerm);
        if (ConfigHelper.getInstance().showCraftableOnlyButton())
            this.widgets.add(new CraftableToggleButtonWidget(getCraftableToggleArea()) {
                @Override
                public void onPressed(int button, double mouseX, double mouseY) {
                    ConfigHelper.getInstance().toggleCraftableOnly();
                    itemListOverlay.updateList(page, searchTerm);
                }
            });
        
        this.itemListOverlay.updateList(getItemListArea(), page, searchTerm);
        this.children.addAll(widgets);
    }
    
    private Rectangle getTextFieldArea() {
        int widthRemoved = ConfigHelper.getInstance().showCraftableOnlyButton() ? 22 : 0;
        if (ConfigHelper.getInstance().sideSearchField())
            return new Rectangle(rectangle.x + 2, window.getScaledHeight() - 22, rectangle.width - 6 - widthRemoved, 18);
        if (Minecraft.getInstance().currentScreen instanceof RecipeViewingWidgetGui) {
            RecipeViewingWidgetGui widget = (RecipeViewingWidgetGui) Minecraft.getInstance().currentScreen;
            return new Rectangle(widget.getBounds().x, window.getScaledHeight() - 22, widget.getBounds().width - widthRemoved, 18);
        }
        return new Rectangle(GuiHelper.getLastMixinGuiContainer().getContainerLeft(), window.getScaledHeight() - 22, GuiHelper.getLastMixinGuiContainer().getContainerWidth() - widthRemoved, 18);
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
        return new Rectangle(rectangle.x + 2, rectangle.y + 24, rectangle.width - 4, rectangle.height - (ConfigHelper.getInstance().sideSearchField() ? 27 + 22 : 27));
    }
    
    public Rectangle getRectangle() {
        return rectangle;
    }
    
    public void renderOverlay(int mouseX, int mouseY, float partialTicks) {
        List<ItemStack> currentStacks = ClientHelper.getInventoryItemsTypes();
        if (getLeft() != lastLeft)
            onInitialized();
        else if (ConfigHelper.getInstance().craftableOnly() && (!hasSameListContent(new LinkedList<>(GuiHelper.inventoryStacks), currentStacks) || (currentStacks.size() != GuiHelper.inventoryStacks.size()))) {
            GuiHelper.inventoryStacks = ClientHelper.getInventoryItemsTypes();
            itemListOverlay.updateList(page, searchTerm);
        }
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        this.render(mouseX, mouseY, partialTicks);
        RenderHelper.disableStandardItemLighting();
        QUEUED_TOOLTIPS.stream().filter(queuedTooltip -> queuedTooltip != null).forEach(queuedTooltip -> Minecraft.getInstance().currentScreen.drawHoveringText(queuedTooltip.text, queuedTooltip.mouse.x, queuedTooltip.mouse.y));
        QUEUED_TOOLTIPS.clear();
        RenderHelper.disableStandardItemLighting();
    }
    
    private boolean hasSameListContent(List<ItemStack> list1, List<ItemStack> list2) {
        Collections.sort(list1, (itemStack, t1) -> {
            return itemStack.getDisplayName().getFormattedText().compareToIgnoreCase(t1.getDisplayName().getFormattedText());
        });
        Collections.sort(list2, (itemStack, t1) -> {
            return itemStack.getDisplayName().getFormattedText().compareToIgnoreCase(t1.getDisplayName().getFormattedText());
        });
        String lastString = String.join("", list1.stream().map(itemStack -> {
            return itemStack.getDisplayName().getFormattedText();
        }).collect(Collectors.toList())), currentString = String.join("", list2.stream().map(itemStack -> {
            return itemStack.getDisplayName().getFormattedText();
        }).collect(Collectors.toList()));
        return lastString.equals(currentString);
    }
    
    public void addTooltip(QueuedTooltip queuedTooltip) {
        QUEUED_TOOLTIPS.add(queuedTooltip);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        if (!GuiHelper.isOverlayVisible())
            return;
        widgets.forEach(widget -> {
            RenderHelper.disableStandardItemLighting();
            widget.draw(int_1, int_2, float_1);
        });
        RenderHelper.disableStandardItemLighting();
        itemListOverlay.draw(int_1, int_2, float_1);
        RenderHelper.disableStandardItemLighting();
        super.render(int_1, int_2, float_1);
    }
    
    private Rectangle calculateBoundary() {
        if (!ConfigHelper.getInstance().isMirrorItemPanel()) {
            int startX = GuiHelper.getLastMixinGuiContainer().getContainerLeft() + GuiHelper.getLastMixinGuiContainer().getContainerWidth() + 10;
            int width = window.getScaledWidth() - startX;
            if (Minecraft.getInstance().currentScreen instanceof RecipeViewingWidgetGui) {
                RecipeViewingWidgetGui widget = (RecipeViewingWidgetGui) Minecraft.getInstance().currentScreen;
                startX = widget.getBounds().x + widget.getBounds().width + 10;
                width = window.getScaledWidth() - startX;
            }
            return new Rectangle(startX, 0, width, window.getScaledHeight());
        }
        int width = GuiHelper.getLastMixinGuiContainer().getContainerLeft() - 6;
        if (Minecraft.getInstance().currentScreen instanceof RecipeViewingWidgetGui) {
            RecipeViewingWidgetGui widget = (RecipeViewingWidgetGui) Minecraft.getInstance().currentScreen;
            width = widget.getBounds().x - 6;
        }
        return new Rectangle(4, 0, width, window.getScaledHeight());
    }
    
    private int getLeft() {
        if (Minecraft.getInstance().currentScreen instanceof RecipeViewingWidgetGui) {
            RecipeViewingWidgetGui widget = (RecipeViewingWidgetGui) Minecraft.getInstance().currentScreen;
            return widget.getBounds().x;
        }
        return GuiHelper.getLastMixinGuiContainer().getContainerLeft();
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
        if (KeyBindHelper.HIDE.matchesKey(int_1, int_2)) {
            GuiHelper.toggleOverlayVisible();
            return true;
        }
        if (!GuiHelper.isOverlayVisible())
            return false;
        for(IGuiEventListener listener : children)
            if (listener.keyPressed(int_1, int_2, int_3))
                return true;
        Point point = ClientHelper.getMouseLocation();
        ItemStack itemStack = null;
        for(IWidget widget : itemListOverlay.getListeners())
            if (widget instanceof ItemSlotWidget && ((ItemSlotWidget) widget).isHighlighted(point.x, point.y)) {
                itemStack = ((ItemSlotWidget) widget).getCurrentStack();
                break;
            }
        if (itemStack == null && Minecraft.getInstance().currentScreen instanceof RecipeViewingWidgetGui) {
            RecipeViewingWidgetGui recipeViewingWidget = (RecipeViewingWidgetGui) Minecraft.getInstance().currentScreen;
            for(IGuiEventListener entry : recipeViewingWidget.getChildren())
                if (entry instanceof ItemSlotWidget && ((ItemSlotWidget) entry).isHighlighted(point.x, point.y)) {
                    itemStack = ((ItemSlotWidget) entry).getCurrentStack();
                    break;
                }
        }
        if (itemStack == null && Minecraft.getInstance().currentScreen instanceof GuiContainer)
            if (GuiHelper.getLastMixinGuiContainer().getHoveredSlot() != null)
                itemStack = GuiHelper.getLastMixinGuiContainer().getHoveredSlot().getStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            if (KeyBindHelper.RECIPE.matchesKey(int_1, int_2))
                return ClientHelper.executeRecipeKeyBind(this, itemStack);
            else if (KeyBindHelper.USAGE.matchesKey(int_1, int_2))
                return ClientHelper.executeUsageKeyBind(this, itemStack);
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (!GuiHelper.isOverlayVisible())
            return false;
        for(IGuiEventListener listener : children)
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!GuiHelper.isOverlayVisible())
            return false;
        return super.mouseClicked(double_1, double_2, int_1);
    }
    
}
