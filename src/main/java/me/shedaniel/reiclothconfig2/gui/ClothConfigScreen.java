package me.shedaniel.reiclothconfig2.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.reiclothconfig2.api.AbstractConfigEntry;
import me.shedaniel.reiclothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.reiclothconfig2.api.MouseUtils;
import me.shedaniel.reiclothconfig2.api.QueuedTooltip;
import me.shedaniel.reiclothconfig2.gui.widget.DynamicElementListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ClothConfigScreen extends GuiScreen {
    
    private static final ResourceLocation CONFIG_TEX = new ResourceLocation("roughlyenoughitems", "textures/gui/cloth_config.png");
    private final List<QueuedTooltip> queuedTooltips = Lists.newArrayList();
    public int nextTabIndex;
    public int selectedTabIndex;
    public double tabsScrollVelocity = 0d;
    public double tabsScrollProgress = 0d;
    public ListWidget listWidget;
    private GuiScreen parent;
    private LinkedHashMap<String, List<AbstractConfigEntry>> tabbedEntries;
    private List<Pair<String, Integer>> tabs;
    private boolean edited;
    private boolean requiresRestart;
    private boolean confirmSave;
    private GuiButton buttonQuit;
    private GuiButton buttonSave;
    private GuiButton buttonLeftTab;
    private GuiButton buttonRightTab;
    private Rectangle tabsBounds, tabsLeftBounds, tabsRightBounds;
    private String title;
    private double tabsMaximumScrolled = -1d;
    private boolean displayErrors;
    private List<ClothConfigTabButton> tabButtons;
    private boolean smoothScrollingTabs = true;
    private boolean smoothScrollingList = true;
    private ResourceLocation defaultBackgroundLocation;
    private Map<String, ResourceLocation> categoryBackgroundLocation;
    
    @SuppressWarnings("deprecation")
    public ClothConfigScreen(GuiScreen parent, String title, Map<String, List<Pair<String, Object>>> o, boolean confirmSave, boolean displayErrors, boolean smoothScrollingList, ResourceLocation defaultBackgroundLocation, Map<String, ResourceLocation> categoryBackgroundLocation) {
        super();
        this.parent = parent;
        this.title = title;
        this.tabbedEntries = Maps.newLinkedHashMap();
        this.smoothScrollingList = smoothScrollingList;
        this.defaultBackgroundLocation = defaultBackgroundLocation;
        o.forEach((tab, pairs) -> {
            List<AbstractConfigEntry> list = Lists.newArrayList();
            for(Pair<String, Object> pair : pairs) {
                if (pair.getSecond() instanceof AbstractConfigListEntry) {
                    list.add((AbstractConfigListEntry) pair.getSecond());
                } else {
                    throw new IllegalArgumentException("Unsupported Type (" + pair.getFirst() + "): " + pair.getSecond().getClass().getSimpleName());
                }
            }
            list.forEach(entry -> entry.setScreen(this));
            tabbedEntries.put(tab, list);
        });
        this.nextTabIndex = 0;
        this.selectedTabIndex = 0;
        this.confirmSave = confirmSave;
        this.edited = false;
        this.requiresRestart = false;
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        this.tabs = tabbedEntries.keySet().stream().map(s -> new Pair<>(s, fontRenderer.getStringWidth(I18n.format(s)) + 8)).collect(Collectors.toList());
        this.tabsScrollProgress = 0d;
        this.tabButtons = Lists.newArrayList();
        this.displayErrors = displayErrors;
        this.categoryBackgroundLocation = categoryBackgroundLocation;
    }
    
    @Override
    public void tick() {
        super.tick();
        for(IGuiEventListener child : getChildren())
            if (child instanceof ITickable)
                ((ITickable) child).tick();
    }
    
    public ResourceLocation getBackgroundLocation() {
        if (categoryBackgroundLocation.containsKey(Lists.newArrayList(tabbedEntries.keySet()).get(selectedTabIndex)))
            return categoryBackgroundLocation.get(Lists.newArrayList(tabbedEntries.keySet()).get(selectedTabIndex));
        return defaultBackgroundLocation;
    }
    
    public boolean isSmoothScrollingList() {
        return smoothScrollingList;
    }
    
    public void setSmoothScrollingList(boolean smoothScrollingList) {
        this.smoothScrollingList = smoothScrollingList;
    }
    
    public boolean isSmoothScrollingTabs() {
        return smoothScrollingTabs;
    }
    
    public void setSmoothScrollingTabs(boolean smoothScrolling) {
        this.smoothScrollingTabs = smoothScrolling;
    }
    
    public boolean isEdited() {
        return edited;
    }
    
    @Deprecated
    public void setEdited(boolean edited) {
        this.edited = edited;
        buttonQuit.displayString = edited ? I18n.format("text.cloth-config.cancel_discard") : I18n.format("gui.cancel");
        buttonSave.enabled = edited;
    }
    
    @SuppressWarnings("deprecation")
    public void setEdited(boolean edited, boolean requiresRestart) {
        setEdited(edited);
        if (!this.requiresRestart && requiresRestart)
            this.requiresRestart = requiresRestart;
    }
    
    @Override
    protected void initGui() {
        super.initGui();
        this.children.clear();
        this.tabButtons.clear();
        if (listWidget != null)
            tabbedEntries.put(tabs.get(selectedTabIndex).getFirst(), listWidget.getChildren());
        selectedTabIndex = nextTabIndex;
        children.add(listWidget = new ListWidget(mc, width, height, 70, height - 32, getBackgroundLocation()));
        listWidget.setSmoothScrolling(this.smoothScrollingList);
        if (tabbedEntries.size() > selectedTabIndex)
            Lists.newArrayList(tabbedEntries.values()).get(selectedTabIndex).forEach(entry -> listWidget.getChildren().add(entry));
        addButton(buttonQuit = new GuiButton(312312, width / 2 - 154, height - 26, 150, 20, edited ? I18n.format("text.cloth-config.cancel_discard") : I18n.format("gui.cancel")) {
            @Override
            public void onClick(double diawd, double djwaidw) {
                super.onClick(diawd, djwaidw);
                if (confirmSave && edited)
                    mc.displayGuiScreen(new GuiYesNo(new QuitSaveConsumer(), I18n.format("text.cloth-config.quit_config"), I18n.format("text.cloth-config.quit_config_sure"), I18n.format("text.cloth-config.quit_discard"), I18n.format("gui.cancel"), 939183));
                else
                    mc.displayGuiScreen(parent);
            }
        });
        addButton(buttonSave = new GuiButton(312321, width / 2 + 4, height - 26, 150, 20, "") {
            @Override
            public void onClick(double diawd, double djwaidw) {
                super.onClick(diawd, djwaidw);
                Map<String, List<Pair<String, Object>>> map = Maps.newLinkedHashMap();
                tabbedEntries.forEach((s, abstractListEntries) -> {
                    List list = abstractListEntries.stream().map(entry -> new Pair(entry.getFieldName(), entry.getValue())).collect(Collectors.toList());
                    map.put(s, list);
                });
                for(List<AbstractConfigEntry> entries : Lists.newArrayList(tabbedEntries.values()))
                    for(AbstractConfigEntry entry : entries)
                        entry.save();
                onSave(map);
                if (requiresRestart)
                    ClothConfigScreen.this.mc.displayGuiScreen(new ClothRequiresRestartScreen(parent));
                else
                    ClothConfigScreen.this.mc.displayGuiScreen(parent);
            }
            
            @Override
            public void render(int int_1, int int_2, float float_1) {
                boolean hasErrors = false;
                if (displayErrors)
                    for(List<AbstractConfigEntry> entries : Lists.newArrayList(tabbedEntries.values())) {
                        for(AbstractConfigEntry entry : entries)
                            if (entry.getError().isPresent()) {
                                hasErrors = true;
                                break;
                            }
                        if (hasErrors)
                            break;
                    }
                enabled = edited && !hasErrors;
                displayString = (displayErrors && hasErrors ? I18n.format("text.cloth-config.error_cannot_save") : I18n.format("text.cloth-config.save_and_done"));
                super.render(int_1, int_2, float_1);
            }
        });
        buttonSave.enabled = edited;
        tabsBounds = new Rectangle(0, 41, width, 24);
        tabsLeftBounds = new Rectangle(0, 41, 18, 24);
        tabsRightBounds = new Rectangle(width - 18, 41, 18, 24);
        children.add(buttonLeftTab = new GuiButton(23123214, 4, 44, 12, 18, "") {
            @Override
            public void onClick(double diawd, double djwaidw) {
                super.onClick(diawd, djwaidw);
                tabsScrollProgress = Integer.MIN_VALUE;
                tabsScrollVelocity = 0d;
                clampTabsScrolled();
            }
            
            @Override
            public void render(int mouseX, int mouseY, float float_1) {
                mc.getTextureManager().bindTexture(CONFIG_TEX);
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0f);
                int int_3 = this.getHoverState(hovered);
                GlStateManager.enableBlend();
                GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
                GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                this.drawTexturedModalRect(x, y, 12, 18 * int_3, width, height);
            }
        });
        int j = 0;
        for(Pair<String, Integer> tab : tabs) {
            tabButtons.add(new ClothConfigTabButton(this, j, -100, 43, tab.getSecond(), 20, I18n.format(tab.getFirst())));
            j++;
        }
        tabButtons.forEach(children::add);
        children.add(buttonRightTab = new GuiButton(31321313, width - 16, 44, 12, 18, "") {
            @Override
            public void onClick(double diawd, double djwaidw) {
                super.onClick(diawd, djwaidw);
                tabsScrollProgress = Integer.MAX_VALUE;
                tabsScrollVelocity = 0d;
                clampTabsScrolled();
            }
            
            @Override
            public void render(int mouseX, int mouseY, float float_1) {
                mc.getTextureManager().bindTexture(CONFIG_TEX);
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0f);
                int int_3 = this.getHoverState(hovered);
                GlStateManager.enableBlend();
                GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
                GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                this.drawTexturedModalRect(x, y, 0, 18 * int_3, width, height);
            }
        });
    }
    
    @Override
    public boolean mouseScrolled(double double_3) {
        Point location = MouseUtils.getMouseLocation();
        if (tabsBounds.contains(location) && !tabsLeftBounds.contains(location) && !tabsRightBounds.contains(location) && double_3 != 0d) {
            if (double_3 < 0)
                tabsScrollVelocity += 16;
            if (double_3 > 0)
                tabsScrollVelocity -= 16;
            return true;
        } else if (listWidget.isMouseOver(location.x, location.y))
            if (listWidget.mouseScrolled(double_3))
                return true;
        return super.mouseScrolled(double_3);
    }
    
    public double getTabsMaximumScrolled() {
        if (tabsMaximumScrolled == -1d) {
            AtomicDouble d = new AtomicDouble();
            tabs.forEach(pair -> d.addAndGet(pair.getSecond() + 2));
            tabsMaximumScrolled = d.get();
        }
        return tabsMaximumScrolled;
    }
    
    public void resetTabsMaximumScrolled() {
        tabsMaximumScrolled = -1d;
        tabsScrollVelocity = 0f;
    }
    
    public void clampTabsScrolled() {
        int xx = 0;
        for(ClothConfigTabButton tabButton : tabButtons)
            xx += tabButton.getWidth() + 2;
        if (xx > width - 40)
            tabsScrollProgress = MathHelper.clamp(tabsScrollProgress, 0, getTabsMaximumScrolled() - width + 40);
        else
            tabsScrollProgress = 0d;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void render(int int_1, int int_2, float float_1) {
        if (smoothScrollingTabs) {
            double change = tabsScrollVelocity * 0.2f;
            if (change != 0) {
                if (change > 0 && change < .2)
                    change = .2;
                else if (change < 0 && change > -.2)
                    change = -.2;
                tabsScrollProgress += change;
                tabsScrollVelocity -= change;
                if (change > 0 == tabsScrollVelocity < 0)
                    tabsScrollVelocity = 0f;
                clampTabsScrolled();
            }
        } else {
            tabsScrollProgress += tabsScrollVelocity;
            tabsScrollVelocity = 0d;
            clampTabsScrolled();
        }
        int xx = 20 - (int) tabsScrollProgress;
        for(ClothConfigTabButton tabButton : tabButtons) {
            tabButton.x = xx;
            xx += tabButton.getWidth() + 2;
        }
        buttonLeftTab.enabled = tabsScrollProgress > 0d;
        buttonRightTab.enabled = tabsScrollProgress < getTabsMaximumScrolled() - width + 40;
        drawBackground(0);
        listWidget.getChildren().forEach(o -> {
            if (o instanceof AbstractConfigEntry)
                ((AbstractConfigEntry) o).setScreen(this);
        });
        listWidget.render(int_1, int_2, float_1);
        overlayBackground(tabsBounds, 32, 32, 32, 255, 255);
        
        drawCenteredString(mc.fontRenderer, title, width / 2, 18, -1);
        tabButtons.forEach(widget -> widget.render(int_1, int_2, float_1));
        overlayBackground(tabsLeftBounds, 64, 64, 64, 255, 255);
        overlayBackground(tabsRightBounds, 64, 64, 64, 255, 255);
        drawShades();
        buttonLeftTab.render(int_1, int_2, float_1);
        buttonRightTab.render(int_1, int_2, float_1);
        
        if (displayErrors && isEditable()) {
            List<String> errors = Lists.newArrayList();
            for(List<AbstractConfigEntry> entries : Lists.newArrayList(tabbedEntries.values()))
                for(AbstractConfigEntry entry : entries)
                    if (entry.getError().isPresent())
                        errors.add(((Optional<String>) entry.getError()).get());
            if (errors.size() > 0) {
                mc.getTextureManager().bindTexture(CONFIG_TEX);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                drawTexturedModalRect(10, 10, 0, 54, 3, 11);
                if (errors.size() == 1)
                    drawString(mc.fontRenderer, "§c" + errors.get(0), 18, 12, -1);
                else
                    drawString(mc.fontRenderer, "§c" + I18n.format("text.cloth-config.multi_error"), 18, 12, -1);
            }
        } else if (!isEditable()) {
            mc.getTextureManager().bindTexture(CONFIG_TEX);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedModalRect(10, 10, 0, 54, 3, 11);
            drawString(mc.fontRenderer, "§c" + I18n.format("text.cloth-config.not_editable"), 18, 12, -1);
        }
        super.render(int_1, int_2, float_1);
        queuedTooltips.forEach(queuedTooltip -> drawHoveringText(queuedTooltip.getText(), queuedTooltip.getX(), queuedTooltip.getY()));
        queuedTooltips.clear();
    }
    
    public void queueTooltip(QueuedTooltip queuedTooltip) {
        queuedTooltips.add(queuedTooltip);
    }
    
    private void drawShades() {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
        GlStateManager.disableAlphaTest();
        GlStateManager.shadeModel(7425);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(tabsBounds.getMinX() + 20, tabsBounds.getMinY() + 4, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos(tabsBounds.getMaxX() - 20, tabsBounds.getMinY() + 4, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos(tabsBounds.getMaxX() - 20, tabsBounds.getMinY(), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos(tabsBounds.getMinX() + 20, tabsBounds.getMinY(), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        tessellator.draw();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(tabsBounds.getMinX() + 20, tabsBounds.getMaxY(), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos(tabsBounds.getMaxX() - 20, tabsBounds.getMaxY(), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos(tabsBounds.getMaxX() - 20, tabsBounds.getMaxY() - 4, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos(tabsBounds.getMinX() + 20, tabsBounds.getMaxY() - 4, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
    }
    
    protected void overlayBackground(Rectangle rect, int red, int green, int blue, int startAlpha, int endAlpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        mc.getTextureManager().bindTexture(getBackgroundLocation());
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(rect.getMinX(), rect.getMaxY(), 0.0D).tex(rect.getMinX() / 32.0D, rect.getMaxY() / 32.0D).color(red, green, blue, endAlpha).endVertex();
        buffer.pos(rect.getMaxX(), rect.getMaxY(), 0.0D).tex(rect.getMaxX() / 32.0D, rect.getMaxY() / 32.0D).color(red, green, blue, endAlpha).endVertex();
        buffer.pos(rect.getMaxX(), rect.getMinY(), 0.0D).tex(rect.getMaxX() / 32.0D, rect.getMinY() / 32.0D).color(red, green, blue, startAlpha).endVertex();
        buffer.pos(rect.getMinX(), rect.getMinY(), 0.0D).tex(rect.getMinX() / 32.0D, rect.getMinY() / 32.0D).color(red, green, blue, startAlpha).endVertex();
        tessellator.draw();
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.allowCloseWithEscape()) {
            if (confirmSave && edited)
                mc.displayGuiScreen(new GuiYesNo(new QuitSaveConsumer(), I18n.format("text.cloth-config.quit_config"), I18n.format("text.cloth-config.quit_config_sure"), I18n.format("text.cloth-config.quit_discard"), I18n.format("gui.cancel"), 1));
            else
                mc.displayGuiScreen(parent);
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    public abstract void onSave(Map<String, List<Pair<String, Object>>> o);
    
    public boolean isEditable() {
        return true;
    }
    
    private class QuitSaveConsumer implements GuiYesNoCallback {
        @Override
        public void confirmResult(boolean t, int id) {
            if (!t)
                mc.displayGuiScreen(ClothConfigScreen.this);
            else
                mc.displayGuiScreen(parent);
            return;
        }
    }
    
    public class ListWidget extends DynamicElementListWidget {
        
        public ListWidget(Minecraft client, int width, int height, int top, int bottom, ResourceLocation backgroundLocation) {
            super(client, width, height, top, bottom, backgroundLocation);
            visible = false;
        }
        
        @Override
        public int getItemWidth() {
            return width - 80;
        }
        
        public ClothConfigScreen getScreen() {
            return ClothConfigScreen.this;
        }
        
        @Override
        protected int getScrollbarPosition() {
            return width - 36;
        }
        
        protected final void clearStuff() {
            this.clearItems();
        }
    }
    
}
