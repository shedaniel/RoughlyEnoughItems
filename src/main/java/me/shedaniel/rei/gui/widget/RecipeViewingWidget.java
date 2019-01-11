package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.audio.PositionedSoundInstance;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.util.Window;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RecipeViewingWidget extends Gui {
    
    private static final Identifier CREATIVE_INVENTORY_TABS = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public final int guiWidth = 176;
    public final int guiHeight = 186;
    
    private List<IWidget> widgets;
    private List<TabWidget> tabs;
    private Window window;
    private Rectangle bounds;
    private Map<IRecipeCategory, List<IRecipeDisplay>> categoriesMap;
    private List<IRecipeCategory> categories;
    private IRecipeCategory selectedCategory;
    private IMixinContainerGui parent;
    private ContainerGuiOverlay overlay;
    private int page, categoryPages;
    private ButtonWidget recipeBack, recipeNext, categoryBack, categoryNext;
    
    public RecipeViewingWidget(Window window, IMixinContainerGui parent, Map<IRecipeCategory, List<IRecipeDisplay>> categoriesMap) {
        this.categoryPages = 0;
        this.parent = parent;
        this.window = window;
        this.widgets = Lists.newArrayList();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.categoriesMap = categoriesMap;
        this.categories = new LinkedList<>(categoriesMap.keySet());
        Collections.reverse(categories);
        this.selectedCategory = categories.get(0);
        this.overlay = parent.getOverlay();
        this.tabs = new ArrayList<>();
    }
    
    public IMixinContainerGui getParent() {
        return parent;
    }
    
    public ContainerGuiOverlay getOverlay() {
        return overlay;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.doesEscapeKeyClose()) {
            MinecraftClient.getInstance().openGui(parent.getContainerGui());
            return true;
        }
        for(GuiEventListener listener : listeners)
            if (listener.keyPressed(int_1, int_2, int_3))
                return true;
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public void onClosed() {
        parent.setOverlay(overlay);
    }
    
    @Override
    protected void onInitialized() {
        super.onInitialized();
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
                RecipeViewingWidget.this.onInitialized();
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
                RecipeViewingWidget.this.onInitialized();
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
                RecipeViewingWidget.this.onInitialized();
            }
        });
        widgets.add(recipeNext = new ButtonWidget((int) bounds.getX() + 159, (int) bounds.getY() + 21, 12, 12, ">") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page++;
                if (page >= getTotalPages(selectedCategory))
                    page = 0;
                RecipeViewingWidget.this.onInitialized();
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
                            MinecraftClient.getInstance().getSoundLoader().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (getId() + categoryPages * 6 == categories.indexOf(selectedCategory))
                                return false;
                            selectedCategory = categories.get(getId() + categoryPages * 6);
                            page = 0;
                            RecipeViewingWidget.this.onInitialized();
                            return true;
                        }
                        return false;
                    }
                });
                tab.setItem(categories.get(j).getCategoryIcon(), categories.get(j).getCategoryName(), tab.getId() + categoryPages * 6 == categories.indexOf(selectedCategory));
            }
        }
        if (page * getRecipesPerPage() < categoriesMap.get(selectedCategory).size()) {
            IRecipeDisplay topDisplay = categoriesMap.get(selectedCategory).get(page * getRecipesPerPage());
            widgets.addAll(selectedCategory.setupDisplay(getParent(), topDisplay, new Rectangle((int) getBounds().getCenterX() - 75, getBounds().y + 40, 150, selectedCategory.usesFullPage() ? 118 : 66)));
            if (!selectedCategory.usesFullPage() && page * getRecipesPerPage() + 1 < categoriesMap.get(selectedCategory).size()) {
                IRecipeDisplay middleDisplay = categoriesMap.get(selectedCategory).get(page * getRecipesPerPage() + 1);
                widgets.addAll(selectedCategory.setupDisplay(getParent(), middleDisplay, new Rectangle((int) getBounds().getCenterX() - 75, getBounds().y + 108, 150, 66)));
            }
        }
        
        overlay.onInitialized();
        listeners.addAll(tabs);
        listeners.add(overlay);
        listeners.addAll(widgets);
    }
    
    private int getRecipesPerPage() {
        if (selectedCategory.usesFullPage())
            return 1;
        return 2;
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        tabs.stream().filter(tabWidget -> {
            return !tabWidget.isSelected();
        }).forEach(tabWidget -> tabWidget.draw(mouseX, mouseY, partialTicks));
        GuiLighting.disable();
        super.draw(mouseX, mouseY, partialTicks);
        widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.draw(mouseX, mouseY, partialTicks);
        });
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        tabs.stream().filter(TabWidget::isSelected).forEach(tabWidget -> tabWidget.draw(mouseX, mouseY, partialTicks));
        overlay.render(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void drawBackground() {
        drawBackground(0);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        this.client.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        this.drawTexturedRect((int) bounds.getX(), (int) bounds.getY(), 0, 0, (int) bounds.getWidth(), (int) bounds.getHeight());
    }
    
    public int getTotalPages(IRecipeCategory category) {
        return MathHelper.ceil(categoriesMap.get(category).size() / (double) getRecipesPerPage());
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for(GuiEventListener listener : listeners)
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseScrolled(double amount) {
        for(GuiEventListener listener : listeners)
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
        for(GuiEventListener entry : getEntries())
            if (entry.mouseClicked(double_1, double_2, int_1)) {
                focusOn(entry);
                if (int_1 == 0)
                    setActive(true);
                return true;
            }
        return false;
    }
    
}
