package mezz.jei.util;

public class ErrorUtil {
    public static void checkNotNull(Object object, String name) {
        if (object == null) {
            throw new NullPointerException(name + " must not be null.");
        }
    }
}
