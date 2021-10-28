/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.client.util;

import me.shedaniel.rei.api.client.gui.Renderer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class CrashReportUtils {
    public static CrashReport essential(Throwable throwable, String task) {
        Throwable temp = throwable;
        while (temp != null) {
            temp = temp.getCause();
            if (temp instanceof ReportedException) {
                return essential(temp, task);
            }
        }
        CrashReport report = CrashReport.forThrowable(throwable, task);
        screen(report, Minecraft.getInstance().screen);
        return report;
    }
    
    private static void screen(CrashReport report, Screen screen) {
        if (screen != null) {
            CrashReportCategory category = report.addCategory("Screen details");
            String screenName = screen.getClass().getCanonicalName();
            category.setDetail("Screen name", () -> screenName);
        }
    }
    
    public static void renderer(CrashReport report, Renderer renderer) {
        if (renderer != null) {
            CrashReportCategory category = report.addCategory("Renderer details");
            try {
                renderer.fillCrashReport(report, category);
            } catch (Throwable throwable) {
                category.setDetailError("Filling Report", throwable);
            }
        }
    }
    
    public static ReportedException throwReport(CrashReport report) {
        return new ReportedException(report);
    }
}
