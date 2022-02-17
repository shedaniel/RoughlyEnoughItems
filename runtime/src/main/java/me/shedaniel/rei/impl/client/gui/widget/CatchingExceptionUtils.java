package me.shedaniel.rei.impl.client.gui.widget;

import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import me.shedaniel.rei.impl.client.gui.error.ErrorsEntryListWidget;
import me.shedaniel.rei.impl.client.gui.error.ErrorsScreen;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
        components.add(new TextComponent(I18n.get("text.rei.crash.description", report.getTitle())));
        components.add((Function<Integer, ErrorsEntryListWidget.Entry>) width -> new ErrorsEntryListWidget.LinkEntry(new TranslatableComponent("text.rei.crash.crash_report"), crashReportFile.toURI().toString(), width));
        components.add(ImmutableTextComponent.EMPTY);
        components.add(new TextComponent(report.getFriendlyReport().replace("\t", "    ")));
        Minecraft.getInstance().setScreen(new ErrorsScreen(new TranslatableComponent("text.rei.crash.title"), components, null, false));
    }
}
