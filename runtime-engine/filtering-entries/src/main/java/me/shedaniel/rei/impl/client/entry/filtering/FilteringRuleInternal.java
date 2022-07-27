package me.shedaniel.rei.impl.client.entry.filtering;

import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public interface FilteringRuleInternal extends FilteringRule {
    FilteringRuleType<?> getType();
    
    default Object prepareCache(boolean async) {
        return null;
    }
    
    FilteringResult processFilteredStacks(FilteringContext context, FilteringCache cache, boolean async);
}
