/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.utils;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.annotations.Internal;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Internal
public class CollectionUtils {
    public static final <T> T findFirstOrNullEquals(List<T> list, T obj) {
        for (T t : list) {
            if (t.equals(obj))
                return t;
        }
        return null;
    }
    
    public static final <T> T findFirstOrNull(List<T> list, Predicate<T> predicate) {
        for (T t : list) {
            if (predicate.test(t))
                return t;
        }
        return null;
    }
    
    public static final <T> boolean anyMatch(List<T> list, Predicate<T> predicate) {
        for (T t : list) {
            if (predicate.test(t))
                return true;
        }
        return false;
    }
    
    public static final boolean anyMatchEqualsAll(List<EntryStack> list, EntryStack stack) {
        for (EntryStack t : list) {
            if (t.equalsAll(stack))
                return true;
        }
        return false;
    }
    
    public static final <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> l = new LinkedList<>();
        for (T t : list) {
            if (predicate.test(t)) {
                l.add(t);
            }
        }
        return l;
    }
    
    public static final <T, R> List<R> map(List<T> list, Function<T, R> function) {
        List<R> l = new LinkedList<>();
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static final <T, R> List<R> map(T[] list, Function<T, R> function) {
        List<R> l = new LinkedList<>();
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static final <T, R> Optional<R> mapAndMax(List<T> list, Function<T, R> function, Comparator<R> comparator) {
        if (list.isEmpty())
            return Optional.empty();
        List<R> copyOf = CollectionUtils.map(list, function);
        copyOf.sort(comparator);
        return Optional.ofNullable(copyOf.get(copyOf.size() - 1));
    }
    
    public static final <T, R> Optional<R> mapAndMax(T[] list, Function<T, R> function, Comparator<R> comparator) {
        if (list.length <= 0)
            return Optional.empty();
        List<R> copyOf = CollectionUtils.map(list, function);
        copyOf.sort(comparator);
        return Optional.ofNullable(copyOf.get(copyOf.size() - 1));
    }
    
    public static final <T> Optional<T> max(List<T> list, Comparator<T> comparator) {
        if (list.isEmpty())
            return Optional.empty();
        ArrayList<T> ts = new ArrayList<>(list);
        ts.sort(comparator);
        return Optional.ofNullable(ts.get(ts.size() - 1));
    }
    
    public static final <T> Optional<T> max(T[] list, Comparator<T> comparator) {
        if (list.length <= 0)
            return Optional.empty();
        T[] copyOf = list.clone();
        Arrays.sort(copyOf, comparator);
        return Optional.ofNullable(copyOf[copyOf.length - 1]);
    }
    
    public static final String joinToString(List<String> list, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (String t : list) {
            joiner.add(t);
        }
        return joiner.toString();
    }
    
    public static final String joinToString(String[] list, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (String t : list) {
            joiner.add(t);
        }
        return joiner.toString();
    }
    
    public static final <T> String mapAndJoinToString(List<T> list, Function<T, String> function, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (T t : list) {
            joiner.add(function.apply(t));
        }
        return joiner.toString();
    }
    
    public static final <T> String mapAndJoinToString(T[] list, Function<T, String> function, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (T t : list) {
            joiner.add(function.apply(t));
        }
        return joiner.toString();
    }
    
    public static final <T, R> List<R> filterAndMap(List<T> list, Predicate<T> predicate, Function<T, R> function) {
        List<R> l = null;
        for (T t : list) {
            if (predicate.test(t)) {
                if (l == null)
                    l = new LinkedList<>();
                l.add(function.apply(t));
            }
        }
        return l == null ? Collections.emptyList() : l;
    }
    
    public static final <T> int sumInt(List<T> list, Function<T, Integer> function) {
        int sum = 0;
        for (T t : list) {
            sum += function.apply(t);
        }
        return sum;
    }
    
    public static final <T> int sumInt(List<Integer> list) {
        int sum = 0;
        for (int t : list) {
            sum += t;
        }
        return sum;
    }
    
    public static final <T> double sumDouble(List<T> list, Function<T, Double> function) {
        double sum = 0;
        for (T t : list) {
            sum += function.apply(t);
        }
        return sum;
    }
    
    public static final <T> double sumDouble(List<Double> list) {
        double sum = 0;
        for (double t : list) {
            sum += t;
        }
        return sum;
    }
}
