package me.shedaniel.rei.impl.client.gui.screen.collapsible;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.gui.widgets.CloseableScissors;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.collapsible.CollapsibleConfigManager;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.screen.collapsible.selection.CustomCollapsibleEntrySelectionScreen;
import me.shedaniel.rei.impl.client.gui.screen.generic.OptionEntriesScreen;
import me.shedaniel.rei.impl.client.gui.widget.DynamicErrorFreeEntryListWidget;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsibleEntryRegistryImpl;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;

public class CollapsibleEntriesScreen extends Screen {
    private final Screen parent;
    private final CollapsibleConfigManager.CollapsibleConfigObject configObject;
    private final Runnable editedSink;
    private final List<CollapsibleEntryWidget> widgets = new ArrayList<>();
    private ListWidget listWidget;
    private boolean dirty = true;
    
    public CollapsibleEntriesScreen(Screen parent, CollapsibleConfigManager.CollapsibleConfigObject configObject, Runnable editedSink) {
        super(new TranslatableComponent("text.rei.collapsible.entries"));
        this.parent = parent;
        this.configObject = configObject;
        this.editedSink = editedSink;
        this.prepareWidgets(configObject);
    }
    
    public void prepareWidgets(CollapsibleConfigManager.CollapsibleConfigObject configObject) {
        this.widgets.clear();
        
        for (CollapsibleConfigManager.CustomGroup customEntry : configObject.customGroups) {
            this.widgets.add(new CollapsibleEntryWidget(true, customEntry.id, new TextComponent(customEntry.name),
                    CollectionUtils.filterAndMap(customEntry.stacks, EntryStackProvider::isValid, EntryStackProvider::provide), configObject, editedSink,
                    () -> {
                        this.prepareWidgets(configObject);
                        this.dirty = true;
                        this.editedSink.run();
                    }));
        }
        
        CollapsibleEntryRegistryImpl collapsibleRegistry = (CollapsibleEntryRegistryImpl) CollapsibleEntryRegistry.getInstance();
        Multimap<ResourceLocation, EntryStack<?>> entries = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);
        for (HashedEntryStackWrapper wrapper : ((EntryRegistryImpl) EntryRegistry.getInstance()).getFilteredList().getList()) {
            for (CollapsibleEntryRegistryImpl.Entry entry : collapsibleRegistry.getEntries()) {
                if (entry.getMatcher().matches(wrapper.unwrap(), wrapper.hashExact())) {
                    entries.put(entry.getId(), wrapper.unwrap());
                }
            }
        }
        
