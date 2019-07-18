package me.shedaniel.reiclothconfig2.gui.entries;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FloatListEntry extends TextFieldListEntry<Float> {
    
    private static Function<String, String> stripCharacters = s -> {
        StringBuilder stringBuilder_1 = new StringBuilder();
        char[] var2 = s.toCharArray();
        int var3 = var2.length;
        
        for(int var4 = 0; var4 < var3; ++var4)
            if (Character.isDigit(var2[var4]) || var2[var4] == '-' || var2[var4] == '.')
                stringBuilder_1.append(var2[var4]);
        
        return stringBuilder_1.toString();
    };
    private float minimum, maximum;
    private Consumer<Float> saveConsumer;
    
    @Deprecated
    public FloatListEntry(String fieldName, Float value, Consumer<Float> saveConsumer) {
        this(fieldName, value, "text.cloth-config.reset_value", null, saveConsumer);
    }
    
    @Deprecated
    public FloatListEntry(String fieldName, Float value, String resetButtonKey, Supplier<Float> defaultValue, Consumer<Float> saveConsumer) {
        super(fieldName, value, resetButtonKey, defaultValue);
        this.minimum = -Float.MAX_VALUE;
        this.maximum = Float.MAX_VALUE;
        this.saveConsumer = saveConsumer;
    }
    
    @Deprecated
    public FloatListEntry(String fieldName, Float value, String resetButtonKey, Supplier<Float> defaultValue, Consumer<Float> saveConsumer, Supplier<Optional<String[]>> tooltipSupplier) {
        this(fieldName, value, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, false);
    }
    
    public FloatListEntry(String fieldName, Float value, String resetButtonKey, Supplier<Float> defaultValue, Consumer<Float> saveConsumer, Supplier<Optional<String[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, value, resetButtonKey, defaultValue, tooltipSupplier, requiresRestart);
        this.minimum = -Float.MAX_VALUE;
        this.maximum = Float.MAX_VALUE;
        this.saveConsumer = saveConsumer;
    }
    
    @Override
    protected String stripAddText(String s) {
        return stripCharacters.apply(s);
    }
    
    @Override
    protected void textFieldPreRender(GuiTextField widget) {
        try {
            double i = Float.valueOf(textFieldWidget.getText());
            if (i < minimum || i > maximum)
                widget.setTextColor(16733525);
            else
                widget.setTextColor(14737632);
        } catch (NumberFormatException ex) {
            widget.setTextColor(16733525);
        }
    }
    
    @Override
    protected boolean isMatchDefault(String text) {
        return getDefaultValue().isPresent() ? text.equals(defaultValue.get().toString()) : false;
    }
    
    public FloatListEntry setMinimum(float minimum) {
        this.minimum = minimum;
        return this;
    }
    
    public FloatListEntry setMaximum(float maximum) {
        this.maximum = maximum;
        return this;
    }
    
    @Override
    public void save() {
        if (saveConsumer != null)
            saveConsumer.accept(getValue());
    }
    
    @Override
    public Float getValue() {
        try {
            return Float.valueOf(textFieldWidget.getText());
        } catch (Exception e) {
            return 0f;
        }
    }
    
    @Override
    public Optional<String> getError() {
        try {
            float i = Float.valueOf(textFieldWidget.getText());
            if (i > maximum)
                return Optional.of(I18n.format("text.cloth-config.error.too_large", maximum));
            else if (i < minimum)
                return Optional.of(I18n.format("text.cloth-config.error.too_small", minimum));
        } catch (NumberFormatException ex) {
            return Optional.of(I18n.format("text.cloth-config.error.not_valid_number_float"));
        }
        return super.getError();
    }
}
