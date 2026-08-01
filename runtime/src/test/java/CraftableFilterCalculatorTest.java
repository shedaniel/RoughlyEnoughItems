package me.shedaniel.rei.impl.client.gui.craftable;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CraftableFilterCalculatorTest {
    @Test
    void copyIngredientsShouldNotMutateTheOriginalMap() {
        Long2LongOpenHashMap original = new Long2LongOpenHashMap();
        original.put(1L, 5L);
        original.put(2L, 3L);

        Long2LongMap copy = CraftableFilterCalculator.copyIngredients(original);
        copy.put(1L, 1L);

        assertEquals(5L, original.get(1L));
        assertEquals(3L, original.get(2L));
        assertEquals(1L, copy.get(1L));
    }
}
