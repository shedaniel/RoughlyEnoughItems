/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget.Interpolation;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget.Precision;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.SearchArgument;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@SuppressWarnings({"deprecation", "rawtypes"})
public class EntryListWidget extends Widget {
    
    private static final Supplier<Boolean> RENDER_EXTRA_CONFIG = () -> ConfigManager.getInstance().getConfig().doesRenderEntryExtraOverlay();
    private static final String SPACE = " ", EMPTY = "";
    private static final Comparator<EntryStack> ASCENDING_COMPARATOR;
    private static List<Item> searchBlacklisted = Lists.newArrayList();
    private static float scroll;
    private static float target;
    private static long start;
    private static long duration;
    private static float maxScroll;
    private static float scrollBarAlpha = 0;
    private static float scrollBarAlphaFuture = 0;
    private static long scrollBarAlphaFutureTime = -1;
    private static boolean draggingScrollBar = false;
    
    static {
        ASCENDING_COMPARATOR = (entry, entry1) -> {
            if (ConfigManager.getInstance().getConfig().getItemListOrdering().equals(ItemListOrdering.name))
                return tryGetEntryStackName(entry).compareToIgnoreCase(tryGetEntryStackName(entry1));
            if (ConfigManager.getInstance().getConfig().getItemListOrdering().equals(ItemListOrdering.item_groups)) {
                if (entry.getType() == EntryStack.Type.ITEM && entry1.getType() == EntryStack.Type.ITEM) {
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
    private List<EntryStack> currentDisplayed;
    private List<Slot> widgets;
    private int width, height, page;
    private Rectangle rectangle, listArea;
    
    public EntryListWidget(int page) {
        this.currentDisplayed = Lists.newArrayList();
        this.width = 0;
        this.height = 0;
        this.page = page;
        this.lastSearchArgument = Lists.newArrayList();
    }
    
    public static List<String> tryGetItemStackToolTip(ItemStack itemStack, boolean careAboutAdvanced) {
        if (!searchBlacklisted.contains(itemStack.getItem()))
            try {
                return CollectionUtils.map(itemStack.getTooltip(MinecraftClient.getInstance().player, MinecraftClient.getInstance().options.advancedItemTooltips && careAboutAdvanced ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL), Text::asFormattedString);
            } catch (Throwable e) {
                e.printStackTrace();
                searchBlacklisted.add(itemStack.getItem());
            }
        return Collections.singletonList(tryGetItemStackName(itemStack));
    }
    
    public static String tryGetEntryStackName(EntryStack stack) {
        if (stack.getType() == EntryStack.Type.ITEM)
            return tryGetItemStackName(stack.getItemStack());
        else if (stack.getType() == EntryStack.Type.FLUID)
            return tryGetFluidName(stack.getFluid());
        return "";
    }
    
    public static String tryGetFluidName(Fluid fluid) {
        Identifier id = Registry.FLUID.getId(fluid);
        if (I18n.hasTranslation("block." + id.toString().replaceFirst(":", ".")))
            return I18n.translate("block." + id.toString().replaceFirst(":", "."));
        return CollectionUtils.mapAndJoinToString(id.getPath().split("_"), StringUtils::capitalize, " ");
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
    
    public static boolean filterEntry(EntryStack entry, List<SearchArgument[]> arguments) {
        if (arguments.isEmpty())
            return true;
        AtomicReference<String> mod = new AtomicReference<>(), tooltips = new AtomicReference<>(), name = new AtomicReference<>();
        for (SearchArgument[] arguments1 : arguments) {
            boolean b = true;
            for (SearchArgument argument : arguments1) {
                if (argument.getArgumentType() == (SearchArgument.ArgumentType.ALWAYS))
                    return true;
                if (argument.getArgumentType() == SearchArgument.ArgumentType.MOD) {
                    fillMod(entry, mod);
                    if (mod.get() != null && !mod.get().isEmpty() && argument.getFunction(!argument.isInclude()).apply(mod.get())) {
                        b = false;
                        break;
                    }
                }
                if (argument.getArgumentType() == SearchArgument.ArgumentType.TOOLTIP) {
                    fillTooltip(entry, tooltips);
                    if (tooltips.get() != null && !tooltips.get().isEmpty() && argument.getFunction(!argument.isInclude()).apply(tooltips.get())) {
                        b = false;
                        break;
                    }
                }
                if (argument.getArgumentType() == SearchArgument.ArgumentType.TEXT) {
                    fillName(entry, name);
                    if (name.get() != null && !name.get().isEmpty() && argument.getFunction(!argument.isInclude()).apply(name.get())) {
                        b = false;
                        break;
                    }
                }
            }
            if (b)
                return true;
        }
        return false;
    }
    
    private static AtomicReference<String> fillMod(EntryStack entry, AtomicReference<String> mod) {
        if (mod.get() == null) {
            Optional<Identifier> identifier = entry.getIdentifier();
            if (identifier.isPresent())
                mod.set(ClientHelper.getInstance().getModFromIdentifier(identifier.get()).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
            else mod.set("");
        }
        return mod;
    }
    
    private static AtomicReference<String> fillTooltip(EntryStack entry, AtomicReference<String> mod) {
        if (mod.get() == null)
            if (entry.getType() == EntryStack.Type.ITEM)
                mod.set(CollectionUtils.joinToString(tryGetItemStackToolTip(entry.getItemStack(), false), "").replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
            else
                mod.set(tryGetEntryStackName(entry).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
        return mod;
    }
    
    private static AtomicReference<String> fillName(EntryStack entry, AtomicReference<String> mod) {
        if (mod.get() == null)
            mod.set(tryGetEntryStackName(entry).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT));
        return mod;
    }
    
    public static float getMaxScroll() {
        return Math.max(maxScroll - ScreenHelper.getLastOverlay().getEntryListWidget().rectangle.height, 0);
    }
    
    public static float getScroll() {
        return scroll;
    }
    
    public static final float clamp(float v) {
        return clamp(v, 300f);
    }
    
    public static final float clamp(float v, float clampExtension) {
        return MathHelper.clamp(v, -clampExtension, getMaxScroll() + clampExtension);
    }
    
    public static void offset(float value, boolean animated) {
        scrollTo(target + value, animated);
    }
    
    public static void scrollTo(float value, boolean animated) {
        scrollTo(value, animated, ClothConfigInitializer.getScrollDuration());
    }
    
    public static void scrollTo(float value, boolean animated, long duration) {
        target = clamp(value);
        
        if (animated) {
            start = System.currentTimeMillis();
            EntryListWidget.duration = duration;
        } else
            scroll = target;
    }
    
    private static void updatePosition(float delta) {
        target = clamp(target);
        if (target < 0) {
            target -= target * (1 - ClothConfigInitializer.getBounceBackMultiplier()) * delta / 3;
        } else if (target > getMaxScroll()) {
            target = (float) ((target - getMaxScroll()) * (1 - (1 - ClothConfigInitializer.getBounceBackMultiplier()) * delta / 3) + getMaxScroll());
        }
        if (!Precision.almostEquals(scroll, target, Precision.FLOAT_EPSILON))
            scroll = (float) Interpolation.expoEase(scroll, target, Math.min((System.currentTimeMillis() - start) / ((double) duration), 1));
        else
            scroll = target;
    }
    
    public int getFullTotalSlotsPerPage() {
        return width * height;
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (ConfigManager.getInstance().getConfig().isEntryListWidgetScrolled() && rectangle.contains(double_1, double_2)) {
            if (scrollBarAlphaFuture == 0)
                scrollBarAlphaFuture = 1f;
            if (System.currentTimeMillis() - scrollBarAlphaFutureTime > 300f)
                scrollBarAlphaFutureTime = System.currentTimeMillis();
            offset((float) (ClothConfigInitializer.getScrollStep() * -double_3), true);
            return true;
        }
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        if (ConfigManager.getInstance().getConfig().doesVillagerScreenHavePermanentScrollBar()) {
            scrollBarAlphaFutureTime = System.currentTimeMillis();
            scrollBarAlphaFuture = 0;
            scrollBarAlpha = 1;
        } else if (scrollBarAlphaFutureTime > 0) {
            long l = System.currentTimeMillis() - scrollBarAlphaFutureTime;
            if (l > 300f) {
                if (scrollBarAlphaFutureTime == 0) {
                    scrollBarAlpha = scrollBarAlphaFuture;
                    scrollBarAlphaFutureTime = -1;
                } else if (l > 2000f && scrollBarAlphaFuture == 1) {
                    scrollBarAlphaFuture = 0;
                    scrollBarAlphaFutureTime = System.currentTimeMillis();
                } else
                    scrollBarAlpha = scrollBarAlphaFuture;
            } else {
                if (scrollBarAlphaFuture == 0)
                    scrollBarAlpha = Math.min(scrollBarAlpha, 1 - Math.min(1f, l / 300f));
                else if (scrollBarAlphaFuture == 1)
                    scrollBarAlpha = Math.max(Math.min(1f, l / 300f), scrollBarAlpha);
            }
        }
        
        GuiLighting.disable();
        RenderSystem.pushMatrix();
        boolean widgetScrolled = ConfigManager.getInstance().getConfig().isEntryListWidgetScrolled();
        if (!widgetScrolled)
            scroll = 0;
        else {
            updatePosition(float_1);
            page = 0;
            ScreenHelper.getLastOverlay().setPage(0);
            ScissorsHandler.INSTANCE.scissor(rectangle);
        }
        widgets.forEach(widget -> {
            if (widgetScrolled) {
                widget.getBounds().y = (int) (widget.backupY - scroll);
                if (widget.getBounds().y <= rectangle.y + rectangle.height && widget.getBounds().getMaxY() >= rectangle.y)
                    widget.render(int_1, int_2, float_1);
            } else {
                widget.render(int_1, int_2, float_1);
            }
        });
        if (widgetScrolled) {
            double height = getMaxScroll();
            if (height > rectangle.height) {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                double maxScroll = height;
                int scrollBarHeight = MathHelper.floor((rectangle.height) * (rectangle.height) / maxScroll);
                scrollBarHeight = MathHelper.clamp(scrollBarHeight, 32, rectangle.height - 8);
                scrollBarHeight = (int) ((double) scrollBarHeight - Math.min((double) (this.scroll < 0.0D ? (int) (-this.scroll) : (this.scroll > (double) this.getMaxScroll() ? (int) this.scroll - this.getMaxScroll() : 0)), (double) scrollBarHeight * 0.75D));
                int minY = (int) Math.min(Math.max((int) this.getScroll() * (rectangle.height - scrollBarHeight) / maxScroll + rectangle.y, rectangle.y), rectangle.getMaxY() - scrollBarHeight);
                double scrollbarPositionMinX = rectangle.getMaxX() - 6, scrollbarPositionMaxX = rectangle.getMaxX() - 1;
                GuiLighting.disable();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.disableAlphaTest();
                RenderSystem.blendFuncSeparate(770, 771, 1, 0);
                RenderSystem.shadeModel(7425);
                buffer.begin(7, VertexFormats.POSITION_COLOR);
                float b = ScreenHelper.isDarkModeEnabled() ? 0.8f : 1f;
                buffer.vertex(scrollbarPositionMinX, minY + scrollBarHeight, 1000D).color(b, b, b, scrollBarAlpha).next();
                buffer.vertex(scrollbarPositionMaxX, minY + scrollBarHeight, 1000D).color(b, b, b, scrollBarAlpha).next();
                buffer.vertex(scrollbarPositionMaxX, minY, 1000D).color(b, b, b, scrollBarAlpha).next();
                buffer.vertex(scrollbarPositionMinX, minY, 1000D).color(b, b, b, scrollBarAlpha).next();
                tessellator.draw();
                RenderSystem.shadeModel(7424);
                RenderSystem.disableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
            }
            ScissorsHandler.INSTANCE.removeLastScissor();
        }
        RenderSystem.popMatrix();
        ClientPlayerEntity player = minecraft.player;
        if (rectangle.contains(PointHelper.fromMouse()) && ClientHelper.getInstance().isCheating() && !player.inventory.getCursorStack().isEmpty() && RoughlyEnoughItemsCore.hasPermissionToUsePackets())
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(I18n.translate("text.rei.delete_items")));
    }
    
    public void updateList(DisplayHelper.DisplayBoundsHandler<?> boundsHandler, Rectangle rectangle, int page, String searchTerm, boolean processSearchTerm) {
        this.rectangle = rectangle;
        this.page = page;
        this.widgets = Lists.newCopyOnWriteArrayList();
        calculateListSize(rectangle);
        if (currentDisplayed.isEmpty() || processSearchTerm)
            currentDisplayed = processSearchTerm(searchTerm, EntryRegistry.getInstance().getStacksList(), CollectionUtils.map(ScreenHelper.inventoryStacks, EntryStack::create));
        int startX = rectangle.getCenterX() - width * 9;
        int startY = rectangle.getCenterY() - height * 9;
        this.listArea = new Rectangle(startX, startY, width * 18, height * 18);
        int fitSlotsPerPage = getTotalFitSlotsPerPage(startX, startY, listArea);
        int j = page * fitSlotsPerPage;
        if (ConfigManager.getInstance().getConfig().isEntryListWidgetScrolled()) {
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
                final EntryStack stack = currentDisplayed.get(j - 1).copy()
                        .setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE)
                        .setting(EntryStack.Settings.Item.RENDER_OVERLAY, RENDER_EXTRA_CONFIG);
                maxScroll = y + 18;
                widgets.add((Slot) new Slot(xx, yy, x, y).entry(stack).noBackground());
            }
            if (j > currentDisplayed.size())
                break;
        }
        EntryListWidget.maxScroll = Math.max(maxScroll - 18, 0);
    }
    
    public int getTotalPage() {
        if (ConfigManager.getInstance().getConfig().isEntryListWidgetScrolled())
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
        for (DisplayHelper.DisplayBoundsHandler sortedBoundsHandler : DisplayHelper.getInstance().getSortedBoundsHandlers(minecraft.currentScreen.getClass())) {
            ActionResult fit = sortedBoundsHandler.canItemSlotWidgetFit(!ConfigManager.getInstance().getConfig().isLeftHandSidePanel(), left, top, minecraft.currentScreen, listArea);
            if (fit != ActionResult.PASS)
                return fit == ActionResult.SUCCESS;
        }
        return true;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int int_1, double double_3, double double_4) {
        if (int_1 == 0 && scrollBarAlpha > 0 && draggingScrollBar) {
            float height = maxScroll;
            int actualHeight = rectangle.height;
            if (height > actualHeight && mouseY >= rectangle.y && mouseY <= rectangle.getMaxY()) {
                double double_5 = (double) Math.max(1, this.getMaxScroll());
                int int_2 = rectangle.height;
                int int_3 = MathHelper.clamp((int) ((float) (int_2 * int_2) / (float) maxScroll), 32, int_2 - 8);
                double double_6 = Math.max(1.0D, double_5 / (double) (int_2 - int_3));
                scrollBarAlphaFutureTime = System.currentTimeMillis();
                scrollBarAlphaFuture = 1f;
                scrollTo(MathHelper.clamp((float) (scroll + double_4 * double_6), 0, height - rectangle.height), false);
            }
        }
        return super.mouseDragged(mouseX, mouseY, int_1, double_3, double_4);
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (rectangle.contains(PointHelper.fromMouse()))
            for (Widget widget : widgets)
                if (widget.keyPressed(int_1, int_2, int_3))
                    return true;
        return false;
    }
    
    public List getCurrentDisplayed() {
        return currentDisplayed;
    }
    
    private List<EntryStack> processSearchTerm(String searchTerm, List<EntryStack> ol, List<EntryStack> inventoryItems) {
        lastSearchArgument.clear();
        List<EntryStack> os = new LinkedList<>(ol);
        if (ConfigManager.getInstance().getConfig().getItemListOrdering() != ItemListOrdering.registry)
            os.sort(ASCENDING_COMPARATOR);
        if (!ConfigManager.getInstance().getConfig().isItemListAscending())
            Collections.reverse(os);
        String[] splitSearchTerm = StringUtils.splitByWholeSeparatorPreserveAllTokens(searchTerm, "|");
        for (String s : splitSearchTerm) {
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
        }
        List<EntryStack> stacks = Collections.emptyList();
        if (lastSearchArgument.isEmpty())
            stacks = os;
        else
            stacks = CollectionUtils.filter(os, entry -> filterEntry(entry, lastSearchArgument));
        if (!ConfigManager.getInstance().isCraftableOnlyEnabled() || stacks.isEmpty() || inventoryItems.isEmpty())
            return Collections.unmodifiableList(stacks);
        List<EntryStack> workingItems = RecipeHelper.getInstance().findCraftableEntriesByItems(inventoryItems);
        List<EntryStack> newList = Lists.newLinkedList();
        for (EntryStack workingItem : workingItems) {
            EntryStack any = CollectionUtils.findFirstOrNullEquals(stacks, workingItem);
            if (any != null)
                newList.add(any);
        }
        if (newList.isEmpty())
            return Collections.unmodifiableList(stacks);
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
        double height = getMaxScroll();
        int actualHeight = rectangle.height;
        if (height > actualHeight && scrollBarAlpha > 0 && double_2 >= rectangle.y && double_2 <= rectangle.getMaxY()) {
            double scrollbarPositionMinX = rectangle.getMaxX() - 6;
            if (double_1 >= scrollbarPositionMinX - 2 & double_1 <= scrollbarPositionMinX + 8) {
                this.draggingScrollBar = true;
                scrollBarAlpha = 1;
                return true;
            }
        }
        this.draggingScrollBar = false;
        
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
    
    private class Slot extends EntryWidget {
        private final int backupY;
        private int xx, yy;
        
        public Slot(int xx, int yy, int x, int y) {
            super(x, y);
            this.xx = xx;
            this.yy = yy;
            this.backupY = y;
        }
        
        public int getBackupY() {
            return backupY;
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
        
        @Override
        protected void queueTooltip(int mouseX, int mouseY, float delta) {
            ClientPlayerEntity player = minecraft.player;
            if (!ClientHelper.getInstance().isCheating() || player.inventory.getCursorStack().isEmpty())
                super.queueTooltip(mouseX, mouseY, delta);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!interactable)
                return super.mouseClicked(mouseX, mouseY, button);
            if (containsMouse(mouseX, mouseY) && ClientHelper.getInstance().isCheating()) {
                EntryStack entry = getCurrentEntry().copy();
                if (entry.getType() == EntryStack.Type.ITEM) {
                    if (ConfigManager.getInstance().getConfig().getItemCheatingMode() == ItemCheatingMode.REI_LIKE)
                        entry.setAmount(button != 1 ? 1 : entry.getItemStack().getMaxCount());
                    else if (ConfigManager.getInstance().getConfig().getItemCheatingMode() == ItemCheatingMode.JEI_LIKE)
                        entry.setAmount(button != 0 ? 1 : entry.getItemStack().getMaxCount());
                    else
                        entry.setAmount(1);
                }
                ClientHelper.getInstance().tryCheatingEntry(entry);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
    
}
