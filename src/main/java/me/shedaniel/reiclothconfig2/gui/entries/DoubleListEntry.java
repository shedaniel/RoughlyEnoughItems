package me.shedaniel.reiclothconfig2.gui.entries;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DoubleListEntry extends TextFieldListEntry<Double> {
    
    private static Function<String, String> stripCharacters = s -> {
        StringBuilder stringBuilder_1 = new StringBuilder();
        char[] var2 = s.toCharArray();
        int var3 = var2.length;
        
        for(int var4 = 0; var4 < var3; ++var4)
            if (Character.isDigit(var2[var4]) || var2[var4] == '-' || var2[var4] == '.')
                stringBuilder_1.append(var2[var4]);
        
        return stringBuilder_1.toString();
    };
    private double minimum, maximum;
    private Consumer<Double> saveConsumer;
    
    @Deprecated
    public DoubleListEntry(String fieldName, Double value, Consumer<Double> saveConsumer) {
        this(fieldName, value, "text.cloth-config.reset_value", null, saveConsumer);
    }
    
    @Deprecated
    public DoubleListEntry(String fieldName, Double value, String resetButtonKey, Supplier<Double> defaultValue, Consumer<Double> saveConsumer) {
        super(fieldName, value, resetButtonKey, defaultValue);
        this.minimum = -Double.MAX_VALUE;
        this.maximum = Double.MAX_VALUE;
        this.saveConsumer = saveConsumer;
    }
    
    @Deprecated
    public DoubleListEntry(String fieldName, Double value, String resetButtonKey, Supplier<Double> defaultValue, Consumer<Double> saveConsumer, Supplier<Optional<String[]>> tooltipSupplier) {
        this(fieldName, value, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, false);
    }
    
    @Deprecated
    public DoubleListEntry(String fieldName, Double value, String resetButtonKey, Supplier<Double> defaultValue, Consumer<Double> saveConsumer, Supplier<Optional<String[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, value, resetButtonKey, defaultValue, tooltipSupplier, requiresRestart);
        this.minimum = -Double.MAX_VALUE;
        this.maximum = Double.MAX_VALUE;
        this.saveConsumer = saveConsumer;
    }
    
    @Override
    protected String stripAddText(String s) {
        return stripCharacters.apply(s);
    }
    
    @Override
    protected void textFieldPreRender(GuiTextField widget) {
        try {
            double i = Double.valueOf(textFieldWidget.getText());
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
    
    @Override
    public void save() {
        if (saveConsumer != null)
            saveConsumer.accept(getValue());
    }
    
    public DoubleListEntry setMinimum(double minimum) {
        this.minimum = minimum;
        return this;
    }
    
    public DoubleListEntry setMaximum(double maximum) {
        this.maximum = maximum;
        return this;
    }
    
    @Override
    public Double getValue() {
        try {
            return Double.valueOf(textFieldWidget.getText());
        } catch (Exception e) {
            return 0d;
        }
    }
    
    @Override
    public Optional<String> getError() {
        try {
            double i = Double.valueOf(textFieldWidget.getText());
            if (i > maximum)
                return Optional.of(I18n.format("text.cloth-config.error.too_large", maximum));
            else if (i < minimum)
                return Optional.of(I18n.format("text.cloth-config.error.too_small", minimum));
        } catch (NumberFormatException ex) {
            return Optional.of(I18n.format("text.cloth-config.error.not_valid_number_double"));
        }
        return super.getError();
    }
}
