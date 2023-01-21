/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.common;

import me.shedaniel.rei.impl.Internals;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface InternalLogger {
    static InternalLogger getInstance() {
        return Internals.getInternalLogger();
    }
    
    void throwException(Throwable throwable);
    
    void log(Level level, String message);
    
    void log(Level level, String message, Throwable throwable);
    
    default void log(Level level, String message, Object... args) {
        log(level, String.format(message, args));
    }
    
    default void fatal(String message) {
        log(Level.FATAL, message);
    }
    
    default void fatal(String message, Throwable throwable) {
        log(Level.FATAL, message, throwable);
    }
    
    default void fatal(String message, Object... args) {
        log(Level.FATAL, String.format(message, args));
    }
    
    default void error(String message) {
        log(Level.ERROR, message);
    }
    
    default void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }
    
    default void error(String message, Object... args) {
        log(Level.ERROR, String.format(message, args));
    }
    
    default void warn(String message) {
        log(Level.WARN, message);
    }
    
    default void warn(String message, Throwable throwable) {
        log(Level.WARN, message, throwable);
    }
    
    default void warn(String message, Object... args) {
        log(Level.WARN, String.format(message, args));
    }
    
    default void info(String message) {
        log(Level.INFO, message);
    }
    
    default void info(String message, Throwable throwable) {
        log(Level.INFO, message, throwable);
    }
    
    default void info(String message, Object... args) {
        log(Level.INFO, String.format(message, args));
    }
    
    default void debug(String message) {
        log(Level.DEBUG, message);
    }
    
    default void debug(String message, Throwable throwable) {
        log(Level.DEBUG, message, throwable);
    }
    
    default void debug(String message, Object... args) {
        log(Level.DEBUG, String.format(message, args));
    }
    
    default void trace(String message) {
        log(Level.TRACE, message);
    }
    
    default void trace(String message, Throwable throwable) {
        log(Level.TRACE, message, throwable);
    }
    
    default void trace(String message, Object... args) {
        log(Level.TRACE, String.format(message, args));
    }
}
