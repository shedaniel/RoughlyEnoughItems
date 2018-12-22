package me.shedaniel.gui;

import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.api.IRecipe;
import me.shedaniel.gui.widget.AEISlot;
import me.shedaniel.gui.widget.Button;
import me.shedaniel.gui.widget.Control;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecipeGui extends GuiContainer {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("almostenoughitems", "textures/gui/recipecontainer.png");
    private final MainWindow mainWindow;
    private final Container container;
    private final GuiScreen prevScreen;
    private final Map<IDisplayCategory, List<IRecipe>> recipes;
    private int guiWidth = 176;
    private int guiHeight = 222;
    ArrayList<IDisplayCategory> categories = new ArrayList<>();
    private int categoryPointer = 0;
    private int recipePointer = 0;
    private List<AEISlot> slots;
    private int cycleCounter = 0;
    private int[] itemPointer;
    List<Control> controls = new LinkedList<>();
    
    public RecipeGui(Container p_i1072_1_, GuiScreen prevScreen, Map<IDisplayCategory, List<IRecipe>> recipes) {
        super(new RecipeContainer());
        this.container = p_i1072_1_;
        this.prevScreen = prevScreen;
        this.recipes = recipes;
        this.mc = Minecraft.getInstance();
        this.itemRender = mc.getItemRenderer();
        this.fontRenderer = mc.fontRenderer;
        this.mainWindow = Minecraft.getInstance().mainWindow;
        
        setupCategories();
    }
    
    private void setupCategories() {
        categories.addAll(recipes.keySet());
        updateRecipe();
    }
    
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        int y = (int) ((mainWindow.getScaledHeight() / 2 - this.guiHeight / 2));
        drawCenteredString(this.fontRenderer, categories.get(categoryPointer).getDisplayName(), guiLeft + guiWidth / 2, y + 11, -1);
        drawCenteredString(this.fontRenderer, String.format("%d/%d", 1 + getCurrentPage(), getTotalPages()), guiLeft + guiWidth / 2, y + 34, -1);
        controls.forEach(Control::draw);
    }
    
    private int getCurrentPage() {
        return recipePointer / 2;
    }
    
    @Override
    public void tick() {
        super.tick();
        slots.forEach(AEISlot::tick);
        controls.forEach(Control::tick);
    }
    
    
    @Override
    public void onResize(Minecraft p_onResize_1_, int p_onResize_2_, int p_onResize_3_) {
        super.onResize(p_onResize_1_, p_onResize_2_, p_onResize_3_);
        updateRecipe();
    }
    
    private void updateRecipe() {
        IRecipe recipe = recipes.get(categories.get(categoryPointer)).get(recipePointer);
        categories.get(categoryPointer).resetRecipes();
        categories.get(categoryPointer).addRecipe(recipe);
        slots = categories.get(categoryPointer).setupDisplay(0);
        if (recipes.get(categories.get(categoryPointer)).size() >= categoryPointer + 2) {
            IRecipe recipe2 = recipes.get(categories.get(categoryPointer)).get(recipePointer + 1);
            categories.get(categoryPointer).addRecipe(recipe2);
            slots.addAll(categories.get(categoryPointer).setupDisplay(1));
        }
        
        guiLeft = (int) ((mainWindow.getScaledWidth() / 2 - this.guiWidth / 2));
        guiTop = (int) ((mainWindow.getScaledHeight() / 2 - this.guiHeight / 2));
        
        for(AEISlot slot : slots) {
            slot.move(guiLeft, guiTop);
        }
        
        Button btnCategoryLeft = new Button(guiLeft + 10, guiTop + 5, 15, 20, "<");
        Button btnCategoryRight = new Button(guiLeft + guiWidth - 25, guiTop + 5, 15, 20, ">");
        btnCategoryRight.onClick = this::btnCategoryRight;
        btnCategoryLeft.onClick = this::btnCategoryLeft;
        
        Button btnRecipeLeft = new Button(guiLeft + 10, guiTop + 28, 15, 20, "<");
        Button btnRecipeRight = new Button(guiLeft + guiWidth - 25, guiTop + 28, 15, 20, ">");
        btnRecipeLeft.setEnabled(recipes.get(categories.get(categoryPointer)).size() > 1 && recipePointer > 0);
        btnRecipeRight.setEnabled(recipes.get(categories.get(categoryPointer)).size() > 1 && getCurrentPage() + 1 < getTotalPages());
        btnRecipeRight.onClick = this::btnRecipeRight;
        btnRecipeLeft.onClick = this::btnRecipeLeft;
        
        controls.clear();
        controls.add(btnCategoryLeft);
        controls.add(btnCategoryRight);
        if (categories.size() <= 2) {
            btnCategoryLeft.setEnabled(false);
            btnCategoryRight.setEnabled(false);
        }
        
        controls.add(btnRecipeLeft);
        controls.add(btnRecipeRight);
        
        itemPointer = new int[9];
        for(int i = 0; i < itemPointer.length; i++) {
            itemPointer[i] = 0;
        }
        
        List<Control> newControls = new LinkedList<>();
        categories.get(categoryPointer).addWidget(newControls, 0);
        if (recipes.get(categories.get(categoryPointer)).size() >= categoryPointer + 2) {
            categories.get(categoryPointer).addWidget(newControls, 1);
        }
        newControls.forEach(f -> f.move(guiLeft, guiTop));
        controls.addAll(newControls);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
        drawDefaultBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        
        int lvt_4_1_ = (int) ((mainWindow.getScaledWidth() / 2 - this.guiWidth / 2));
        int lvt_5_1_ = (int) ((mainWindow.getScaledHeight() / 2 - this.guiHeight / 2));
        
        this.drawTexturedModalRect(lvt_4_1_, lvt_5_1_, 0, 0, this.guiWidth, this.guiHeight);
        slots.forEach(AEISlot::draw);
    }
    
    
    @Override
    protected void initGui() {
        super.initGui();
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (p_keyPressed_1_ == 259 && prevScreen != null && AEIRenderHelper.focusedControl == null) {
            Minecraft.getInstance().displayGuiScreen(prevScreen);
            return true;
        }
        
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }
    
    private boolean btnCategoryLeft(int button) {
        recipePointer = 0;
        categoryPointer--;
        if (categoryPointer < 0) {
            categoryPointer = categories.size() - 1;
        }
        updateRecipe();
        return true;
    }
    
    private boolean btnCategoryRight(int button) {
        recipePointer = 0;
        categoryPointer++;
        if (categoryPointer >= categories.size()) {
            categoryPointer = 0;
        }
        updateRecipe();
        return true;
    }
    
    private boolean btnRecipeLeft(int button) {
        recipePointer -= 2;
        if (recipePointer < 0) {
            recipePointer = (getTotalPages() - 1) * 2;
        }
        updateRecipe();
        return true;
    }
    
    private boolean btnRecipeRight(int button) {
        recipePointer += 2;
        if (recipePointer >= recipes.get(categories.get(categoryPointer)).size()) {
            recipePointer = 0;
        }
        updateRecipe();
        return true;
    }
    
    private int riseDoublesToInt(double i) {
        return (int) (i + (i % 1 == 0 ? 0 : 1));
    }
    
    private int getTotalPages() {
        return MathHelper.clamp(riseDoublesToInt(recipes.get(categories.get(categoryPointer)).size() / 2), 1, Integer.MAX_VALUE);
    }
}
