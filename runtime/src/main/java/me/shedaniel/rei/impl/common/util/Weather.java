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

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum Weather {
    CLEAR(0, "text.rei.weather.clear"),
    RAIN(1, "text.rei.weather.rain"),
    THUNDER(2, "text.rei.weather.thunder");
    
    private final int id;
    private final String translateKey;
    
    Weather(int id, String translateKey) {
        this.id = id;
        this.translateKey = translateKey;
    }
    
    public static Weather byId(int id) {
        return byId(id, CLEAR);
    }
    
    public static Weather byId(int id, Weather defaultWeather) {
        for (Weather weather : values()) {
            if (weather.id == id)
                return weather;
        }
        return defaultWeather;
    }
    
    public int getId() {
        return id;
    }
    
    public String getTranslateKey() {
        return translateKey;
    }
}
