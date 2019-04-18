package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.ItemCheatingMode;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ItemListOrdering;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.client.SearchArgument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ItemListOverlay extends Widget {
    
    private static List<Item> searchBlacklisted = Lists.newArrayList();
    private List<Widget> widgets;
    private int width, height, page;
    private Rectangle rectangle, listArea;
    private List<ItemStack> currentDisplayed;
    
    public ItemListOverlay(int page) {
        this.currentDisplayed = Lists.newArrayList();
        this.width = 0;
        this.height = 0;
        this.page = page;
    }
    
    public static List<String> tryGetItemStackToolTip(ItemStack itemStack) {
        if (!searchBlacklisted.contains(itemStack.getItem()))
            try {
                return MinecraftClient.getInstance().currentScreen.getTooltipFromItem(itemStack);
            } catch (Throwable e) {
                e.printStackTrace();
                searchBlacklisted.add(itemStack.getItem());
            }
        return Collections.singletonList(tryGetItemStackName(itemStack));
    }
    
    public static String tryGetItemStackName(ItemStack stack) {
        if (!searchBlacklisted.contains(stack.getItem()))
            try {
                return stack.getDisplayName().getFormattedText();
            } catch (Throwable e) {
                e.printStackTrace();
                searchBlacklisted.add(stack.getItem());
            }
        try {
            return I18n.translate("item." + Registry.ITEM.getId(stack.getItem()).toString().replace(":", "."));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "ERROR";
    }
    
    public int getFullTotalSlotsPerPage() {
        return width * height;
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        GuiLighting.disable();
        widgets.forEach(widget -> widget.render(int_1, int_2, float_1));
        ClientPlayerEntity player = minecraft.player;
        if (rectangle.contains(ClientUtils.getMouseLocation()) && ClientHelper.isCheating() && !player.inventory.getCursorStack().isEmpty() && minecraft.isInSingleplayer())
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(I18n.translate("text.rei.delete_items")));
    }
    
    public void updateList(DisplayHelper.DisplayBoundsHandler boundsHandler, Rectangle rectangle, int page, String searchTerm) {
        this.rectangle = rectangle;
        this.page = page;
        this.widgets = Lists.newLinkedList();
        calculateListSize(rectangle);
        currentDisplayed = processSearchTerm(searchTerm, RoughlyEnoughItemsCore.getItemRegisterer().getItemList(), ScreenHelper.inventoryStacks);
        int startX = (int) rectangle.getCenterX() - width * 9;
        int startY = (int) rectangle.getCenterY() - height * 9;
        this.listArea = new Rectangle((int) startX, (int) startY, width * 18, height * 18);
        int fitSlotsPerPage = getTotalFitSlotsPerPage(listArea.x, listArea.y, listArea);
        int j = page * fitSlotsPerPage;
        for(int i = 0; i < getFullTotalSlotsPerPage(); i++) {
            if (j >= currentDisplayed.size())
                break;
            int x = startX + (i % width) * 18, y = startY + MathHelper.floor(i / width) * 18;
            if (!canBeFit(x, y, listArea))
                continue;
            j++;
            widgets.add(new ItemSlotWidget(x, y, Collections.singletonList(currentDisplayed.get(j)), false, true, true) {
                @Override
                protected void queueTooltip(ItemStack itemStack, float delta) {
                    ClientPlayerEntity player = minecraft.player;
                    if (!ClientHelper.isCheating() || player.inventory.getCursorStack().isEmpty())
                        super.queueTooltip(itemStack, delta);
                }
                
                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    if (isHighlighted(mouseX, mouseY)) {
                        if (ClientHelper.isCheating()) {
                            if (getCurrentStack() != null && !getCurrentStack().isEmpty()) {
                                ItemStack cheatedStack = getCurrentStack().copy();
                                if (RoughlyEnoughItemsCore.getConfigManager().getConfig().itemCheatingMode == ItemCheatingMode.REI_LIKE)
                                    cheatedStack.setAmount(button != 1 ? 1 : cheatedStack.getMaxAmount());
                                else if (RoughlyEnoughItemsCore.getConfigManager().getConfig().itemCheatingMode == ItemCheatingMode.JEI_LIKE)
                                    cheatedStack.setAmount(button != 0 ? 1 : cheatedStack.getMaxAmount());
                                else
                                    cheatedStack.setAmount(1);
                                return ClientHelper.tryCheatingStack(cheatedStack);
                            }
                        } else if (button == 0)
                            return ClientHelper.executeRecipeKeyBind(getCurrentStack().copy());
                        else if (button == 1)
                            return ClientHelper.executeUsageKeyBind(getCurrentStack().copy());
                    }
                    return false;
                }
            });
        }
    }
    
    public int getTotalPage() {
        int fitSlotsPerPage = getTotalFitSlotsPerPage(listArea.x, listArea.y, listArea);
        if (fitSlotsPerPage > 0)
            return MathHelper.ceil(getCurrentDisplayed().size() / fitSlotsPerPage);
        return 0;
    }
    
    public int getTotalFitSlotsPerPage(int startX, int startY, Rectangle listArea) {
        int slots = 0;
        for(int i = 0; i < getFullTotalSlotsPerPage(); i++)
            if (canBeFit(startX + (i % width) * 18, startY + MathHelper.floor(i / width) * 18, listArea))
                slots++;
        return slots;
    }
    
    public boolean canBeFit(int left, int top, Rectangle listArea) {
        List<DisplayHelper.DisplayBoundsHandler> sortedBoundsHandlers = RoughlyEnoughItemsCore.getDisplayHelper().getSortedBoundsHandlers(minecraft.currentScreen.getClass());
        ActionResult result = ActionResult.SUCCESS;
        for(DisplayHelper.DisplayBoundsHandler sortedBoundsHandler : sortedBoundsHandlers) {
            ActionResult fit = sortedBoundsHandler.canItemSlotWidgetFit(!RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel, left, top, minecraft.currentScreen, listArea);
            if (fit != ActionResult.PASS) {
                result = fit;
                break;
            }
        }
        return result == ActionResult.SUCCESS;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        for(Widget widget : widgets)
            if (widget.keyPressed(int_1, int_2, int_3))
                return true;
        return false;
    }
    
    public Rectangle getListArea() {
        return listArea;
    }
    
    public List<ItemStack> getCurrentDisplayed() {
        return currentDisplayed;
    }
    
    private List<ItemStack> processSearchTerm(String searchTerm, List<ItemStack> ol, List<ItemStack> inventoryItems) {
        List<ItemStack> os = new LinkedList<>(ol), stacks = Lists.newArrayList(), finalStacks = Lists.newArrayList();
        List<ItemGroup> itemGroups = new LinkedList<>(Arrays.asList(ItemGroup.GROUPS));
        itemGroups.add(null);
        ItemListOrdering ordering = RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering;
        if (ordering != ItemListOrdering.registry)
            Collections.sort(os, (itemStack, t1) -> {
                if (ordering.equals(ItemListOrdering.name))
                    return tryGetItemStackName(itemStack).compareToIgnoreCase(tryGetItemStackName(t1));
                if (ordering.equals(ItemListOrdering.item_groups))
                    return itemGroups.indexOf(itemStack.getItem().getItemGroup()) - itemGroups.indexOf(t1.getItem().getItemGroup());
                return 0;
            });
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending)
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
        List<ItemStack> workingItems = RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() && inventoryItems.size() > 0 ? new ArrayList<>() : new LinkedList<>(ol);
        if (RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled()) {
            RecipeHelper.getInstance().findCraftableByItems(inventoryItems).forEach(workingItems::add);
            workingItems.addAll(inventoryItems);
        }
        final List<ItemStack> finalWorkingItems = workingItems;
        finalStacks.addAll(stacks.stream().filter(itemStack -> {
            if (!RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled())
                return true;
            for(ItemStack workingItem : finalWorkingItems)
                if (itemStack.isEqualIgnoreTags(workingItem))
                    return true;
            return false;
        }).distinct().collect(Collectors.toList()));
        return finalStacks;
    }
    
    private boolean filterItem(ItemStack itemStack, List<SearchArgument> arguments) {
        String mod = ClientHelper.getModFromItem(itemStack.getItem());
        List<String> toolTipsList = tryGetItemStackToolTip(itemStack);
        String toolTipsMixed = toolTipsList.stream().skip(1).collect(Collectors.joining()).toLowerCase();
        String allMixed = toolTipsList.stream().collect(Collectors.joining()).toLowerCase();
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
    
    public void calculateListSize(Rectangle rect) {
        int xOffset = 0, yOffset = 0;
        width = 0;
        height = 0;
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
        if (rectangle.contains(double_1, double_2)) {
            ClientPlayerEntity player = minecraft.player;
            if (ClientHelper.isCheating() && !player.inventory.getCursorStack().isEmpty() && minecraft.isInSingleplayer()) {
                ClientHelper.sendDeletePacket();
                return true;
            }
            if (!player.inventory.getCursorStack().isEmpty() && minecraft.isInSingleplayer())
                return false;
            for(Widget widget : children())
                if (widget.mouseClicked(double_1, double_2, int_1))
                    return true;
        }
        return false;
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
}
