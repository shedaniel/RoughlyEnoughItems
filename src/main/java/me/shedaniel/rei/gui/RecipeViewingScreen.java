package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.widget.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.audio.PositionedSoundInstance;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.InputListener;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class RecipeViewingScreen extends Screen {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final Color SUB_COLOR = new Color(159, 159, 159);
    private static final Identifier CREATIVE_INVENTORY_TABS = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private final List<Widget> widgets;
    private final List<TabWidget> tabs;
    private final Map<RecipeCategory, List<RecipeDisplay>> categoriesMap;
    private final List<RecipeCategory> categories;
    public int guiWidth;
    public int guiHeight;
    public int page, categoryPages;
    public int largestWidth, largestHeight;
    public boolean choosePageActivated;
    public RecipeChoosePageWidget recipeChoosePageWidget;
    private Window window;
    private Rectangle bounds;
    private RecipeCategory selectedCategory;
    private ButtonWidget recipeBack, recipeNext, categoryBack, categoryNext;
    
    public RecipeViewingScreen(Window window, Map<RecipeCategory, List<RecipeDisplay>> categoriesMap) {
        this.categoryPages = 0;
        this.window = window;
        this.widgets = Lists.newArrayList();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, 176, 186);
        this.categoriesMap = categoriesMap;
        this.categories = Lists.newArrayList();
        RecipeHelper.getInstance().getAllCategories().forEach(category -> {
            if (categoriesMap.containsKey(category))
                categories.add(category);
        });
        this.selectedCategory = categories.get(0);
        this.tabs = new ArrayList<>();
        this.choosePageActivated = false;
    }
    
    public static SpeedCraftFunctional getSpeedCraftFunctionalByCategory(ContainerScreen containerScreen, RecipeCategory category) {
        for(SpeedCraftFunctional functional : RecipeHelper.getInstance().getSpeedCraftFunctional(category))
            for(Class aClass : functional.getFunctioningFor())
                if (containerScreen.getClass().isAssignableFrom(aClass))
                    return functional;
        return null;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && choosePageActivated) {
            choosePageActivated = false;
            onInitialized();
            return true;
        }
        if ((int_1 == 256 || this.client.options.keyInventory.matchesKey(int_1, int_2)) && this.doesEscapeKeyClose()) {
            MinecraftClient.getInstance().openScreen(ScreenHelper.getLastContainerScreen());
            ScreenHelper.getLastOverlay().onInitialized();
            return true;
        }
        if (int_1 == 258) {
            if (isShiftPressed())
                this.method_19354();
            else
                this.focusNext();
            return true;
        }
        if (choosePageActivated) {
            if (recipeChoosePageWidget.keyPressed(int_1, int_2, int_3))
                return true;
            return false;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void onInitialized() {
        super.onInitialized();
        this.listeners.clear();
        this.tabs.clear();
        this.widgets.clear();
        this.largestWidth = window.getScaledWidth() - 100;
        this.largestHeight = window.getScaledHeight() - 40;
        this.guiWidth = MathHelper.clamp(getCurrentDisplayed().stream().map(display -> selectedCategory.getDisplayWidth(display)).max(Integer::compareTo).orElse(150) + 30, 0, largestWidth);
        this.guiHeight = MathHelper.floor(MathHelper.clamp((selectedCategory.getDisplayHeight() + 7d) * (getRecipesPerPage() + 1d) + 40d, 186d, (double) largestHeight));
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.page = MathHelper.clamp(page, 0, getTotalPages(selectedCategory) - 1);
        
        widgets.add(categoryBack = new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 5, 12, 12, new TranslatableTextComponent("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex--;
                if (currentCategoryIndex < 0)
                    currentCategoryIndex = categories.size() - 1;
                selectedCategory = categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / 6d);
                page = 0;
                RecipeViewingScreen.this.onInitialized();
            }
        });
        widgets.add(new ClickableLabelWidget((int) bounds.getCenterX(), (int) bounds.getY() + 7, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                this.text = selectedCategory.getCategoryName();
                super.draw(mouseX, mouseY, partialTicks);
                if (isHighlighted(mouseX, mouseY))
                    ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(I18n.translate("text.rei.view_all_categories").split("\n")));
                else if (focused)
                    ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(new Point(x, y), I18n.translate("text.rei.view_all_categories").split("\n")));
            }
            
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundLoader().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ClientHelper.executeViewAllRecipesKeyBind();
            }
        });
        widgets.add(categoryNext = new ButtonWidget((int) bounds.getMaxX() - 17, (int) bounds.getY() + 5, 12, 12, new TranslatableTextComponent("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex++;
                if (currentCategoryIndex >= categories.size())
                    currentCategoryIndex = 0;
                selectedCategory = categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / 6d);
                page = 0;
                RecipeViewingScreen.this.onInitialized();
            }
        });
        categoryBack.enabled = categories.size() > 1;
        categoryNext.enabled = categories.size() > 1;
        
        widgets.add(recipeBack = new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 21, 12, 12, new TranslatableTextComponent("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                page--;
                if (page < 0)
                    page = getTotalPages(selectedCategory) - 1;
                RecipeViewingScreen.this.onInitialized();
            }
        });
        widgets.add(new ClickableLabelWidget((int) bounds.getCenterX(), (int) bounds.getY() + 23, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                this.text = String.format("%d/%d", page + 1, getTotalPages(selectedCategory));
                super.draw(mouseX, mouseY, partialTicks);
                if (isHighlighted(mouseX, mouseY))
                    ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(I18n.translate("text.rei.choose_page").split("\n")));
                else if (focused)
                    ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(new Point(x, y), I18n.translate("text.rei.choose_page").split("\n")));
            }
            
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundLoader().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                RecipeViewingScreen.this.choosePageActivated = true;
                RecipeViewingScreen.this.onInitialized();
            }
        });
        widgets.add(recipeNext = new ButtonWidget((int) bounds.getMaxX() - 17, (int) bounds.getY() + 21, 12, 12, new TranslatableTextComponent("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                page++;
                if (page >= getTotalPages(selectedCategory))
                    page = 0;
                RecipeViewingScreen.this.onInitialized();
            }
        });
        recipeBack.enabled = categoriesMap.get(selectedCategory).size() > getRecipesPerPage();
        recipeNext.enabled = categoriesMap.get(selectedCategory).size() > getRecipesPerPage();
        
        for(int i = 0; i < 6; i++) {
            int j = i + categoryPages * 6;
            if (categories.size() > j) {
                TabWidget tab;
                tabs.add(tab = new TabWidget(i, this, new Rectangle(bounds.x + 4 + 28 * i, bounds.y - 28, 28, 28)) {
                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (getBounds().contains(mouseX, mouseY)) {
                            MinecraftClient.getInstance().getSoundLoader().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (getId() + categoryPages * 6 == categories.indexOf(selectedCategory))
                                return false;
                            selectedCategory = categories.get(getId() + categoryPages * 6);
                            page = 0;
                            RecipeViewingScreen.this.onInitialized();
                            return true;
                        }
                        return false;
                    }
                });
                tab.setItem(categories.get(j).getCategoryIcon(), categories.get(j).getCategoryName(), tab.getId() + categoryPages * 6 == categories.indexOf(selectedCategory));
            }
        }
        Optional<ButtonAreaSupplier> supplier = RecipeHelper.getInstance().getSpeedCraftButtonArea(selectedCategory);
        final SpeedCraftFunctional functional = getSpeedCraftFunctionalByCategory(ScreenHelper.getLastContainerScreen(), selectedCategory);
        int recipeHeight = selectedCategory.getDisplayHeight();
        List<RecipeDisplay> currentDisplayed = getCurrentDisplayed();
        for(int i = 0; i < currentDisplayed.size(); i++) {
            int finalI = i;
            final Supplier<RecipeDisplay> displaySupplier = () -> currentDisplayed.get(finalI);
            int displayWidth = selectedCategory.getDisplayWidth(displaySupplier.get());
            final Rectangle displayBounds = new Rectangle((int) getBounds().getCenterX() - displayWidth / 2, getBounds().y + 40 + recipeHeight * i + 7 * i, displayWidth, recipeHeight);
            widgets.addAll(selectedCategory.setupDisplay(displaySupplier, displayBounds));
            if (supplier.isPresent())
                widgets.add(new SpeedCraftingButtonWidget(supplier.get().get(displayBounds), supplier.get().getButtonText(), functional, displaySupplier));
        }
        if (choosePageActivated)
            recipeChoosePageWidget = new RecipeChoosePageWidget(this, page, getTotalPages(selectedCategory));
        else
            recipeChoosePageWidget = null;
        
        listeners.addAll(tabs);
        listeners.add(ScreenHelper.getLastOverlay(true, false));
        listeners.addAll(widgets);
    }
    
    public List<Widget> getWidgets() {
        return widgets;
    }
    
    public List<RecipeDisplay> getCurrentDisplayed() {
        List<RecipeDisplay> list = Lists.newArrayList();
        int recipesPerPage = getRecipesPerPage();
        for(int i = 0; i <= recipesPerPage; i++)
            if (page * (recipesPerPage + 1) + i < categoriesMap.get(selectedCategory).size())
                list.add(categoriesMap.get(selectedCategory).get(page * (recipesPerPage + 1) + i));
        return list;
    }
    
    public RecipeCategory getSelectedCategory() {
        return selectedCategory;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getCategoryPage() {
        return categoryPages;
    }
    
    private int getRecipesPerPage() {
        if (selectedCategory.getDisplaySettings().getFixedRecipesPerPage() > 0)
            return selectedCategory.getDisplaySettings().getFixedRecipesPerPage() - 1;
        int height = selectedCategory.getDisplayHeight();
        return MathHelper.clamp(MathHelper.floor(((double) largestHeight - 40d) / ((double) height + 7d)) - 1, 0, Math.min(RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage - 1, selectedCategory.getMaximumRecipePerPage() - 1));
    }
    
    private int getRecipesPerPageByHeight() {
        int height = selectedCategory.getDisplayHeight();
        return MathHelper.clamp(MathHelper.floor(((double) guiHeight - 40d) / ((double) height + 7d)) - 1, 0, Math.min(RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage - 1, selectedCategory.getMaximumRecipePerPage() - 1));
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float delta) {
        this.drawGradientRect(0, 0, this.screenWidth, this.screenHeight, -1072689136, -804253680);
        if (selectedCategory != null)
            selectedCategory.drawCategoryBackground(bounds, mouseX, mouseY, delta);
        else {
            new RecipeBaseWidget(bounds).draw(mouseX, mouseY, delta);
            drawRect(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, SUB_COLOR.getRGB());
            drawRect(bounds.x + 17, bounds.y + 21, bounds.x + bounds.width - 17, bounds.y + 33, SUB_COLOR.getRGB());
        }
        tabs.stream().filter(tabWidget -> {
            return !tabWidget.isSelected();
        }).forEach(tabWidget -> tabWidget.draw(mouseX, mouseY, delta));
        GuiLighting.disable();
        super.draw(mouseX, mouseY, delta);
        widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.draw(mouseX, mouseY, delta);
        });
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        tabs.stream().filter(TabWidget::isSelected).forEach(tabWidget -> tabWidget.draw(mouseX, mouseY, delta));
        ScreenHelper.getLastOverlay().drawOverlay(mouseX, mouseY, delta);
        if (choosePageActivated) {
            zOffset = 500.0f;
            this.drawGradientRect(0, 0, this.screenWidth, this.screenHeight, -1072689136, -804253680);
            zOffset = 0.0f;
            recipeChoosePageWidget.draw(mouseX, mouseY, delta);
        }
    }
    
    public int getTotalPages(RecipeCategory category) {
        return MathHelper.ceil(categoriesMap.get(category).size() / (double) (getRecipesPerPage() + 1));
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (choosePageActivated) {
            if (recipeChoosePageWidget.charTyped(char_1, int_1))
                return true;
            return false;
        }
        for(InputListener listener : listeners)
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (choosePageActivated) {
            if (recipeChoosePageWidget.mouseDragged(double_1, double_2, int_1, double_3, double_4))
                return true;
            return false;
        }
        return super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
    }
    
    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        if (choosePageActivated) {
            if (recipeChoosePageWidget.mouseReleased(double_1, double_2, int_1))
                return true;
            return false;
        }
        return super.mouseReleased(double_1, double_2, int_1);
    }
    
    @Override
    public boolean mouseScrolled(double i, double j, double amount) {
        for(InputListener listener : listeners)
            if (listener.mouseScrolled(i, j, amount))
                return true;
        if (getBounds().contains(ClientHelper.getMouseLocation())) {
            if (amount > 0 && recipeBack.enabled)
                recipeBack.onPressed();
            else if (amount < 0 && recipeNext.enabled)
                recipeNext.onPressed();
        }
        if ((new Rectangle(bounds.x, bounds.y - 28, bounds.width, 28)).contains(ClientHelper.getMouseLocation())) {
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
            if (recipeChoosePageWidget.isHighlighted(double_1, double_2)) {
                if (recipeChoosePageWidget.mouseClicked(double_1, double_2, int_1))
                    return true;
                return false;
            } else {
                choosePageActivated = false;
                onInitialized();
                return false;
            }
        for(InputListener entry : getInputListeners())
            if (entry.mouseClicked(double_1, double_2, int_1)) {
                focusOn(entry);
                if (int_1 == 0)
                    setActive(true);
                return true;
            }
        return false;
    }
    
    @Override
    public InputListener getFocused() {
        if (choosePageActivated)
            return recipeChoosePageWidget;
        return super.getFocused();
    }
    
}
