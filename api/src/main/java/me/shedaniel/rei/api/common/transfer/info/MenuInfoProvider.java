package me.shedaniel.rei.api.common.transfer.info;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Optional;

/**
 * A provider of {@link MenuInfo}, to provide info conditionally, or dynamically.
 *
 * @param <T> the type of the menu
 * @param <D> the type of display
 */
@FunctionalInterface
public interface MenuInfoProvider<T extends AbstractContainerMenu, D extends Display> {
    Optional<MenuInfo<T, D>> provide(CategoryIdentifier<D> categoryId, Class<T> menuClass);
}
