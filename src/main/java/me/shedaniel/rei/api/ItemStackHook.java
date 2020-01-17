/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ItemStackHook {
    void rei_setRenderEnchantmentGlint(boolean b);
}
