package me.shedaniel.reiclothconfig2.api;

import me.shedaniel.reiclothconfig2.impl.ConfigEntryBuilderImpl;
import me.shedaniel.reiclothconfig2.impl.builders.*;

import java.util.List;

public interface ConfigEntryBuilder {
    
    static ConfigEntryBuilder create() {
        return ConfigEntryBuilderImpl.create();
    }
    
    String getResetButtonKey();
    
    ConfigEntryBuilder setResetButtonKey(String resetButtonKey);
    
    IntListBuilder startIntList(String fieldNameKey, List<Integer> value);
    
    LongListBuilder startLongList(String fieldNameKey, List<Long> value);
    
    FloatListBuilder startFloatList(String fieldNameKey, List<Float> value);
    
    DoubleListBuilder startDoubleList(String fieldNameKey, List<Double> value);
    
    StringListBuilder startStrList(String fieldNameKey, List<String> value);
    
    SubCategoryBuilder startSubCategory(String fieldNameKey);
    
    SubCategoryBuilder startSubCategory(String fieldNameKey, List<AbstractConfigListEntry> entries);
    
    BooleanToggleBuilder startBooleanToggle(String fieldNameKey, boolean value);
    
    TextFieldBuilder startTextField(String fieldNameKey, String value);
    
    TextDescriptionBuilder startTextDescription(String value);
    
    <T extends Enum<?>> EnumSelectorBuilder<T> startEnumSelector(String fieldNameKey, Class<T> clazz, T value);
    
    IntFieldBuilder startIntField(String fieldNameKey, int value);
    
    LongFieldBuilder startLongField(String fieldNameKey, long value);
    
    FloatFieldBuilder startFloatField(String fieldNameKey, float value);
    
    DoubleFieldBuilder startDoubleField(String fieldNameKey, double value);
    
    IntSliderBuilder startIntSlider(String fieldNameKey, int value, int min, int max);
    
    LongSliderBuilder startLongSlider(String fieldNameKey, long value, long min, long max);
}
