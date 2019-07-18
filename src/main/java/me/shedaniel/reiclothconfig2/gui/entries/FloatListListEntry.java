package me.shedaniel.reiclothconfig2.gui.entries;

import me.shedaniel.rei.api.GuiTextFieldHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FloatListListEntry extends BaseListEntry<Float, FloatListListEntry.FloatListCell> {
    
    private float minimum, maximum;
    
    @Deprecated
    public FloatListListEntry(String fieldName, List<Float> value, boolean defaultExpended, Supplier<Optional<String[]>> tooltipSupplier, Consumer<List<Float>> saveConsumer, Supplier<List<Float>> defaultValue, String resetButtonKey) {
        this(fieldName, value, defaultExpended, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, false);
    }
    
    @Deprecated
    public FloatListListEntry(String fieldName, List<Float> value, boolean defaultExpended, Supplier<Optional<String[]>> tooltipSupplier, Consumer<List<Float>> saveConsumer, Supplier<List<Float>> defaultValue, String resetButtonKey, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, defaultValue, baseListEntry -> new FloatListCell(0, (FloatListListEntry) baseListEntry), saveConsumer, resetButtonKey, requiresRestart);
        this.minimum = -Float.MAX_VALUE;
        this.maximum = Float.MAX_VALUE;
        for(float f : value)
            cells.add(new FloatListCell(f, this));
        this.widgets.addAll(cells);
        expended = defaultExpended;
    }
    
    @Override
    public List<Float> getValue() {
        return cells.stream().map(cell -> Float.valueOf(cell.widget.getText())).collect(Collectors.toList());
    }
    
    public FloatListListEntry setMaximum(float maximum) {
        this.maximum = maximum;
        return this;
    }
    
    public FloatListListEntry setMinimum(float minimum) {
        this.minimum = minimum;
        return this;
    }
    
    @Override
    protected FloatListCell getFromValue(Float value) {
        return new FloatListCell(value, this);
    }
    
    public static class FloatListCell extends BaseListCell {
        
        private Function<String, String> stripCharacters = s -> {
            StringBuilder stringBuilder_1 = new StringBuilder();
            char[] var2 = s.toCharArray();
            int var3 = var2.length;
            
            for(int var4 = 0; var4 < var3; ++var4)
                if (Character.isDigit(var2[var4]) || var2[var4] == '-' || var2[var4] == '.')
                    stringBuilder_1.append(var2[var4]);
            
            return stringBuilder_1.toString();
        };
        private GuiTextField widget;
        private boolean isSelected;
        private FloatListListEntry listListEntry;
        
        public FloatListCell(float value, FloatListListEntry listListEntry) {
            this.listListEntry = listListEntry;
            widget = new GuiTextField(51414, Minecraft.getInstance().fontRenderer, 0, 0, 100, 18) {
                @Override
                public void drawTextField(int int_1, int int_2, float float_1) {
                    boolean f = isFocused();
                    setFocused(isSelected);
                    try {
                        float i = Float.valueOf(getText());
                        if (i < listListEntry.minimum || i > listListEntry.maximum)
                            widget.setTextColor(16733525);
                        else
                            widget.setTextColor(14737632);
                    } catch (NumberFormatException ex) {
                        widget.setTextColor(16733525);
                    }
                    super.drawTextField(int_1, int_2, float_1);
                    setFocused(f);
                }
                
                @Override
                public void writeText(String string_1) {
                    super.writeText(stripCharacters.apply(string_1));
                }
            };
            widget.setMaxStringLength(999999);
            widget.setEnableBackgroundDrawing(false);
            widget.setText(value + "");
            widget.setTextAcceptHandler((i, s) -> {
                if (!(value + "").equalsIgnoreCase(s))
                    listListEntry.getScreen().setEdited(true, listListEntry.isRequiresRestart());
            });
        }
        
        @Override
        public Optional<String> getError() {
            try {
                float i = Float.valueOf(widget.getText());
                if (i > listListEntry.maximum)
                    return Optional.of(I18n.format("text.cloth-config.error.too_large", listListEntry.maximum));
                else if (i < listListEntry.minimum)
                    return Optional.of(I18n.format("text.cloth-config.error.too_small", listListEntry.minimum));
            } catch (NumberFormatException ex) {
                return Optional.of(I18n.format("text.cloth-config.error.not_valid_number_float"));
            }
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
