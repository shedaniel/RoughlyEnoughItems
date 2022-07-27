package me.shedaniel.rei.impl.client.entry.filtering;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.impl.client.config.entries.FilteringEntry;
import me.shedaniel.rei.impl.client.entry.filtering.rules.ManualFilteringRuleType;
import me.shedaniel.rei.impl.client.entry.filtering.rules.SearchFilteringRuleType;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.function.BiFunction;

@ApiStatus.Experimental
public interface FilteringRuleType<T extends FilteringRuleInternal> {
    BiMap<ResourceLocation, FilteringRuleType<?>> REGISTRY = Util.make(HashBiMap.create(), registry -> {
        registry.put(new ResourceLocation("roughlyenoughitems", "search"), SearchFilteringRuleType.INSTANCE);
        registry.put(new ResourceLocation("roughlyenoughitems", "manual"), ManualFilteringRuleType.INSTANCE);
    });
    
    static CompoundTag save(FilteringRuleInternal rule, CompoundTag tag) {
        tag.putString("id", REGISTRY.inverse().get(rule.getType()).toString());
        tag.put("rule", ((FilteringRuleType<FilteringRuleInternal>) rule.getType()).saveTo(rule, new CompoundTag()));
        return tag;
    }
    
    CompoundTag saveTo(T rule, CompoundTag tag);
    
    static FilteringRule read(CompoundTag tag) {
        return REGISTRY.get(ResourceLocation.tryParse(tag.getString("id"))).createFromTag(tag.getCompound("rule"));
    }
    
    T createFromTag(CompoundTag tag);
    
    @ApiStatus.Internal
    default Optional<BiFunction<FilteringEntry, Screen, Screen>> createEntryScreen() {
        return Optional.empty();
    }
    
    default Component getTitle() {
        return Component.nullToEmpty(FilteringRuleType.REGISTRY.inverse().get(this).toString());
    }
    
    default Component getSubtitle() {
        return Component.nullToEmpty(null);
    }
    
    T createNew();
}
