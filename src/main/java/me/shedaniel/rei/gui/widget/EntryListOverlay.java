/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.client.SearchArgument;
import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntryListOverlay extends Widget {
    
    private static final String SPACE = " ", EMPTY = "";
    private static final Comparator<Entry> ASCENDING_COMPARATOR;
    private static List<Item> searchBlacklisted = Lists.newArrayList();
    
    static {
        ASCENDING_COMPARATOR = (entry, entry1) -> {
            if (RoughlyEnoughItemsCore.getConfigManager().getConfig().getItemListOrdering().equals(ItemListOrdering.name))
                return tryGetEntryName(entry).compareToIgnoreCase(tryGetEntryName(entry1));
            if (RoughlyEnoughItemsCore.getConfigManager().getConfig().getItemListOrdering().equals(ItemListOrdering.item_groups)) {
                if (entry.getEntryType() == Entry.Type.ITEM && entry1.getEntryType() == Entry.Type.ITEM) {
                    ItemStack stack0 = entry.getItemStack();
                    ItemStack stack1 = entry1.getItemStack();
                    List<ItemGroup> itemGroups = Arrays.asList(ItemGroup.GROUPS);
                    return itemGroups.indexOf(stack0.getItem().getGroup()) - itemGroups.indexOf(stack1.getItem().getGroup());
                }
            }
            return 0;
        };
    }
    
    private final List<SearchArgument[]> lastSearchArgument;
    private List<Entry> currentDisplayed;
    private List<Widget> widgets;
    private int width, height, page;
    private Rectangle rectangle, listArea;
    
    public EntryListOverlay(int page) {
        this.currentDisplayed = Lists.newArrayList();
        this.width = 0;
        this.height = 0;
        this.page = page;
        this.lastSearchArgument = Lists.newArrayList();
    }
    
    public static List<String> tryGetItemStackToolTip(ItemStack itemStack, boolean careAboutAdvanced) {
        if (!searchBlacklisted.contains(itemStack.getItem()))
            try {
                return itemStack.getTooltip(MinecraftClient.getInstance().player, MinecraftClient.getInstance().options.advancedItemTooltips && careAboutAdvanced ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL).stream().map(Text::asFormattedString).collect(Collectors.toList());
            } catch (Throwable e) {
                e.printStackTrace();
                searchBlacklisted.add(itemStack.getItem());
            }
        return Collections.singletonList(tryGetItemStackName(itemStack));
    }
    
    public static String tryGetEntryName(Entry stack) {
        if (stack.getEntryType() == Entry.Type.ITEM)
            return tryGetItemStackName(stack.getItemStack());
        else if (stack.getEntryType() == Entry.Type.FLUID)
            return tryGetFluidName(stack.getFluid());
        return "";
    }
    
    public static String tryGetFluidName(Fluid fluid) {
        return Stream.of(Registry.FLUID.getId(fluid).getPath().split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }
    
    public static String tryGetItemStackName(ItemStack stack) {
        if (!searchBlacklisted.contains(stack.getItem()))
            try {
                return stack.getName().asFormattedString();
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
    
    public static boolean filterEntry(Entry entry, List<SearchArgument[]> arguments) {
        if (arguments.isEmpty())
            return true;
        AtomicReference<String> mod = new AtomicReference<>(), tooltips = new AtomicReference<>(), name = new AtomicReference<>();
        for (SearchArgument[] arguments1 : arguments) {
            boolean b = true;
            for (SearchArgument argument : arguments1) {
                if (argument.getArgumentType() == (SearchArgument.ArgumentType.ALWAYS))
                    return true;
                if (argument.getArgumentType() == SearchArgument.ArgumentType.MOD)
                    if (argument.getFunction(!argument.isInclude()).apply(fillMod(entry, mod).get())) {
                        b = false;
                        break;
                    }
                if (argument.getArgumentType() == SearchArgument.ArgumentType.TOOLTIP)
                    if (argument.getFunction(!argument.isInclude()).apply(fillTooltip(entry, tooltips).get())) {
                        b = false;
                        break;
                    }
                if (argument.getArgumentType() == SearchArgument.ArgumentType.TEXT)
                    if (argument.getFunction(!argument.isInclude()).apply(fillName(entry, name).get())) {
                        b = false;
                        break;
                    }
            }
            if (b)
                return true;
        }
        return false;
    }
    
    private static AtomicReference<String> fillMod(Entry entry, AtomicReference<String> mod) {
        if (mod.get() == null)
            if (entry.getEntryType() == Entry.Type.ITEM)
                mod.set(ClientHelper.getInstance().getModFromItem(entry.getItemStack().getItem()).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
            else if (entry.getEntryType() == Entry.Type.FLUID)
                mod.set(ClientHelper.getInstance().getModFromIdentifier(Registry.FLUID.getId(entry.getFluid())).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
        return mod;
    }
    
    private static AtomicReference<String> fillTooltip(Entry entry, AtomicReference<String> mod) {
        if (mod.get() == null)
            if (entry.getEntryType() == Entry.Type.ITEM)
                mod.set(tryGetItemStackToolTip(entry.getItemStack(), false).stream().collect(Collectors.joining("")).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
            else
                mod.set(tryGetFluidName(entry.getFluid()).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
        return mod;
    }
    
    private static AtomicReference<String> fillName(Entry entry, AtomicReference<String> mod) {
        if (mod.get() == null)
            if (entry.getEntryType() == Entry.Type.ITEM)
                mod.set(tryGetItemStackName(entry.getItemStack()).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
            else
                mod.set(tryGetFluidName(entry.getFluid()).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
        return mod;
    }
    
    public int getFullTotalSlotsPerPage() {
        return width * height;
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        GuiLighting.disable();
        widgets.forEach(widget -> widget.render(int_1, int_2, float_1));
        ClientPlayerEntity player = minecraft.player;
        if (rectangle.contains(ClientUtils.getMouseLocation()) && ClientHelper.getInstance().isCheating() && !player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.hasPermissionToUsePackets())
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(I18n.translate("text.rei.delete_items")));
    }
    
    public void updateList(DisplayHelper.DisplayBoundsHandler boundsHandler, Rectangle rectangle, int page, String searchTerm, boolean processSearchTerm) {
        this.rectangle = rectangle;
        this.page = page;
        this.widgets = Lists.newLinkedList();
        calculateListSize(rectangle);
        if (currentDisplayed.isEmpty() || processSearchTerm)
            currentDisplayed = processSearchTerm(searchTerm, RoughlyEnoughItemsCore.getEntryRegistry().getEntryList(), new ArrayList<>(ScreenHelper.inventoryStacks));
        int startX = (int) rectangle.getCenterX() - width * 9;
        int startY = (int) rectangle.getCenterY() - height * 9;
        this.listArea = new Rectangle((int) startX, (int) startY, width * 18, height * 18);
        int fitSlotsPerPage = getTotalFitSlotsPerPage(startX, startY, listArea);
        int j = page * fitSlotsPerPage;
        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                int x = startX + xx * 18, y = startY + yy * 18;
                if (!canBeFit(x, y, listArea))
                    continue;
                j++;
                if (j > currentDisplayed.size())
                    break;
                final Entry entry = currentDisplayed.get(j - 1);
                widgets.add(new SlotWidget(x, y, entry.getEntryType() == Entry.Type.ITEM ? Renderer.fromItemStackNoCounts(entry.getItemStack()) : Renderer.fromFluid(entry.getFluid()), false, true, true) {
                    @Override
                    protected void queueTooltip(ItemStack itemStack, float delta) {
                        ClientPlayerEntity player = minecraft.player;
                        if (!ClientHelper.getInstance().isCheating() || player.inventory.getCursorStack().isEmpty())
                            super.queueTooltip(itemStack, delta);
                    }
                    
                    @Override
                    protected List<String> getExtraFluidToolTips(Fluid fluid) {
                        if (MinecraftClient.getInstance().options.advancedItemTooltips)
                            return Collections.singletonList("§8" + Registry.FLUID.getId(fluid).toString());
                        return super.getExtraFluidToolTips(fluid);
                    }
                    
                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (isCurrentRendererItem() && containsMouse(mouseX, mouseY)) {
                            if (ClientHelper.getInstance().isCheating()) {
                                if (getCurrentItemStack() != null && !getCurrentItemStack().isEmpty()) {
                                    ItemStack cheatedStack = getCurrentItemStack().copy();
                                    if (RoughlyEnoughItemsCore.getConfigManager().getConfig().getItemCheatingMode() == ItemCheatingMode.REI_LIKE)
                                        cheatedStack.setCount(button != 1 ? 1 : cheatedStack.getMaxCount());
                                    else if (RoughlyEnoughItemsCore.getConfigManager().getConfig().getItemCheatingMode() == ItemCheatingMode.JEI_LIKE)
                                        cheatedStack.setCount(button != 0 ? 1 : cheatedStack.getMaxCount());
                                    else
                                        cheatedStack.setCount(1);
                                    return ClientHelper.getInstance().tryCheatingStack(cheatedStack);
                                }
                            } else if (button == 0)
                                return ClientHelper.getInstance().executeRecipeKeyBind(getCurrentItemStack().copy());
                            else if (button == 1)
                                return ClientHelper.getInstance().executeUsageKeyBind(getCurrentItemStack().copy());
                        }
                        return false;
                    }
                });
            }
            if (j > currentDisplayed.size())
                break;
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
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (canBeFit(startX + x * 18, startY + y * 18, listArea))
                    slots++;
        return slots;
    }
    
    public boolean canBeFit(int left, int top, Rectangle listArea) {
        for (DisplayHelper.DisplayBoundsHandler sortedBoundsHandler : RoughlyEnoughItemsCore.getDisplayHelper().getSortedBoundsHandlers(minecraft.currentScreen.getClass())) {
            ActionResult fit = sortedBoundsHandler.canItemSlotWidgetFit(!RoughlyEnoughItemsCore.getConfigManager().getConfig().isLeftHandSidePanel(), left, top, minecraft.currentScreen, listArea);
            if (fit != ActionResult.PASS)
                return fit == ActionResult.SUCCESS;
        }
        return true;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        for (Widget widget : widgets)
            if (widget.keyPressed(int_1, int_2, int_3))
                return true;
        return false;
    }
    
    public Rectangle getListArea() {
        return listArea;
    }
    
    public List<Entry> getCurrentDisplayed() {
        return currentDisplayed;
    }
    
    private List<Entry> processSearchTerm(String searchTerm, List<Entry> ol, List<ItemStack> inventoryItems) {
        lastSearchArgument.clear();
        List<Entry> os = new LinkedList<>(ol);
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().getItemListOrdering() != ItemListOrdering.registry)
            os = ol.stream().sorted(ASCENDING_COMPARATOR).collect(Collectors.toList());
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().isItemListAscending())
            Collections.reverse(os);
        String[] splitSearchTerm = StringUtils.splitByWholeSeparatorPreserveAllTokens(searchTerm, "|");
        Arrays.stream(splitSearchTerm).forEachOrdered(s -> {
            String[] split = StringUtils.split(s);
            SearchArgument[] arguments = new SearchArgument[split.length];
            for (int i = 0; i < split.length; i++) {
                String s1 = split[i];
                if (s1.startsWith("@-") || s1.startsWith("-@"))
                    arguments[i] = new SearchArgument(SearchArgument.ArgumentType.MOD, s1.substring(2), false);
                else if (s1.startsWith("@"))
                    arguments[i] = new SearchArgument(SearchArgument.ArgumentType.MOD, s1.substring(1), true);
                else if (s1.startsWith("#-") || s1.startsWith("-#"))
                    arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, s1.substring(2), false);
                else if (s1.startsWith("#"))
                    arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, s1.substring(1), true);
                else if (s1.startsWith("-"))
                    arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TEXT, s1.substring(1), false);
                else
                    arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TEXT, s1, true);
            }
            if (arguments.length > 0)
                lastSearchArgument.add(arguments);
            else
                lastSearchArgument.add(new SearchArgument[]{SearchArgument.ALWAYS});
        });
        List<Entry> stacks = Collections.emptyList();
        if (lastSearchArgument.isEmpty())
            stacks = os;
        else
            stacks = os.stream().filter(entry -> filterEntry(entry, lastSearchArgument)).collect(Collectors.toList());
        if (!RoughlyEnoughItemsCore.getConfigManager().isCraftableOnlyEnabled() || stacks.isEmpty() || inventoryItems.isEmpty())
            return Collections.unmodifiableList(stacks);
        List<ItemStack> workingItems = RecipeHelper.getInstance().findCraftableByItems(inventoryItems);
        List<Entry> newList = Lists.newArrayList();
        for (ItemStack workingItem : workingItems) {
            Optional<Entry> any = stacks.stream().filter(i -> i.getItemStack() != null && i.getItemStack().isItemEqualIgnoreDamage(workingItem)).findAny();
            //            if (stacks.stream().anyMatch(i -> i.getItemStack() != null && i.getItemStack().isItemEqualIgnoreDamage(workingItem)))
            //                newList.add(Entry.create(workingItem));
            if (any.isPresent())
                newList.add(any.get());
        }
        return newList;
    }
    
    public List<SearchArgument[]> getLastSearchArgument() {
        return lastSearchArgument;
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
            if (ClientHelper.getInstance().isCheating() && !player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.hasPermissionToUsePackets()) {
                ClientHelper.getInstance().sendDeletePacket();
                return true;
            }
            if (!player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.hasPermissionToUsePackets())
                return false;
            for (Widget widget : children())
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
