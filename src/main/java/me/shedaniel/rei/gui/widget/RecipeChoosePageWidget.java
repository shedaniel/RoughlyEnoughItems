package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.RelativePoint;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class RecipeChoosePageWidget extends DraggableWidget {
    
    private int currentPage;
    private int maxPage;
    private Rectangle bounds, grabBounds, dragBounds;
    private List<IWidget> widgets;
    private RecipeViewingScreen recipeViewingScreen;
    private TextFieldWidget textFieldWidget;
    private RecipeBaseWidget base1, base2;
    private ButtonWidget btnDone;
    
    public RecipeChoosePageWidget(RecipeViewingScreen recipeViewingScreen, int currentPage, int maxPage) {
        super(getPointFromConfig());
        this.recipeViewingScreen = recipeViewingScreen;
        this.currentPage = currentPage;
        this.maxPage = maxPage;
        initWidgets(getMidPoint());
    }
    
    private static Point getPointFromConfig() {
        MainWindow window = Minecraft.getInstance().mainWindow;
        RelativePoint point = RoughlyEnoughItemsCore.getConfigManager().getConfig().choosePageDialogPoint;
        return new Point((int) point.getX(window.getScaledWidth()), (int) point.getY(window.getScaledHeight()));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public Rectangle getGrabBounds() {
        return grabBounds;
    }
    
    @Override
    public Rectangle getDragBounds() {
        return dragBounds;
    }
    
    @Override
    public boolean isHighlighted(int mouseX, int mouseY) {
        return getBounds().contains(mouseX, mouseY) || new Rectangle(bounds.x + bounds.width - 50, bounds.y + bounds.height - 3, 50, 36).contains(mouseX, mouseY);
    }
    
    @Override
    public void updateWidgets(Point midPoint) {
        this.bounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 40);
        this.grabBounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 16);
        this.dragBounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 70);
        base1.getBounds().setLocation(bounds.x + bounds.width - 50, bounds.y + bounds.height - 6);
        base2.getBounds().setBounds(bounds);
        textFieldWidget.getBounds().setLocation(bounds.x + 7, bounds.y + 16);
        btnDone.getBounds().setLocation(bounds.x + bounds.width - 45, bounds.y + bounds.height + 3);
    }
    
    @Override
    protected void initWidgets(Point midPoint) {
        this.bounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 40);
        this.grabBounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 16);
        this.dragBounds = new Rectangle(midPoint.x - 50, midPoint.y - 20, 100, 70);
        this.widgets = Lists.newArrayList();
        this.widgets.add(base1 = new RecipeBaseWidget(new Rectangle(bounds.x + bounds.width - 50, bounds.y + bounds.height - 6, 50, 36)));
        this.widgets.add(base2 = new RecipeBaseWidget(bounds));
        this.widgets.add(new IWidget() {
            @Override
            public List<IWidget> getListeners() {
                return Lists.newArrayList();
            }
            
            @Override
            public void draw(int i, int i1, float v) {
                Minecraft.getInstance().fontRenderer.drawString(I18n.format("text.rei.choose_page"), bounds.x + 5, bounds.y + 5, 4210752);
                String endString = String.format(" /%d", maxPage);
                int width = Minecraft.getInstance().fontRenderer.getStringWidth(endString);
                Minecraft.getInstance().fontRenderer.drawString(endString, bounds.x + bounds.width - 5 - width, bounds.y + 22, 4210752);
            }
        });
        String endString = String.format(" /%d", maxPage);
        int width = Minecraft.getInstance().fontRenderer.getStringWidth(endString);
        this.widgets.add(textFieldWidget = new TextFieldWidget(bounds.x + 7, bounds.y + 16, bounds.width - width - 12, 18));
        textFieldWidget.stripInvaild = s -> {
            StringBuilder stringBuilder_1 = new StringBuilder();
            char[] var2 = s.toCharArray();
            int var3 = var2.length;
            
            for(int var4 = 0; var4 < var3; ++var4) {
                char char_1 = var2[var4];
                if (Character.isDigit(char_1))
                    stringBuilder_1.append(char_1);
            }
            
            return stringBuilder_1.toString();
        };
        textFieldWidget.setText(String.valueOf(currentPage + 1));
        widgets.add(btnDone = new ButtonWidget(bounds.x + bounds.width - 45, bounds.y + bounds.height + 3, 40, 20, I18n.format("gui.done")) {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                recipeViewingScreen.page = MathHelper.clamp(getIntFromString(textFieldWidget.getText()).orElse(0) - 1, 0, recipeViewingScreen.getTotalPages(recipeViewingScreen.getSelectedCategory()) - 1);
                recipeViewingScreen.choosePageActivated = false;
                recipeViewingScreen.initGui();
            }
        });
        textFieldWidget.setFocused(true);
    }
    
    @Override
    public Point processMidPoint(Point midPoint, Point mouse, Point startPoint, MainWindow window, int relateX, int relateY) {
        return new Point(MathHelper.clamp(mouse.x - relateX, getDragBounds().width / 2, window.getScaledWidth() - getDragBounds().width / 2), MathHelper.clamp(mouse.y - relateY, 20, window.getScaledHeight() - 50));
    }
    
    @Override
    public List<IWidget> getListeners() {
        return widgets;
    }
    
    @Override
    public void draw(int i, int i1, float v) {
        widgets.forEach(widget -> {
            RenderHelper.disableStandardItemLighting();
            GlStateManager.translatef(0, 0, 600);
            widget.draw(i, i1, v);
            GlStateManager.translatef(0, 0, -600);
        });
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for(IWidget widget : widgets)
            if (widget.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 335 || int_1 == 257) {
            recipeViewingScreen.page = MathHelper.clamp(getIntFromString(textFieldWidget.getText()).orElse(0) - 1, 0, recipeViewingScreen.getTotalPages(recipeViewingScreen.getSelectedCategory()) - 1);
            recipeViewingScreen.choosePageActivated = false;
            recipeViewingScreen.initGui();
            return true;
        }
        for(IWidget widget : widgets)
            if (widget.keyPressed(int_1, int_2, int_3))
                return true;
        return false;
    }
    
    public Optional<Integer> getIntFromString(String s) {
        try {
            return Optional.of(Integer.valueOf(s));
        } catch (Exception e) {
        }
        return Optional.empty();
    }
    
    @Override
    public void onMouseReleaseMidPoint(Point midPoint) {
        ConfigManager configManager = RoughlyEnoughItemsCore.getConfigManager();
        MainWindow window = Minecraft.getInstance().mainWindow;
        configManager.getConfig().choosePageDialogPoint = new RelativePoint(midPoint.getX() / window.getScaledWidth(), midPoint.getY() / window.getScaledHeight());
        try {
            configManager.saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
