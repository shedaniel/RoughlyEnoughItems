package me.shedaniel.rei.gui.config;

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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ItemListOrderingEntry extends ListEntry {
    private AtomicReference<Pair<ItemListOrdering, Boolean>> value;
    private ButtonWidget buttonWidget;
    
    public ItemListOrderingEntry(String fieldName, Pair<ItemListOrdering, Boolean> val) {
        super(fieldName);
        this.value = new AtomicReference(val);
        this.buttonWidget = new ButtonWidget(0, 0, 150, 20, "") {
            public void onPressed() {
                int index = Arrays.asList(ItemListOrdering.values()).indexOf(value.get().getKey()) + 1;
                boolean currentAscending = value.get().getValue();
                if (index >= ItemListOrdering.values().length) {
                    index = 0;
                    currentAscending = !currentAscending;
                }
                ItemListOrderingEntry.this.value.set(new Pair<>(ItemListOrdering.values()[index], currentAscending));
                ((ListWidget) ItemListOrderingEntry.this.getParent()).getScreen().setEdited(true);
            }
        };
    }
    
    public Object getObject() {
        return this.value.get();
    }
    
    public void draw(int entryWidth, int height, int i3, int i4, boolean isSelected, float delta) {
        Window window = MinecraftClient.getInstance().window;
        Point mouse = ClientUtils.getMouseLocation();
        this.buttonWidget.y = this.getY();
        this.buttonWidget.setMessage(I18n.translate("text.rei.config.list_ordering_button", I18n.translate(value.get().getKey().getNameTranslationKey()), I18n.translate(value.get().getValue() ? "ordering.rei.ascending" : "ordering.rei.descending")));
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(I18n.translate(this.getFieldName(), new Object[0]), (float) (window.getScaledWidth() - this.getX() - MinecraftClient.getInstance().textRenderer.getStringWidth(I18n.translate(this.getFieldName(), new Object[0]))), (float) (this.getY() + 5), 16777215);
            this.buttonWidget.x = this.getX();
        } else {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(I18n.translate(this.getFieldName(), new Object[0]), (float) this.getX(), (float) (this.getY() + 5), 16777215);
            this.buttonWidget.x = window.getScaledWidth() - this.getX() - this.buttonWidget.getWidth();
        }
        
        this.buttonWidget.render(mouse.x, mouse.y, delta);
    }
    
    public String getYesNoText(boolean bool) {
        return bool ? "§aYes" : "§cNo";
    }
    
    @Override
    public List<? extends InputListener> getInputListeners() {
        return Collections.singletonList(buttonWidget);
    }
    
    public boolean isActive() {
        return this.buttonWidget.isHovered();
    }
    
    public void setActive(boolean b) {
    }
    
    public InputListener getFocused() {
        return null;
    }
    
    public void setFocused(InputListener inputListener) {
    }
    
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        return this.buttonWidget.mouseClicked(double_1, double_2, int_1);
    }
    
    @Override
    public void save() {
        RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering = value.get().getKey();
        RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending = value.get().getValue();
    }
}
