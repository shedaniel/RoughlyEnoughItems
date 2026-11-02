package me.shedaniel.rei.optimization;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import me.shedaniel.rei.api.client.search.method.CharacterUnpackingInputMethod;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.impl.client.search.argument.InputMethodMatcher;
import me.shedaniel.rei.impl.client.search.method.DefaultInputMethod;
import me.shedaniel.rei.impl.client.search.method.unihan.JyutpingInputMethod;
import me.shedaniel.rei.impl.client.search.method.unihan.PinyinInputMethod;

/** Query-local immutable data only. Plugin methods retain their original evaluation path. */
public final class SearchTerms {
    private SearchTerms() {}

    public record Prepared(InputMethod<?> owner, Iterable<?> values) {}

    public static boolean stable(InputMethod<?> method) {
        Class<?> type = method.getClass();
        return type == DefaultInputMethod.class || type == PinyinInputMethod.class || type == JyutpingInputMethod.class;
    }

    /** Preserve the original atomic transition when the observed language actually changes. */
    public static Object getOrUpdateLanguage(AtomicReference<?> marker, Object language) {
        Object previous = marker.get();
        if (Objects.equals(previous, language)) return previous;
        @SuppressWarnings("unchecked") AtomicReference<Object> writable = (AtomicReference<Object>) marker;
        return writable.getAndSet(language);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean contains(InputMethod method, Iterable filters, String text) {
        if (method instanceof CharacterUnpackingInputMethod unpacking) {
            // Allocate lazily: empty iterables must not evaluate text or call the input method.
            IntList codePoints = null;
            for (Object filter : filters) {
                // Unknown plugin methods may observe/mutate this list; keep their original path.
                if (!stable(method) || codePoints == null) codePoints = IntList.of(text.codePoints().toArray());
                if (InputMethodMatcher.contains(unpacking, codePoints, (IntList) filter)) return true;
            }
        } else {
            for (Object filter : filters) if (method.contains(text, filter)) return true;
        }
        return false;
    }
}
