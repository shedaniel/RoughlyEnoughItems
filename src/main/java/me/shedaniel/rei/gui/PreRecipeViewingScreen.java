/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.widget.ButtonWidget;
import me.shedaniel.rei.gui.widget.HighlightableWidget;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PreRecipeViewingScreen extends Screen {
    
    private static final Identifier IDENTIFIER = new Identifier("roughlyenoughitems", "textures/gui/screenshot.png");
    private final List<Widget> widgets;
    private boolean original;
    private Map<RecipeCategory<?>, List<RecipeDisplay>> map;
    
    public PreRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map) {
        super(new TranslatableText("text.rei.recipe_screen_type.selection"));
        this.widgets = Lists.newArrayList();
        this.original = true;
        this.map = map;
    }
    
    @Override
    protected void init() {
        this.children.clear();
        this.widgets.clear();
        this.widgets.add(new ButtonWidget(width / 2 - 100, height - 40, 200, 20, I18n.translate("text.rei.select")) {
            @Override
            public void onPressed() {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().screenType = original ? RecipeScreenType.ORIGINAL : RecipeScreenType.VILLAGER;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ClientHelper.getInstance().openRecipeViewingScreen(map);
            }
        });
        this.widgets.add(new ScreenTypeSelection(width / 2 - 200 - 5, height / 2 - 112 / 2 - 10, 0));
        this.widgets.add(new ScreenTypeSelection(width / 2 + 5, height / 2 - 112 / 2 - 10, 112));
        this.children.addAll(widgets);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 20, 16777215);
        int i = 30;
        for(String s : this.font.wrapStringToWidthAsList(I18n.translate("text.rei.recipe_screen_type.selection.sub"), width - 30)) {
            this.drawCenteredString(this.font, Formatting.GRAY.toString() + s, width / 2, i, -1);
            i += 10;
        }
        super.render(int_1, int_2, float_1);
        this.widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.render(int_1, int_2, float_1);
        });
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if ((int_1 == 256 || this.minecraft.options.keyInventory.matchesKey(int_1, int_2)) && this.shouldCloseOnEsc()) {
            MinecraftClient.getInstance().openScreen(ScreenHelper.getLastContainerScreen());
            ScreenHelper.getLastOverlay().init();
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    public class ScreenTypeSelection extends HighlightableWidget {
        
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
            if (original == (v == 0)) {
                fillGradient(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + 2, 0xFFFFFFFF, 0xFFFFFFFF);
                fillGradient(bounds.x, bounds.y + bounds.height - 2, bounds.x + bounds.width, bounds.y + bounds.height, 0xFFFFFFFF, 0xFFFFFFFF);
                fillGradient(bounds.x, bounds.y, bounds.x + 2, bounds.y + bounds.height, 0xFFFFFFFF, 0xFFFFFFFF);
                fillGradient(bounds.x + bounds.width - 2, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, 0xFFFFFFFF, 0xFFFFFFFF);
            }
        }
        
        @Override
        public boolean mouseClicked(double double_1, double double_2, int int_1) {
            if (isHighlighted(double_1, double_2)) {
                minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                original = (v == 0);
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
