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

package me.shedaniel.rei.impl.client.util;

import me.shedaniel.rei.impl.common.InternalLogger;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadCreator {
    private final ThreadGroup group;
    private final AtomicInteger threadId = new AtomicInteger(0);
    
    public ThreadCreator(String groupName) {
        this.group = new ThreadGroup(groupName);
    }
    
    public ThreadGroup group() {
        return group;
    }
    
    public AtomicInteger threadId() {
        return threadId;
    }
    
    public ExecutorService asService() {
        return this.asService(Runtime.getRuntime().availableProcessors());
    }
    
    public ExecutorService asService(int poolSize) {
        return new ForkJoinPool(poolSize, pool -> {
            ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setName(group().getName() + "-" + worker.getPoolIndex());
            worker.setContextClassLoader(getClass().getClassLoader());
            return worker;
        }, ($, exception) -> {
            if (!(exception instanceof InterruptedException) && !(exception instanceof CancellationException) && !(exception instanceof ThreadDeath)) {
                InternalLogger.getInstance().throwException(exception);
            }
        }, true);
    }
}
