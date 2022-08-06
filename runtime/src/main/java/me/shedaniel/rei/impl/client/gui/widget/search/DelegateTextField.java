/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client.gui.widget.search;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface DelegateTextField extends TextField {
    TextField delegateTextField();
    
    @Override
    default void setFocusedResponder(BooleanConsumer responder) {
        delegateTextField().setFocusedResponder(responder);
    }
    
    @Override
    @Nullable
    default String getSuggestion() {
        return delegateTextField().getSuggestion();
    }
    
    @Override
    default void setSuggestion(@Nullable String suggestion) {
        delegateTextField().setSuggestion(suggestion);
    }
    
    @Override
    default void setBorderColorProvider(BorderColorProvider borderColorProvider) {
        delegateTextField().setBorderColorProvider(borderColorProvider);
    }
    
    @Override
    default void setFormatter(TextFormatter formatter) {
        delegateTextField().setFormatter(formatter);
    }
    
    @Override
    default void setSuggestionRenderer(SuggestionRenderer renderer) {
        delegateTextField().setSuggestionRenderer(renderer);
    }
    
    @Override
    default void setTextTransformer(Function<String, String> textTransformer) {
        delegateTextField().setTextTransformer(textTransformer);
    }
    
    @Override
    default void setResponder(Consumer<String> responder) {
        delegateTextField().setResponder(responder);
    }
    
    @Override
    default String getText() {
        return delegateTextField().getText();
    }
    
    @Override
    default void setText(String text) {
        delegateTextField().setText(text);
    }
    
    @Override
    default String getSelectedText() {
        return delegateTextField().getSelectedText();
    }
    
    @Override
    default void addText(String text) {
        delegateTextField().addText(text);
    }
    
    @Override
    default void moveCursorTo(int cursor) {
        delegateTextField().moveCursorTo(cursor);
    }
    
    @Override
    default void moveCursorToStart() {
        delegateTextField().moveCursorToStart();
    }
    
    @Override
    default void moveCursorToEnd() {
        delegateTextField().moveCursorToEnd();
    }
    
    @Override
    default int getMaxLength() {
        return delegateTextField().getMaxLength();
    }
    
    @Override
    default void setMaxLength(int maxLength) {
        delegateTextField().setMaxLength(maxLength);
    }
    
    @Override
    default int getCursor() {
        return delegateTextField().getCursor();
    }
    
    @Override
    default void setCursorPosition(int cursor) {
        delegateTextField().setCursorPosition(cursor);
    }
    
    @Override
    default boolean hasBorder() {
        return delegateTextField().hasBorder();
    }
    
    @Override
    default void setHasBorder(boolean hasBorder) {
        delegateTextField().setHasBorder(hasBorder);
    }
    
    @Override
    default void setEditableColor(int editableColor) {
        delegateTextField().setEditableColor(editableColor);
    }
    
    @Override
    default void setNotEditableColor(int notEditableColor) {
        delegateTextField().setNotEditableColor(notEditableColor);
    }
    
    @Override
    default boolean isFocused() {
        return delegateTextField().isFocused();
    }
    
    @Override
    default void setFocused(boolean focused) {
        delegateTextField().setFocused(focused);
    }
    
    @Override
    default void tick() {
        delegateTextField().tick();
    }
}
