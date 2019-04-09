package me.shedaniel.rei.gui.config;

import com.google.common.collect.Lists;
import me.shedaniel.cloth.gui.ClothConfigScreen.ListEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ItemListOrdering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ItemListOrderingEntry extends ListEntry {
    private AtomicReference<Pair<ItemListOrdering, Boolean>> value;
    private ButtonWidget buttonWidget;
    private ButtonWidget resetButton;
    private List<Element> widgets;
    
    public ItemListOrderingEntry(String fieldName, Pair<ItemListOrdering, Boolean> val) {
        super(fieldName);
        this.value = new AtomicReference(val);
        this.buttonWidget = new ButtonWidget(0, 0, 150, 20, "", widget -> {
            int index = Arrays.asList(ItemListOrdering.values()).indexOf(value.get().getLeft()) + 1;
            boolean currentAscending = value.get().getRight();
            if (index >= ItemListOrdering.values().length) {
                index = 0;
                currentAscending = !currentAscending;
            }
            ItemListOrderingEntry.this.value.set(new Pair<>(ItemListOrdering.values()[index], currentAscending));
            getScreen().setEdited(true);
        });
        this.resetButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getStringWidth(I18n.translate("text.cloth.reset_value")) + 6, 20, I18n.translate("text.cloth.reset_value"), (widget) -> {
            this.value.set((Pair) getDefaultValue().get());
            getScreen().setEdited(true);
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
    
    @Override
    public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        Window window = MinecraftClient.getInstance().window;
        this.resetButton.active = this.getDefaultValue().isPresent() && (((Pair<ItemListOrdering, Boolean>) this.getDefaultValue().get()).getLeft() != this.value.get().getLeft() || ((Pair<ItemListOrdering, Boolean>) this.getDefaultValue().get()).getRight().booleanValue() != this.value.get().getRight().booleanValue());
        this.resetButton.y = y;
        this.buttonWidget.y = y;
        this.buttonWidget.setMessage(I18n.translate("text.rei.config.list_ordering_button", I18n.translate(value.get().getLeft().getNameTranslationKey()), I18n.translate(value.get().getRight() ? "ordering.rei.ascending" : "ordering.rei.descending")));
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(I18n.translate(this.getFieldName(), new Object[0]), (float) (window.getScaledWidth() - x - MinecraftClient.getInstance().textRenderer.getStringWidth(I18n.translate(this.getFieldName(), new Object[0]))), (float) (y + 5), 16777215);
            this.resetButton.x = x;
            this.buttonWidget.x = x + this.resetButton.getWidth() + 2;
            this.buttonWidget.setWidth(150 - this.resetButton.getWidth() - 2);
        } else {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(I18n.translate(this.getFieldName(), new Object[0]), (float) x, (float) (y + 5), 16777215);
            this.resetButton.x = window.getScaledWidth() - x - this.resetButton.getWidth();
            this.buttonWidget.x = window.getScaledWidth() - x - 150;
            this.buttonWidget.setWidth(150 - this.resetButton.getWidth() - 2);
        }
        this.buttonWidget.render(mouseX, mouseY, delta);
        this.resetButton.render(mouseX, mouseY, delta);
    }
    
    public String getYesNoText(boolean bool) {
        return bool ? "§aYes" : "§cNo";
    }
    
    @Override
    public List<? extends Element> children() {
        return widgets;
    }
    
    @Override
    public void save() {
        RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering = value.get().getLeft();
        RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending = value.get().getRight();
    }
}
