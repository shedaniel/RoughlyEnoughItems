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

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.config.ConfigAccess;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.literal;
import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public interface OptionValueEntry<T> {
    Component getOption(T value);
    
    static <T> OptionValueEntry<T> noOp() {
        return value -> literal(value.toString());
    }
    
    static OptionValueEntry.Selection<Boolean> ofBoolean(Component falseText, Component trueText) {
        return new Selection<>() {
            @Override
            public List<Boolean> getOptions() {
                return List.of(false, true);
            }
            
            @Override
            public Component getOption(Boolean value) {
                return value ? trueText : falseText;
            }
        };
    }
    
    static OptionValueEntry.Selection<Boolean> trueFalse() {
        return ofBoolean(translatable("config.rei.value.trueFalse.false"),
                translatable("config.rei.value.trueFalse.true"));
    }
    
    static OptionValueEntry.Selection<Boolean> enabledDisabled() {
        return ofBoolean(translatable("config.rei.value.enabledDisabled.false"),
                translatable("config.rei.value.enabledDisabled.true"));
    }
    
    static <T> OptionValueEntry.Selection<T> enumOptions(T... array) {
        Class<T> type = (Class<T>) array.getClass().getComponentType();
        Object[] constants = type.getEnumConstants();
        return new Selection<>() {
            @Override
            public List<T> getOptions() {
                return CollectionUtils.map(constants, type::cast);
            }
            
            @Override
            public Component getOption(T value) {
                return literal(value.toString());
            }
        };
    }
    
    static <T> OptionValueEntry<T> options(T... options) {
        return new Selection<>() {
            @Override
            public List<T> getOptions() {
                return Arrays.asList(options);
            }
            
            @Override
            public Component getOption(T value) {
                return literal(value.toString());
            }
        };
    }
    
    static <T> OptionValueEntry.Configure<T> configure(Configurator<T> configurator) {
        return new Configure<>() {
            @Override
            public void configure(ConfigAccess access, CompositeOption<T> option, Runnable onClose) {
                configurator.configure(access, option, onClose);
            }
            
            @Override
            public Component getOption(T value) {
                return translatable("config.rei.texts.configure");
            }
        };
    }
    
    static <T> OptionValueEntry.Configure<T> details(Configurator<T> configurator) {
        return new Configure<>() {
            @Override
            public void configure(ConfigAccess access, CompositeOption<T> option, Runnable onClose) {
                configurator.configure(access, option, onClose);
            }
            
            @Override
            public Component getOption(T value) {
                return translatable("config.rei.texts.details");
            }
        };
    }
    
    static <T> OptionValueEntry.Configure<T> reload(Configurator<T> configurator) {
        return new Configure<>() {
            @Override
            public void configure(ConfigAccess access, CompositeOption<T> option, Runnable onClose) {
                configurator.configure(access, option, onClose);
            }
            
            @Override
            public Component getOption(T value) {
                return translatable("config.rei.texts.reload");
            }
        };
    }
    
    static OptionValueEntry<ModifierKeyCode> keybind() {
        return ModifierKeyCode::getLocalizedName;
    }
    
    static OptionValueEntry.String string() {
        return ConfigUtils::literal;
    }
    
    interface Configurator<T> {
        void configure(ConfigAccess access, CompositeOption<T> option, Runnable onClose);
    }
    
    interface Configure<T> extends OptionValueEntry<T>, Configurator<T> {
    }
    
    interface Selection<T> extends OptionValueEntry<T> {
        List<T> getOptions();
        
        default Selection<T> overrideText(Function<T, Component> textFunction) {
            return new Selection<>() {
                @Override
                public List<T> getOptions() {
                    return Selection.this.getOptions();
                }
                
                @Override
                public Component getOption(T value) {
                    return textFunction.apply(value);
                }
            };
        }
    }
    
    interface String extends OptionValueEntry<java.lang.String> {
    }
}
