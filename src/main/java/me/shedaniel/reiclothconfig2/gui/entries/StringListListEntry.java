package me.shedaniel.reiclothconfig2.gui.entries;

import me.shedaniel.rei.api.GuiTextFieldHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StringListListEntry extends BaseListEntry<String, StringListListEntry.StringListCell> {
    
    @Deprecated
    public StringListListEntry(String fieldName, List<String> value, boolean defaultExpended, Supplier<Optional<String[]>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, String resetButtonKey) {
        this(fieldName, value, defaultExpended, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, false);
    }
    
    @Deprecated
    public StringListListEntry(String fieldName, List<String> value, boolean defaultExpended, Supplier<Optional<String[]>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, String resetButtonKey, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, defaultValue, baseListEntry -> new StringListCell("", (StringListListEntry) baseListEntry), saveConsumer, resetButtonKey, requiresRestart);
        for(String str : value)
            cells.add(new StringListCell(str, this));
        this.widgets.addAll(cells);
        expended = defaultExpended;
    }
    
    @Override
    public List<String> getValue() {
        return cells.stream().map(cell -> cell.widget.getText()).collect(Collectors.toList());
    }
    
    @Override
    protected StringListCell getFromValue(String value) {
        return new StringListCell(value, this);
    }
    
    public static class StringListCell extends BaseListCell {
        
        private GuiTextField widget;
        private boolean isSelected;
        private StringListListEntry listListEntry;
        
        public StringListCell(String value, StringListListEntry listListEntry) {
            this.listListEntry = listListEntry;
            widget = new GuiTextField(124214, Minecraft.getInstance().fontRenderer, 0, 0, 100, 18) {
                @Override
                public void drawTextField(int int_1, int int_2, float float_1) {
                    boolean f = isFocused();
                    setFocused(isSelected);
                    widget.setTextColor(14737632);
                    super.drawTextField(int_1, int_2, float_1);
                    setFocused(f);
                }
            };
            widget.setMaxStringLength(999999);
            widget.setEnableBackgroundDrawing(false);
            widget.setText(value);
            widget.setTextAcceptHandler((i, s) -> {
                if (!value.contentEquals(s))
                    listListEntry.getScreen().setEdited(true, listListEntry.isRequiresRestart());
            });
        }
        
        @Override
        public Optional<String> getError() {
            return Optional.empty();
        }
        
        @Override
        public int getCellHeight() {
            return 20;
        }
        
        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            ((GuiTextFieldHooks) widget).rei_setWidth(entryWidth - 12);
            widget.x = x;
            widget.y = y + 1;
            widget.setEnabled(listListEntry.isEditable());
            this.isSelected = isSelected;
            widget.drawTextField(mouseX, mouseY, delta);
            if (isSelected && listListEntry.isEditable())
                drawRect(x, y + 12, x + entryWidth - 12, y + 13, getError().isPresent() ? 0xffff5555 : 0xffe0e0e0);
        }
        
        @Override
        public List<? extends IGuiEventListener> getChildren() {
            return Collections.singletonList(widget);
        }
        
    }
    
}
