package me.shedaniel.rei.impl;

import me.shedaniel.rei.RoughlyEnoughItemsState;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

@ApiStatus.Internal
public final class IssuesDetector {
    private static final List<Triple<BooleanSupplier, @Nullable String, String>> ISSUES;
    
    static {
        ISSUES = new ArrayList<>();
        register(() -> {
            try {
                FabricLoader instance = FabricLoader.getInstance();
                for (Field field : instance.getClass().getDeclaredFields()) {
                    if (Logger.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Logger logger = (Logger) field.get(instance);
                        if (logger.getName().toLowerCase(Locale.ROOT).contains("subsystem"))
                            return true;
                    }
                }
            } catch (Throwable ignored) {
            }
            return false;
        }, ".ignoresubsystem", "Subsystem is detected (probably though Aristois), please contact support from them if anything happens.");
    }
    
    private static void register(BooleanSupplier detector, @Nullable String ignoreFileName, String issueMessage) {
        ISSUES.add(Triple.of(detector, ignoreFileName, issueMessage));
    }
    
    public static void detect() {
        FabricLoader instance = FabricLoader.getInstance();
        File reiConfigFolder = instance.getConfigDir().resolve("roughlyenoughitems").toFile();
        for (Triple<BooleanSupplier, String, String> issue : ISSUES) {
            if (issue.getLeft().getAsBoolean()) {
                if (issue.getMiddle() != null) {
                    File ignoreFile = new File(reiConfigFolder, issue.getMiddle());
                    if (ignoreFile.exists()) {
                        return;
                    }
                }
                RoughlyEnoughItemsState.warn(issue.getRight());
                RoughlyEnoughItemsState.onContinue(() -> {
                    try {
                        if (issue.getMiddle() != null) {
                            reiConfigFolder.mkdirs();
                            File ignoreFile = new File(reiConfigFolder, issue.getMiddle());
                            ignoreFile.createNewFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }
        }
    }
}
