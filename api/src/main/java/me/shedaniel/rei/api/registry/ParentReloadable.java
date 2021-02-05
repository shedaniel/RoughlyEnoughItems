package me.shedaniel.rei.api.registry;

import java.util.List;

public interface ParentReloadable extends Reloadable {
    List<Reloadable> getReloadables();
    
    default void registerReloadable(Reloadable reloadable) {
        this.getReloadables().add(reloadable);
    }
    
    @Override
    default void resetData() {
        for (Reloadable reloadable : getReloadables()) {
            reloadable.resetData();
        }
    }
}
