package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.IRecipeCategory;
import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecipeViewingWidget extends Gui {
    
    private static final Identifier CREATIVE_INVENTORY_TABS = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public final int guiWidth = 176;
    public final int guiHeight = 158;
    
    private List<IWidget> widgets;
    private Window window;
    private Rectangle bounds;
    private Map<IRecipeCategory, List<IRecipeDisplay>> categoriesMap;
    private List<IRecipeCategory> categories;
    private IRecipeCategory selectedCategory;
    private IMixinContainerGui parent;
    private ContainerGuiOverlay overlay;
    private int page;
    
    public RecipeViewingWidget(ContainerGuiOverlay overlay, Window window, IMixinContainerGui parent, Map<IRecipeCategory, List<IRecipeDisplay>> categoriesMap) {
        this.parent = parent;
        this.window = window;
        this.widgets = Lists.newArrayList();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.categoriesMap = categoriesMap;
        this.categories = new LinkedList<>(categoriesMap.keySet());
        this.selectedCategory = categories.get(0);
        this.overlay = overlay;
    }
    
    public ContainerGui getParent() {
        return parent.getContainerGui();
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
    protected void onInitialized() {
        super.onInitialized();
        this.widgets.clear();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, guiWidth, guiHeight);
        
        widgets.add(new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 5, 12, 12, "<") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
            }
        });
        widgets.add(new ButtonWidget((int) bounds.getX() + 159, (int) bounds.getY() + 5, 12, 12, ">") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
            }
        });
        
        widgets.add(new ButtonWidget((int) bounds.getX() + 5, (int) bounds.getY() + 21, 12, 12, "<") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
            }
        });
        widgets.add(new ButtonWidget((int) bounds.getX() + 159, (int) bounds.getY() + 21, 12, 12, ">") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
            }
        });
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
        overlay.onInitialized();
        listeners.add(overlay);
        listeners.addAll(widgets);
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        this.client.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        this.drawTexturedRect((int) bounds.getX(), (int) bounds.getY(), 0, 0, (int) bounds.getWidth(), (int) bounds.getHeight());
        
        GuiLighting.disable();
        super.draw(mouseX, mouseY, partialTicks);
        widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.draw(mouseX, mouseY, partialTicks);
        });
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        overlay.render(mouseX, mouseY, partialTicks);
    }
    
    public int getTotalPages(IRecipeCategory category) {
        if (category.usesFullPage())
            return categoriesMap.get(category).size();
        return MathHelper.ceil(categoriesMap.get(category).size() / 2.0);
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
