package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.ItemListOrdering;
import me.shedaniel.rei.client.SearchArgument;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemListOverlay extends Gui implements IWidget {
    
    private static List<Item> searchBlacklisted = Lists.newArrayList();
    private List<IWidget> widgets;
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
                return Minecraft.getInstance().currentScreen.getItemToolTip(itemStack);
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
            return I18n.format("item." + ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().replace(":", "."));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "ERROR";
    }
    
    public int getTotalSlotsPerPage() {
        return width * height;
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        widgets.forEach(widget -> widget.draw(int_1, int_2, float_1));
        EntityPlayerSP player = Minecraft.getInstance().player;
        if (rectangle.contains(ClientHelper.getMouseLocation()) && ClientHelper.isCheating() && !player.inventory.getItemStack().isEmpty() && Minecraft.getInstance().isSingleplayer())
            GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.format("text.rei.delete_items"))));
    }
    
    public void updateList(Rectangle bounds, int page, String searchTerm) {
        this.rectangle = bounds;
        currentDisplayed = processSearchTerm(searchTerm, RoughlyEnoughItemsCore.getItemRegistry().getItemList(), GuiHelper.inventoryStacks);
        this.widgets = Lists.newLinkedList();
        this.page = page;
        calculateListSize(rectangle);
        double startX = rectangle.getCenterX() - width * 9;
        double startY = rectangle.getCenterY() - height * 9;
        this.listArea = new Rectangle((int) startX, (int) startY, width * 18, height * 18);
        for(int i = 0; i < getTotalSlotsPerPage(); i++) {
            int j = i + page * getTotalSlotsPerPage();
            if (j >= currentDisplayed.size())
                break;
            ItemSlotWidget slotWidget = new ItemSlotWidget((int) (startX + (i % width) * 18), (int) (startY + MathHelper.floor(i / width) * 18), Collections.singletonList(currentDisplayed.get(j)), false, true, true) {
                @Override
                protected void drawToolTip(ItemStack itemStack) {
                    EntityPlayerSP player = Minecraft.getInstance().player;
                    if (!ClientHelper.isCheating() || player.inventory.getItemStack().isEmpty() || !Minecraft.getInstance().isSingleplayer())
                        super.drawToolTip(itemStack);
                }
                
                @Override
                public boolean onMouseClick(int button, double mouseX, double mouseY) {
                    if (isHighlighted(mouseX, mouseY)) {
                        if (ClientHelper.isCheating()) {
                            if (getCurrentStack() != null && !getCurrentStack().isEmpty()) {
                                ItemStack cheatedStack = getCurrentStack().copy();
                                cheatedStack.setCount(button == 0 ? 1 : button == 1 ? cheatedStack.getMaxStackSize() : cheatedStack.getCount());
                                return ClientHelper.tryCheatingStack(cheatedStack);
                            }
                        } else if (button == 0)
                            return ClientHelper.executeRecipeKeyBind(getCurrentStack().copy());
                        else if (button == 1)
                            return ClientHelper.executeUsageKeyBind(getCurrentStack().copy());
                    }
                    return false;
                }
            };
            widgets.add(slotWidget);
        }
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        for(IWidget widget : widgets)
            if (widget.keyPressed(int_1, int_2, int_3))
                return true;
        return false;
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
                    return itemGroups.indexOf(itemStack.getItem().getGroup()) - itemGroups.indexOf(t1.getItem().getGroup());
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
                if (itemStack.isItemEqual(workingItem))
                    return true;
            return false;
        }).distinct().collect(Collectors.toList()));
        return finalStacks;
    }
    
    private boolean filterItem(ItemStack itemStack, List<SearchArgument> arguments) {
        String mod = ClientHelper.getModFromItemStack(itemStack);
        List<String> toolTipsList = tryGetItemStackToolTip(itemStack);
        String toolTipsMixed = toolTipsList.stream().skip(1).collect(Collectors.joining()).toLowerCase();
        String allMixed = Stream.of(tryGetItemStackName(itemStack), toolTipsMixed).collect(Collectors.joining()).toLowerCase();
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
        if (rectangle.contains(double_1, double_2)) {
            EntityPlayerSP player = Minecraft.getInstance().player;
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
        }
        return false;
    }
    
    @Override
    public List<IWidget> getListeners() {
        return widgets;
    }
    
}
