package me.shedaniel.gui;

import com.google.common.collect.ImmutableList;
import me.shedaniel.ClientListener;
import me.shedaniel.Core;
import me.shedaniel.config.REIItemListOrdering;
import me.shedaniel.gui.widget.Button;
import me.shedaniel.gui.widget.*;
import me.shedaniel.impl.REIRecipeManager;
import me.shedaniel.listenerdefinitions.IMixinGuiContainer;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.IRegistry;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiItemList extends Drawable {
    
    public final int FOOTERSIZE;
    private GuiContainer overlayedGui;
    private static int page = 0;
    private ArrayList<REISlot> displaySlots;
    protected ArrayList<Control> controls;
    private boolean needsResize = false;
    Button buttonLeft, buttonRight, buttonCheating, buttonConfig;
    private TextBox searchBox;
    private ArrayList<ItemStack> view;
    private Control lastHovered;
    protected boolean visible = true;
    private int oldGuiLeft = 0;
    private boolean cheatMode = false;
    private List<ItemStack> lastPlayerItems = new ArrayList<>();
    
    public GuiItemList(GuiContainer overlayedGui) {
        super(calculateRect(overlayedGui));
        FOOTERSIZE = Core.runtimeConfig.centreSearchBox ? 18 : 44;
        displaySlots = new ArrayList<>();
        controls = new ArrayList<>();
        this.overlayedGui = overlayedGui;
        view = new ArrayList<>();
        resize();
    }
    
    public boolean canCheat() {
        EntityPlayerSP player = Minecraft.getInstance().player;
        if (cheatMode) {
            if (!player.hasPermissionLevel(1)) {
                cheatClicked(0);
                return false;
            }
            return true;
        }
        return false;
    }
    
    private static Rectangle calculateRect(GuiContainer overlayedGui) {
        MainWindow res = REIRenderHelper.getResolution();
        int startX = (((IMixinGuiContainer) overlayedGui).getGuiLeft() + ((IMixinGuiContainer) overlayedGui).getXSize()) + 10;
        int width = res.getScaledWidth() - startX;
        return new Rectangle(startX, 0, width, res.getScaledHeight());
    }
    
    protected void resize() {
        MainWindow res = REIRenderHelper.getResolution();
        
        if (overlayedGui != Minecraft.getInstance().currentScreen) {
            if (Minecraft.getInstance().currentScreen instanceof GuiContainer) {
                overlayedGui = (GuiContainer) Minecraft.getInstance().currentScreen;
            } else {
                needsResize = true;
                return;
            }
        }
        oldGuiLeft = ((IMixinGuiContainer) overlayedGui).getGuiLeft();
        rect = calculateRect(overlayedGui);
        page = 0;
        buttonLeft = new Button(rect.x, rect.y + 3, 16, 20, "<");
        buttonLeft.onClick = this::btnLeftClicked;
        buttonRight = new Button(rect.x + rect.width - 18, rect.y + 3, 16, 20, ">");
        buttonRight.onClick = this::btnRightClicked;
        controls.clear();
        controls.add(buttonLeft);
        controls.add(buttonRight);
        String savedText = "";
        if (searchBox != null) {
            savedText = searchBox.getText();
        }
        searchBox = new TextBox(getSearchBoxArea());
        searchBox.setText(savedText);
        if (Core.config.enableCraftableOnlyButton) {
            CraftableToggleButton buttonCraftableOnly = new CraftableToggleButton(getCraftableToggleArea());
            buttonCraftableOnly.setOnClick(i -> {
                Core.runtimeConfig.craftableOnly = !Core.runtimeConfig.craftableOnly;
                REIRenderHelper.updateSearch();
                return true;
            });
            controls.add(buttonCraftableOnly);
        }
        controls.add(searchBox);
        buttonCheating = new Button(5, 5, 45, 20, getCheatModeText());
        buttonCheating.onClick = this::cheatClicked;
        buttonConfig = new Button(5, 28, 45, 20, I18n.format("text.rei.config"));
        buttonConfig.onClick = i -> {
            Minecraft.getInstance().displayGuiScreen(null);
            Minecraft.getInstance().displayGuiScreen(new ConfigGui(overlayedGui));
            return true;
        };
        controls.add(buttonConfig);
        controls.add(buttonCheating);
        calculateSlots();
        updateView();
        fillSlots();
        controls.addAll(displaySlots);
    }
    
    private Rectangle getSearchBoxArea() {
        int widthOffset = Core.config.enableCraftableOnlyButton ? -24 : 0;
        int ch = ((IMixinGuiContainer) overlayedGui).getContainerHeight(), cw = ((IMixinGuiContainer) overlayedGui).getContainerWidth();
        if (Core.runtimeConfig.centreSearchBox) {
            if (ch + 4 + 18 > Minecraft.getInstance().mainWindow.getScaledHeight()) //Will be out of bounds
                return new Rectangle(overlayedGui.width / 2 - cw / 2, rect.height + 100, cw + widthOffset, 18);
            return new Rectangle(overlayedGui.width / 2 - cw / 2, rect.height - 31, cw + widthOffset, 18);
        }
        return new Rectangle(rect.x, rect.height - 31, rect.width - 4 + widthOffset, 18);
    }
    
    private Rectangle getCraftableToggleArea() {
        Rectangle searchBoxArea = getSearchBoxArea();
        searchBoxArea.setLocation(searchBoxArea.x + searchBoxArea.width + 4, searchBoxArea.y - 1);
        searchBoxArea.setSize(20, 20);
        return searchBoxArea;
    }
    
    private void fillSlots() {
        page = MathHelper.clamp(page, 0, MathHelper.ceil(view.size() / displaySlots.size()));
        int firstSlot = page * displaySlots.size();
        for(int i = 0; i < displaySlots.size(); i++) {
            if (firstSlot + i < view.size() && firstSlot + i >= 0) {
                displaySlots.get(i).setStack(view.get(firstSlot + i));
            } else {
                displaySlots.get(i).setStack(ItemStack.EMPTY);
            }
        }
    }
    
    private void calculateSlots() {
        int x = rect.x, y = rect.y + 20;
        MainWindow res = REIRenderHelper.getResolution();
        displaySlots.clear();
        int xOffset = 0, yOffset = 0, row = 0, perRow = 0, currentX = 0, currentY = 0;
        while (true) {
            xOffset += 18;
            if (row == 0)
                perRow++;
            if (x + xOffset + 22 > res.getScaledWidth()) {
                xOffset = 0;
                yOffset += 18;
                row++;
            }
            if (y + yOffset + 9 + FOOTERSIZE > rect.height) {
                xOffset = 0;
                yOffset = 0;
                break;
            }
        }
        x += (rect.width - perRow * 18) / 2;
        y += (rect.height - FOOTERSIZE - 2 - row * 18) / 2;
        while (true) {
            REISlot slot = new REISlot(x + xOffset, y + yOffset);
            slot.setCheatable(true);
            if (REIRecipeManager.instance().canAddSlot(Minecraft.getInstance().currentScreen.getClass(), slot.rect))
                displaySlots.add(slot);
            xOffset += 18;
            currentX++;
            if (currentX >= perRow) {
                xOffset = 0;
                yOffset += 18;
                currentX = 0;
                currentY++;
            }
            if (currentY >= row)
                break;
        }
    }
    
    @Override
    public void draw() {
        if (!visible)
            return;
        if (Minecraft.getInstance().currentScreen instanceof GuiContainer)
            overlayedGui = (GuiContainer) Minecraft.getInstance().currentScreen;
        if (needsResize == true || oldGuiLeft != ((IMixinGuiContainer) overlayedGui).getGuiLeft())
            resize();
        else if (Core.runtimeConfig.craftableOnly && (!hasSameListContent(new LinkedList<>(lastPlayerItems), getInventoryItemsTypes()) || (getInventoryItemsTypes().size() != lastPlayerItems.size()))) {
            this.lastPlayerItems = new LinkedList<>(getInventoryItemsTypes());
            updateView();
        }
        GlStateManager.pushMatrix();
        updateButtons();
        controls.forEach(Control::draw);
        RenderHelper.disableStandardItemLighting();
        String header = String.format("%s/%s", page + 1, MathHelper.ceil(view.size() / displaySlots.size()) + 1);
        Minecraft.getInstance().fontRenderer.drawStringWithShadow(header, rect.x + (rect.width / 2) - (Minecraft.getInstance().fontRenderer.getStringWidth(header) / 2), rect.y + 10, -1);
        GlStateManager.popMatrix();
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
    
    private void updateButtons() {
        buttonLeft.setEnabled(MathHelper.ceil(view.size() / displaySlots.size()) > 1);
        buttonRight.setEnabled(MathHelper.ceil(view.size() / displaySlots.size()) > 1);
    }
    
    public boolean btnRightClicked(int button) {
        if (button == 0) {
            page++;
            if (page > MathHelper.ceil(view.size() / displaySlots.size()))
                page = 0;
            fillSlots();
            return true;
        }
        return false;
    }
    
    public boolean btnLeftClicked(int button) {
        if (button == 0) {
            page--;
            if (page < 0)
                page = MathHelper.ceil(view.size() / displaySlots.size());
            fillSlots();
            return true;
        }
        return false;
    }
    
    public boolean cheatClicked(int button) {
        if (button == 0) {
            cheatMode = !cheatMode;
            buttonCheating.setString(getCheatModeText());
            return true;
        }
        return false;
    }
    
    private String getCheatModeText() {
        return I18n.format(String.format("%s%s", "text.rei.", cheatMode ? "cheat" : "nocheat"));
    }
    
    protected void updateView() {
        String searchText = searchBox.getText();
        view.clear();
        List<ItemStack> stacks = new ArrayList<>();
        if (ClientListener.stackList != null) {
            List<ItemStack> stackList = new LinkedList<>(ClientListener.stackList);
            List<ItemGroup> itemGroups = new LinkedList<>(Arrays.asList(ItemGroup.GROUPS));
            itemGroups.add(null);
            if (Core.config.itemListOrdering != REIItemListOrdering.REGISTRY)
                Collections.sort(stackList, (itemStack, t1) -> {
                    if (Core.config.itemListOrdering.equals(REIItemListOrdering.NAME))
                        return itemStack.getDisplayName().getFormattedText().compareToIgnoreCase(t1.getDisplayName().getFormattedText());
                    if (Core.config.itemListOrdering.equals(REIItemListOrdering.ITEM_GROUPS))
                        return itemGroups.indexOf(itemStack.getItem().getGroup()) - itemGroups.indexOf(t1.getItem().getGroup());
                    return 0;
                });
            if (!Core.config.isAscending)
                Collections.reverse(stackList);
            Arrays.stream(searchText.split("\\|")).forEachOrdered(s -> {
                List<SearchArgument> arguments = new ArrayList<>();
                while (s.startsWith(" ")) s = s.substring(1);
                while (s.endsWith(" ")) s = s.substring(0, s.length());
                if (s.startsWith("@-") || s.startsWith("-@"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.MOD, s.substring(2), false));
                else if (s.startsWith("@"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.MOD, s.substring(1), true));
                else if (s.startsWith("#-") || s.startsWith("-#"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, s.substring(2), false));
                else if (s.startsWith("#"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, s.substring(1), true));
                else if (s.startsWith("-"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.TEXT, s.substring(1), false));
                else
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.TEXT, s, true));
                stackList.stream().filter(itemStack -> filterItem(itemStack, arguments)).forEachOrdered(stacks::add);
            });
        }
        List<ItemStack> workingItems = ClientListener.stackList == null || (Core.runtimeConfig.craftableOnly && lastPlayerItems.size() > 0) ? new ArrayList<>() : ClientListener.stackList;
        if (Core.runtimeConfig.craftableOnly) {
            REIRecipeManager.instance().findCraftableByItems(lastPlayerItems).forEach(workingItems::add);
            workingItems.addAll(lastPlayerItems);
        }
        final List<ItemStack> finalWorkingItems = workingItems;
        view.addAll(stacks.stream().filter(itemStack -> {
            if (!Core.runtimeConfig.craftableOnly)
                return true;
            for(ItemStack workingItem : finalWorkingItems)
                if (itemStack.isItemEqual(workingItem))
                    return true;
            return false;
        }).distinct().collect(Collectors.toList()));
        page = 0;
        fillSlots();
    }
    
    private List<ItemStack> getInventoryItemsTypes() {
        List<NonNullList<ItemStack>> field_7543 = ImmutableList.of(Minecraft.getInstance().player.inventory.mainInventory, Minecraft.getInstance().player.inventory.armorInventory
                , Minecraft.getInstance().player.inventory.offHandInventory);
        List<ItemStack> inventoryStacks = new ArrayList<>();
        field_7543.forEach(itemStacks -> itemStacks.forEach(itemStack -> {
            if (!itemStack.getItem().equals(Items.AIR))
                inventoryStacks.add(itemStack);
        }));
        return inventoryStacks;
    }
    
    private boolean filterItem(ItemStack itemStack, List<SearchArgument> arguments) {
        String mod = getMod(itemStack);
        List<String> toolTipsList = REIRenderHelper.getOverlayedGui().getItemToolTip(itemStack);
        String toolTipsMixed = toolTipsList.stream().skip(1).collect(Collectors.joining()).toLowerCase();
        String allMixed = Stream.of(itemStack.getDisplayName().getString(), toolTipsMixed).collect(Collectors.joining()).toLowerCase();
        for(SearchArgument searchArgument : arguments.stream().filter(searchArgument -> !searchArgument.isInclude()).collect(Collectors.toList())) {
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.MOD))
                if (mod.toLowerCase().contains(searchArgument.getText().toLowerCase()))
                    return false;
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.TOOLTIP))
                if (toolTipsMixed.contains(searchArgument.getText().toLowerCase()))
                    return false;
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.TEXT))
                if (allMixed.contains(searchArgument.getText().toLowerCase()))
                    return false;
        }
        for(SearchArgument searchArgument : arguments.stream().filter(SearchArgument::isInclude).collect(Collectors.toList())) {
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.MOD))
                if (!mod.toLowerCase().contains(searchArgument.getText().toLowerCase()))
                    return false;
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.TOOLTIP))
                if (!toolTipsMixed.contains(searchArgument.getText().toLowerCase()))
                    return false;
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.TEXT))
                if (!allMixed.contains(searchArgument.getText().toLowerCase()))
                    return false;
        }
        return true;
    }
    
    public void tick() {
        controls.forEach(f -> f.tick());
    }
    
    public void setLastHovered(Control ctrl) {
        lastHovered = ctrl;
    }
    
    public Control getLastHovered() {
        return lastHovered;
    }
    
    private String getMod(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            ResourceLocation location = IRegistry.ITEM.getKey(stack.getItem());
            return location.getNamespace();
        }
        return "";
    }
    
}
