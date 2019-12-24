/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.annotations.Internal;
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Deprecated
@Internal
public class SearchArgument {

    public static final SearchArgument ALWAYS = new SearchArgument(ArgumentType.ALWAYS, "", true);
    @Deprecated
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

    @Deprecated
    public static String tryGetEntryStackName(EntryStack stack) {
        if (stack.getType() == EntryStack.Type.ITEM)
            return tryGetItemStackName(stack.getItemStack());
        else if (stack.getType() == EntryStack.Type.FLUID)
            return tryGetFluidName(stack.getFluid());
        return "";
    }

    @Deprecated
    public static String tryGetEntryStackTooltip(EntryStack stack) {
        QueuedTooltip tooltip = stack.getTooltip(0, 0);
        if (tooltip != null)
            return CollectionUtils.joinToString(tooltip.getText(), "\n");
        return "";
    }

    @Deprecated
    public static String tryGetFluidName(Fluid fluid) {
        Identifier id = Registry.FLUID.getId(fluid);
        if (I18n.hasTranslation("block." + id.toString().replaceFirst(":", ".")))
            return I18n.translate("block." + id.toString().replaceFirst(":", "."));
        return CollectionUtils.mapAndJoinToString(id.getPath().split("_"), StringUtils::capitalize, " ");
    }

    @Deprecated
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

    @Deprecated
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

    @Deprecated
    @Internal
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