        for (CollapsibleEntryRegistryImpl.Entry entry : collapsibleRegistry.getEntries()) {
            this.widgets.add(new CollapsibleEntryWidget(false, entry.getId(), entry.getName(), entries.get(entry.getId()), configObject, editedSink,
                    () -> {
                        this.prepareWidgets(configObject);
                        this.dirty = true;
                        this.editedSink.run();
                    }));
        }
    }
    
    @Override
    public void init() {
        super.init();
        {
            Component backText = new TextComponent("↩ ").append(new TranslatableComponent("gui.back"));
            addRenderableWidget(new Button(4, 4, font.width(backText) + 10, 20, backText,
                    button -> this.onClose()));
        }
        {
            Component addText = new TextComponent(" + ");
            addRenderableWidget(new Button(width - 4 - 20, 4, 20, 20, addText, $ -> {
                setupCustom(new ResourceLocation("custom:" + UUID.randomUUID()), "", new ArrayList<>(), this.configObject, () -> {
                    this.prepareWidgets(configObject);
                    this.dirty = true;
                    this.editedSink.run();
                });
            }));
        }
        
        this.listWidget = new ListWidget(width, height, 30);
        ((List<GuiEventListener>) this.children()).add(this.listWidget);
        this.dirty = true;
    }
    
    public static void setupCustom(ResourceLocation id, String name, List<EntryStack<?>> stacks, CollapsibleConfigManager.CollapsibleConfigObject configObject, Runnable markDirty) {
        Minecraft.getInstance().setScreen(new OptionEntriesScreen(new TranslatableComponent("text.rei.collapsible.entries.custom.title"), Minecraft.getInstance().screen) {
            private TextFieldListEntry entry;
            
            @Override
            public void addEntries(Consumer<ListEntry> entryConsumer) {
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, new TranslatableComponent("text.rei.collapsible.entries.custom.id").withStyle(ChatFormatting.GRAY)
                        .append(new TextComponent(" " + id).withStyle(ChatFormatting.DARK_GRAY)));
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, new TranslatableComponent("text.rei.collapsible.entries.custom.name").withStyle(ChatFormatting.GRAY));
                entryConsumer.accept(this.entry = new TextFieldListEntry(width - 36, widget -> {
                    widget.setMaxLength(40);
                    if (this.entry != null) widget.setValue(this.entry.getWidget().getValue());
                    else widget.setValue(name);
                }));
                addEmpty(entryConsumer, 10);
                entryConsumer.accept(new ButtonListEntry(width - 36, $ -> new TranslatableComponent("text.rei.collapsible.entries.custom.select"), ($, button) -> {
                    CustomCollapsibleEntrySelectionScreen screen = new CustomCollapsibleEntrySelectionScreen(stacks);
                    screen.parent = this.minecraft.screen;
                    this.minecraft.setScreen(screen);
                }));
            }
            
            @Override
            public void save() {
                configObject.customGroups.removeIf(customGroup -> customGroup.id.equals(id));
                configObject.customGroups.add(new CollapsibleConfigManager.CustomGroup(id, this.entry.getWidget().getValue(),
                        CollectionUtils.map(stacks, EntryStackProvider::ofStack)));
                markDirty.run();
            }
        });
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        if (this.dirty) {
            this.listWidget.clear();
            
            for (CollapsibleEntryWidget widget : this.widgets) {
                this.listWidget.add(widget);
            }
            
            this.dirty = false;
        }
        
        this.listWidget.render(poses, mouseX, mouseY, delta);
        super.render(poses, mouseX, mouseY, delta);
        this.font.drawShadow(poses, this.title, this.width / 2.0F - this.font.width(this.title) / 2.0F, 12.0F, -1);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.listWidget.mouseScrolled(mouseX, mouseY, amount) || super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.listWidget.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.listWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
    
    private static class ListWidget extends Widget {
        private static final int PADDING = 6;
        private final int width;
        private final int height;
        private final int top;
        private final ScrollingContainer scroller = new ScrollingContainer() {
            @Override
            public Rectangle getBounds() {
                return new Rectangle(0, top, width, height - top);
            }
            
            @Override
            public int getMaxScrollHeight() {
                return getMaxScrollDist();
            }
        };
        private final List<CollapsibleEntryWidget>[] columns;
        private final List<CollapsibleEntryWidget> children = new ArrayList<>();
        
        public ListWidget(int width, int height, int top) {
            this.width = width;
            this.height = height;
            this.top = top;
            this.columns = new List[Math.max(1, (width - 12 - PADDING) / (130 + PADDING))];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = new ArrayList<>();
            }
        }
        
        @Override
        public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
            this.scroller.updatePosition(delta);
            
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            DynamicErrorFreeEntryListWidget.renderBackBackground(poses, buffer, tesselator,
                    GuiComponent.BACKGROUND_LOCATION, 0, this.top, this.width, this.height, 0, 32);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 771, 0, 1);
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            Matrix4f matrix = poses.last().pose();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, this.top + 4, 0.0F).uv(0, 1).color(0x00000000).endVertex();
            buffer.vertex(matrix, this.width, this.top + 4, 0.0F).uv(1, 1).color(0x00000000).endVertex();
            buffer.vertex(matrix, this.width, this.top, 0.0F).uv(1, 0).color(0xFF000000).endVertex();
            buffer.vertex(matrix, 0, this.top, 0.0F).uv(0, 0).color(0xFF000000).endVertex();
            tesselator.end();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            
            try (CloseableScissors scissors = scissor(poses, new Rectangle(0, this.top, this.width - 6, this.height - this.top))) {
                int entryWidth = (this.width - 12 - 6 - PADDING) / this.columns.length - PADDING;
                for (int i = 0; i < this.columns.length; i++) {
                    int x = 6 + PADDING + i * (entryWidth + PADDING);
                    int y = this.top + PADDING - scroller.scrollAmountInt();
                    for (CollapsibleEntryWidget widget : this.columns[i]) {
                        widget.setPosition(x, y);
                        widget.setWidth(entryWidth);
                        widget.render(poses, mouseX, mouseY, delta);
                        y += widget.getHeight() + PADDING;
                    }
                }
            }
            
            this.scroller.renderScrollBar();
            
            DynamicErrorFreeEntryListWidget.renderBackBackground(poses, buffer, tesselator,
                    GuiComponent.BACKGROUND_LOCATION, 0, 0, this.width, this.top, 0, 64);
            ScreenOverlayImpl.getInstance().lateRender(poses, mouseX, mouseY, delta);
        }
        
        private int getMaxScrollDist() {
            return Arrays.stream(this.columns).mapToInt(ListWidget::getHeightOf)
                           .max()
                           .orElse(0)
                   + PADDING * 2;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            for (CollapsibleEntryWidget widget : children) {
                if (widget.mouseScrolled(mouseX, mouseY, amount)) {
                    return true;
                }
            }
            if (mouseY > this.top) {
                this.scroller.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
                return true;
            } else {
                return false;
            }
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.scroller.updateDraggingState(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        }
        
        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return this.scroller.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        
        public void clear() {
            this.children.clear();
            for (List<CollapsibleEntryWidget> column : columns) {
                column.clear();
            }
        }
        
        public void add(CollapsibleEntryWidget widget) {
            Arrays.stream(columns)
                    .min(Comparator.comparingInt(ListWidget::getHeightOf))
                    .ifPresent(widgets -> widgets.add(widget));
            this.children.add(widget);
        }
        
        private static int getHeightOf(List<CollapsibleEntryWidget> widgets) {
            int height = 0;
            for (CollapsibleEntryWidget w : widgets) {
                height += w.getHeight() + PADDING;
            }
            return Math.max(0, height - PADDING);
        }
    }
}
