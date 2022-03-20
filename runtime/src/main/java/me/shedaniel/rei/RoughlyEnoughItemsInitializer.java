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

package me.shedaniel.rei;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class RoughlyEnoughItemsInitializer {
    public static final String COMPATIBLE_MC_VERSION = "1.19";
    
    public static void onInitialize() {
        RoughlyEnoughItemsState.env = isClient() ? EnvType.CLIENT : EnvType.SERVER;
        RoughlyEnoughItemsState.isDev = isDev();
        
        if (getMinecraftVersion().startsWith("1.") && !getMinecraftVersion().startsWith(COMPATIBLE_MC_VERSION)) {
            RoughlyEnoughItemsState.error("Your current REI version (for " + COMPATIBLE_MC_VERSION + ") is not compatible with your current Minecraft version (" + getMinecraftVersion() + ").");
        }
        
        checkMods();
        
        if (RoughlyEnoughItemsState.getErrors().isEmpty()) {
            initializeEntryPoint(false, "me.shedaniel.rei.RoughlyEnoughItemsCore");
        }
    }
    
    public static void onInitializeClient() {
        if (RoughlyEnoughItemsState.getErrors().isEmpty()) {
            initializeEntryPoint(true, "me.shedaniel.rei.RoughlyEnoughItemsCoreClient");
            initializeEntryPoint(true, "me.shedaniel.rei.REIModMenuEntryPoint");
            initializeEntryPoint(true, "me.shedaniel.rei.impl.client.ClientHelperImpl");
            initializeEntryPoint(true, "me.shedaniel.rei.impl.client.REIRuntimeImpl");
        }
        
        initializeEntryPoint(true, "me.shedaniel.rei.impl.client.ErrorDisplayer");
    }
    
    public static void initializeEntryPoint(boolean client, String className) {
        try {
            Class<?> name = Class.forName(className);
            Object instance = name.getConstructor().newInstance();
            Method method = null;
            if (client) {
                if (isClient()) {
                    try {
                        method = name.getDeclaredMethod("onInitializeClient");
                    } catch (NoSuchMethodException ignored) {
                    }
                    if (method != null) {
                        MethodHandles.lookup().unreflect(method).bindTo(instance).invoke();
                    }
                }
            } else {
                try {
                    method = name.getDeclaredMethod("onInitialize");
                } catch (NoSuchMethodException ignored) {
                }
                if (method != null) {
                    MethodHandles.lookup().unreflect(method).bindTo(instance).invoke();
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize REI entry point: " + className, e);
        }
    }
    
    @ExpectPlatform
    public static boolean isClient() {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static boolean isDev() {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static void checkMods() {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static String getMinecraftVersion() {
        throw new AssertionError();
    }
}