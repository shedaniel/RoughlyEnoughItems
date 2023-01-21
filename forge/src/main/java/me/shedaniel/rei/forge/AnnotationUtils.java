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

package me.shedaniel.rei.forge;

import com.google.common.collect.Lists;
import me.shedaniel.rei.impl.init.PrimitivePlatformAdapter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AnnotationUtils {
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    
    public static <A, T> void scanAnnotation(Class<A> clazz, Predicate<Class<T>> predicate, TriConsumer<List<String>, Supplier<T>, Class<T>> consumer) {
        scanAnnotation(Type.getType(clazz), predicate, consumer);
    }
    
    public static <T> void scanAnnotation(Type annotationType, Predicate<Class<T>> predicate, TriConsumer<List<String>, Supplier<T>, Class<T>> consumer) {
        List<Triple<List<String>, Supplier<T>, Class<T>>> instances = Lists.newArrayList();
        for (ModFileScanData data : ModList.get().getAllScanData()) {
            List<String> modIds = data.getIModInfoData().stream()
                    .flatMap(info -> info.getMods().stream())
                    .map(IModInfo::getModId)
                    .collect(Collectors.toList());
            out:
            for (ModFileScanData.AnnotationData annotation : data.getAnnotations()) {
                if (annotationType.equals(annotation.annotationType())) {
                    Object value = annotation.annotationData().get("value");
                    boolean enabled;
                    
                    if (value instanceof Dist[]) {
                        enabled = Arrays.asList((Dist[]) value).contains(FMLEnvironment.dist);
                    } else if (value instanceof ModAnnotation.EnumHolder) {
                        enabled = Objects.equals(((ModAnnotation.EnumHolder) value).getValue(), FMLEnvironment.dist.name());
                    } else if (value instanceof List) {
                        List<ModAnnotation.EnumHolder> holders = ((List<?>) value).stream().filter(o -> o instanceof ModAnnotation.EnumHolder)
                                .map(o -> (ModAnnotation.EnumHolder) o).toList();
                        if (!holders.isEmpty()) {
                            enabled = holders.stream()
                                    .anyMatch(o -> Objects.equals(o.getValue(), FMLEnvironment.dist.name()));
                        } else {
                            enabled = true;
                        }
                    } else {
                        enabled = true;
                    }
                    
                    if (!enabled) continue;
                    
                    try {
                        Class<T> clazz = (Class<T>) Class.forName(annotation.memberName());
                        if (predicate.test(clazz)) {
                            instances.add(new ImmutableTriple<>(modIds, () -> {
                                try {
                                    return clazz.getDeclaredConstructor().newInstance();
                                } catch (Throwable throwable) {
                                    LOGGER.error("Failed to load plugin: " + annotation.memberName(), throwable);
                                    return null;
                                }
                            }, clazz));
                        }
                    } catch (Throwable throwable) {
                        Throwable t = throwable;
                        while (t != null) {
                            if (t.getMessage() != null && t.getMessage().contains("invalid dist DEDICATED_SERVER") && !PrimitivePlatformAdapter.get().isClient()) {
                                LOGGER.warn("Plugin " + annotation.memberName() + " is attempting to load on the server, but is not compatible with the server. " +
                                            "The mod should declare the environments it is compatible with in the @" + annotationType.getClassName() + " annotation.");
                                continue out;
                            }
                            t = t.getCause();
                        }
                        LOGGER.error("Failed to load plugin: " + annotation.memberName(), throwable);
                    }
                }
            }
        }
        
        for (Triple<List<String>, Supplier<T>, Class<T>> pair : instances) {
            consumer.accept(pair.getLeft(), pair.getMiddle(), pair.getRight());
        }
    }
}
