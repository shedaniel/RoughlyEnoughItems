/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.server;

import com.google.common.collect.Maps;
import net.minecraft.container.Container;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ContainerInfoHandler {
    private static final Map<Identifier, Map<Class<? extends Container>, ContainerInfo>> containerInfoMap = Maps.newHashMap();
    
    public static void registerContainerInfo(Identifier category, ContainerInfo containerInfo) {
        if (!containerInfoMap.containsKey(category))
            containerInfoMap.put(category, Maps.newHashMap());
        containerInfoMap.get(category).put(containerInfo.getContainerClass(), containerInfo);
    }
    
    public static boolean isCategoryHandled(Identifier category) {
        return containerInfoMap.containsKey(category) && !containerInfoMap.get(category).isEmpty();
    }
    
    public static ContainerInfo getContainerInfo(Identifier category, Class<?> containerClass) {
        return containerInfoMap.get(category).get(containerClass);
    }
}
