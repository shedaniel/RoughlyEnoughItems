package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.api.SpeedCraftAreaSupplier;
import me.shedaniel.rei.api.SpeedCraftFunctional;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.RecipeHelper;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RecipeViewingWidgetGui extends GuiScreen {
    
    public static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    public final int guiWidth = 176;
    public final int guiHeight = 186;
    
    private List<IWidget> widgets;
    private List<TabWidget> tabs;
    private MainWindow window;
    private Rectangle bounds;
    private Map<IRecipeCategory, List<IRecipeDisplay>> categoriesMap;
    private List<IRecipeCategory> categories;
    private IRecipeCategory selectedCategory;
    private int page, categoryPages;
    private ButtonWidget recipeBack, recipeNext, categoryBack, categoryNext;
    
    public RecipeViewingWidgetGui(MainWindow window, Map<IRecipeCategory, List<IRecipeDisplay>> categoriesMap) {
        this.categoryPages = 0;
        this.window = window;
        this.widgets = Lists.newArrayList();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.categoriesMap = categoriesMap;
        this.categories = Lists.newArrayList();
        RecipeHelper.getInstance().getCategories().forEach(category -> {
            if (categoriesMap.containsKey(category))
                categories.add(category);
        });
        this.selectedCategory = categories.get(0);
        this.tabs = new ArrayList<>();
    }
    
    public static SpeedCraftFunctional getSpeedCraftFunctionalByCategory(GuiContainer guiContainer, IRecipeCategory category) {
        for(SpeedCraftFunctional functional : RecipeHelper.getInstance().getSpeedCraftFunctional(category))
            for(Class aClass : functional.getFunctioningFor())
                if (guiContainer.getClass().isAssignableFrom(aClass))
                    return functional;
        return null;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if ((int_1 == 256 || mc.gameSettings.keyBindInventory.matchesKey(int_1, int_2)) && this.allowCloseWithEscape()) {
            Minecraft.getInstance().displayGuiScreen(GuiHelper.getLastGuiContainer());
            return true;
        }
        for(IGuiEventListener listener : eventListeners)
            if (listener.keyPressed(int_1, int_2, int_3))
                return true;
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    @Override
    protected void initGui() {
        super.initGui();
        this.tabs.clear();
        this.widgets.clear();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, guiWidth, guiHeight);
        
        widgets.add(categoryBack = new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 5, 12, 12, "<") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex--;
                if (currentCategoryIndex < 0)
                    currentCategoryIndex = categories.size() - 1;
                selectedCategory = categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / 6d);
                RecipeViewingWidgetGui.this.initGui();
            }
        });
        widgets.add(categoryNext = new ButtonWidget((int) bounds.getX() + 159, (int) bounds.getY() + 5, 12, 12, ">") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex++;
                if (currentCategoryIndex >= categories.size())
                    currentCategoryIndex = 0;
                selectedCategory = categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / 6d);
                RecipeViewingWidgetGui.this.initGui();
            }
        });
        categoryBack.enabled = categories.size() > 1;
        categoryNext.enabled = categories.size() > 1;
        
        widgets.add(recipeBack = new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 21, 12, 12, "<") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page--;
                if (page < 0)
                    page = getTotalPages(selectedCategory) - 1;
                RecipeViewingWidgetGui.this.initGui();
            }
        });
        widgets.add(recipeNext = new ButtonWidget((int) bounds.getX() + 159, (int) bounds.getY() + 21, 12, 12, ">") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page++;
                if (page >= getTotalPages(selectedCategory))
                    page = 0;
                RecipeViewingWidgetGui.this.initGui();
            }
        });
        recipeBack.enabled = categoriesMap.get(selectedCategory).size() > getRecipesPerPage();
        recipeNext.enabled = categoriesMap.get(selectedCategory).size() > getRecipesPerPage();
        
        widgets.add(new LabelWidget((int) bounds.getCenterX(), (int) bounds.getY() + 7, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                this.text = selectedCategory.getCategoryName();
                super.draw(mouseX, mouseY, partialTicks);
            }
        });
        widgets.add(new LabelWidget((int) bounds.getCenterX(), (int) bounds.getY() + 23, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                this.text = String.format("%d/%d", page + 1, getTotalPages(selectedCategory));
                super.draw(mouseX, mouseY, partialTicks);
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
                            Minecraft.getInstance().getSoundHandler().play(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (getId() + categoryPages * 6 == categories.indexOf(selectedCategory))
                                return false;
                            selectedCategory = categories.get(getId() + categoryPages * 6);
                            page = 0;
                            RecipeViewingWidgetGui.this.initGui();
                            return true;
                        }
                        return false;
                    }
                });
                tab.setItem(categories.get(j).getCategoryIcon(), categories.get(j).getCategoryName(), tab.getId() + categoryPages * 6 == categories.indexOf(selectedCategory));
            }
        }
        
        SpeedCraftAreaSupplier supplier = RecipeHelper.getInstance().getSpeedCraftButtonArea(selectedCategory);
        final SpeedCraftFunctional functional = getSpeedCraftFunctionalByCategory(GuiHelper.getLastGuiContainer(), selectedCategory);
        if (page * getRecipesPerPage() < categoriesMap.get(selectedCategory).size()) {
            final Supplier<IRecipeDisplay> topDisplaySupplier = () -> categoriesMap.get(selectedCategory).get(page * getRecipesPerPage());
            final Rectangle topBounds = new Rectangle((int) getBounds().getCenterX() - 75, getBounds().y + 40, 150, selectedCategory.usesFullPage() ? 140 : 66);
            widgets.addAll(selectedCategory.setupDisplay(topDisplaySupplier, topBounds));
            if (supplier != null)
                widgets.add(new SpeedCraftingButtonWidget(supplier.get(topBounds), supplier.getButtonText(), functional, topDisplaySupplier));
            if (!selectedCategory.usesFullPage() && page * getRecipesPerPage() + 1 < categoriesMap.get(selectedCategory).size()) {
                final Supplier<IRecipeDisplay> middleDisplaySupplier = () -> {
                    return categoriesMap.get(selectedCategory).get(page * getRecipesPerPage() + 1);
                };
                final Rectangle middleBounds = new Rectangle((int) getBounds().getCenterX() - 75, getBounds().y + 113, 150, 66);
                widgets.addAll(selectedCategory.setupDisplay(middleDisplaySupplier, middleBounds));
                if (supplier != null)
                    widgets.add(new SpeedCraftingButtonWidget(supplier.get(middleBounds), supplier.getButtonText(), functional, middleDisplaySupplier));
            }
        }
        
        GuiHelper.getLastOverlay().onInitialized();
        eventListeners.addAll(tabs);
        eventListeners.add(GuiHelper.getLastOverlay());
        eventListeners.addAll(widgets);
    }
    
    private int getRecipesPerPage() {
        if (selectedCategory.usesFullPage())
            return 1;
        return 2;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
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
    }
    
    public int getPage() {
        return page;
    }
    
    public int getCategoryPage() {
        return categoryPages;
    }
    
    @Override
    public void drawDefaultBackground() {
        drawWorldBackground(0);
        if (selectedCategory != null)
            selectedCategory.drawCategoryBackground(bounds);
        else {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.disableStandardItemLighting();
            this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
            this.drawTexturedModalRect((int) bounds.getX(), (int) bounds.getY(), 0, 0, (int) bounds.getWidth(), (int) bounds.getHeight());
        }
    }
    
    public int getTotalPages(IRecipeCategory category) {
        return MathHelper.ceil(categoriesMap.get(category).size() / (double) getRecipesPerPage());
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for(IGuiEventListener listener : eventListeners)
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseScrolled(double amount) {
        for(IGuiEventListener listener : eventListeners)
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
        for(IGuiEventListener entry : getChildren())
            if (entry.mouseClicked(double_1, double_2, int_1)) {
                focusOn(entry);
                if (int_1 == 0)
                    setDragging(true);
                return true;
            }
        return false;
    }
    
}
