/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.api.annotations.Internal;

@Internal
@Deprecated
public interface ItemStackHook {
    void rei_setRenderEnchantmentGlint(boolean b);
}
