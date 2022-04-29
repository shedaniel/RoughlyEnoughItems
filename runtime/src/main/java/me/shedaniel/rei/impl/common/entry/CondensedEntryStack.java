package me.shedaniel.rei.impl.common.entry;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CondensedEntryStack<A, B> extends TypedEntryStack<A> {

    public static final Map<ResourceLocation, Boolean> CHILD_VISIBILITY = new HashMap<>();

    public ResourceKey<? extends Registry<B>> registryKey;
    public Function<A, B> entryFromStack;

    private EntryStack<A> currentlyActiveEntry = null;

    private Set<CondensedEntryStack<A, B>> childrenSet;

    public final ResourceLocation condensedEntryId;
    public final boolean isChild;

    private long lastTick = 0;

    protected CondensedEntryStack(ResourceLocation condensedEntryId, EntryDefinition<A> definition, A value, boolean isChild) {
        super(definition, value);

        this.condensedEntryId = condensedEntryId;
        this.isChild = isChild;

        if(!this.isChild && !CHILD_VISIBILITY.containsKey(condensedEntryId)) {
            CHILD_VISIBILITY.put(condensedEntryId, Boolean.FALSE);
        }
    }

    public void setupChildSet(ResourceKey<Registry<B>> registryKey, Collection<B> colletion, Function<B, A> defaultStackMethod){
        this.registryKey = registryKey;

        setChildrenEntrySet(colletion.stream()
                .map(colletionEntry -> new CondensedEntryStack<A, B>(this.condensedEntryId, this.getDefinition(), defaultStackMethod.apply(colletionEntry), true))
                .collect(Collectors.toSet()));
    }

    public void setupChildSet(ResourceKey<Registry<B>> registryKey, Predicate<B> predicate, Function<B, A> defaultStackMethod){
        Registry<B> registry = (Registry<B>) Registry.REGISTRY.get(registryKey.location());
        this.registryKey = registryKey;

        Set<CondensedEntryStack<A, B>> childrenEntrySet = new HashSet<>();

        if(registry != null) {
            for (B registryEntry : registry) {
                if (predicate.test(registryEntry)) {
                    childrenEntrySet.add(new CondensedEntryStack<A, B>(this.condensedEntryId, this.getDefinition(), defaultStackMethod.apply(registryEntry), true));
                }
            }
        }

        setChildrenEntrySet(childrenEntrySet);
    }

    public void setupChildSet(TagKey<B> entryTag, Function<B, A> defaultStackMethod){
        Registry<B> registry = (Registry<B>) Registry.REGISTRY.get(entryTag.registry().location());
        this.registryKey = entryTag.registry();

        Set<CondensedEntryStack<A, B>> childrenEntrySet = new HashSet<>();

        for (Map.Entry<ResourceKey<B>, B> entry : registry.entrySet()) {
            if (registry instanceof MappedRegistry mappedRegistry) {
                if (((Map<B, Holder.Reference<B>>) mappedRegistry.byValue).get(entry.getValue()).is(entryTag)) {
                    childrenEntrySet.add(new CondensedEntryStack<A, B>(this.condensedEntryId, this.getDefinition(), defaultStackMethod.apply(entry.getValue()), true));
                }
            }
        }

        setChildrenEntrySet(childrenEntrySet);
    }

    public void setEntryFromStackFunction(Function<A, B> entryFromStack){
        this.entryFromStack = entryFromStack;
    }

    public Set<CondensedEntryStack<A, B>> getChildrenEntrySet() {
        return childrenSet;
    }

    public void setChildrenEntrySet(Set<CondensedEntryStack<A, B>> childrenEntrySet) {
        this.childrenSet = childrenEntrySet;
    }

    @Override
    public boolean currentlyVisible() {
        return isChild ? CHILD_VISIBILITY.get(this.condensedEntryId) : super.currentlyVisible();
    }

    public void toggleChildren(){
        if(!this.isChild)
            CHILD_VISIBILITY.put(this.condensedEntryId, !CHILD_VISIBILITY.get(this.condensedEntryId));
    }

    public void getNextValue(){
        int index = new Random().nextInt(this.getChildrenEntrySet().size());
        Iterator<CondensedEntryStack<A, B>> iter = this.getChildrenEntrySet().iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }

        currentlyActiveEntry = iter.next();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public A getDisplayValue() {
        if(currentlyActiveEntry != null && !this.isChild) {
            return currentlyActiveEntry.getValue();
        }else{
            return this.getValue();
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void getExtraDataEvent() {
        if(!isChild) {
            if (Minecraft.getInstance().level.getGameTime() - lastTick > 60) {
                this.getNextValue();
                lastTick = Minecraft.getInstance().level.getGameTime();
            }
        }
    }

    @Override
    protected EntryStack<A> wrap(A value, boolean copySettings) {
        CondensedEntryStack<A, B> stack = new CondensedEntryStack<A, B>(this.condensedEntryId, this.getDefinition(), value, this.isChild);

        if(!isChild) {
            stack.currentlyActiveEntry = currentlyActiveEntry;
            stack.setChildrenEntrySet(this.getChildrenEntrySet());
        }

        stack.registryKey = registryKey;

        if (copySettings) {
            for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
                stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
            }
        }

        return stack;
    }


}
