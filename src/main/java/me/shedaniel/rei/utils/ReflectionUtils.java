package me.shedaniel.rei.utils;

import me.shedaniel.rei.RoughlyEnoughItemsCore;

import java.lang.reflect.Field;
import java.util.Optional;

public class ReflectionUtils {
    
    public static <T> Optional<T> getField(Object parent, Class<T> clazz, String... possibleNames) {
        for(String possibleName : possibleNames)
            try {
                Field field = parent.getClass().getDeclaredField(possibleName);
                if (!field.isAccessible())
                    field.setAccessible(true);
                return Optional.ofNullable(clazz.cast(field.get(parent)));
            } catch (Exception e) {
            }
        RoughlyEnoughItemsCore.LOGGER.warn("Reflection failed! Trying to get " + possibleNames + " from %s", clazz.getName());
        return Optional.empty();
    }
    
}
