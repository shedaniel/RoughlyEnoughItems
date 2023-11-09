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

package me.shedaniel.rei.impl.client.gui.config.components;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.gui.config.ConfigAccess;
import me.shedaniel.rei.impl.client.gui.config.options.CompositeOption;
import me.shedaniel.rei.impl.client.gui.config.options.OptionCategory;
import me.shedaniel.rei.impl.client.gui.config.options.OptionGroup;
import me.shedaniel.rei.impl.client.gui.widget.ListWidget;
import me.shedaniel.rei.impl.client.gui.widget.ScrollableViewWidget;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget;
import me.shedaniel.rei.impl.common.util.RectangleUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ConfigSearchListWidget {
    public static WidgetWithBounds create(ConfigAccess access, List<OptionCategory> categories, TextField textField, Rectangle bounds) {
        Mutable<WidgetWithBounds> list = new MutableObject<>(null);
        Consumer<String> responder = string -> {
            Collection<ConfigSearchWidget.SearchResult> results = ConfigSearchWidget.matchResult(categories, string);
            list.setValue(createList(access, results, string, bounds));
        };
        ((TextFieldWidget) textField).setResponder(responder);
        responder.accept(textField.getText());
        
        return ScrollableViewWidget.create(bounds, Widgets.delegateWithBounds(list::getValue), true);
    }
    
    private static WidgetWithBounds createList(ConfigAccess access, Collection<ConfigSearchWidget.SearchResult> results, String searchTerm, Rectangle bounds) {
        return ListWidget.builderOfWidgets(RectangleUtils.inset(bounds, 6, 6),
                        collectResultWidgets(access, results, searchTerm, bounds))
                .gap(7)
                .calculateTotalHeightDynamically(true)
                .build()
                .withPadding(0, 5);
    }
    
    private static List<WidgetWithBounds> collectResultWidgets(ConfigAccess access, Collection<ConfigSearchWidget.SearchResult> results, String searchTerm, Rectangle bounds) {
        List<ConfigSearchWidget.SearchResult> collapsedResults = new ArrayList<>();
        for (ConfigSearchWidget.SearchResult result : results) {
            if (result instanceof ConfigSearchWidget.IndividualResult individualResult) {
                int lastMatchGroup = -1;
                for (int i = 0; i < collapsedResults.size(); i++) {
                    ConfigSearchWidget.SearchResult prev = collapsedResults.get(i);
                    if (prev instanceof ConfigSearchWidget.IndividualResult prevInd && prevInd.group().getGroupName().getString().equals(individualResult.group().getGroupName().getString())) {
                        lastMatchGroup = i;
                    }
                }
                if (lastMatchGroup == -1) {
                    collapsedResults.add(result);
                } else {
                    collapsedResults.add(lastMatchGroup + 1, result);
                }
            } else {
                collapsedResults.add(result);
            }
        }
        
        List<WidgetWithBounds> widgets = new ArrayList<>();
        ConfigSearchWidget.SearchResult last = null;
        List<CompositeOption<?>> merge = null;
        for (ConfigSearchWidget.SearchResult result : collapsedResults) {
            if (last instanceof ConfigSearchWidget.IndividualResult lastInd && result instanceof ConfigSearchWidget.IndividualResult currInd) {
                if (lastInd.group().getGroupName().getString().equals(currInd.group().getGroupName().getString())) {
                    if (merge != null) {
                        merge.add(((ConfigSearchWidget.IndividualResult) currInd.decompose(searchTerm)).option());
                    } else {
                        merge = new ArrayList<>(List.of(((ConfigSearchWidget.IndividualResult) lastInd.decompose(searchTerm)).option(),
                                ((ConfigSearchWidget.IndividualResult) currInd.decompose(searchTerm)).option()));
                    }
                    
                    last = result;
                    continue;
                }
            }
            
            if (last != null) {
                // Commit last
                if (merge != null) {
                    OptionGroup group = ((ConfigSearchWidget.IndividualResult) last).group().copy();
                    group.getOptions().clear();
                    group.getOptions().addAll(merge);
                    merge = null;
                    widgets.add(createSearchResult(access, group, bounds.width - 12 - 6));
                } else {
                    widgets.add(createSearchResult(access, last.decompose(searchTerm), bounds.width - 12 - 6));
                }
            }
            last = result;
        }
        
        if (last != null) {
            // Commit last
            if (merge != null) {
                OptionGroup group = ((ConfigSearchWidget.IndividualResult) last).group().copy();
                group.getOptions().clear();
                group.getOptions().addAll(merge);
                widgets.add(createSearchResult(access, group, bounds.width - 12 - 6));
            } else {
                widgets.add(createSearchResult(access, last.decompose(searchTerm), bounds.width - 12 - 6));
            }
        }
        
        return widgets;
    }
    
    private static WidgetWithBounds createSearchResult(ConfigAccess access, Object result, int width) {
        if (result instanceof OptionCategory category) return Widgets.noOp();
        if (result instanceof OptionGroup group) {
            return ConfigGroupWidget.create(access, group, width, false);
        }
        if (result instanceof ConfigSearchWidget.IndividualResult individualResult) {
            OptionGroup group = individualResult.group().copy();
            group.getOptions().clear();
            group.getOptions().add(individualResult.option());
            return ConfigGroupWidget.create(access, group, width, false);
        }
        return Widgets.noOp();
    }
}
