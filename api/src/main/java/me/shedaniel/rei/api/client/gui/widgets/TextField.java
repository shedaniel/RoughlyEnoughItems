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

package me.shedaniel.rei.api.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.shedaniel.clothconfig2.api.TickableWidget;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

import static me.shedaniel.rei.api.client.gui.widgets.Widget.mouse;

public interface TextField extends TickableWidget {
    void setFormatter(TextFormatter formatter);
    
    TextFormatter getFormatter();
    
    void setSuggestionRenderer(SuggestionRenderer renderer);
    
    SuggestionRenderer getSuggestionRenderer();
    
    String getText();
    
    void setText(String text);
    
    String getSelectedText();
    
    void addText(String text);
    
    void moveCursorTo(int cursor);
    
    void moveCursorToStart();
    
    void moveCursorToEnd();
    
    int getMaxLength();
    
    void setMaxLength(int maxLength);
    
    int getCursor();
    
    void setCursorPosition(int cursor);
    
    boolean hasBorder();
    
    void setHasBorder(boolean hasBorder);
    
    void setEditableColor(int editableColor);
    
    void setNotEditableColor(int notEditableColor);
    
    boolean isFocused();
    
    void setFocused(boolean focused);
    
    void setResponder(Consumer<String> responder);
    
    void setFocusedResponder(BooleanConsumer responder);
    
    void setTextTransformer(Function<String, String> textTransformer);
    
    @Nullable
    String getSuggestion();
    
    void setSuggestion(@Nullable String suggestion);
    
    void setBorderColorProvider(BorderColorProvider borderColorProvider);
    
    WidgetWithBounds asWidget();
    
    interface TextFormatter {
        TextFormatter DEFAULT = (text, index) -> {
            return FormattedCharSequence.forward(text, Style.EMPTY);
        };
        
        FormattedCharSequence format(String text, int index);
    }
    
    interface SuggestionRenderer {
        void renderSuggestion(PoseStack matrices, int x, int y, int color);
    }
    
    interface BorderColorProvider {
        BorderColorProvider DEFAULT = textField -> textField.asWidget().containsMouse(mouse()) || textField.isFocused() ? 0xffffffff : 0xffa0a0a0;
        
        int getBorderColor(TextField textField);
    }
}
