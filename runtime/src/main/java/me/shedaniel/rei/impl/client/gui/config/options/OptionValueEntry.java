/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.gui.config.options;

import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.network.chat.Component;

import java.util.List;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public interface OptionValueEntry<T> {
    static <T> OptionValueEntry<T> noOp() {
        return new OptionValueEntry<T>() {
        };
    }
    
    static OptionValueEntry<Boolean> ofBoolean(Component falseText, Component trueText) {
        return new Selection<Boolean>() {
            @Override
            public List<Component> getOptions() {
                return List.of(falseText, trueText);
            }
            
            @Override
            public Component getOption(Boolean value) {
                return value ? trueText : falseText;
            }
        };
    }
    
    static OptionValueEntry<Boolean> trueFalse() {
        return ofBoolean(translatable("config.rei.value.trueFalse.false"),
                translatable("config.rei.value.trueFalse.true"));
    }
    
    static OptionValueEntry<Boolean> enabledDisabled() {
        return ofBoolean(translatable("config.rei.value.enabledDisabled.false"),
                translatable("config.rei.value.enabledDisabled.true"));
    }
    
    static <T> OptionValueEntry<T> enumOptions(T... array) {
        Class<?> type = array.getClass().getComponentType();
        Object[] constants = type.getEnumConstants();
        return new Selection<T>() {
            @Override
            public List<Component> getOptions() {
                return CollectionUtils.map(constants, o -> getOption((T) o));
            }
            
            @Override
            public Component getOption(T value) {
                return null;
            }
        };
    }
    
    interface Selection<T> extends OptionValueEntry<T> {
        List<Component> getOptions();
        
        Component getOption(T value);
    }
}
