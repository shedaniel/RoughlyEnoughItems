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
    
    public static <T> Optional<T> getField(Object parent, Class<T> clazz, int index) {
        try {
            Field field = parent.getClass().getDeclaredFields()[index];
            if (!field.isAccessible())
                field.setAccessible(true);
            return Optional.ofNullable(clazz.cast(field.get(parent)));
        } catch (Exception e) {
        }
        RoughlyEnoughItemsCore.LOGGER.warn("Reflection failed! Trying to get #" + index + " from %s", clazz.getName());
        return Optional.empty();
    }
    
    public static <T> Optional<T> getStaticField(Class parentClass, Class<T> clazz, String... possibleNames) {
        for(String possibleName : possibleNames)
            try {
                Field field = parentClass.getDeclaredField(possibleName);
                if (!field.isAccessible())
                    field.setAccessible(true);
                return Optional.ofNullable(clazz.cast(field.get(null)));
            } catch (Exception e) {
            }
        RoughlyEnoughItemsCore.LOGGER.warn("Reflection failed! Trying to get " + possibleNames + " from %s", clazz.getName());
        return Optional.empty();
    }
    
    public static <T> Optional<T> getStaticField(Class parentClass, Class<T> clazz, int index) {
        try {
            Field field = parentClass.getDeclaredFields()[index];
            if (!field.isAccessible())
                field.setAccessible(true);
            return Optional.ofNullable(clazz.cast(field.get(null)));
        } catch (Exception e) {
        }
        RoughlyEnoughItemsCore.LOGGER.warn("Reflection failed! Trying to get #" + index + " from %s", clazz.getName());
        return Optional.empty();
    }
    
    public static class ReflectionException extends Exception {}
    
}
