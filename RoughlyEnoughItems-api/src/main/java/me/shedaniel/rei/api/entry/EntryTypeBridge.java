package me.shedaniel.rei.api.entry;

import me.shedaniel.rei.api.EntryStack;
import net.minecraft.world.InteractionResultHolder;

import java.util.stream.Stream;

@FunctionalInterface
public interface EntryTypeBridge<A, B> {
    InteractionResultHolder<Stream<EntryStack<B>>> bridge(EntryStack<A> object);
}
