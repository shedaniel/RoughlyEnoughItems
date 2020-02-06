/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.widget.ButtonWidget;
import me.shedaniel.rei.gui.widget.LabelWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class PreRecipeViewingScreen extends Screen {
    
    private static final Identifier IDENTIFIER = new Identifier("roughlyenoughitems", "textures/gui/screenshot.png");
    private final List<Widget> widgets;
    protected long start;
    protected long duration;
    private boolean isSet;
    private boolean original;
    private double frame = 0;
    private double target = 0;
    private BooleanConsumer callback;
    private Screen parent;
    private boolean showTips;
    
    public PreRecipeViewingScreen(Screen parent, RecipeScreenType type, boolean showTips, BooleanConsumer callback) {
        super(new TranslatableText("text.rei.recipe_screen_type.selection"));
        this.widgets = Lists.newArrayList();
        if (type == RecipeScreenType.UNSET) {
            this.isSet = false;
            this.original = true;
        } else {
            this.isSet = true;
            this.original = type == RecipeScreenType.ORIGINAL;
            moveFrameTo(original ? 0 : 1, false, 0);
        }
        this.callback = callback;
        this.parent = parent;
        this.showTips = showTips;
    }
    
    public final double clamp(double v) {
        return clamp(v, 30);
    }
    
    public final double clamp(double v, double clampExtension) {
        return MathHelper.clamp(v, -clampExtension, 1 + clampExtension);
    }
    
    private void moveFrameTo(double value, boolean animated, long duration) {
        target = clamp(value);
        
        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else
            frame = target;
    }
    
    @Override
    protected void init() {
        this.children.clear();
        this.widgets.clear();
        this.widgets.add(new ButtonWidget(new Rectangle(width / 2 - 100, height - 40, 200, 20), NarratorManager.EMPTY) {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                enabled = isSet;
                setText(enabled ? I18n.translate("text.rei.select") : I18n.translate("config.roughlyenoughitems.recipeScreenType.unset"));
                super.render(mouseX, mouseY, delta);
            }
            
            @Override
            public void onPressed() {
                callback.accept(original);
            }
        });
        this.widgets.add(new ScreenTypeSelection(width / 2 - 200 - 5, height / 2 - 112 / 2 - 10, 0));
        this.widgets.add(LabelWidget.create(new Point(width / 2 - 200 - 5 + 104, height / 2 - 112 / 2 + 115), I18n.translate("config.roughlyenoughitems.recipeScreenType.original")).noShadow().color(-1124073473));
        this.widgets.add(new ScreenTypeSelection(width / 2 + 5, height / 2 - 112 / 2 - 10, 112));
        this.widgets.add(LabelWidget.create(new Point(width / 2 + 5 + 104, height / 2 - 112 / 2 + 115), I18n.translate("config.roughlyenoughitems.recipeScreenType.villager")).noShadow().color(-1124073473));
        this.children.addAll(widgets);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        if (this.minecraft.world != null) {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.fillGradient(0, 0, this.width, this.height, -16777216, -16777216);
        }
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 20, 16777215);
        if (showTips) {
            int i = 30;
            for (String s : this.font.wrapStringToWidthAsList(I18n.translate("text.rei.recipe_screen_type.selection.sub"), width - 30)) {
                this.drawCenteredString(this.font, Formatting.GRAY.toString() + s, width / 2, i, -1);
                i += 10;
            }
        }
        super.render(int_1, int_2, float_1);
        for (Widget widget : widgets) {
            widget.render(int_1, int_2, float_1);
        }
        if (isSet) {
            updateFramePosition(float_1);
            int x = (int) (width / 2 - 205 + (210 * frame));
            int y = height / 2 - 112 / 2 - 10;
            fillGradient(x - 2, y - 4, x - 6 + 208, y - 4 + 2, -1778384897, -1778384897);
            fillGradient(x - 2, y - 4 + 120 - 2, x - 6 + 208, y - 4 + 120, -1778384897, -1778384897);
            fillGradient(x - 4, y - 4, x - 4 + 2, y - 4 + 120, -1778384897, -1778384897);
            fillGradient(x - 4 + 208 - 2, y - 4, x - 4 + 208, y - 4 + 120, -1778384897, -1778384897);
        }
    }
    
    private void updateFramePosition(float delta) {
        target = clamp(target);
        if (!DynamicNewSmoothScrollingEntryListWidget.Precision.almostEquals(frame, target, DynamicNewSmoothScrollingEntryListWidget.Precision.FLOAT_EPSILON))
            frame = ease(frame, target, Math.min((System.currentTimeMillis() - start) / ((double) duration), 1));
        else
            frame = target;
    }
    
    private double ease(double start, double end, double amount) {
        return start + (end - start) * EasingMethod.EasingMethodImpl.LINEAR.apply(amount);
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 || this.minecraft.options.keyInventory.matchesKey(int_1, int_2)) {
            MinecraftClient.getInstance().openScreen(parent);
            if (parent instanceof ContainerScreen)
                ScreenHelper.getLastOverlay().init();
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    public class ScreenTypeSelection extends WidgetWithBounds {
        
        private Rectangle bounds;
        private int u, v;
        
        public ScreenTypeSelection(int x, int y, int v) {
            this.bounds = new Rectangle(x - 4, y - 4, 208, 120);
            this.u = 0;
            this.v = v;
        }
        
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        @Override
        public void render(int i, int i1, float delta) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(IDENTIFIER);
            blit(bounds.x + 4, bounds.y + 4, u, v, 200, 112);
        }
        
        @Override
        public boolean mouseClicked(double double_1, double double_2, int int_1) {
            if (containsMouse(double_1, double_2)) {
                original = (v == 0);
                if (!isSet) {
                    moveFrameTo(!original ? 0 : 1, false, 0);
                }
                isSet = true;
                moveFrameTo(original ? 0 : 1, true, 1000);
                return true;
            }
            return false;
        }
        
        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }
    }
}
