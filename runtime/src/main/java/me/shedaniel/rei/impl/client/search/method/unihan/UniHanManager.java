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

package me.shedaniel.rei.impl.client.search.method.unihan;

import me.shedaniel.rei.impl.common.InternalLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipInputStream;

public class UniHanManager {
    private final Path unihanPath;
    
    public UniHanManager(Path unihanPath) {
        this.unihanPath = unihanPath;
    }
    
    public boolean downloaded() {
        return Files.exists(unihanPath);
    }
    
    public void download() {
        if (downloaded()) return;
        try {
            URL url = new URL("https://www.unicode.org/Public/UCD/latest/ucd/Unihan.zip");
            Files.deleteIfExists(unihanPath);
            Path parent = unihanPath.getParent();
            if (parent != null) Files.createDirectories(parent);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            long completeFileSize = httpConnection.getContentLength();
            BufferedInputStream inputStream = new BufferedInputStream(httpConnection.getInputStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream, 1024);
            byte[] data = new byte[1024];
            long downloadedFileSize = 0;
            int x;
            int lastPercent = 0;
            while ((x = inputStream.read(data, 0, 1024)) >= 0) {
                downloadedFileSize += x;
                double progress = (double) downloadedFileSize / (double) completeFileSize;
                int percent = (int) (progress * 100);
                if (percent > lastPercent) {
                    lastPercent = percent;
                    InternalLogger.getInstance().debug("Downloading UniHan Progress: %d%%".formatted(percent));
                }
                bufferedStream.write(data, 0, x);
            }
            bufferedStream.close();
            inputStream.close();
            Files.write(unihanPath, outputStream.toByteArray(), StandardOpenOption.CREATE);
            InternalLogger.getInstance().debug("Downloaded UniHan");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Path getUnihanPath() {
        return unihanPath;
    }
    
    public void load(DataConsumer consumer) throws IOException {
        try (ZipInputStream inputStream = new ZipInputStream(Files.newInputStream(getUnihanPath()))) {
            while (inputStream.getNextEntry() != null) {
                read(IOUtils.lineIterator(inputStream, StandardCharsets.UTF_8), consumer);
            }
        }
    }
    
    private void read(LineIterator lines, DataConsumer consumer) {
        int i = 0;
        while (lines.hasNext()) {
            i++;
            String line = lines.nextLine();
            if (line.startsWith("#") || line.isEmpty()) continue;
            if (!line.startsWith("U+")) {
                throw new IllegalArgumentException("Invalid line: " + i + ", " + line);
            }
            int firstTab = line.indexOf('\t');
            String code = line.substring(2, firstTab);
            int codePoint = Integer.parseInt(code, 16);
            int secondTab = line.indexOf('\t', firstTab + 1);
            String fieldKey = line.substring(firstTab + 1, secondTab);
            String data = line.substring(secondTab + 1);
            consumer.read(codePoint, fieldKey, data);
        }
    }
    
    @FunctionalInterface
    public interface DataConsumer {
        void read(int codepoint, String fieldKey, String data);
    }
}
