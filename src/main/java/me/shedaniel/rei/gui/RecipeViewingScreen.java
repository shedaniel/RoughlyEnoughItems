package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
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
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class RecipeViewingScreen extends Screen {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final Color SUB_COLOR = new Color(159, 159, 159);
    private static final Identifier CREATIVE_INVENTORY_TABS = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    public int guiWidth;
    public int guiHeight;
    public int page, categoryPages;
    public int largestWidth, largestHeight;
    public boolean choosePageActivated;
    public RecipeChoosePageWidget recipeChoosePageWidget;
    private List<IWidget> widgets;
    private List<TabWidget> tabs;
    private Window window;
    private Rectangle bounds;
    private Map<IRecipeCategory, List<IRecipeDisplay>> categoriesMap;
    private List<IRecipeCategory> categories;
    private IRecipeCategory selectedCategory;
    private ButtonWidget recipeBack, recipeNext, categoryBack, categoryNext;
    
    public RecipeViewingScreen(Window window, Map<IRecipeCategory, List<IRecipeDisplay>> categoriesMap) {
        this.categoryPages = 0;
        this.window = window;
        this.widgets = Lists.newArrayList();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, 176, 186);
        this.categoriesMap = categoriesMap;
        this.categories = Lists.newArrayList();
        IRecipeHelper.getInstance().getAllCategories().forEach(category -> {
            if (categoriesMap.containsKey(category))
                categories.add(category);
        });
        this.selectedCategory = categories.get(0);
        this.tabs = new ArrayList<>();
        this.choosePageActivated = false;
    }
    
    public static SpeedCraftFunctional getSpeedCraftFunctionalByCategory(ContainerScreen containerScreen, IRecipeCategory category) {
        for(SpeedCraftFunctional functional : IRecipeHelper.getInstance().getSpeedCraftFunctional(category))
            for(Class aClass : functional.getFunctioningFor())
                if (containerScreen.getClass().isAssignableFrom(aClass))
                    return functional;
        return null;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if ((int_1 == 256 || this.client.options.keyInventory.matchesKey(int_1, int_2)) && this.doesEscapeKeyClose()) {
            MinecraftClient.getInstance().openScreen(GuiHelper.getLastContainerScreen());
            GuiHelper.getLastOverlay().onInitialized();
            return true;
        }
        if (choosePageActivated) {
            if (recipeChoosePageWidget.keyPressed(int_1, int_2, int_3))
                return true;
            return false;
        }
        for(InputListener listener : listeners)
            if (listener.keyPressed(int_1, int_2, int_3))
                return true;
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void onInitialized() {
        super.onInitialized();
        this.tabs.clear();
        this.widgets.clear();
        this.largestWidth = window.getScaledWidth() - 100;
        this.largestHeight = window.getScaledHeight() - 40;
        this.guiWidth = MathHelper.clamp(getCurrentDisplayed().stream().map(display -> selectedCategory.getDisplayWidth(display)).max(Integer::compareTo).orElse(150) + 30, 0, largestWidth);
        this.guiHeight = MathHelper.floor(MathHelper.clamp((selectedCategory.getDisplayHeight() + 7) * (getRecipesPerPage() + 1) + 40f, 186f, (float) largestHeight));
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.page = MathHelper.clamp(page, 0, getTotalPages(selectedCategory) - 1);
        
        widgets.add(categoryBack = new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 5, 12, 12, new TranslatableTextComponent("text.rei.left_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex--;
                if (currentCategoryIndex < 0)
                    currentCategoryIndex = categories.size() - 1;
                selectedCategory = categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / 6d);
                RecipeViewingScreen.this.onInitialized();
            }
        });
        widgets.add(categoryNext = new ButtonWidget((int) bounds.getMaxX() - 17, (int) bounds.getY() + 5, 12, 12, new TranslatableTextComponent("text.rei.right_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex++;
                if (currentCategoryIndex >= categories.size())
                    currentCategoryIndex = 0;
                selectedCategory = categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / 6d);
                RecipeViewingScreen.this.onInitialized();
            }
        });
        categoryBack.enabled = categories.size() > 1;
        categoryNext.enabled = categories.size() > 1;
        
        widgets.add(recipeBack = new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 21, 12, 12, new TranslatableTextComponent("text.rei.left_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page--;
                if (page < 0)
                    page = getTotalPages(selectedCategory) - 1;
                RecipeViewingScreen.this.onInitialized();
            }
        });
        widgets.add(recipeNext = new ButtonWidget((int) bounds.getMaxX() - 17, (int) bounds.getY() + 21, 12, 12, new TranslatableTextComponent("text.rei.right_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page++;
                if (page >= getTotalPages(selectedCategory))
                    page = 0;
                RecipeViewingScreen.this.onInitialized();
            }
        });
        recipeBack.enabled = categoriesMap.get(selectedCategory).size() > getRecipesPerPage();
        recipeNext.enabled = categoriesMap.get(selectedCategory).size() > getRecipesPerPage();
        
        widgets.add(new ClickableLabelWidget((int) bounds.getCenterX(), (int) bounds.getY() + 7, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                this.text = selectedCategory.getCategoryName();
                super.draw(mouseX, mouseY, partialTicks);
                if (isHighlighted(mouseX, mouseY))
                    GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(new Point(mouseX, mouseY), Arrays.asList(I18n.translate("text.rei.view_all_categories").split("\n"))));
            }
            
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundLoader().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ClientHelper.executeViewAllRecipesKeyBind(GuiHelper.getLastOverlay());
            }
        });
        widgets.add(new ClickableLabelWidget((int) bounds.getCenterX(), (int) bounds.getY() + 23, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                this.text = String.format("%d/%d", page + 1, getTotalPages(selectedCategory));
                super.draw(mouseX, mouseY, partialTicks);
                if (isHighlighted(mouseX, mouseY))
                    GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(new Point(mouseX, mouseY), Arrays.asList(I18n.translate("text.rei.choose_page").split("\n"))));
            }
            
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundLoader().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                RecipeViewingScreen.this.choosePageActivated = true;
                RecipeViewingScreen.this.onInitialized();
            }
        });
        for(int i = 0; i < 6; i++) {
            int j = i + categoryPages * 6;
            if (categories.size() > j) {
                TabWidget tab;
                tabs.add(tab = new TabWidget(i, this, new Rectangle(bounds.x + 4 + 28 * i, bounds.y - 28, 28, 28)) {
                    @Override
                    public boolean onMouseClick(int button, double mouseX, double mouseY) {
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
        Optional<SpeedCraftAreaSupplier> supplier = IRecipeHelper.getInstance().getSpeedCraftButtonArea(selectedCategory);
        final SpeedCraftFunctional functional = getSpeedCraftFunctionalByCategory(GuiHelper.getLastContainerScreen(), selectedCategory);
        int recipeHeight = selectedCategory.getDisplayHeight();
        List<IRecipeDisplay> currentDisplayed = getCurrentDisplayed();
        for(int i = 0; i < currentDisplayed.size(); i++) {
            int finalI = i;
            final Supplier<IRecipeDisplay> displaySupplier = () -> {
                return currentDisplayed.get(finalI);
            };
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
    
        GuiHelper.getLastOverlay().onInitialized();
        listeners.addAll(tabs);
        listeners.add(GuiHelper.getLastOverlay());
        listeners.addAll(widgets);
    }
    
    public List<IRecipeDisplay> getCurrentDisplayed() {
        List<IRecipeDisplay> list = Lists.newArrayList();
        int recipesPerPage = getRecipesPerPage();
        for(int i = 0; i <= recipesPerPage; i++)
            if (recipesPerPage > 0 && page * (recipesPerPage + 1) + i < categoriesMap.get(selectedCategory).size())
                list.add(categoriesMap.get(selectedCategory).get(page * (recipesPerPage + 1) + i));
        return list;
    }
    
    public IRecipeCategory getSelectedCategory() {
        return selectedCategory;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getCategoryPage() {
        return categoryPages;
    }
    
    private int getRecipesPerPage() {
        int height = selectedCategory.getDisplayHeight();
        return MathHelper.clamp(MathHelper.floor(((float) largestHeight - 40f) / ((float) height + 7f)) - 1, 0, Math.min(RoughlyEnoughItemsCore.getConfigHelper().getConfig().maxRecipePerPage - 1, selectedCategory.getMaximumRecipePerPage() - 1));
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float delta) {
        this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
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
        GuiHelper.getLastOverlay().drawOverlay(mouseX, mouseY, delta);
        if (choosePageActivated) {
            zOffset = 500.0f;
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
            zOffset = 0.0f;
            recipeChoosePageWidget.draw(mouseX, mouseY, delta);
        }
    }
    
    public int getTotalPages(IRecipeCategory category) {
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
    public boolean mouseScrolled(double amount) {
        for(InputListener listener : listeners)
            if (listener.mouseScrolled(amount))
                return true;
        if (getBounds().contains(ClientHelper.getMouseLocation())) {
            if (amount > 0 && recipeBack.enabled)
                recipeBack.onPressed(0, 0, 0);
            else if (amount < 0 && recipeNext.enabled)
                recipeNext.onPressed(0, 0, 0);
        }
        if ((new Rectangle(bounds.x, bounds.y - 28, bounds.width, 28)).contains(ClientHelper.getMouseLocation())) {
            if (amount > 0 && categoryBack.enabled)
                categoryBack.onPressed(0, 0, 0);
            else if (amount < 0 && categoryNext.enabled)
                categoryNext.onPressed(0, 0, 0);
        }
        return super.mouseScrolled(amount);
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
                    method_1966(true); //setActive
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
