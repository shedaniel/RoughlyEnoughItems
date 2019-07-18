package me.shedaniel.reiclothconfig2.impl;

import com.google.common.collect.Lists;
import me.shedaniel.reiclothconfig2.api.RunSixtyTimesEverySec;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RunSixtyTimesEverySecImpl {
    
    public static final List<RunSixtyTimesEverySec> TICKS_LIST = Lists.newCopyOnWriteArrayList();
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    
    static {
        EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            TICKS_LIST.removeIf(Objects::isNull);
            TICKS_LIST.iterator().forEachRemaining(RunSixtyTimesEverySec::run);
        }, 0, 1000 / 60, TimeUnit.MILLISECONDS);
    }
    
}
