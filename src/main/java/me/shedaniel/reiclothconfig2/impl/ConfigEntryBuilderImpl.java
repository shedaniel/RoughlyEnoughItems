package me.shedaniel.reiclothconfig2.impl;

import me.shedaniel.reiclothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.reiclothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.reiclothconfig2.impl.builders.*;

import java.util.List;
import java.util.UUID;

public class ConfigEntryBuilderImpl implements ConfigEntryBuilder {
    
    private String resetButtonKey = "text.cloth-config.reset_value";
    
    private ConfigEntryBuilderImpl() {
    }
    
    public static ConfigEntryBuilderImpl create() {
        return new ConfigEntryBuilderImpl();
    }
    
    @Override
    public String getResetButtonKey() {
        return resetButtonKey;
    }
    
    @Override
    public ConfigEntryBuilder setResetButtonKey(String resetButtonKey) {
        this.resetButtonKey = resetButtonKey;
        return this;
    }
    
    @Override
    public IntListBuilder startIntList(String fieldNameKey, List<Integer> value) {
        return new IntListBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public LongListBuilder startLongList(String fieldNameKey, List<Long> value) {
        return new LongListBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public FloatListBuilder startFloatList(String fieldNameKey, List<Float> value) {
        return new FloatListBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public DoubleListBuilder startDoubleList(String fieldNameKey, List<Double> value) {
        return new DoubleListBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public StringListBuilder startStrList(String fieldNameKey, List<String> value) {
        return new StringListBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public SubCategoryBuilder startSubCategory(String fieldNameKey) {
        return new SubCategoryBuilder(resetButtonKey, fieldNameKey);
    }
    
    @Override
    public SubCategoryBuilder startSubCategory(String fieldNameKey, List<AbstractConfigListEntry> entries) {
        SubCategoryBuilder builder = new SubCategoryBuilder(resetButtonKey, fieldNameKey);
        builder.addAll(entries);
        return builder;
    }
    
    @Override
    public BooleanToggleBuilder startBooleanToggle(String fieldNameKey, boolean value) {
        return new BooleanToggleBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public TextFieldBuilder startTextField(String fieldNameKey, String value) {
        return new TextFieldBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public TextDescriptionBuilder startTextDescription(String value) {
        return new TextDescriptionBuilder(resetButtonKey, UUID.randomUUID().toString(), value);
    }
    
    @Override
    public <T extends Enum<?>> EnumSelectorBuilder<T> startEnumSelector(String fieldNameKey, Class<T> clazz, T value) {
        return new EnumSelectorBuilder<T>(resetButtonKey, fieldNameKey, clazz, value);
    }
    
    @Override
    public IntFieldBuilder startIntField(String fieldNameKey, int value) {
        return new IntFieldBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public LongFieldBuilder startLongField(String fieldNameKey, long value) {
        return new LongFieldBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public FloatFieldBuilder startFloatField(String fieldNameKey, float value) {
        return new FloatFieldBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public DoubleFieldBuilder startDoubleField(String fieldNameKey, double value) {
        return new DoubleFieldBuilder(resetButtonKey, fieldNameKey, value);
    }
    
    @Override
    public IntSliderBuilder startIntSlider(String fieldNameKey, int value, int min, int max) {
        return new IntSliderBuilder(resetButtonKey, fieldNameKey, value, min, max);
    }
    
    @Override
    public LongSliderBuilder startLongSlider(String fieldNameKey, long value, long min, long max) {
        return new LongSliderBuilder(resetButtonKey, fieldNameKey, value, min, max);
    }
    
}
