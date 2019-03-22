package me.shedaniel.rei.gui.config;

import com.google.common.collect.Lists;
import javafx.util.Pair;
import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.cloth.gui.ClothConfigScreen.ListEntry;
import me.shedaniel.cloth.gui.ClothConfigScreen.ListWidget;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ItemListOrdering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.InputListener;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ItemListOrderingEntry extends ListEntry {
    private AtomicReference<Pair<ItemListOrdering, Boolean>> value;
    private ButtonWidget buttonWidget;
    private ButtonWidget resetButton;
    private List<InputListener> widgets;
    
    public ItemListOrderingEntry(String fieldName, Pair<ItemListOrdering, Boolean> val) {
        super(fieldName);
        this.value = new AtomicReference(val);
        this.buttonWidget = new ButtonWidget(0, 0, 150, 20, "", widget -> {
            int index = Arrays.asList(ItemListOrdering.values()).indexOf(value.get().getKey()) + 1;
            boolean currentAscending = value.get().getValue();
            if (index >= ItemListOrdering.values().length) {
                index = 0;
                currentAscending = !currentAscending;
            }
            ItemListOrderingEntry.this.value.set(new Pair<>(ItemListOrdering.values()[index], currentAscending));
            ((ListWidget) ItemListOrderingEntry.this.getParent()).getScreen().setEdited(true);
        });
        this.resetButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getStringWidth(I18n.translate("text.cloth.reset_value")) + 6, 20, I18n.translate("text.cloth.reset_value"), (widget) -> {
            this.value.set((Pair) getDefaultValue().get());
            ((ListWidget) this.getParent()).getScreen().setEdited(true);
        });
        this.widgets = Lists.newArrayList(this.buttonWidget, this.resetButton);
    }
    
    public Object getObject() {
        return this.value.get();
    }
    
    @Override
    public Optional<Object> getDefaultValue() {
        return Optional.of(new Pair<>(ItemListOrdering.registry, true));
    }
    
    public void draw(int entryWidth, int height, int i3, int i4, boolean isSelected, float delta) {
        Window window = MinecraftClient.getInstance().window;
        Point mouse = ClientUtils.getMouseLocation();
        this.resetButton.active = this.getDefaultValue().isPresent() && (((Pair<ItemListOrdering, Boolean>) this.getDefaultValue().get()).getKey() != this.value.get().getKey() || ((Pair<ItemListOrdering, Boolean>) this.getDefaultValue().get()).getValue().booleanValue() != this.value.get().getValue().booleanValue());
        this.resetButton.y = this.getY();
        this.buttonWidget.y = this.getY();
        this.buttonWidget.setMessage(I18n.translate("text.rei.config.list_ordering_button", I18n.translate(value.get().getKey().getNameTranslationKey()), I18n.translate(value.get().getValue() ? "ordering.rei.ascending" : "ordering.rei.descending")));
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(I18n.translate(this.getFieldName(), new Object[0]), (float) (window.getScaledWidth() - this.getX() - MinecraftClient.getInstance().textRenderer.getStringWidth(I18n.translate(this.getFieldName(), new Object[0]))), (float) (this.getY() + 5), 16777215);
            this.resetButton.x = this.getX();
            this.buttonWidget.x = this.getX() + this.resetButton.getWidth() + 2;
            this.buttonWidget.setWidth(150 - this.resetButton.getWidth() - 2);
        } else {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(I18n.translate(this.getFieldName(), new Object[0]), (float) this.getX(), (float) (this.getY() + 5), 16777215);
            this.resetButton.x = window.getScaledWidth() - this.getX() - this.resetButton.getWidth();
            this.buttonWidget.x = window.getScaledWidth() - this.getX() - 150;
            this.buttonWidget.setWidth(150 - this.resetButton.getWidth() - 2);
        }
        this.buttonWidget.render(mouse.x, mouse.y, delta);
        this.resetButton.render(mouse.x, mouse.y, delta);
    }
    
    public String getYesNoText(boolean bool) {
        return bool ? "§aYes" : "§cNo";
    }
    
    @Override
    public List<? extends InputListener> getInputListeners() {
        return widgets;
    }
    
    public boolean isActive() {
        return this.buttonWidget.isHovered() || this.resetButton.isHovered();
    }
    
    public void setActive(boolean b) {
    }
    
    public InputListener getFocused() {
        return null;
    }
    
    public void setFocused(InputListener inputListener) {
    }
    
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (this.buttonWidget.mouseClicked(double_1, double_2, int_1))
            return true;
        return this.resetButton.mouseClicked(double_1, double_2, int_1);
    }
    
    @Override
    public void save() {
        RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering = value.get().getKey();
        RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending = value.get().getValue();
    }
}
