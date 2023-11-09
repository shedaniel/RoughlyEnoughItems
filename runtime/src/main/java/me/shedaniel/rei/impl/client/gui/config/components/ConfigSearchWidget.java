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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Label;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.gui.config.REIConfigScreen;
import me.shedaniel.rei.impl.client.gui.config.options.CompositeOption;
import me.shedaniel.rei.impl.client.gui.config.options.OptionCategory;
import me.shedaniel.rei.impl.client.gui.config.options.OptionGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public class ConfigSearchWidget {
    public static WidgetWithBounds create(IntSupplier width) {
        Label label = Widgets.createLabel(new Point(21, 6), translatable("config.rei.texts.search_options"))
                .leftAligned();
        Font font = Minecraft.getInstance().font;
        Rectangle bounds = new Rectangle(0, 0, label.getBounds().getMaxX(), 7 * 3);
        return Widgets.concatWithBounds(
                bounds,
                new Widget() {
                    @Override
                    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                        boolean hovering = new Rectangle(-1, -1, width.getAsInt() + 2, 21).contains(mouseX, mouseY);
                        for (Widget widget : List.of(Widgets.createFilledRectangle(new Rectangle(1, 1, width.getAsInt() - 2, 18), hovering ? 0x50FFFFFF : 0x25FFFFFF),
                                Widgets.createFilledRectangle(new Rectangle(-1, -1, width.getAsInt() + 2, 1), hovering ? 0x90FFFFFF : 0x45FFFFFF),
                                Widgets.createFilledRectangle(new Rectangle(-1, 20, width.getAsInt() + 2, 1), hovering ? 0x90FFFFFF : 0x45FFFFFF),
                                Widgets.createFilledRectangle(new Rectangle(-1, 0, 1, 20), hovering ? 0x90FFFFFF : 0x45FFFFFF),
                                Widgets.createFilledRectangle(new Rectangle(width.getAsInt(), 0, 1, 20), hovering ? 0x90FFFFFF : 0x45FFFFFF))) {
                            widget.render(poses, mouseX, mouseY, delta);
                        }
                        label.setColor(hovering ? 0xFFE1E1E1 : 0xFFC0C0C0);
                    }
                    
                    @Override
                    public List<? extends GuiEventListener> children() {
                        return List.of();
                    }
                    
                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (new Rectangle(-1, -1, width.getAsInt() + 2, 21).contains(mouseX, mouseY)) {
                            Widgets.produceClickSound();
                            ((REIConfigScreen) Minecraft.getInstance().screen).setSearching(true);
                            return true;
                        }
                        
                        return false;
                    }
                },
                Widgets.withTranslate(label, 0, 0.5, 0),
                Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems:textures/gui/config/search_options.png"), new Rectangle(3, 3, 16, 16), 0, 0, 1, 1, 1, 1)
        
        );
    }
    
    public static WidgetWithBounds createTiny() {
        Rectangle bounds = new Rectangle(0, 0, 16, 16);
        return Widgets.withTooltip(Widgets.concatWithBounds(
                bounds,
                new Widget() {
                    @Override
                    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                        boolean hovering = new Rectangle(-1, -1, 18, 18).contains(mouseX, mouseY);
                        poses.pushPose();
                        poses.translate(-0.5, -0.5, 0);
                        for (Widget widget : List.of(Widgets.createFilledRectangle(new Rectangle(-1, -1, 18, 18), hovering ? 0x50FFFFFF : 0x25FFFFFF),
                                Widgets.createFilledRectangle(new Rectangle(-3, -3, 22, 1), hovering ? 0x90FFFFFF : 0x45FFFFFF),
                                Widgets.createFilledRectangle(new Rectangle(-3, 18, 22, 1), hovering ? 0x90FFFFFF : 0x45FFFFFF),
                                Widgets.createFilledRectangle(new Rectangle(-3, -2, 1, 20), hovering ? 0x90FFFFFF : 0x45FFFFFF),
                                Widgets.createFilledRectangle(new Rectangle(18, -2, 1, 20), hovering ? 0x90FFFFFF : 0x45FFFFFF))) {
                            widget.render(poses, mouseX, mouseY, delta);
                        }
                        poses.popPose();
                    }
                    
                    @Override
                    public List<? extends GuiEventListener> children() {
                        return List.of();
                    }
                    
                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (new Rectangle(-1, -1, 18, 18).contains(mouseX, mouseY)) {
                            Widgets.produceClickSound();
                            ((REIConfigScreen) Minecraft.getInstance().screen).setSearching(true);
                            return true;
                        }
                        
                        return false;
                    }
                },
                Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems:textures/gui/config/search_options.png"), bounds, 0, 0, 1, 1, 1, 1)
        
        ), translatable("config.rei.texts.search_options"));
    }
    
    public static Collection<SearchResult> matchResult(List<OptionCategory> categories, String searchTerm) {
        if (searchTerm.isBlank()) return Collections.emptyList();
        List<ScoredResult> scoredResults = collectSearchResults(categories, searchTerm);
        // Remove categories results for now, we might add it back later.
        scoredResults.removeIf(result -> result.result() instanceof CategoryResult);
        
        // Distinct them.
        Set<SearchResult> distinctResults = new LinkedHashSet<>();
        for (ScoredResult result : scoredResults) {
            if (result.score() >= 0.001F) {
                distinctResults.add(result.result());
            }
        }
        
        // Remove duplicates.
        Set<OptionCategory> visitedCategories = new HashSet<>();
        Set<OptionGroup> visitedGroups = new HashSet<>();
        Iterator<SearchResult> iterator = distinctResults.iterator();
        while (iterator.hasNext()) {
            SearchResult result = iterator.next();
            if (result instanceof CategoryResult categoryResult) {
                visitedCategories.add(categoryResult.category());
            } else if (result instanceof GroupResult groupResult) {
                if (visitedCategories.contains(((GroupResult) result).category)) {
                    iterator.remove();
                } else {
                    visitedGroups.add(groupResult.group());
                }
            } else if (result instanceof IndividualResult individualResult) {
                if (visitedCategories.contains(individualResult.category()) || visitedGroups.contains(individualResult.group())) {
                    iterator.remove();
                }
            }
        }
        
        return distinctResults;
    }
    
    private static List<ScoredResult> collectSearchResults(List<OptionCategory> categories, String searchTerm) {
        String lcSearchTerm = searchTerm.toLowerCase(Locale.ROOT);
        
        // Find all options that match.
        List<SearchResult> results = new ArrayList<>();
        List<SearchResult> fuzzyResults = new ArrayList<>();
        for (OptionCategory category : categories) {
            if (category.getKey().toLowerCase(Locale.ROOT).contains(lcSearchTerm)) {
                results.add(new CategoryResult(category, new MatchComposite(category.getKey(), MatchType.KEY)));
            } else {
                fuzzyResults.add(new CategoryResult(category, new MatchComposite(category.getKey(), MatchType.KEY)));
            }
            if (category.getName().getString().toLowerCase(Locale.ROOT).contains(lcSearchTerm)) {
                results.add(new CategoryResult(category, new MatchComposite(category.getName().getString(), MatchType.NAME)));
            } else {
                fuzzyResults.add(new CategoryResult(category, new MatchComposite(category.getName().getString(), MatchType.NAME)));
            }
            if (!category.getDescription().getString().endsWith(".desc") && category.getDescription().getString().toLowerCase(Locale.ROOT).contains(lcSearchTerm)) {
                results.add(new CategoryResult(category, new MatchComposite(category.getDescription().getString(), MatchType.DESCRIPTION)));
            } else {
                fuzzyResults.add(new CategoryResult(category, new MatchComposite(category.getDescription().getString(), MatchType.DESCRIPTION)));
            }
            
            for (OptionGroup group : category.getGroups()) {
                if (group.getId().toLowerCase(Locale.ROOT).contains(lcSearchTerm)) {
                    results.add(new GroupResult(category, group, new MatchComposite(group.getId(), MatchType.KEY)));
                } else {
                    fuzzyResults.add(new GroupResult(category, group, new MatchComposite(group.getId(), MatchType.KEY)));
                }
                if (group.getGroupName().getString().toLowerCase(Locale.ROOT).contains(lcSearchTerm)) {
                    results.add(new GroupResult(category, group, new MatchComposite(group.getGroupName().getString(), MatchType.NAME)));
                } else {
                    fuzzyResults.add(new GroupResult(category, group, new MatchComposite(group.getGroupName().getString(), MatchType.NAME)));
                }
                
                for (CompositeOption<?> option : group.getOptions()) {
                    if (option.getId().toLowerCase(Locale.ROOT).contains(lcSearchTerm)) {
                        results.add(new IndividualResult(category, group, option, new MatchComposite(option.getId(), MatchType.KEY)));
                    } else {
                        fuzzyResults.add(new IndividualResult(category, group, option, new MatchComposite(option.getId(), MatchType.KEY)));
                    }
                    if (option.getName().getString().toLowerCase(Locale.ROOT).contains(lcSearchTerm)) {
                        results.add(new IndividualResult(category, group, option, new MatchComposite(option.getName().getString(), MatchType.NAME)));
                    } else {
                        fuzzyResults.add(new IndividualResult(category, group, option, new MatchComposite(option.getName().getString(), MatchType.NAME)));
                    }
                    if (!option.getDescription().getString().endsWith(".desc") && option.getDescription().getString().toLowerCase(Locale.ROOT).contains(lcSearchTerm)) {
                        results.add(new IndividualResult(category, group, option, new MatchComposite(option.getDescription().getString(), MatchType.DESCRIPTION)));
                    } else {
                        fuzzyResults.add(new IndividualResult(category, group, option, new MatchComposite(option.getDescription().getString(), MatchType.DESCRIPTION)));
                    }
                }
            }
        }
        
        return Stream.concat(results.stream().map(result -> {
                    return new ScoredResult(result, similarity(result.matched().matched(), searchTerm) * result.matched().type().multiplier());
                }), fuzzyResults.stream().map(result -> {
                    return new ScoredResult(result, (float) Math.pow(similarity(result.matched().matched(), searchTerm), 1.5) * result.matched().type().multiplier() * 0.9F);
                }).filter(result -> result.score() > 0.5F)).sorted(Comparator.comparingDouble(ScoredResult::score).reversed())
                .collect(Collectors.toList());
    }
    
    public interface SearchResult {
        MatchComposite matched();
        
        Object decompose(String searchTerm);
    }
    
    public record ScoredResult(
            SearchResult result,
            float score
    ) {
    }
    
    public record IndividualResult(
            OptionCategory category,
            OptionGroup group,
            CompositeOption<?> option,
            MatchComposite matched
    ) implements SearchResult {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IndividualResult that = (IndividualResult) o;
            return Objects.equals(option, that.option);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(option);
        }
        
        @Override
        public Object decompose(String searchTerm) {
            CompositeOption<?> copied = option().copy();
            if (matched().type() == MatchType.NAME) copied.setOptionNameHighlight(searchTerm);
            else if (matched().type() == MatchType.DESCRIPTION) copied.setOptionDescriptionHighlight(searchTerm);
            return new IndividualResult(category(), group(), copied, matched());
        }
    }
    
    public record GroupResult(
            OptionCategory category,
            OptionGroup group,
            MatchComposite matched
    ) implements SearchResult {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupResult that = (GroupResult) o;
            return Objects.equals(group, that.group);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(group);
        }
        
        @Override
        public Object decompose(String searchTerm) {
            OptionGroup copied = this.group().copy();
            copied.setGroupNameHighlight(searchTerm);
            return copied;
        }
    }
    
    public record CategoryResult(
            OptionCategory category,
            MatchComposite matched
    ) implements SearchResult {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CategoryResult that = (CategoryResult) o;
            return Objects.equals(category, that.category);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(category);
        }
        
        @Override
        public Object decompose(String searchTerm) {
            return this.category();
        }
    }
    
    public enum MatchType {
        KEY(0.0005F),
        NAME(0.98F),
        DESCRIPTION(0.8F),
        ;
        
        private final float multiplier;
        
        MatchType(float multiplier) {
            this.multiplier = multiplier;
        }
        
        public float multiplier() {
            return multiplier;
        }
    }
    
    public record MatchComposite(
            String matched,
            MatchType type
    ) {
    }
    
    private static float similarity(String first, String second) {
        String firstLowerCase = first.toLowerCase(Locale.ROOT);
        String secondLowerCase = second.toLowerCase(Locale.ROOT);
        if (!Objects.equals(first, firstLowerCase) || !Objects.equals(second, secondLowerCase)) {
            return (innerSimilarity(first, second) + innerSimilarity(firstLowerCase, secondLowerCase)) / 2.0F;
        } else {
            return innerSimilarity(first, second);
        }
    }
    
    private static float innerSimilarity(String first, String second) {
        String longer = first;
        String shorter = second;
        if (first.length() < second.length()) {
            longer = second;
            shorter = first;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0F;
        } else {
            return (longerLength - editDistance(longer, shorter)) / ((float) longerLength);
        }
    }
    
    private static int editDistance(String s11, String s22) {
        int[] costs = new int[s22.length() + 1];
        for (int i = 0; i <= s11.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s22.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s11.charAt(i - 1) != s22.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s22.length()] = lastValue;
            }
        }
        return costs[s22.length()];
    }
}
