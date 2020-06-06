package me.shedaniel.rei.impl.search;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public final class ArgumentsRegistry {
    public static final List<Argument> ARGUMENTS = Lists.newArrayList();
    
    static {
        ARGUMENTS.add(AlwaysMatchingArgument.INSTANCE);
        ARGUMENTS.add(ModArgument.INSTANCE);
        ARGUMENTS.add(TooltipArgument.INSTANCE);
        ARGUMENTS.add(TagArgument.INSTANCE);
        ARGUMENTS.add(RegexArgument.INSTANCE);
        ARGUMENTS.add(TextArgument.INSTANCE);
    }
}
