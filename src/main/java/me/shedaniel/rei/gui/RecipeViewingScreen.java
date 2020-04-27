/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.widgets.Button;
import me.shedaniel.rei.api.widgets.Panel;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.RecipeChoosePageWidget;
import me.shedaniel.rei.gui.widget.TabWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ClientHelperImpl;
import me.shedaniel.rei.impl.InternalWidgets;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.impl.widgets.PanelWidget;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@ApiStatus.Internal
public class RecipeViewingScreen extends Screen implements RecipeScreen {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private final List<Widget> preWidgets = Lists.newArrayList();
    private final List<Widget> widgets = Lists.newArrayList();
    private final Map<Rectangle, List<Widget>> recipeBounds = Maps.newHashMap();
    private final List<TabWidget> tabs = Lists.newArrayList();
    private final Map<RecipeCategory<?>, List<RecipeDisplay>> categoriesMap;
    private final List<RecipeCategory<?>> categories;
    private final RecipeCategory<RecipeDisplay> selectedCategory;
    public int guiWidth;
    public int guiHeight;
    public int page;
    public int categoryPages = -1;
    public int largestWidth, largestHeight;
    public boolean choosePageActivated = false;
    public RecipeChoosePageWidget recipeChoosePageWidget;
    private int tabsPerPage = 5;
    private Rectangle bounds;
    @Nullable
    private Panel workingStationsBaseWidget;
    private Button recipeBack, recipeNext, categoryBack, categoryNext;
    private EntryStack ingredientStackToNotice = EntryStack.empty();
    private EntryStack resultStackToNotice = EntryStack.empty();
    
