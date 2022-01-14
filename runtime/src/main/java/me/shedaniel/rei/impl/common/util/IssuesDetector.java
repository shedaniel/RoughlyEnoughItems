/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.common.util;

import dev.architectury.platform.Platform;
import me.shedaniel.rei.RoughlyEnoughItemsState;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@ApiStatus.Internal
public final class IssuesDetector {
    private static final List<Triple<BooleanSupplier, @Nullable String, String>> ISSUES = new ArrayList<>();
    
    public static void register(BooleanSupplier detector, @Nullable String ignoreFileName, String issueMessage) {
        ISSUES.add(Triple.of(detector, ignoreFileName, issueMessage));
    }
    
    public static void detect() {
        File reiConfigFolder = Platform.getConfigFolder().resolve("roughlyenoughitems").toFile();
        for (Triple<BooleanSupplier, String, String> issue : ISSUES) {
            if (issue.getLeft().getAsBoolean()) {
                if (issue.getMiddle() != null) {
                    File ignoreFile = new File(reiConfigFolder, issue.getMiddle());
                    if (ignoreFile.exists()) {
                        return;
                    }
                    RoughlyEnoughItemsState.warn(issue.getRight());
                } else {
                    RoughlyEnoughItemsState.error(issue.getRight());
                }
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
