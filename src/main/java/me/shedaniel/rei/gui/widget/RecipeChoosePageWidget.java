package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.RelativePoint;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RecipeChoosePageWidget extends DraggableWidget {
    
    private int currentPage;
    private int maxPage;
    private Rectangle bounds, grabBounds, dragBounds;
    private List<Widget> widgets;
    private RecipeViewingScreen recipeViewingScreen;
    private TextFieldWidget textFieldWidget;
    private CategoryBaseWidget base1, base2;
    private ButtonWidget btnDone;
    
    public RecipeChoosePageWidget(RecipeViewingScreen recipeViewingScreen, int currentPage, int maxPage) {
        super(getPointFromConfig());
        this.recipeViewingScreen = recipeViewingScreen;
        this.currentPage = currentPage;
        this.maxPage = maxPage;
        initWidgets(getMidPoint());
    }
    
    private static Point getPointFromConfig() {
        Window window = MinecraftClient.getInstance().window;
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
    public boolean isHighlighted(double mouseX, double mouseY) {
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
        this.widgets.add(base1 = new CategoryBaseWidget(new Rectangle(bounds.x + bounds.width - 50, bounds.y + bounds.height - 6, 50, 36)));
        this.widgets.add(base2 = new CategoryBaseWidget(bounds));
        this.widgets.add(new Widget() {
            @Override
            public List<Widget> children() {
                return Collections.emptyList();
            }
            
            @Override
            public void render(int i, int i1, float v) {
                font.draw(I18n.translate("text.rei.choose_page"), bounds.x + 5, bounds.y + 5, 4210752);
                String endString = String.format(" /%d", maxPage);
                int width = font.getStringWidth(endString);
                font.draw(endString, bounds.x + bounds.width - 5 - width, bounds.y + 22, 4210752);
            }
        });
        String endString = String.format(" /%d", maxPage);
        int width = font.getStringWidth(endString);
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
        widgets.add(btnDone = new ButtonWidget(bounds.x + bounds.width - 45, bounds.y + bounds.height + 3, 40, 20, I18n.translate("gui.done")) {
            @Override
            public void onPressed() {
                recipeViewingScreen.page = MathHelper.clamp(getIntFromString(textFieldWidget.getText()).orElse(0) - 1, 0, recipeViewingScreen.getTotalPages(recipeViewingScreen.getSelectedCategory()) - 1);
                recipeViewingScreen.choosePageActivated = false;
                recipeViewingScreen.init();
            }
        });
        textFieldWidget.setFocused(true);
    }
    
    @Override
    public Point processMidPoint(Point midPoint, Point mouse, Point startPoint, Window window, int relateX, int relateY) {
        return new Point(MathHelper.clamp(mouse.x - relateX, getDragBounds().width / 2, window.getScaledWidth() - getDragBounds().width / 2), MathHelper.clamp(mouse.y - relateY, 20, window.getScaledHeight() - 50));
    }
    
    @Override
    public List<Widget> children() {
        return widgets;
    }
    
    @Override
    public void render(int i, int i1, float v) {
        widgets.forEach(widget -> {
            GuiLighting.disable();
            GlStateManager.translatef(0, 0, 800);
            widget.render(i, i1, v);
            GlStateManager.translatef(0, 0, -800);
        });
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for(Widget widget : widgets)
            if (widget.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 335 || int_1 == 257) {
            recipeViewingScreen.page = MathHelper.clamp(getIntFromString(textFieldWidget.getText()).orElse(0) - 1, 0, recipeViewingScreen.getTotalPages(recipeViewingScreen.getSelectedCategory()) - 1);
            recipeViewingScreen.choosePageActivated = false;
            recipeViewingScreen.init();
            return true;
        }
        for(Widget widget : widgets)
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
        Window window = minecraft.window;
        configManager.getConfig().choosePageDialogPoint = new RelativePoint(midPoint.getX() / window.getScaledWidth(), midPoint.getY() / window.getScaledHeight());
        try {
            configManager.saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
