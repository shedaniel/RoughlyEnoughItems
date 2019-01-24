package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.*;
import me.shedaniel.rei.listeners.ClientLoaded;
import me.shedaniel.rei.listeners.IMixinGuiContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemListOverlay extends Gui implements IWidget {
    
    private IMixinGuiContainer containerGui;
    private List<IWidget> widgets = new ArrayList<>();
    private int width, height, page;
    private Rectangle rectangle;
    private List<ItemStack> currentDisplayed;
    
    public ItemListOverlay(IMixinGuiContainer containerGui, int page) {
        this.currentDisplayed = Lists.newArrayList();
        this.containerGui = containerGui;
        this.width = 0;
        this.height = 0;
        this.page = page;
    }
    
    public int getTotalSlotsPerPage() {
        return width * height;
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        widgets.forEach(widget -> widget.draw(int_1, int_2, float_1));
        EntityPlayerSP player = Minecraft.getInstance().player;
        if (rectangle.contains(ClientHelper.getMouseLocation()) && ClientHelper.isCheating() && !player.inventory.getItemStack().isEmpty() && Minecraft.getInstance().isSingleplayer())
            GuiHelper.getOverlay(containerGui.getContainerGui()).addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.format("text.rei.delete_items"))));
    }
    
    public void updateList(int page, String searchTerm) {
        updateList(rectangle, page, searchTerm);
    }
    
    public void updateList(Rectangle rect, int page, String searchTerm) {
        this.rectangle = rect;
        if (ClientHelper.getItemList().isEmpty())
            RoughlyEnoughItemsCore.getListeners(ClientLoaded.class).forEach(ClientLoaded::clientLoaded);
        currentDisplayed = processSearchTerm(searchTerm, ClientHelper.getItemList(), GuiHelper.inventoryStacks);
        this.widgets.clear();
        this.page = page;
        calculateListSize(rect);
        double startX = rect.getCenterX() - width * 9;
        double startY = rect.getCenterY() - height * 9;
        for(int i = 0; i < getTotalSlotsPerPage(); i++) {
            int j = i + page * getTotalSlotsPerPage();
            if (j >= currentDisplayed.size())
                break;
            widgets.add(new ItemSlotWidget((int) (startX + (i % width) * 18), (int) (startY + MathHelper.floor(i / width) * 18), currentDisplayed.get(j), false, true, containerGui) {
                @Override
                protected void drawToolTip(ItemStack itemStack) {
                    EntityPlayerSP player = Minecraft.getInstance().player;
                    if (!ClientHelper.isCheating() || player.inventory.getItemStack().isEmpty() || !Minecraft.getInstance().isSingleplayer())
                        super.drawToolTip(itemStack);
                }
                
                @Override
                public boolean onMouseClick(int button, double mouseX, double mouseY) {
                    if (getBounds().contains(mouseX, mouseY)) {
                        if (ClientHelper.isCheating()) {
                            if (getCurrentStack() != null && !getCurrentStack().isEmpty()) {
                                ItemStack cheatedStack = getCurrentStack().copy();
                                cheatedStack.setCount(button == 0 ? 1 : button == 1 ? cheatedStack.getMaxStackSize() : cheatedStack.getCount());
                                return ClientHelper.tryCheatingStack(cheatedStack);
                            }
                        } else {
                            if (button == 0)
                                return ClientHelper.executeRecipeKeyBind(GuiHelper.getOverlay(containerGui.getContainerGui()), getCurrentStack().copy(), containerGui);
                            else if (button == 1)
                                return ClientHelper.executeUsageKeyBind(GuiHelper.getOverlay(containerGui.getContainerGui()), getCurrentStack().copy(), containerGui);
                        }
                    }
                    return false;
                }
            });
        }
    }
    
    public List<ItemStack> getCurrentDisplayed() {
        return currentDisplayed;
    }
    
    private List<ItemStack> processSearchTerm(String searchTerm, List<ItemStack> ol, List<ItemStack> inventoryItems) {
        List<ItemStack> os = new LinkedList<>(ol), stacks = Lists.newArrayList(), finalStacks = Lists.newArrayList();
        List<ItemGroup> itemGroups = new LinkedList<>(Arrays.asList(ItemGroup.GROUPS));
        itemGroups.add(null);
        REIItemListOrdering ordering = RoughlyEnoughItemsCore.getConfigHelper().getItemListOrdering();
        if (ordering != REIItemListOrdering.REGISTRY)
            Collections.sort(os, (itemStack, t1) -> {
                if (ordering.equals(REIItemListOrdering.NAME))
                    return itemStack.getDisplayName().getFormattedText().compareToIgnoreCase(t1.getDisplayName().getFormattedText());
                if (ordering.equals(REIItemListOrdering.ITEM_GROUPS))
                    return itemGroups.indexOf(itemStack.getItem().getGroup()) - itemGroups.indexOf(t1.getItem().getGroup());
                return 0;
            });
        if (!RoughlyEnoughItemsCore.getConfigHelper().isAscending())
            Collections.reverse(os);
        String[] splitSearchTerm = StringUtils.splitByWholeSeparatorPreserveAllTokens(searchTerm, "|");
        Arrays.stream(splitSearchTerm).forEachOrdered(s -> {
            List<SearchArgument> arguments = Lists.newArrayList();
            Arrays.stream(StringUtils.split(s)).forEachOrdered(s1 -> {
                if (s1.startsWith("@-") || s1.startsWith("-@"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.MOD, s1.substring(2), false));
                else if (s1.startsWith("@"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.MOD, s1.substring(1), true));
                else if (s1.startsWith("#-") || s1.startsWith("-#"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, s1.substring(2), false));
                else if (s1.startsWith("#"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, s1.substring(1), true));
                else if (s1.startsWith("-"))
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.TEXT, s1.substring(1), false));
                else
                    arguments.add(new SearchArgument(SearchArgument.ArgumentType.TEXT, s1, true));
            });
            os.stream().filter(itemStack -> arguments.isEmpty() || filterItem(itemStack, arguments)).forEachOrdered(stacks::add);
        });
        if (splitSearchTerm.length == 0)
            stacks.addAll(os);
        List<ItemStack> workingItems = RoughlyEnoughItemsCore.getConfigHelper().craftableOnly() && inventoryItems.size() > 0 ? new ArrayList<>() : new LinkedList<>(ol);
        if (RoughlyEnoughItemsCore.getConfigHelper().craftableOnly()) {
            RecipeHelper.findCraftableByItems(inventoryItems).forEach(workingItems::add);
            workingItems.addAll(inventoryItems);
        }
        final List<ItemStack> finalWorkingItems = workingItems;
        finalStacks.addAll(stacks.stream().filter(itemStack -> {
            if (!RoughlyEnoughItemsCore.getConfigHelper().craftableOnly())
                return true;
            for(ItemStack workingItem : finalWorkingItems)
                if (itemStack.isItemEqual(workingItem))
                    return true;
            return false;
        }).distinct().collect(Collectors.toList()));
        return finalStacks;
    }
    
    private boolean filterItem(ItemStack itemStack, List<SearchArgument> arguments) {
        String mod = ClientHelper.getModFromItemStack(itemStack);
        List<String> toolTipsList = getStackTooltip(itemStack);
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
    
    private List<String> getStackTooltip(ItemStack itemStack) {
        Minecraft client = Minecraft.getInstance();
        return itemStack.getTooltip(client.player, client.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL).stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
    }
    
    private void calculateListSize(Rectangle rect) {
        int xOffset = 0, yOffset = 0;
        this.width = 0;
        this.height = 0;
        while (true) {
            xOffset += 18;
            if (height == 0)
                width++;
            if (xOffset + 19 > rect.width) {
                xOffset = 0;
                yOffset += 18;
                height++;
            }
            if (yOffset + 19 > rect.height)
                break;
        }
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        EntityPlayerSP player = Minecraft.getInstance().player;
        if (rectangle.contains(double_1, double_2))
            if (ClientHelper.isCheating() && !player.inventory.getItemStack().isEmpty() && Minecraft.getInstance().isSingleplayer()) {
                ClientHelper.sendDeletePacket();
                return true;
            }
        if (!player.inventory.getItemStack().isEmpty() && Minecraft.getInstance().isSingleplayer())
            return false;
        if (onMouseClick(int_1, double_1, double_2))
            return true;
        for(IWidget widget : getListeners())
            if (widget.mouseClicked(double_1, double_2, int_1))
                return true;
        return false;
    }
    
    @Override
    public List<IWidget> getListeners() {
        return widgets;
    }
    
}
