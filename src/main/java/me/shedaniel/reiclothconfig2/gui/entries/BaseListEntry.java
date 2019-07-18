package me.shedaniel.reiclothconfig2.gui.entries;

import com.google.common.collect.Lists;
import me.shedaniel.reiclothconfig2.api.QueuedTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseListEntry<T, C extends BaseListCell> extends TooltipListEntry<List<T>> {
    
    protected static final ResourceLocation CONFIG_TEX = new ResourceLocation("roughlyenoughitems","textures/gui/cloth_config.png");
    protected final List<C> cells;
    protected final List<IGuiEventListener> widgets;
    protected boolean expended;
    protected Consumer<List<T>> saveConsumer;
    protected ListLabelWidget labelWidget;
    protected GuiButton resetWidget;
    protected Function<BaseListEntry, C> createNewInstance;
    protected Supplier<List<T>> defaultValue;
    protected String addTooltip = I18n.format("text.cloth-config.list.add"), removeTooltip = I18n.format("text.cloth-config.list.remove");
    
    @Deprecated
    public BaseListEntry(String fieldName, Supplier<Optional<String[]>> tooltipSupplier, Supplier<List<T>> defaultValue, Function<BaseListEntry, C> createNewInstance, Consumer<List<T>> saveConsumer, String resetButtonKey) {
        this(fieldName, tooltipSupplier, defaultValue, createNewInstance, saveConsumer, resetButtonKey, false);
    }
    
    public BaseListEntry(String fieldName, Supplier<Optional<String[]>> tooltipSupplier, Supplier<List<T>> defaultValue, Function<BaseListEntry, C> createNewInstance, Consumer<List<T>> saveConsumer, String resetButtonKey, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        this.cells = Lists.newArrayList();
        this.labelWidget = new ListLabelWidget();
        this.widgets = Lists.newArrayList(labelWidget);
        this.resetWidget = new GuiButton(2131321, 0, 0, Minecraft.getInstance().fontRenderer.getStringWidth(I18n.format(resetButtonKey)) + 6, 20, I18n.format(resetButtonKey)) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                widgets.removeAll(cells);
                cells.clear();
                defaultValue.get().stream().map(t -> getFromValue(t)).forEach(cells::add);
                widgets.addAll(cells);
                getScreen().setEdited(true, isRequiresRestart());
            }
        };
        this.widgets.add(resetWidget);
        this.saveConsumer = saveConsumer;
        this.createNewInstance = createNewInstance;
        this.defaultValue = defaultValue;
    }
    
    protected abstract C getFromValue(T value);
    
    public Function<BaseListEntry, C> getCreateNewInstance() {
        return createNewInstance;
    }
    
    public void setCreateNewInstance(Function<BaseListEntry, C> createNewInstance) {
        this.createNewInstance = createNewInstance;
    }
    
    public String getAddTooltip() {
        return addTooltip;
    }
    
    public void setAddTooltip(String addTooltip) {
        this.addTooltip = addTooltip;
    }
    
    public String getRemoveTooltip() {
        return removeTooltip;
    }
    
    public void setRemoveTooltip(String removeTooltip) {
        this.removeTooltip = removeTooltip;
    }
    
    @Override
    public Optional<List<T>> getDefaultValue() {
        return Optional.ofNullable(defaultValue.get());
    }
    
    @Override
    public int getItemHeight() {
        if (expended) {
            int i = 24;
            for(BaseListCell entry : cells)
                i += entry.getCellHeight();
            return i;
        }
        return 24;
    }
    
    @Override
    public List<? extends IGuiEventListener> getChildren() {
        return widgets;
    }
    
    @Override
    public Optional<String> getError() {
        String error = null;
        for(BaseListCell entry : cells)
            if (entry.getError().isPresent()) {
                if (error != null)
                    return Optional.ofNullable(I18n.format("text.cloth-config.multi_error"));
                return Optional.ofNullable((String) entry.getError().get());
            }
        return Optional.ofNullable(error);
    }
    
    @Override
    public void save() {
        if (saveConsumer != null)
            saveConsumer.accept(getValue());
    }
    
    @Override
    public boolean isMouseInside(int mouseX, int mouseY, int x, int y, int entryWidth, int entryHeight) {
        labelWidget.rectangle.x = x - 15;
        labelWidget.rectangle.y = y;
        labelWidget.rectangle.width = entryWidth + 15;
        labelWidget.rectangle.height = 24;
        return labelWidget.rectangle.contains(mouseX, mouseY) && getParent().isMouseOver(mouseX, mouseY) && !resetWidget.isMouseOver();
    }
    
    protected boolean isInsideCreateNew(double mouseX, double mouseY) {
        return mouseX >= labelWidget.rectangle.x + 12 && mouseY >= labelWidget.rectangle.y + 3 && mouseX <= labelWidget.rectangle.x + 12 + 11 && mouseY <= labelWidget.rectangle.y + 3 + 11;
    }
    
    protected boolean isInsideDelete(double mouseX, double mouseY) {
        return mouseX >= labelWidget.rectangle.x + 25 && mouseY >= labelWidget.rectangle.y + 3 && mouseX <= labelWidget.rectangle.x + 25 + 11 && mouseY <= labelWidget.rectangle.y + 3 + 11;
    }
    
    public Optional<String[]> getTooltip(int mouseX, int mouseY) {
        if (addTooltip != null && isInsideCreateNew(mouseX, mouseY))
            return Optional.of(new String[]{addTooltip});
        if (removeTooltip != null && isInsideDelete(mouseX, mouseY))
            return Optional.of(new String[]{removeTooltip});
        if (getTooltipSupplier() != null)
            return getTooltipSupplier().get();
        return Optional.empty();
    }
    
    @Override
    public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        labelWidget.rectangle.x = x - 19;
        labelWidget.rectangle.y = y;
        labelWidget.rectangle.width = entryWidth + 19;
        labelWidget.rectangle.height = 24;
        if (isMouseInside(mouseX, mouseY, x, y, entryWidth, entryHeight)) {
            Optional<String[]> tooltip = getTooltip(mouseX, mouseY);
            if (tooltip.isPresent() && tooltip.get().length > 0)
                getScreen().queueTooltip(QueuedTooltip.create(new Point(mouseX, mouseY), tooltip.get()));
        }
        Minecraft.getInstance().getTextureManager().bindTexture(CONFIG_TEX);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color4f(1, 1, 1, 1);
        BaseListCell focused = !expended || getFocused() == null || !(getFocused() instanceof BaseListCell) ? null : (BaseListCell) getFocused();
        boolean insideCreateNew = isInsideCreateNew(mouseX, mouseY);
        boolean insideDelete = isInsideDelete(mouseX, mouseY);
        drawTexturedModalRect(x - 15, y + 4, 24 + 9, (labelWidget.rectangle.contains(mouseX, mouseY) && !insideCreateNew && !insideDelete ? 18 : 0) + (expended ? 9 : 0), 9, 9);
        drawTexturedModalRect(x - 15 + 13, y + 4, 24 + 18, insideCreateNew ? 9 : 0, 9, 9);
        drawTexturedModalRect(x - 15 + 26, y + 4, 24 + 27, focused == null ? 0 : insideDelete ? 18 : 9, 9, 9);
        resetWidget.x = x + entryWidth - resetWidget.getWidth();
        resetWidget.y = y;
        resetWidget.enabled = isEditable() && getDefaultValue().isPresent();
        resetWidget.render(mouseX, mouseY, delta);
        Minecraft.getInstance().fontRenderer.drawStringWithShadow(I18n.format(getFieldName()), x + 24, y + 5, labelWidget.rectangle.contains(mouseX, mouseY) && !resetWidget.isMouseOver() && !insideDelete && !insideCreateNew ? 0xffe6fe16 : -1);
        if (expended) {
            int yy = y + 24;
            for(BaseListCell cell : cells) {
                cell.render(-1, yy, x + 14, entryWidth - 14, cell.getCellHeight(), mouseX, mouseY, getParent().getFocused() != null && getParent().getFocused().equals(this) && getFocused() != null && getFocused().equals(cell), delta);
                yy += cell.getCellHeight();
            }
        }
    }
    
    public class ListLabelWidget implements IGuiEventListener {
        private Rectangle rectangle = new Rectangle();
        
        @Override
        public boolean mouseClicked(double double_1, double double_2, int int_1) {
            if (resetWidget.isMouseOver()) {
                return false;
            } else if (isInsideCreateNew(double_1, double_2)) {
                expended = true;
                C cell;
                cells.add(0, cell = createNewInstance.apply(BaseListEntry.this));
                widgets.add(0, cell);
                getScreen().setEdited(true, isRequiresRestart());
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else if (isInsideDelete(double_1, double_2)) {
                BaseListCell focused = !expended && getFocused() == null || !(getFocused() instanceof BaseListCell) ? null : (BaseListCell) getFocused();
                if (focused != null) {
                    cells.remove(focused);
                    widgets.remove(focused);
                    getScreen().setEdited(true, isRequiresRestart());
                    Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                return true;
            } else if (rectangle.contains(double_1, double_2)) {
                expended = !expended;
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return false;
        }
    }
    
}
