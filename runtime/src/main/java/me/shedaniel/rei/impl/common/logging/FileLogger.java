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

package me.shedaniel.rei.impl.common.logging;

import me.shedaniel.rei.impl.common.InternalLogger;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger implements InternalLogger {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Writer writer;
    
    public FileLogger(Path file) {
        Writer w;
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            file.toFile().createNewFile();
            w = new OutputStreamWriter(new FileOutputStream(file.toFile()), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            exception.printStackTrace();
            w = new OutputStreamWriter(new NullOutputStream());
        }
        
        this.writer = w;
    }
    
    @Override
    public void throwException(Throwable throwable) {
        throwable.printStackTrace(new PrintWriter(writer, true));
    }
    
    @Override
    public void log(Level level, String message) {
        message = String.format("[%s] [%s/%s] %s", DATE_TIME_FORMATTER.format(LocalDateTime.now()), Thread.currentThread().getName(), level, message);
        
        try {
            writer.write(message);
            writer.write("\n");
            writer.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    
    @Override
    public void log(Level level, String message, Throwable throwable) {
        log(level, message);
        throwable.printStackTrace(new PrintWriter(writer, true));
    }
}
