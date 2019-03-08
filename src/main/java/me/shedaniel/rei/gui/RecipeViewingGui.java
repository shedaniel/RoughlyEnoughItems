package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.gui.widget.*;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class RecipeViewingGui extends GuiScreen {
    
    public static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final Color SUB_COLOR = new Color(159, 159, 159);
    private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private final List<IWidget> widgets;
    private final List<TabWidget> tabs;
    private final Map<RecipeCategory, List<RecipeDisplay>> categoriesMap;
    private final List<RecipeCategory> categories;
    public int guiWidth;
    public int guiHeight;
    public int largestWidth, largestHeight;
    public boolean choosePageActivated;
    public RecipeChoosePageWidget recipeChoosePageWidget;
    public int page, categoryPages;
    private MainWindow window;
    private Rectangle bounds;
    private RecipeCategory selectedCategory;
    private ButtonWidget recipeBack, recipeNext, categoryBack, categoryNext;
    
    public RecipeViewingGui(MainWindow window, Map<RecipeCategory, List<RecipeDisplay>> categoriesMap) {
        this.categoryPages = 0;
        this.window = window;
        this.widgets = Lists.newArrayList();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, guiWidth, guiHeight);
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
    
    public static SpeedCraftFunctional getSpeedCraftFunctionalByCategory(GuiContainer guiContainer, RecipeCategory category) {
        for(SpeedCraftFunctional functional : RecipeHelper.getInstance().getSpeedCraftFunctional(category))
            for(Class aClass : functional.getFunctioningFor())
                if (guiContainer.getClass().isAssignableFrom(aClass))
                    return functional;
        return null;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && choosePageActivated) {
            choosePageActivated = false;
            initGui();
            return true;
        }
        if ((int_1 == 256 || mc.gameSettings.keyBindInventory.matchesKey(int_1, int_2)) && this.allowCloseWithEscape()) {
            Minecraft.getInstance().displayGuiScreen(GuiHelper.getLastGuiContainer());
            GuiHelper.getLastOverlay().init();
            return true;
        }
        if (choosePageActivated) {
            if (recipeChoosePageWidget.keyPressed(int_1, int_2, int_3))
                return true;
            return false;
        }
        for(IGuiEventListener listener : children)
            if (listener.keyPressed(int_1, int_2, int_3))
                return true;
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        this.children.clear();
        this.tabs.clear();
        this.widgets.clear();
        this.largestWidth = window.getScaledWidth() - 100;
        this.largestHeight = window.getScaledHeight() - 40;
        this.guiWidth = MathHelper.clamp(getCurrentDisplayed().stream().map(display -> selectedCategory.getDisplayWidth(display)).max(Integer::compareTo).orElse(150) + 30, 0, largestWidth);
        this.guiHeight = MathHelper.floor(MathHelper.clamp((selectedCategory.getDisplayHeight() + 7d) * (getRecipesPerPage() + 1d) + 40d, 186d, (double) largestHeight));
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.page = MathHelper.clamp(page, 0, getTotalPages(selectedCategory) - 1);
        
        widgets.add(categoryBack = new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 5, 12, 12, new TextComponentTranslation("text.rei.left_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex--;
                if (currentCategoryIndex < 0)
                    currentCategoryIndex = categories.size() - 1;
                selectedCategory = categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / 6d);
                page = 0;
                RecipeViewingGui.this.initGui();
            }
        });
        widgets.add(categoryNext = new ButtonWidget((int) bounds.getMaxX() - 17, (int) bounds.getY() + 5, 12, 12, new TextComponentTranslation("text.rei.right_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex++;
                if (currentCategoryIndex >= categories.size())
                    currentCategoryIndex = 0;
                selectedCategory = categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / 6d);
                page = 0;
                RecipeViewingGui.this.initGui();
            }
        });
        categoryBack.enabled = categories.size() > 1;
        categoryNext.enabled = categories.size() > 1;
        
        widgets.add(recipeBack = new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 21, 12, 12, new TextComponentTranslation("text.rei.left_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page--;
                if (page < 0)
                    page = getTotalPages(selectedCategory) - 1;
                RecipeViewingGui.this.initGui();
            }
        });
        widgets.add(recipeNext = new ButtonWidget((int) bounds.getMaxX() - 17, (int) bounds.getY() + 21, 12, 12, new TextComponentTranslation("text.rei.right_arrow")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page++;
                if (page >= getTotalPages(selectedCategory))
                    page = 0;
                RecipeViewingGui.this.initGui();
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
                    GuiHelper.getLastOverlay().addTooltip(QueuedTooltip.create(I18n.format("text.rei.view_all_categories").split("\n")));
            }
            
            @Override
            public void onLabelClicked() {
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ClientHelper.executeViewAllRecipesKeyBind();
            }
        });
        widgets.add(new ClickableLabelWidget((int) bounds.getCenterX(), (int) bounds.getY() + 23, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                this.text = String.format("%d/%d", page + 1, getTotalPages(selectedCategory));
                super.draw(mouseX, mouseY, partialTicks);
                if (isHighlighted(mouseX, mouseY))
                    GuiHelper.getLastOverlay().addTooltip(QueuedTooltip.create(I18n.format("text.rei.choose_page").split("\n")));
            }
            
            @Override
            public void onLabelClicked() {
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                RecipeViewingGui.this.choosePageActivated = true;
                RecipeViewingGui.this.initGui();
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
                            Minecraft.getInstance().getSoundHandler().play(SimpleSound.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (getId() + categoryPages * 6 == categories.indexOf(selectedCategory))
                                return false;
                            selectedCategory = categories.get(getId() + categoryPages * 6);
                            page = 0;
                            RecipeViewingGui.this.initGui();
                            return true;
                        }
                        return false;
                    }
                });
                tab.setItem(categories.get(j).getCategoryIcon(), categories.get(j).getCategoryName(), tab.getId() + categoryPages * 6 == categories.indexOf(selectedCategory));
            }
        }
        
        Optional<ButtonAreaSupplier> supplier = RecipeHelper.getInstance().getSpeedCraftButtonArea(selectedCategory);
        final SpeedCraftFunctional functional = getSpeedCraftFunctionalByCategory(GuiHelper.getLastGuiContainer(), selectedCategory);
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
        
        children.addAll(tabs);
        children.add(GuiHelper.getLastOverlay(true, false));
        children.addAll(widgets);
    }
    
    public List<IWidget> getWidgets() {
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
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        if (selectedCategory != null)
            selectedCategory.drawCategoryBackground(bounds, mouseX, mouseY, partialTicks);
        else {
            new RecipeBaseWidget(bounds).draw(mouseX, mouseY, partialTicks);
            drawRect(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, SUB_COLOR.getRGB());
            drawRect(bounds.x + 17, bounds.y + 21, bounds.x + bounds.width - 17, bounds.y + 33, SUB_COLOR.getRGB());
        }
        tabs.stream().filter(tabWidget -> {
            return !tabWidget.isSelected();
        }).forEach(tabWidget -> tabWidget.draw(mouseX, mouseY, partialTicks));
        RenderHelper.disableStandardItemLighting();
        super.render(mouseX, mouseY, partialTicks);
        widgets.forEach(widget -> {
            RenderHelper.disableStandardItemLighting();
            widget.draw(mouseX, mouseY, partialTicks);
        });
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        tabs.stream().filter(TabWidget::isSelected).forEach(tabWidget -> tabWidget.draw(mouseX, mouseY, partialTicks));
        GuiHelper.getLastOverlay().renderOverlay(mouseX, mouseY, partialTicks);
        if (choosePageActivated) {
            zLevel = 500.0f;
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
            zLevel = 0.0f;
            recipeChoosePageWidget.draw(mouseX, mouseY, partialTicks);
        }
    }
    
    public int getPage() {
        return page;
    }
    
    public int getCategoryPage() {
        return categoryPages;
    }
    
    public int getTotalPages(RecipeCategory category) {
        return MathHelper.ceil(categoriesMap.get(category).size() / ((double) getRecipesPerPage() + 1));
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
        for(IGuiEventListener listener : children)
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
        for(IGuiEventListener listener : children)
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
                initGui();
                return false;
            }
        for(IGuiEventListener entry : getChildren())
            if (entry.mouseClicked(double_1, double_2, int_1)) {
                focusOn(entry);
                if (int_1 == 0)
                    setDragging(true);
                return true;
            }
        return false;
    }
    
    public RecipeCategory getSelectedCategory() {
        return selectedCategory;
    }
    
    @Override
    public IGuiEventListener getFocused() {
        if (choosePageActivated)
            return recipeChoosePageWidget;
        return super.getFocused();
    }
    
}
