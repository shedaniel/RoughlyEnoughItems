package me.shedaniel.reiclothconfig2.api;

import me.shedaniel.reiclothconfig2.impl.RunSixtyTimesEverySecImpl;

public interface RunSixtyTimesEverySec {
    
    void run();
    
    default boolean isRegistered() {
        return RunSixtyTimesEverySecImpl.TICKS_LIST.contains(this);
    }
    
    default void registerTick() {
        RunSixtyTimesEverySecImpl.TICKS_LIST.removeIf(runSixtyTimesEverySec -> runSixtyTimesEverySec == this);
        RunSixtyTimesEverySecImpl.TICKS_LIST.add(this);
    }
    
    default void unregisterTick() {
        RunSixtyTimesEverySecImpl.TICKS_LIST.remove(this);
    }
    
}
