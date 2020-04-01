/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@ApiStatus.Internal
public class SearchArgument {
    
    private static final String SPACE = " ", EMPTY = "";
    private static final SearchArgument ALWAYS = new SearchArgument(ArgumentType.ALWAYS, "", true);
    private static List<Item> searchBlacklisted = Lists.newArrayList();
    private ArgumentType argumentType;
    private String text;
    public final Function<String, Boolean> INCLUDE = s -> s.contains(text);
    public final Function<String, Boolean> NOT_INCLUDE = s -> !s.contains(text);
    private boolean include;
    
    public SearchArgument(ArgumentType argumentType, String text, boolean include) {
        this(argumentType, text, include, true);
    }
    
    public SearchArgument(ArgumentType argumentType, String text, boolean include, boolean autoLowerCase) {
        this.argumentType = argumentType;
        this.text = autoLowerCase ? text.toLowerCase(Locale.ROOT) : text;
        this.include = include;
    }
    
    @ApiStatus.Internal
    public static List<SearchArgument.SearchArguments> processSearchTerm(String searchTerm) {
        List<SearchArgument.SearchArguments> searchArguments = Lists.newArrayList();
        for (String split : StringUtils.splitByWholeSeparatorPreserveAllTokens(searchTerm.toLowerCase(Locale.ROOT), "|")) {
            String[] terms = StringUtils.split(split);
            if (terms.length == 0)
                searchArguments.add(SearchArgument.SearchArguments.ALWAYS);
            else {
                SearchArgument[] arguments = new SearchArgument[terms.length];
                for (int i = 0; i < terms.length; i++) {
                    String term = terms[i];
                    if (term.startsWith("-@") || term.startsWith("@-")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.MOD, term.substring(2), false);
                    } else if (term.startsWith("@")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.MOD, term.substring(1), true);
                    } else if (term.startsWith("-$") || term.startsWith("$-")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TAG, term.substring(2), false);
                    } else if (term.startsWith("$")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TAG, term.substring(1), true);
                    } else if (term.startsWith("-#") || term.startsWith("#-")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, term.substring(2), false);
                    } else if (term.startsWith("#")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, term.substring(1), true);
                    } else if (term.startsWith("-")) {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TEXT, term.substring(1), false);
                    } else {
                        arguments[i] = new SearchArgument(SearchArgument.ArgumentType.TEXT, term, true);
                    }
                }
                searchArguments.add(new SearchArgument.SearchArguments(arguments));
            }
        }
        return searchArguments;
    }
    
    @ApiStatus.Internal
    public static boolean canSearchTermsBeAppliedTo(EntryStack stack, List<SearchArgument.SearchArguments> searchArguments) {
        if (searchArguments.isEmpty())
            return true;
        MinecraftClient minecraft = MinecraftClient.getInstance();
        String mod = null;
        String modName = null;
        String name = null;
        String tooltip = null;
        String[] tags = null;
        for (SearchArgument.SearchArguments arguments : searchArguments) {
            boolean applicable = true;
            for (SearchArgument argument : arguments.getArguments()) {
                if (argument.getArgumentType() == SearchArgument.ArgumentType.ALWAYS)
                    return true;
                else if (argument.getArgumentType() == SearchArgument.ArgumentType.MOD) {
                    if (mod == null)
                        mod = stack.getIdentifier().map(Identifier::getNamespace).orElse("").replace(SPACE, EMPTY).toLowerCase(Locale.ROOT);
                    if (mod != null && !mod.isEmpty()) {
                        if (argument.getFunction(!argument.isInclude()).apply(mod)) {
                            if (modName == null)
                                modName = ClientHelper.getInstance().getModFromModId(mod).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT);
                            if (modName == null || modName.isEmpty() || argument.getFunction(!argument.isInclude()).apply(modName)) {
                                applicable = false;
                                break;
                            }
                            break;
                        }
                    }
                } else if (argument.getArgumentType() == SearchArgument.ArgumentType.TEXT) {
                    if (name == null)
                        name = SearchArgument.tryGetEntryStackName(stack).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT);
                    if (name != null && !name.isEmpty() && argument.getFunction(!argument.isInclude()).apply(name)) {
                        applicable = false;
                        break;
                    }
                } else if (argument.getArgumentType() == SearchArgument.ArgumentType.TOOLTIP) {
                    if (tooltip == null)
                        tooltip = SearchArgument.tryGetEntryStackTooltip(stack).replace(SPACE, EMPTY).toLowerCase(Locale.ROOT);
                    if (tooltip != null && !tooltip.isEmpty() && argument.getFunction(!argument.isInclude()).apply(tooltip)) {
                        applicable = false;
                        break;
                    }
                } else if (argument.getArgumentType() == SearchArgument.ArgumentType.TAG) {
                    if (tags == null) {
                        if (stack.getType() == EntryStack.Type.ITEM) {
                            Identifier[] tagsFor = minecraft.getNetworkHandler().getTagManager().items().getTagsFor(stack.getItem()).toArray(new Identifier[0]);
                            tags = new String[tagsFor.length];
                            for (int i = 0; i < tagsFor.length; i++)
                                tags[i] = tagsFor[i].toString();
                        } else if (stack.getType() == EntryStack.Type.FLUID) {
                            Identifier[] tagsFor = minecraft.getNetworkHandler().getTagManager().fluids().getTagsFor(stack.getFluid()).toArray(new Identifier[0]);
                            tags = new String[tagsFor.length];
                            for (int i = 0; i < tagsFor.length; i++)
                                tags[i] = tagsFor[i].toString();
                        } else
                            tags = new String[0];
                    }
                    if (tags != null && tags.length > 0) {
                        boolean a = false;
                        for (String tag : tags)
                            if (argument.getFunction(argument.isInclude()).apply(tag))
                                a = true;
                        if (!a) {
                            applicable = false;
                            break;
                        }
                    } else {
                        applicable = false;
                        break;
                    }
                }
            }
            if (applicable)
                return true;
        }
        return false;
    }
    
    public static String tryGetEntryStackName(EntryStack stack) {
        if (stack.getType() == EntryStack.Type.ITEM)
            return tryGetItemStackName(stack.getItemStack());
        else if (stack.getType() == EntryStack.Type.FLUID)
            return tryGetFluidName(stack.getFluid());
        QueuedTooltip tooltip = stack.getTooltip(PointHelper.getMouseX(), PointHelper.getMouseY());
        if (tooltip != null)
            return tooltip.getText().isEmpty() ? "" : tooltip.getText().get(0);
        return "";
    }
    
    public static String tryGetEntryStackNameNoFormatting(EntryStack stack) {
        if (stack.getType() == EntryStack.Type.ITEM)
            return tryGetItemStackNameNoFormatting(stack.getItemStack());
        else if (stack.getType() == EntryStack.Type.FLUID)
            return tryGetFluidName(stack.getFluid());
        QueuedTooltip tooltip = stack.getTooltip(PointHelper.getMouseX(), PointHelper.getMouseY());
        if (tooltip != null)
            return tooltip.getText().isEmpty() ? "" : tooltip.getText().get(0);
        return "";
    }
    
    public static String tryGetEntryStackTooltip(EntryStack stack) {
        QueuedTooltip tooltip = stack.getTooltip(0, 0);
        if (tooltip != null)
            return CollectionUtils.joinToString(tooltip.getText(), "\n");
        return "";
    }
    
    public static String tryGetFluidName(Fluid fluid) {
        Identifier id = Registry.FLUID.getId(fluid);
        if (I18n.hasTranslation("block." + id.toString().replaceFirst(":", ".")))
            return I18n.translate("block." + id.toString().replaceFirst(":", "."));
        return CollectionUtils.mapAndJoinToString(id.getPath().split("_"), StringUtils::capitalize, " ");
    }
    
    public static List<String> tryGetItemStackToolTip(ItemStack itemStack, boolean careAboutAdvanced) {
        if (!searchBlacklisted.contains(itemStack.getItem()))
            try {
                return CollectionUtils.map(itemStack.getTooltip(MinecraftClient.getInstance().player, MinecraftClient.getInstance().options.advancedItemTooltips && careAboutAdvanced ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL), Text::asFormattedString);
            } catch (Throwable e) {
                e.printStackTrace();
                searchBlacklisted.add(itemStack.getItem());
            }
        return Collections.singletonList(tryGetItemStackName(itemStack));
    }
    
    public static String tryGetItemStackName(ItemStack stack) {
        if (!searchBlacklisted.contains(stack.getItem()))
            try {
                return stack.getName().asFormattedString();
            } catch (Throwable e) {
                e.printStackTrace();
                searchBlacklisted.add(stack.getItem());
            }
        try {
            return I18n.translate("item." + Registry.ITEM.getId(stack.getItem()).toString().replace(":", "."));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "ERROR";
    }
    
    public static String tryGetItemStackNameNoFormatting(ItemStack stack) {
        if (!searchBlacklisted.contains(stack.getItem()))
            try {
                return stack.getName().asString();
            } catch (Throwable e) {
                e.printStackTrace();
                searchBlacklisted.add(stack.getItem());
            }
        try {
            return I18n.translate("item." + Registry.ITEM.getId(stack.getItem()).toString().replace(":", "."));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "ERROR";
    }
    
    public Function<String, Boolean> getFunction(boolean include) {
        return include ? INCLUDE : NOT_INCLUDE;
    }
    
    public ArgumentType getArgumentType() {
        return argumentType;
    }
    
    public String getText() {
        return text;
    }
    
    public boolean isInclude() {
        return include;
    }
    
    @Override
    public String toString() {
        return String.format("Argument[%s]: name = %s, include = %b", argumentType.name(), text, include);
    }
    
    public enum ArgumentType {
        TEXT,
        MOD,
        TOOLTIP,
        TAG,
        ALWAYS
    }
    
    public static class SearchArguments {
        public static final SearchArguments ALWAYS = new SearchArguments(new SearchArgument[]{SearchArgument.ALWAYS});
        private SearchArgument[] arguments;
        
        public SearchArguments(SearchArgument[] arguments) {
            this.arguments = arguments;
        }
        
        public SearchArgument[] getArguments() {
            return arguments;
        }
    }
    
}
