/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.zeitheron.hammercore.client.utils.Scissors;
import me.shedaniel.clothconfig2.api.RunSixtyTimesEverySec;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.SearchArgument;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntryListWidget extends Widget {
    
    private static final String SPACE = " ", EMPTY = "";
    private static final Comparator<Entry> ASCENDING_COMPARATOR;
    private static List<Item> searchBlacklisted = Lists.newArrayList();
    private static float scroll;
    private static float scrollVelocity;
    private static float maxScroll;
    protected static RunSixtyTimesEverySec scroller = () -> {
        try {
            if (scrollVelocity == 0.0F && scroll >= 0.0F && scroll <= getMaxScroll()) {
                scrollerUnregisterTick();
            } else {
                float change = scrollVelocity * 0.3F;
                if (scrollVelocity != 0) {
                    scroll += change;
                    scrollVelocity -= scrollVelocity * (scroll >= 0.0F && scroll <= getMaxScroll() ? 0.2D : 0.4D);
                    if (Math.abs(scrollVelocity) < 0.1F) {
                        scrollVelocity = 0.0F;
                    }
                }
                
                if (scroll < 0.0F && scrollVelocity == 0.0F) {
                    scroll = Math.min(scroll + (0.0F - scroll) * 0.2F, 0.0F);
                    if (Math.abs(scroll) < 0.1F)
                        scroll = 0.0F;
                } else if (scroll > getMaxScroll() && scrollVelocity == 0.0F) {
                    scroll = Math.max(scroll - (scroll - getMaxScroll()) * 0.2F, getMaxScroll());
                    if (scroll > getMaxScroll() && scroll < getMaxScroll() + 0.1F) {
                        scroll = getMaxScroll();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
    
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
    private List<Slot> widgets;
    private int width, height, page;
    private Rectangle rectangle, listArea;
    
    public EntryListWidget(int page) {
        this.currentDisplayed = Lists.newArrayList();
        this.width = 0;
        this.height = 0;
        this.page = page;
        this.lastSearchArgument = Lists.newArrayList();
        scroller.unregisterTick();
        this.scrollVelocity = 0;
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
    
    private static void scrollerUnregisterTick() {
        scroller.unregisterTick();
    }
    
    public static float getMaxScroll() {
        return Math.max(maxScroll - ScreenHelper.getLastOverlay().getEntryListWidget().rectangle.height, 0);
    }
    
    public static float getScroll() {
        return scroll;
    }
    
    public static float getScrollVelocity() {
        return scrollVelocity;
    }
    
    public int getFullTotalSlotsPerPage() {
        return width * height;
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (!this.scroller.isRegistered())
            this.scroller.registerTick();
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().isEntryListWidgetScrolled() && rectangle.contains(double_1, double_2)) {
            if (this.scroll >= 0F && double_3 > 0)
                scrollVelocity -= 24;
            else if (this.scroll <= this.getMaxScroll() && double_3 < 0)
                scrollVelocity += 24;
            return true;
        }
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        GuiLighting.disable();
        RenderHelper.pushMatrix();
        boolean widgetScrolled = RoughlyEnoughItemsCore.getConfigManager().getConfig().isEntryListWidgetScrolled();
        if (!widgetScrolled)
            scroll = 0;
        else {
            page = 0;
            ScreenHelper.getLastOverlay().setPage(0);
            Scissors.begin();
            Scissors.scissor(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
        widgets.forEach(widget -> {
            if (widgetScrolled) {
                widget.y = (int) (widget.backupY - scroll);
                if (widget.y <= rectangle.y + rectangle.height && widget.y + widget.getBounds().height >= rectangle.y)
                    widget.render(int_1, int_2, float_1);
            } else {
                widget.render(int_1, int_2, float_1);
            }
        });
        if (widgetScrolled)
            Scissors.end();
        RenderHelper.popMatrix();
        ClientPlayerEntity player = minecraft.player;
        if (rectangle.contains(PointHelper.fromMouse()) && ClientHelper.getInstance().isCheating() && !player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.hasPermissionToUsePackets())
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(I18n.translate("text.rei.delete_items")));
    }
    
    public void updateList(DisplayHelper.DisplayBoundsHandler boundsHandler, Rectangle rectangle, int page, String searchTerm, boolean processSearchTerm) {
        this.rectangle = rectangle;
        this.page = page;
        this.widgets = Lists.newCopyOnWriteArrayList();
        calculateListSize(rectangle);
        if (currentDisplayed.isEmpty() || processSearchTerm)
            currentDisplayed = processSearchTerm(searchTerm, RoughlyEnoughItemsCore.getEntryRegistry().getEntryList(), new ArrayList<>(ScreenHelper.inventoryStacks));
        int startX = rectangle.getCenterX() - width * 9;
        int startY = rectangle.getCenterY() - height * 9;
        this.listArea = new Rectangle(startX, startY, width * 18, height * 18);
        int fitSlotsPerPage = getTotalFitSlotsPerPage(startX, startY, listArea);
        int j = page * fitSlotsPerPage;
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().isEntryListWidgetScrolled()) {
            height = Integer.MAX_VALUE;
            j = 0;
        }
        float maxScroll = 0;
        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                int x = startX + xx * 18, y = startY + yy * 18;
                if (!canBeFit(x, y, listArea))
                    continue;
                j++;
                if (j > currentDisplayed.size())
                    break;
                final Entry entry = currentDisplayed.get(j - 1);
                maxScroll = y + 18;
                widgets.add(new Slot(entry, xx, yy, x, y, entry.getEntryType() == Entry.Type.ITEM ? Renderer.fromItemStackNoCounts(entry.getItemStack()) : Renderer.fromFluid(entry.getFluid()), false, true, true) {
                    @Override
                    protected void queueTooltip(ItemStack itemStack, float delta) {
                        ClientPlayerEntity player = minecraft.player;
                        if (!ClientHelper.getInstance().isCheating() || player.inventory.getCursorStack().isEmpty())
                            super.queueTooltip(itemStack, delta);
                    }
                    
                    @Override
                    protected List<String> getExtraFluidToolTips(Fluid fluid) {
                        if (MinecraftClient.getInstance().options.advancedItemTooltips)
                            return Collections.singletonList(Formatting.DARK_GRAY.toString() + Registry.FLUID.getId(fluid).toString());
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
        EntryListWidget.maxScroll = maxScroll;
        if (!scroller.isRegistered())
            scroller.registerTick();
    }
    
    public int getTotalPage() {
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().isEntryListWidgetScrolled())
            return 1;
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
        if (rectangle.contains(PointHelper.fromMouse()))
            for (Widget widget : widgets)
                if (widget.keyPressed(int_1, int_2, int_3))
                    return true;
        return false;
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
        List<Entry> newList = Lists.newLinkedList();
        for (ItemStack workingItem : workingItems) {
            Optional<Entry> any = stacks.stream().filter(i -> i.getItemStack() != null && i.getItemStack().isItemEqualIgnoreDamage(workingItem)).findAny();
            if (any.isPresent())
                newList.add(any.get());
        }
        return Collections.unmodifiableList(newList);
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
    public List<Slot> children() {
        return widgets;
    }
    
    public class Slot extends SlotWidget {
        private final int backupY;
        private int xx, yy;
        private Entry entry;
        
        public Slot(Entry entry, int xx, int yy, int x, int y, Renderer renderer, boolean drawBackground, boolean showToolTips, boolean clickToMoreRecipes) {
            super(x, y, renderer, drawBackground, showToolTips, clickToMoreRecipes);
            this.xx = xx;
            this.yy = yy;
            this.backupY = y;
            this.entry = entry;
        }
        
        public int getBackupY() {
            return backupY;
        }
        
        public Entry getEntry() {
            return entry;
        }
        
        public int getXx() {
            return xx;
        }
        
        public int getYy() {
            return yy;
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return super.containsMouse(mouseX, mouseY) && rectangle.contains(mouseX, mouseY);
        }
    }
    
}
