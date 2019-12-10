package me.shedaniel.rei.utils;

import java.util.function.Supplier;

@Deprecated
public class ExecutorUtil {
    private ExecutorUtil() {
    }
    
    public static void execute(Supplier<Runnable> runnableSupplier) {
        runnableSupplier.get().run();
    }
}
