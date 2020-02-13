/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@ApiStatus.Internal
public class RecipeViewingScreen extends Screen {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private final List<Widget> preWidgets;
    private final List<Widget> widgets;
    private final Map<Rectangle, List<Widget>> recipeBounds;
    private final List<TabWidget> tabs;
    private final Map<RecipeCategory<?>, List<RecipeDisplay>> categoriesMap;
    private final List<RecipeCategory<?>> categories;
    public int guiWidth;
    public int guiHeight;
    public int page, categoryPages;
    public int largestWidth, largestHeight;
    public boolean choosePageActivated;
    public RecipeChoosePageWidget recipeChoosePageWidget;
    private int tabsPerPage = 5;
    private Rectangle bounds;
    @Nullable private CategoryBaseWidget workingStationsBaseWidget;
    private RecipeCategory<RecipeDisplay> selectedCategory;
    private ButtonWidget recipeBack, recipeNext, categoryBack, categoryNext;
    private EntryStack mainStackToNotice = EntryStack.empty();
    
    public RecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> categoriesMap) {
        super(new LiteralText(""));
        this.categoryPages = 0;
        this.preWidgets = Lists.newArrayList();
        this.widgets = Lists.newArrayList();
        this.recipeBounds = Maps.newHashMap();
        Window window = MinecraftClient.getInstance().getWindow();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, 176, 150);
        this.categoriesMap = categoriesMap;
        this.categories = Lists.newArrayList();
        for (RecipeCategory<?> category : RecipeHelper.getInstance().getAllCategories()) {
            if (categoriesMap.containsKey(category))
                categories.add(category);
        }
        this.selectedCategory = (RecipeCategory<RecipeDisplay>) categories.get(0);
        this.tabs = new ArrayList<>();
        this.choosePageActivated = false;
    }
    
    static void transformNotice(List<Widget> setupDisplay, EntryStack mainStackToNotice) {
        if (mainStackToNotice.isEmpty())
            return;
        for (Widget widget : setupDisplay) {
            if (widget instanceof EntryWidget) {
                EntryWidget entry = (EntryWidget) widget;
                if (entry.entries().size() > 1) {
                    EntryStack stack = CollectionUtils.firstOrNullEqualsAll(entry.entries(), mainStackToNotice);
                    if (stack != null) {
                        entry.clearStacks();
                        entry.entry(stack);
                    }
                }
            }
        }
    }
    
    public void addMainStackToNotice(EntryStack stack) {
        this.mainStackToNotice = stack;
    }
    
    @Nullable
    public CategoryBaseWidget getWorkingStationsBaseWidget() {
        return workingStationsBaseWidget;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && choosePageActivated) {
            choosePageActivated = false;
            init();
            return true;
        }
        if ((int_1 == 256 || this.minecraft.options.keyInventory.matchesKey(int_1, int_2)) && this.shouldCloseOnEsc()) {
            MinecraftClient.getInstance().openScreen(ScreenHelper.getLastContainerScreen());
            ScreenHelper.getLastOverlay().init();
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
            if (recipeNext.enabled)
                recipeNext.onPressed();
            return recipeNext.enabled;
        } else if (ConfigObject.getInstance().getPreviousPageKeybind().matchesKey(int_1, int_2)) {
            if (recipeBack.enabled)
                recipeBack.onPressed();
            return recipeBack.enabled;
        }
        for (Element element : children())
            if (element.keyPressed(int_1, int_2, int_3))
                return true;
        if (int_1 == 259) {
            if (ScreenHelper.hasLastRecipeScreen())
                minecraft.openScreen(ScreenHelper.getLastRecipeScreen());
            else
                minecraft.openScreen(ScreenHelper.getLastContainerScreen());
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
        this.largestHeight = Math.max(height - 36, 100);
        int maxWidthDisplay = CollectionUtils.mapAndMax(getCurrentDisplayed(), display -> selectedCategory.getDisplayWidth(display), (Comparator<Integer>) Comparator.naturalOrder()).orElse(150);
        this.guiWidth = maxWidthDisplay + 20;
        this.guiHeight = MathHelper.floor(MathHelper.clamp((double) (selectedCategory.getDisplayHeight() + 4) * (getRecipesPerPage() + 1) + 36, 100, largestHeight));
        this.tabsPerPage = Math.max(5, MathHelper.floor((guiWidth - 20d) / tabSize));
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, height / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.page = MathHelper.clamp(page, 0, getTotalPages(selectedCategory) - 1);
        ButtonWidget w, w2;
        this.widgets.add(w = new ButtonWidget(new Rectangle(bounds.x + 2, bounds.y - 16, 10, 10), I18n.translate("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                categoryPages--;
                if (categoryPages < 0)
                    categoryPages = MathHelper.ceil(categories.size() / (float) tabsPerPage) - 1;
                RecipeViewingScreen.this.init();
            }
        });
        this.widgets.add(w2 = new ButtonWidget(new Rectangle(bounds.x + bounds.width - 12, bounds.y - 16, 10, 10), I18n.translate("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                categoryPages++;
                if (categoryPages > MathHelper.ceil(categories.size() / (float) tabsPerPage) - 1)
                    categoryPages = 0;
                RecipeViewingScreen.this.init();
            }
        });
        w.enabled = w2.enabled = categories.size() > tabsPerPage;
        widgets.add(categoryBack = new ButtonWidget(new Rectangle(bounds.getX() + 5, bounds.getY() + 5, 12, 12), I18n.translate("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex--;
                if (currentCategoryIndex < 0)
                    currentCategoryIndex = categories.size() - 1;
                selectedCategory = (RecipeCategory<RecipeDisplay>) categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / (double) tabsPerPage);
                page = 0;
                RecipeViewingScreen.this.init();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.previous_category"));
            }
        });
        widgets.add(new ClickableLabelWidget(new Point(bounds.getCenterX(), bounds.getY() + 7), "") {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                setText(selectedCategory.getCategoryName());
                super.render(mouseX, mouseY, delta);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.view_all_categories"));
            }
            
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ClientHelper.getInstance().executeViewAllRecipesKeyBind();
            }
        });
        widgets.add(categoryNext = new ButtonWidget(new Rectangle(bounds.getMaxX() - 17, bounds.getY() + 5, 12, 12), I18n.translate("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex++;
                if (currentCategoryIndex >= categories.size())
                    currentCategoryIndex = 0;
                selectedCategory = (RecipeCategory<RecipeDisplay>) categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / (double) tabsPerPage);
                page = 0;
                RecipeViewingScreen.this.init();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.next_category"));
            }
        });
        categoryBack.enabled = categories.size() > 1;
        categoryNext.enabled = categories.size() > 1;
        
        widgets.add(recipeBack = new ButtonWidget(new Rectangle(bounds.getX() + 5, bounds.getY() + 19, 12, 12), I18n.translate("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                page--;
                if (page < 0)
                    page = getTotalPages(selectedCategory) - 1;
                RecipeViewingScreen.this.init();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.previous_page"));
            }
        });
        widgets.add(new ClickableLabelWidget(new Point(bounds.getCenterX(), bounds.getY() + 21), "") {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                setText(String.format("%d/%d", page + 1, getTotalPages(selectedCategory)));
                super.render(mouseX, mouseY, delta);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.choose_page"));
            }
            
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                RecipeViewingScreen.this.choosePageActivated = true;
                RecipeViewingScreen.this.init();
            }
        }.clickable(categoriesMap.get(selectedCategory).size() > getRecipesPerPageByHeight()));
        widgets.add(recipeNext = new ButtonWidget(new Rectangle(bounds.getMaxX() - 17, bounds.getY() + 19, 12, 12), I18n.translate("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                page++;
                if (page >= getTotalPages(selectedCategory))
                    page = 0;
                RecipeViewingScreen.this.init();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.next_page"));
            }
        });
        recipeBack.enabled = recipeNext.enabled = categoriesMap.get(selectedCategory).size() > getRecipesPerPageByHeight();
        int tabV = isCompactTabs ? 166 : 192;
        for (int i = 0; i < tabsPerPage; i++) {
            int j = i + categoryPages * tabsPerPage;
            if (categories.size() > j) {
                TabWidget tab;
                tabs.add(tab = new TabWidget(i, tabSize, bounds.x + bounds.width / 2 - Math.min(categories.size() - categoryPages * tabsPerPage, tabsPerPage) * tabSize / 2, bounds.y, 0, tabV) {
                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (containsMouse(mouseX, mouseY)) {
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (getId() + categoryPages * tabsPerPage == categories.indexOf(selectedCategory))
                                return false;
                            selectedCategory = (RecipeCategory<RecipeDisplay>) categories.get(getId() + categoryPages * tabsPerPage);
                            page = 0;
                            RecipeViewingScreen.this.init();
                            return true;
                        }
                        return false;
                    }
                });
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
            final Rectangle displayBounds = new Rectangle(getBounds().getCenterX() - displayWidth / 2, getBounds().y - 2 + 36 + recipeHeight * i + 4 * i, displayWidth, recipeHeight);
            List<Widget> setupDisplay = selectedCategory.setupDisplay(displaySupplier, displayBounds);
            transformNotice(setupDisplay, mainStackToNotice);
            recipeBounds.put(displayBounds, setupDisplay);
            this.widgets.addAll(setupDisplay);
            if (supplier.isPresent() && supplier.get().get(displayBounds) != null)
                this.widgets.add(new AutoCraftingButtonWidget(displayBounds, supplier.get().get(displayBounds), supplier.get().getButtonText(), displaySupplier, setupDisplay, selectedCategory));
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
            preWidgets.add(workingStationsBaseWidget = new CategoryBaseWidget(new Rectangle(xx - 5, yy - 5, 15 + innerWidth * 16, 10 + actualHeight * 16)));
            preWidgets.add(new SlotBaseWidget(new Rectangle(xx - 1, yy - 1, innerWidth * 16 + 2, actualHeight * 16 + 2)));
            int index = 0;
            List<String> list = Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("text.rei.working_station"));
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
    public void render(int mouseX, int mouseY, float delta) {
        this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        for (Widget widget : preWidgets) {
            widget.render(mouseX, mouseY, delta);
        }
        if (selectedCategory != null)
            selectedCategory.drawCategoryBackground(bounds, mouseX, mouseY, delta);
        else {
            PanelWidget.render(bounds, -1);
            if (ScreenHelper.isDarkModeEnabled()) {
                fill(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF404040);
                fill(bounds.x + 17, bounds.y + 19, bounds.x + bounds.width - 17, bounds.y + 30, 0xFF404040);
            } else {
                fill(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF9E9E9E);
                fill(bounds.x + 17, bounds.y + 19, bounds.x + bounds.width - 17, bounds.y + 31, 0xFF9E9E9E);
            }
        }
        for (TabWidget tab : tabs) {
            if (!tab.isSelected())
                tab.render(mouseX, mouseY, delta);
        }
        super.render(mouseX, mouseY, delta);
        for (Widget widget : widgets) {
            widget.render(mouseX, mouseY, delta);
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        for (TabWidget tab : tabs) {
            if (tab.isSelected())
                tab.render(mouseX, mouseY, delta);
        }
        ScreenHelper.getLastOverlay().render(mouseX, mouseY, delta);
        ScreenHelper.getLastOverlay().lateRender(mouseX, mouseY, delta);
        {
            ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
            if (export.matchesCurrentKey()) {
                for (Map.Entry<Rectangle, List<Widget>> entry : recipeBounds.entrySet()) {
                    Rectangle bounds = entry.getKey();
                    setBlitOffset(470);
                    if (bounds.contains(mouseX, mouseY)) {
                        fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 1744822402, 1744822402);
                        String s = I18n.translate("text.rei.release_export", export.getLocalizedName());
                        MatrixStack matrixStack_1 = new MatrixStack();
                        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                        matrixStack_1.translate(0.0D, 0.0D, 480);
                        Matrix4f matrix4f_1 = matrixStack_1.peek().getModel();
                        font.draw(s, bounds.getCenterX() - font.getStringWidth(s) / 2f, bounds.getCenterY() - 4.5f, 0xff000000, false, matrix4f_1, immediate, false, 0, 15728880);
                        immediate.draw();
                    } else {
                        fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 1744830463, 1744830463);
                    }
                    setBlitOffset(0);
                }
            }
        }
        if (choosePageActivated) {
            setBlitOffset(500);
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            setBlitOffset(0);
            recipeChoosePageWidget.render(mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
        if (export.matchesKey(keyCode, scanCode)) {
            for (Map.Entry<Rectangle, List<Widget>> entry : recipeBounds.entrySet()) {
                Rectangle bounds = entry.getKey();
                if (bounds.contains(PointHelper.fromMouse())) {
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
        if (getBounds().contains(PointHelper.fromMouse())) {
            if (amount > 0 && recipeBack.enabled)
                recipeBack.onPressed();
            else if (amount < 0 && recipeNext.enabled)
                recipeNext.onPressed();
        }
        if ((new Rectangle(bounds.x, bounds.y - 28, bounds.width, 28)).contains(PointHelper.fromMouse())) {
            if (amount > 0 && categoryBack.enabled)
                categoryBack.onPressed();
            else if (amount < 0 && categoryNext.enabled)
                categoryNext.onPressed();
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
