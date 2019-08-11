/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.zeitheron.hammercore.client.utils.Scissors;
import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.gui.renderers.RecipeRenderer;
import me.shedaniel.rei.gui.widget.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VillagerRecipeViewingScreen extends Screen {
    
    private static final int TABS_PER_PAGE = 8;
    private final Map<RecipeCategory<?>, List<RecipeDisplay>> categoryMap;
    private final List<RecipeCategory<?>> categories;
    private final List<Widget> widgets;
    private final List<ButtonWidget> buttonWidgets;
    private final List<Renderer> recipeRenderers;
    private final List<TabWidget> tabs;
    public Rectangle bounds, scrollListBounds;
    private int selectedCategoryIndex, selectedRecipeIndex;
    private double scroll;
    private float scrollBarAlpha = 0;
    private float scrollBarAlphaFuture = 0;
    private long scrollBarAlphaFutureTime = -1;
    private boolean draggingScrollBar = false;
    private int tabsPage;
    
    public VillagerRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map) {
        super(new LiteralText(""));
        this.widgets = Lists.newArrayList();
        this.categoryMap = Maps.newLinkedHashMap();
        this.selectedCategoryIndex = 0;
        this.selectedRecipeIndex = 0;
        this.scrollBarAlpha = 0;
        this.scrollBarAlphaFuture = 0;
        this.scroll = 0;
        this.draggingScrollBar = false;
        this.tabsPage = 0;
        this.categories = Lists.newArrayList();
        this.buttonWidgets = Lists.newArrayList();
        this.tabs = Lists.newArrayList();
        this.recipeRenderers = Lists.newArrayList();
        RecipeHelper.getInstance().getAllCategories().forEach(category -> {
            if (map.containsKey(category)) {
                categories.add(category);
                categoryMap.put(category, map.get(category));
            }
        });
    }
    
    @Override
    protected void init() {
        super.init();
        this.draggingScrollBar = false;
        this.children.clear();
        this.widgets.clear();
        this.buttonWidgets.clear();
        this.recipeRenderers.clear();
        this.tabs.clear();
        int largestWidth = width - 100;
        int largestHeight = height - 40;
        RecipeCategory<RecipeDisplay> category = (RecipeCategory<RecipeDisplay>) categories.get(selectedCategoryIndex);
        RecipeDisplay display = categoryMap.get(category).get(selectedRecipeIndex);
        int guiWidth = MathHelper.clamp(category.getDisplayWidth(display) + 30, 0, largestWidth) + 100;
        int guiHeight = MathHelper.clamp(category.getDisplayHeight() + 40, 166, largestHeight);
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, height / 2 - guiHeight / 2, guiWidth, guiHeight);
        
        List<List<ItemStack>> workingStations = RoughlyEnoughItemsCore.getRecipeHelper().getWorkingStations(category.getIdentifier());
        if (!workingStations.isEmpty()) {
            int ww = MathHelper.floor((bounds.width - 16) / 18f);
            int w = Math.min(ww, workingStations.size());
            int h = MathHelper.ceil(workingStations.size() / ((float) ww));
            int xx = bounds.x + 16;
            int yy = bounds.y + bounds.height + 5;
            widgets.add(new CategoryBaseWidget(new Rectangle(xx - 6, bounds.y + bounds.height - 5, 11 + w * 18, 15 + h * 18)));
            int index = 0;
            List<String> list = Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("text.rei.working_station"));
            for (List<ItemStack> workingStation : workingStations) {
                widgets.add(new SlotWidget(xx, yy, Renderer.fromItemStacks(workingStation), true, true, true) {
                    @Override
                    protected List<String> getExtraItemToolTips(ItemStack stack) {
                        return list;
                    }
                });
                index++;
                xx += 18;
                if (index >= ww) {
                    index = 0;
                    xx = bounds.x + 16;
                    yy += 18;
                }
            }
        }
        
        this.widgets.add(new CategoryBaseWidget(bounds));
        this.scrollListBounds = new Rectangle(bounds.x + 4, bounds.y + 17, 97 + 5, guiHeight - 17 - 7);
        this.widgets.add(new SlotBaseWidget(scrollListBounds));
        
        Rectangle recipeBounds = new Rectangle(bounds.x + 100 + (guiWidth - 100) / 2 - category.getDisplayWidth(display) / 2, bounds.y + bounds.height / 2 - category.getDisplayHeight() / 2, category.getDisplayWidth(display), category.getDisplayHeight());
        this.widgets.addAll(category.setupDisplay(() -> display, recipeBounds));
        Optional<ButtonAreaSupplier> supplier = RecipeHelper.getInstance().getSpeedCraftButtonArea(category);
        if (supplier.isPresent() && supplier.get().get(recipeBounds) != null)
            this.widgets.add(new AutoCraftingButtonWidget(supplier.get().get(recipeBounds), supplier.get().getButtonText(), () -> display));
        
        int index = 0;
        for (RecipeDisplay recipeDisplay : categoryMap.get(category)) {
            int finalIndex = index;
            RecipeRenderer recipeRenderer;
            recipeRenderers.add(recipeRenderer = category.getSimpleRenderer(recipeDisplay));
            buttonWidgets.add(new ButtonWidget(bounds.x + 5, 0, recipeRenderer.getWidth(), recipeRenderer.getHeight(), "") {
                @Override
                public void onPressed() {
                    selectedRecipeIndex = finalIndex;
                    VillagerRecipeViewingScreen.this.init();
                }
                
                @Override
                protected int getTextureId(boolean boolean_1) {
                    enabled = selectedRecipeIndex != finalIndex;
                    return super.getTextureId(boolean_1);
                }
            });
            index++;
        }
        for (int i = 0; i < TABS_PER_PAGE; i++) {
            int j = i + tabsPage * TABS_PER_PAGE;
            if (categories.size() > j) {
                TabWidget tab;
                tabs.add(tab = new TabWidget(i, new Rectangle(bounds.x + bounds.width / 2 - Math.min(categories.size() - tabsPage * TABS_PER_PAGE, TABS_PER_PAGE) * 14 + i * 28, bounds.y - 28, 28, 28)) {
                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (getBounds().contains(mouseX, mouseY)) {
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (getId() + tabsPage * TABS_PER_PAGE == selectedCategoryIndex)
                                return false;
                            selectedCategoryIndex = getId() + tabsPage * TABS_PER_PAGE;
                            scroll = 0;
                            selectedRecipeIndex = 0;
                            VillagerRecipeViewingScreen.this.init();
                            return true;
                        }
                        return false;
                    }
                });
                tab.setRenderer(categories.get(j), categories.get(j).getIcon(), categories.get(j).getCategoryName(), tab.getId() + tabsPage * TABS_PER_PAGE == selectedCategoryIndex);
            }
        }
        ButtonWidget w, w2;
        this.widgets.add(w = new ButtonWidget(bounds.x + 2, bounds.y - 16, 10, 10, new TranslatableText("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                tabsPage--;
                if (tabsPage < 0)
                    tabsPage = MathHelper.ceil(categories.size() / (float) TABS_PER_PAGE) - 1;
                VillagerRecipeViewingScreen.this.init();
            }
        });
        this.widgets.add(w2 = new ButtonWidget(bounds.x + bounds.width - 12, bounds.y - 16, 10, 10, new TranslatableText("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                tabsPage++;
                if (tabsPage > MathHelper.ceil(categories.size() / (float) TABS_PER_PAGE) - 1)
                    tabsPage = 0;
                VillagerRecipeViewingScreen.this.init();
            }
        });
        w.enabled = w2.enabled = categories.size() > TABS_PER_PAGE;
        
        this.widgets.add(new ClickableLabelWidget(bounds.x + 4 + scrollListBounds.width / 2, bounds.y + 6, categories.get(selectedCategoryIndex).getCategoryName()) {
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ClientHelper.getInstance().executeViewAllRecipesKeyBind();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.view_all_categories"));
            }
            
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                font.draw((isHovered(mouseX, mouseY) ? Formatting.UNDERLINE.toString() : "") + text, x - font.getStringWidth(text) / 2, y, getDefaultColor());
                if (clickable && getTooltips().isPresent())
                    if (!focused && containsMouse(mouseX, mouseY))
                        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltips().get().split("\n")));
                    else if (focused)
                        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(new Point(x, y), getTooltips().get().split("\n")));
            }
            
            @Override
            public int getDefaultColor() {
                return ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : 4210752;
            }
        });
        
        this.children.addAll(buttonWidgets);
        this.widgets.addAll(tabs);
        this.children.addAll(widgets);
        this.children.add(ScreenHelper.getLastOverlay(true, false));
        ScreenHelper.getLastOverlay().init();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int int_1) {
        double height = buttonWidgets.stream().map(ButtonWidget::getBounds).collect(Collectors.summingDouble(Rectangle::getHeight));
        int actualHeight = scrollListBounds.height - 2;
        if (height > actualHeight && scrollBarAlpha > 0 && mouseY >= scrollListBounds.y + 1 && mouseY <= scrollListBounds.getMaxY() - 1) {
            double scrollbarPositionMinX = scrollListBounds.getMaxX() - 6;
            if (mouseX >= scrollbarPositionMinX & mouseX <= scrollbarPositionMinX + 8) {
                this.draggingScrollBar = true;
                scrollBarAlpha = 1;
                return false;
            }
        }
        this.draggingScrollBar = false;
        return super.mouseClicked(mouseX, mouseY, int_1);
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for (Element listener : children())
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        double height = buttonWidgets.stream().map(ButtonWidget::getBounds).collect(Collectors.summingDouble(Rectangle::getHeight));
        if (scrollListBounds.contains(double_1, double_2) && height > scrollListBounds.height - 2) {
            if (double_3 > 0)
                scroll -= 16;
            else
                scroll += 16;
            scroll = MathHelper.clamp(scroll, 0, height - scrollListBounds.height + 2);
            if (scrollBarAlphaFuture == 0)
                scrollBarAlphaFuture = 1f;
            scrollBarAlphaFutureTime = System.currentTimeMillis();
            return true;
        }
        for (Element listener : children())
            if (listener.mouseScrolled(double_1, double_2, double_3))
                return true;
        if (bounds.contains(ClientUtils.getMouseLocation())) {
            if (double_3 < 0 && categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex++;
                if (selectedRecipeIndex >= categoryMap.get(categories.get(selectedCategoryIndex)).size())
                    selectedRecipeIndex = 0;
                init();
            } else if (categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex--;
                if (selectedRecipeIndex < 0)
                    selectedRecipeIndex = categoryMap.get(categories.get(selectedCategoryIndex)).size() - 1;
                init();
                return true;
            }
        }
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().doesVillagerScreenHavePermanentScrollBar()) {
            scrollBarAlphaFutureTime = System.currentTimeMillis();
            scrollBarAlphaFuture = 0;
            scrollBarAlpha = 1;
        } else if (scrollBarAlphaFutureTime > 0) {
            long l = System.currentTimeMillis() - scrollBarAlphaFutureTime;
            if (l > 300f) {
                if (scrollBarAlphaFutureTime == 0) {
                    scrollBarAlpha = scrollBarAlphaFuture;
                    scrollBarAlphaFutureTime = -1;
                } else if (l > 2000f && scrollBarAlphaFuture == 1) {
                    scrollBarAlphaFuture = 0;
                    scrollBarAlphaFutureTime = System.currentTimeMillis();
                } else
                    scrollBarAlpha = scrollBarAlphaFuture;
            } else {
                if (scrollBarAlphaFuture == 0)
                    scrollBarAlpha = Math.min(scrollBarAlpha, 1 - Math.min(1f, l / 300f));
                else if (scrollBarAlphaFuture == 1)
                    scrollBarAlpha = Math.max(Math.min(1f, l / 300f), scrollBarAlpha);
            }
        }
        this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        int yOffset = 0;
        this.widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.render(mouseX, mouseY, delta);
        });
        GuiLighting.disable();
        ScreenHelper.getLastOverlay().render(mouseX, mouseY, delta);
        GlStateManager.pushMatrix();
        Scissors.begin();
        Scissors.scissor(0, scrollListBounds.y + 1, width, scrollListBounds.height - 2);
        for (int i = 0; i < buttonWidgets.size(); i++) {
            ButtonWidget buttonWidget = buttonWidgets.get(i);
            buttonWidget.getBounds().y = scrollListBounds.y + 1 + yOffset - (int) scroll;
            if (buttonWidget.getBounds().getMaxY() > scrollListBounds.getMinY() && buttonWidget.getBounds().getMinY() < scrollListBounds.getMaxY()) {
                GuiLighting.disable();
                buttonWidget.render(mouseX, mouseY, delta);
            }
            yOffset += buttonWidget.getBounds().height;
        }
        for (int i = 0; i < buttonWidgets.size(); i++) {
            if (buttonWidgets.get(i).getBounds().getMaxY() > scrollListBounds.getMinY() && buttonWidgets.get(i).getBounds().getMinY() < scrollListBounds.getMaxY()) {
                GuiLighting.disable();
                recipeRenderers.get(i).setBlitOffset(1);
                recipeRenderers.get(i).render(buttonWidgets.get(i).getBounds().x, buttonWidgets.get(i).getBounds().y, mouseX, mouseY, delta);
            }
        }
        double height = buttonWidgets.stream().map(ButtonWidget::getBounds).collect(Collectors.summingDouble(Rectangle::getHeight));
        if (height > scrollListBounds.height - 2) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBufferBuilder();
            double maxScroll = height - scrollListBounds.height + 2;
            int scrollBarHeight = MathHelper.floor((scrollListBounds.height - 2) * (scrollListBounds.height - 2) / maxScroll);
            scrollBarHeight = MathHelper.clamp(scrollBarHeight, 32, scrollListBounds.height - 2 - 8);
            int minY = (int) (scroll * (scrollListBounds.height - 2 - scrollBarHeight) / maxScroll) + scrollListBounds.y + 1;
            if (minY < this.scrollListBounds.y + 1)
                minY = this.scrollListBounds.y + 1;
            double scrollbarPositionMinX = scrollListBounds.getMaxX() - 6, scrollbarPositionMaxX = scrollListBounds.getMaxX() - 2;
            GuiLighting.disable();
            GlStateManager.disableTexture();
            GlStateManager.enableBlend();
            GlStateManager.disableAlphaTest();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.shadeModel(7425);
            buffer.begin(7, VertexFormats.POSITION_COLOR);
            float b = ScreenHelper.isDarkModeEnabled() ? 0.37f : 1f;
            buffer.vertex(scrollbarPositionMinX, minY + scrollBarHeight, 1000D).color(b, b, b, scrollBarAlpha).next();
            buffer.vertex(scrollbarPositionMaxX, minY + scrollBarHeight, 1000D).color(b, b, b, scrollBarAlpha).next();
            buffer.vertex(scrollbarPositionMaxX, minY, 1000D).color(b, b, b, scrollBarAlpha).next();
            buffer.vertex(scrollbarPositionMinX, minY, 1000D).color(b, b, b, scrollBarAlpha).next();
            tessellator.draw();
            GlStateManager.shadeModel(7424);
            GlStateManager.disableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.enableTexture();
        }
        Scissors.end();
        GlStateManager.popMatrix();
        ScreenHelper.getLastOverlay().lateRender(mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int int_1, double double_3, double double_4) {
        if (int_1 == 0 && scrollBarAlpha > 0 && draggingScrollBar) {
            double height = buttonWidgets.stream().map(ButtonWidget::getBounds).collect(Collectors.summingDouble(Rectangle::getHeight));
            int actualHeight = scrollListBounds.height - 2;
            if (height > actualHeight && mouseY >= scrollListBounds.y + 1 && mouseY <= scrollListBounds.getMaxY() - 1) {
                int int_3 = MathHelper.clamp((int) ((actualHeight * actualHeight) / height), 32, actualHeight - 8);
                double double_6 = Math.max(1.0D, Math.max(1d, height) / (double) (actualHeight - int_3));
                scrollBarAlphaFutureTime = System.currentTimeMillis();
                scrollBarAlphaFuture = 1f;
                scroll = MathHelper.clamp(scroll + double_4 * double_6, 0, height - scrollListBounds.height + 2);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, int_1, double_3, double_4);
    }
    
    private int getReal(int i) {
        return (int) (i / ((double) minecraft.window.getScaledWidth() / (double) minecraft.window.getWidth()));
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if ((int_1 == 256 || this.minecraft.options.keyInventory.matchesKey(int_1, int_2)) && this.shouldCloseOnEsc()) {
            MinecraftClient.getInstance().openScreen(ScreenHelper.getLastContainerScreen());
            ScreenHelper.getLastOverlay().init();
            return true;
        }
        if (int_1 == 258) {
            boolean boolean_1 = !hasShiftDown();
            if (!this.changeFocus(boolean_1))
                this.changeFocus(boolean_1);
            return true;
        }
        if (ClientHelper.getInstance().getNextPageKeyBinding().matchesKey(int_1, int_2)) {
            if (categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex++;
                if (selectedRecipeIndex >= categoryMap.get(categories.get(selectedCategoryIndex)).size())
                    selectedRecipeIndex = 0;
                init();
                return true;
            }
            return false;
        } else if (ClientHelper.getInstance().getPreviousPageKeyBinding().matchesKey(int_1, int_2)) {
            if (categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex--;
                if (selectedRecipeIndex < 0)
                    selectedRecipeIndex = categoryMap.get(categories.get(selectedCategoryIndex)).size() - 1;
                init();
                return true;
            }
            return false;
        }
        for (Element element : children())
            if (element.keyPressed(int_1, int_2, int_3))
                return true;
        return super.keyPressed(int_1, int_2, int_3);
    }
    
}
