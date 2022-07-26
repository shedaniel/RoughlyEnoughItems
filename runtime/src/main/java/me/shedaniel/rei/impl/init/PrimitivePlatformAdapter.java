package me.shedaniel.rei.impl.init;

import java.util.ServiceLoader;

public interface PrimitivePlatformAdapter {
    ServiceLoader<PrimitivePlatformAdapter> LOADER = ServiceLoader.load(PrimitivePlatformAdapter.class);
    
    static PrimitivePlatformAdapter get() {
        return LOADER.findFirst().orElseThrow();
    }
    
    boolean isClient();
    
    boolean isDev();
    
    void checkMods();
    
    String getMinecraftVersion();
    
    int compareVersions(String version1, String version2);
}
