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
    private static final Map<String, Map<Class<? extends Container>, ContainerInfo>> containerInfoMap = Maps.newLinkedHashMap();
    
    public static void registerContainerInfo(Identifier category, ContainerInfo containerInfo) {
        if (!containerInfoMap.containsKey(category.toString()))
            containerInfoMap.put(category.toString(), Maps.newLinkedHashMap());
        containerInfoMap.get(category.toString()).put(containerInfo.getContainerClass(), containerInfo);
    }
    
    public static boolean isCategoryHandled(Identifier category) {
        return containerInfoMap.containsKey(category.toString()) && !containerInfoMap.get(category.toString()).isEmpty();
    }
    
    public static ContainerInfo getContainerInfo(Identifier category, Class<?> containerClass) {
        if (!isCategoryHandled(category))
            return null;
        Map<Class<? extends Container>, ContainerInfo> infoMap = containerInfoMap.get(category.toString());
        if (infoMap.containsKey(containerClass))
            return infoMap.get(containerClass);
        for (Map.Entry<Class<? extends Container>, ContainerInfo> entry : infoMap.entrySet())
            if (entry.getKey().isAssignableFrom(containerClass))
                return entry.getValue();
        return null;
    }
}
