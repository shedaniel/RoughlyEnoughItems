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

package me.shedaniel.rei.impl.client.gui.widget;

import me.shedaniel.rei.impl.client.gui.error.ErrorsEntryListWidget;
import me.shedaniel.rei.impl.client.gui.error.ErrorsScreen;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class CatchingExceptionUtils {
    public static void handleThrowable(Throwable throwable, String task) {
        CrashReport report = CrashReportUtils.essential(throwable, task);
        File reportsFolder = new File(Minecraft.getInstance().gameDirectory, "crash-reports");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        File crashReportFile = new File(reportsFolder, "crash-" + format.format(new Date()) + "-client.txt");
        report.saveToFile(crashReportFile);
        Bootstrap.realStdoutPrintln(report.getFriendlyReport());
        List<Object> components = new ArrayList<>();
        components.add(Component.literal(I18n.get("text.rei.crash.description", report.getTitle())));
        components.add((Function<Integer, ErrorsEntryListWidget.Entry>) width -> new ErrorsEntryListWidget.LinkEntry(Component.translatable("text.rei.crash.crash_report"), crashReportFile.toURI().toString(), width));
        components.add(Component.empty());
        components.add(Component.literal(report.getFriendlyReport().replace("\t", "    ")));
        Minecraft.getInstance().setScreen(new ErrorsScreen(Component.translatable("text.rei.crash.title"), components, null, false));
    }
}