    public RecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> categoriesMap, @Nullable Identifier category) {
        super(NarratorManager.EMPTY);
        Window window = MinecraftClient.getInstance().getWindow();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, 176, 150);
        this.categoriesMap = categoriesMap;
        this.categories = Lists.newArrayList(categoriesMap.keySet());
        RecipeCategory<?> selected = categories.get(0);
        if (category != null) {
            for (RecipeCategory<?> recipeCategory : categories) {
                if (recipeCategory.getIdentifier().equals(category)) {
                    selected = recipeCategory;
                    break;
                }
            }
        }
        this.selectedCategory = (RecipeCategory<RecipeDisplay>) selected;
    }
    
    @ApiStatus.Internal
    static void transformIngredientNotice(List<Widget> setupDisplay, EntryStack noticeStack) {
        transformNotice(1, setupDisplay, noticeStack);
    }
    
    @ApiStatus.Internal
    static void transformResultNotice(List<Widget> setupDisplay, EntryStack noticeStack) {
        transformNotice(2, setupDisplay, noticeStack);
    }
    
    private static void transformNotice(int marker, List<Widget> setupDisplay, EntryStack noticeStack) {
        if (noticeStack.isEmpty())
            return;
        for (Widget widget : setupDisplay) {
            if (widget instanceof EntryWidget) {
                EntryWidget entry = (EntryWidget) widget;
                if (entry.getNoticeMark() == marker && entry.entries().size() > 1) {
                    EntryStack stack = CollectionUtils.findFirstOrNullEqualsEntryIgnoreAmount(entry.entries(), noticeStack);
                    if (stack != null) {
                        entry.clearStacks();
                        entry.entry(stack);
                    }
                }
            }
        }
    }
    
    @ApiStatus.Internal
    @Override
    public void addIngredientStackToNotice(EntryStack stack) {
        this.ingredientStackToNotice = stack;
    }
    
    @ApiStatus.Internal
    @Override
    public void addResultStackToNotice(EntryStack stack) {
        this.resultStackToNotice = stack;
    }
    
    @Override
    public Identifier getCurrentCategory() {
        return selectedCategory.getIdentifier();
    }
    
    @Override
    public void recalculateCategoryPage() {
        this.categoryPages = -1;
    }
    
    @Nullable
    public Panel getWorkingStationsBaseWidget() {
        return workingStationsBaseWidget;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && choosePageActivated) {
            choosePageActivated = false;
            init();
            return true;
        }
        if (int_1 == 258) {
            boolean boolean_1 = !hasShiftDown();
            if (!this.changeFocus(boolean_1))
                this.changeFocus(boolean_1);
            return true;
        }
        if (choosePageActivated)
            return recipeChoosePageWidget.keyPressed(int_1, int_2, int_3);
        else if (ConfigObject.getInstance().getNextPageKeybind().matchesKey(int_1, int_2)) {
            if (recipeNext.isEnabled())
                recipeNext.onClick();
            return recipeNext.isEnabled();
        } else if (ConfigObject.getInstance().getPreviousPageKeybind().matchesKey(int_1, int_2)) {
            if (recipeBack.isEnabled())
                recipeBack.onClick();
            return recipeBack.isEnabled();
        }
        for (Element element : children())
            if (element.keyPressed(int_1, int_2, int_3))
                return true;
        if (int_1 == 256 || this.client.options.keyInventory.matchesKey(int_1, int_2)) {
            MinecraftClient.getInstance().openScreen(ScreenHelper.getLastHandledScreen());
            ScreenHelper.getLastOverlay().init();
            return true;
        }
        if (int_1 == 259) {
            if (ScreenHelper.hasLastRecipeScreen())
                client.openScreen(ScreenHelper.getLastRecipeScreen());
            else
                client.openScreen(ScreenHelper.getLastHandledScreen());
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void init() {
        super.init();
        boolean isCompactTabs = ConfigObject.getInstance().isUsingCompactTabs();
        int tabSize = isCompactTabs ? 24 : 28;
        this.children.clear();
        this.recipeBounds.clear();
        this.tabs.clear();
        this.preWidgets.clear();
        this.widgets.clear();
        this.largestWidth = width - 100;
        this.largestHeight = Math.max(height - 34 - 30, 100);
        int maxWidthDisplay = CollectionUtils.mapAndMax(getCurrentDisplayed(), selectedCategory::getDisplayWidth, Comparator.naturalOrder()).orElse(150);
        this.guiWidth = Math.max(maxWidthDisplay + 40, 0);
        this.guiHeight = MathHelper.floor(MathHelper.clamp((double) (selectedCategory.getDisplayHeight() + 4) * (getRecipesPerPage() + 1) + 36, 100, largestHeight));
        if (!ConfigObject.getInstance().shouldResizeDynamically()) this.guiHeight = largestHeight;
        this.tabsPerPage = Math.max(5, MathHelper.floor((guiWidth - 20d) / tabSize));
        if (this.categoryPages == -1) {
            this.categoryPages = Math.max(0, categories.indexOf(selectedCategory) / tabsPerPage);
        }
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, height / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.page = MathHelper.clamp(page, 0, getTotalPages(selectedCategory) - 1);
        this.widgets.add(Widgets.createButton(new Rectangle(bounds.x, bounds.y - 16, 10, 10), new TranslatableText("text.rei.left_arrow"))
                .onClick(button -> {
                    categoryPages--;
                    if (categoryPages < 0)
                        categoryPages = MathHelper.ceil(categories.size() / (float) tabsPerPage) - 1;
                    RecipeViewingScreen.this.init();
                })
                .enabled(categories.size() > tabsPerPage));
        this.widgets.add(Widgets.createButton(new Rectangle(bounds.x + bounds.width - 10, bounds.y - 16, 10, 10), new TranslatableText("text.rei.right_arrow"))
                .onClick(button -> {
                    categoryPages++;
                    if (categoryPages > MathHelper.ceil(categories.size() / (float) tabsPerPage) - 1)
                        categoryPages = 0;
                    RecipeViewingScreen.this.init();
                })
                .enabled(categories.size() > tabsPerPage));
        widgets.add(categoryBack = Widgets.createButton(new Rectangle(bounds.getX() + 5, bounds.getY() + 5, 12, 12), new TranslatableText("text.rei.left_arrow"))
                .onClick(button -> {
                    int currentCategoryIndex = categories.indexOf(selectedCategory);
                    currentCategoryIndex--;
                    if (currentCategoryIndex < 0)
                        currentCategoryIndex = categories.size() - 1;
                    ClientHelperImpl.getInstance().openRecipeViewingScreen(categoriesMap, categories.get(currentCategoryIndex).getIdentifier(), ingredientStackToNotice, resultStackToNotice);
                }).tooltipLine(I18n.translate("text.rei.previous_category")));
        widgets.add(Widgets.createClickableLabel(new Point(bounds.getCenterX(), bounds.getY() + 7), new LiteralText(selectedCategory.getCategoryName()), clickableLabelWidget -> {
            ClientHelper.getInstance().executeViewAllRecipesKeyBind();
        }).tooltipLine(I18n.translate("text.rei.view_all_categories")));
        widgets.add(categoryNext = Widgets.createButton(new Rectangle(bounds.getMaxX() - 17, bounds.getY() + 5, 12, 12), new TranslatableText("text.rei.right_arrow"))
                .onClick(button -> {
                    int currentCategoryIndex = categories.indexOf(selectedCategory);
                    currentCategoryIndex++;
                    if (currentCategoryIndex >= categories.size())
                        currentCategoryIndex = 0;
                    ClientHelperImpl.getInstance().openRecipeViewingScreen(categoriesMap, categories.get(currentCategoryIndex).getIdentifier(), ingredientStackToNotice, resultStackToNotice);
                }).tooltipLine(I18n.translate("text.rei.next_category")));
        categoryBack.setEnabled(categories.size() > 1);
        categoryNext.setEnabled(categories.size() > 1);
        
        widgets.add(recipeBack = Widgets.createButton(new Rectangle(bounds.getX() + 5, bounds.getY() + 19, 12, 12), new TranslatableText("text.rei.left_arrow"))
                .onClick(button -> {
                    page--;
                    if (page < 0)
                        page = getTotalPages(selectedCategory) - 1;
                    RecipeViewingScreen.this.init();
                }).tooltipLine(I18n.translate("text.rei.previous_page")));
        widgets.add(Widgets.createClickableLabel(new Point(bounds.getCenterX(), bounds.getY() + 21), NarratorManager.EMPTY, label -> {
            RecipeViewingScreen.this.choosePageActivated = true;
            RecipeViewingScreen.this.init();
        }).onRender((matrices, label) -> {
            label.setText(new LiteralText(String.format("%d/%d", page + 1, getTotalPages(selectedCategory))));
            label.setClickable(getTotalPages(selectedCategory) > 1);
        }).tooltipSupplier(label -> label.isClickable() ? I18n.translate("text.rei.choose_page") : null));
        widgets.add(recipeNext = Widgets.createButton(new Rectangle(bounds.getMaxX() - 17, bounds.getY() + 19, 12, 12), new TranslatableText("text.rei.right_arrow"))
                .onClick(button -> {
                    page++;
                    if (page >= getTotalPages(selectedCategory))
                        page = 0;
                    RecipeViewingScreen.this.init();
                }).tooltipLine(I18n.translate("text.rei.next_page")));
        recipeBack.setEnabled(getTotalPages(selectedCategory) > 1);
        recipeNext.setEnabled(getTotalPages(selectedCategory) > 1);
        int tabV = isCompactTabs ? 166 : 192;
        for (int i = 0; i < tabsPerPage; i++) {
            int j = i + categoryPages * tabsPerPage;
            if (categories.size() > j) {
                TabWidget tab;
                tabs.add(tab = TabWidget.create(i, tabSize, bounds.x + bounds.width / 2 - Math.min(categories.size() - categoryPages * tabsPerPage, tabsPerPage) * tabSize / 2, bounds.y, 0, tabV, widget -> {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    if (widget.getId() + categoryPages * tabsPerPage == categories.indexOf(selectedCategory))
                        return false;
                    ClientHelperImpl.getInstance().openRecipeViewingScreen(categoriesMap, categories.get(widget.getId() + categoryPages * tabsPerPage).getIdentifier(), ingredientStackToNotice, resultStackToNotice);
                    return true;
                }));
                tab.setRenderer(categories.get(j), categories.get(j).getLogo(), categories.get(j).getCategoryName(), tab.getId() + categoryPages * tabsPerPage == categories.indexOf(selectedCategory));
            }
        }
        Optional<ButtonAreaSupplier> supplier = RecipeHelper.getInstance().getAutoCraftButtonArea(selectedCategory);
        int recipeHeight = selectedCategory.getDisplayHeight();
        List<RecipeDisplay> currentDisplayed = getCurrentDisplayed();
        for (int i = 0; i < currentDisplayed.size(); i++) {
            final RecipeDisplay display = currentDisplayed.get(i);
            final Supplier<RecipeDisplay> displaySupplier = () -> display;
            int displayWidth = selectedCategory.getDisplayWidth(displaySupplier.get());
            final Rectangle displayBounds = new Rectangle(getBounds().getCenterX() - displayWidth / 2, getBounds().getCenterY() + 16 - recipeHeight * (getRecipesPerPage() + 1) / 2 - 2 * (getRecipesPerPage() + 1) + recipeHeight * i + 4 * i, displayWidth, recipeHeight);
            List<Widget> setupDisplay = selectedCategory.setupDisplay(display, displayBounds);
            transformIngredientNotice(setupDisplay, ingredientStackToNotice);
            transformResultNotice(setupDisplay, resultStackToNotice);
            recipeBounds.put(displayBounds, setupDisplay);
            this.widgets.addAll(setupDisplay);
            if (supplier.isPresent() && supplier.get().get(displayBounds) != null)
                this.widgets.add(InternalWidgets.createAutoCraftingButtonWidget(displayBounds, supplier.get().get(displayBounds), new LiteralText(supplier.get().getButtonText()), displaySupplier, setupDisplay, selectedCategory));
        }
        if (choosePageActivated)
            recipeChoosePageWidget = new RecipeChoosePageWidget(this, page, getTotalPages(selectedCategory));
        else
            recipeChoosePageWidget = null;
        
        workingStationsBaseWidget = null;
        List<List<EntryStack>> workingStations = RecipeHelper.getInstance().getWorkingStations(selectedCategory.getIdentifier());
        if (!workingStations.isEmpty()) {
            int hh = MathHelper.floor((bounds.height - 16) / 18f);
            int actualHeight = Math.min(hh, workingStations.size());
            int innerWidth = MathHelper.ceil(workingStations.size() / ((float) hh));
            int xx = bounds.x - (8 + innerWidth * 16) + 6;
            int yy = bounds.y + 16;
            preWidgets.add(workingStationsBaseWidget = Widgets.createCategoryBase(new Rectangle(xx - 5, yy - 5, 15 + innerWidth * 16, 10 + actualHeight * 16)));
            preWidgets.add(Widgets.createSlotBase(new Rectangle(xx - 1, yy - 1, innerWidth * 16 + 2, actualHeight * 16 + 2)));
            int index = 0;
            List<Text> list = Collections.singletonList(new TranslatableText("text.rei.working_station").method_27692(Formatting.YELLOW));
            xx += (innerWidth - 1) * 16;
            for (List<EntryStack> workingStation : workingStations) {
                preWidgets.add(new WorkstationSlotWidget(xx, yy, CollectionUtils.map(workingStation, stack -> stack.copy().setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> list))));
                index++;
                yy += 16;
                if (index >= hh) {
                    index = 0;
                    yy = bounds.y + 16;
                    xx -= 16;
                }
            }
        }
        
        children.addAll(tabs);
        children.add(ScreenHelper.getLastOverlay(true, false));
        children.addAll(widgets);
        children.addAll(preWidgets);
    }
    
    public List<Widget> getWidgets() {
        return widgets;
    }
    
    public List<RecipeDisplay> getCurrentDisplayed() {
        List<RecipeDisplay> list = Lists.newArrayList();
        int recipesPerPage = getRecipesPerPage();
        for (int i = 0; i <= recipesPerPage; i++)
            if (page * (recipesPerPage + 1) + i < categoriesMap.get(selectedCategory).size())
                list.add(categoriesMap.get(selectedCategory).get(page * (recipesPerPage + 1) + i));
        return list;
    }
    
    public RecipeCategory<RecipeDisplay> getSelectedCategory() {
        return selectedCategory;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getCategoryPage() {
        return categoryPages;
    }
    
    private int getRecipesPerPage() {
        if (selectedCategory.getFixedRecipesPerPage() > 0)
            return selectedCategory.getFixedRecipesPerPage() - 1;
        int height = selectedCategory.getDisplayHeight();
        return MathHelper.clamp(MathHelper.floor(((double) largestHeight - 36) / ((double) height + 4)) - 1, 0, Math.min(ConfigObject.getInstance().getMaxRecipePerPage() - 1, selectedCategory.getMaximumRecipePerPage() - 1));
    }
    
    private int getRecipesPerPageByHeight() {
        int height = selectedCategory.getDisplayHeight();
        return MathHelper.clamp(MathHelper.floor(((double) guiHeight - 36) / ((double) height + 4)), 0, Math.min(ConfigObject.getInstance().getMaxRecipePerPage() - 1, selectedCategory.getMaximumRecipePerPage() - 1));
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
        for (Widget widget : preWidgets) {
            widget.render(matrices, mouseX, mouseY, delta);
        }
        PanelWidget.render(matrices, bounds, -1);
        if (REIHelper.getInstance().isDarkThemeEnabled()) {
            fill(matrices, bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF404040);
            fill(matrices, bounds.x + 17, bounds.y + 19, bounds.x + bounds.width - 17, bounds.y + 30, 0xFF404040);
        } else {
            fill(matrices, bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF9E9E9E);
            fill(matrices, bounds.x + 17, bounds.y + 19, bounds.x + bounds.width - 17, bounds.y + 31, 0xFF9E9E9E);
        }
        for (TabWidget tab : tabs) {
            if (!tab.isSelected())
                tab.render(matrices, mouseX, mouseY, delta);
        }
        super.render(matrices, mouseX, mouseY, delta);
        for (Widget widget : widgets) {
            widget.render(matrices, mouseX, mouseY, delta);
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        for (TabWidget tab : tabs) {
            if (tab.isSelected())
                tab.render(matrices, mouseX, mouseY, delta);
        }
        ScreenHelper.getLastOverlay().render(matrices, mouseX, mouseY, delta);
        ScreenHelper.getLastOverlay().lateRender(matrices, mouseX, mouseY, delta);
        {
            ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
            if (export.matchesCurrentKey()) {
                for (Map.Entry<Rectangle, List<Widget>> entry : recipeBounds.entrySet()) {
                    Rectangle bounds = entry.getKey();
                    setZOffset(470);
                    if (bounds.contains(mouseX, mouseY)) {
                        fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 1744822402, 1744822402);
                        Text text = new TranslatableText("text.rei.release_export", export.getLocalizedName());
                        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                        matrices.push();
                        matrices.translate(0.0D, 0.0D, 480);
                        Matrix4f matrix4f = matrices.peek().getModel();
                        textRenderer.draw(text, bounds.getCenterX() - textRenderer.method_27525(text) / 2f, bounds.getCenterY() - 4.5f, 0xff000000, false, matrix4f, immediate, false, 0, 15728880);
                        immediate.draw();
                        matrices.pop();
                    } else {
                        fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 1744830463, 1744830463);
                    }
                    setZOffset(0);
                }
            }
        }
        if (choosePageActivated) {
            setZOffset(500);
            this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
            setZOffset(0);
            recipeChoosePageWidget.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
        if (export.matchesKey(keyCode, scanCode)) {
            for (Map.Entry<Rectangle, List<Widget>> entry : recipeBounds.entrySet()) {
                Rectangle bounds = entry.getKey();
                if (bounds.contains(PointHelper.ofMouse())) {
                    RecipeDisplayExporter.exportRecipeDisplay(bounds, entry.getValue());
                    break;
                }
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    public int getTotalPages(RecipeCategory<RecipeDisplay> category) {
        return MathHelper.ceil(categoriesMap.get(category).size() / (double) (getRecipesPerPage() + 1));
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (choosePageActivated) {
            return recipeChoosePageWidget.charTyped(char_1, int_1);
        }
        for (Element listener : children())
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (choosePageActivated) {
            return recipeChoosePageWidget.mouseDragged(double_1, double_2, int_1, double_3, double_4);
        }
        return super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
    }
    
    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        if (choosePageActivated) {
            return recipeChoosePageWidget.mouseReleased(double_1, double_2, int_1);
        }
        return super.mouseReleased(double_1, double_2, int_1);
    }
    
    @Override
    public boolean mouseScrolled(double i, double j, double amount) {
        for (Element listener : children())
            if (listener.mouseScrolled(i, j, amount))
                return true;
        if (getBounds().contains(PointHelper.ofMouse())) {
            if (amount > 0 && recipeBack.isEnabled())
                recipeBack.onClick();
            else if (amount < 0 && recipeNext.isEnabled())
                recipeNext.onClick();
        }
        if ((new Rectangle(bounds.x, bounds.y - 28, bounds.width, 28)).contains(PointHelper.ofMouse())) {
            if (amount > 0 && categoryBack.isEnabled())
                categoryBack.onClick();
            else if (amount < 0 && categoryNext.isEnabled())
                categoryNext.onClick();
        }
        return super.mouseScrolled(i, j, amount);
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (choosePageActivated)
            if (recipeChoosePageWidget.containsMouse(double_1, double_2)) {
                return recipeChoosePageWidget.mouseClicked(double_1, double_2, int_1);
            } else {
                choosePageActivated = false;
                init();
                return false;
            }
        for (Element entry : children())
            if (entry.mouseClicked(double_1, double_2, int_1)) {
                setFocused(entry);
                if (int_1 == 0)
                    setDragging(true);
                return true;
            }
        return false;
    }
    
    @Override
    public Element getFocused() {
        if (choosePageActivated)
            return recipeChoosePageWidget;
        return super.getFocused();
    }
    
    public static class WorkstationSlotWidget extends EntryWidget {
        public WorkstationSlotWidget(int x, int y, List<EntryStack> widgets) {
            super(new Point(x, y));
            entries(widgets);
            noBackground();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return getInnerBounds().contains(mouseX, mouseY);
        }
    }
    
}
