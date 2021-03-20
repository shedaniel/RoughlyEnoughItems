package me.shedaniel.rei.api.registry.screen;

import me.shedaniel.math.Rectangle;

import java.util.Collection;

@FunctionalInterface
public interface ExclusionZonesProvider<T> {
    Collection<Rectangle> provide(T screen);
}
