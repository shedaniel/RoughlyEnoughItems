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
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompositeOption<T> {
    private final String id;
    private final Component name;
    private final Component description;
    private final Function<ConfigObjectImpl, T> bind;
    private final BiConsumer<ConfigObjectImpl, T> save;
    @Nullable
    private ConfigPreviewer<T> previewer;
    @Nullable
    private Supplier<T> defaultValue = null;
    private OptionValueEntry<T> entry = OptionValueEntry.noOp();
    private boolean requiresLevel = false;
    @Nullable
    private String optionNameHighlight = null;
    @Nullable
    private String optionDescriptionHighlight = null;
    
    public CompositeOption(String id, Component name, Component description, Function<ConfigObjectImpl, T> bind, BiConsumer<ConfigObjectImpl, T> save) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.bind = bind;
        this.save = save;
    }
    
    public CompositeOption<T> entry(OptionValueEntry<T> entry) {
        this.entry = entry;
        return this;
    }
    
    public CompositeOption<Boolean> ofBoolean(Component falseText, Component trueText) {
        return ((CompositeOption<Boolean>) this).entry(OptionValueEntry.ofBoolean(falseText, trueText));
    }
    
    public CompositeOption<Boolean> trueFalse() {
        return ((CompositeOption<Boolean>) this).entry(OptionValueEntry.trueFalse());
    }
    
    public CompositeOption<Boolean> enabledDisabled() {
        return ((CompositeOption<Boolean>) this).entry(OptionValueEntry.enabledDisabled());
    }
    
    public CompositeOption<ModifierKeyCode> keybind() {
        return ((CompositeOption<ModifierKeyCode>) this).entry(OptionValueEntry.keybind());
    }
    
    public CompositeOption<String> string() {
        return ((CompositeOption<String>) this).entry(OptionValueEntry.string());
    }
    
    public CompositeOption<T> enumOptions(T... entry) {
        return this.entry(OptionValueEntry.enumOptions(entry));
    }
    
    public CompositeOption<T> options(T... entry) {
        return this.entry(OptionValueEntry.options(entry));
    }
    
    public CompositeOption<T> configure(OptionValueEntry.Configurator<T> configurator) {
        return this.entry(OptionValueEntry.configure(configurator));
    }
    
    public CompositeOption<T> details(OptionValueEntry.Configurator<T> configurator) {
        return this.entry(OptionValueEntry.details(configurator));
    }
    
    public CompositeOption<T> reload(OptionValueEntry.Configurator<T> configurator) {
        return this.entry(OptionValueEntry.reload(configurator));
    }
    
    public CompositeOption<T> requiresLevel() {
        this.requiresLevel = true;
        return this;
    }
    
    public void setOptionNameHighlight(@Nullable String optionNameHighlight) {
        this.optionNameHighlight = optionNameHighlight;
    }
    
    public void setOptionDescriptionHighlight(@Nullable String optionDescriptionHighlight) {
        this.optionDescriptionHighlight = optionDescriptionHighlight;
    }
    
    public boolean isRequiresLevel() {
        return requiresLevel;
    }
    
    @Nullable
    public String getOptionNameHighlight() {
        return optionNameHighlight;
    }
    
    @Nullable
    public String getOptionDescriptionHighlight() {
        return optionDescriptionHighlight;
    }
    
    public CompositeOption<T> previewer(ConfigPreviewer<T> previewer) {
        this.previewer = previewer;
        return this;
    }
    
    public CompositeOption<T> defaultValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public String getId() {
        return id;
    }
    
    public Component getName() {
        return name;
    }
    
    public Component getDescription() {
        return description;
    }
    
    public Function<ConfigObjectImpl, T> getBind() {
        return bind;
    }
    
    public BiConsumer<ConfigObjectImpl, T> getSave() {
        return save;
    }
    
    public OptionValueEntry<T> getEntry() {
        return entry;
    }
    
    @Nullable
    public ConfigPreviewer<T> getPreviewer() {
        return previewer;
    }
    
    public boolean hasPreview() {
        return previewer != null;
    }
    
    @Nullable
    public T getDefaultValue() {
        if (defaultValue == null) return null;
        return defaultValue.get();
    }
    
    public CompositeOption<T> copy() {
        CompositeOption<T> option = new CompositeOption<>(id, name, description, bind, save);
        option.entry = entry;
        option.previewer = previewer;
        option.defaultValue = defaultValue;
        option.requiresLevel = requiresLevel;
        return option;
    }
}
