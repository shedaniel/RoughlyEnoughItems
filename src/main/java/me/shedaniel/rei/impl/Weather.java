/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

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
