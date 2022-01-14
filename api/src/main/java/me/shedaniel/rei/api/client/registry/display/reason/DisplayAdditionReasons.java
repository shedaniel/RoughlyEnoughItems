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

package me.shedaniel.rei.api.client.registry.display.reason;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Experimental
public interface DisplayAdditionReasons {
    @Nullable <T extends DisplayAdditionReason> T get(Class<? extends T> c);
    
    @Nullable <T extends DisplayAdditionReason> T get(T c);
    
    <T extends DisplayAdditionReason> boolean has(Class<? extends T> c);
    
    <T extends DisplayAdditionReason> boolean has(T c);
    
    @ApiStatus.Internal
    class Impl implements DisplayAdditionReasons {
        public static final Impl EMPTY = new Impl(DisplayAdditionReason.NONE);
        private final DisplayAdditionReason[] reasons;
        
        public Impl(DisplayAdditionReason[] reasons) {
            this.reasons = reasons;
        }
        
        @Override
        @Nullable
        public <T extends DisplayAdditionReason> T get(Class<? extends T> c) {
            for (DisplayAdditionReason reason : reasons) {
                if (Objects.equals(reason.getClass(), c)) {
                    return (T) reason;
                }
            }
            return null;
        }
        
        @Override
        public <T extends DisplayAdditionReason> @Nullable T get(T c) {
            for (DisplayAdditionReason reason : reasons) {
                if (Objects.equals(reason, c)) {
                    return (T) reason;
                }
            }
            return null;
        }
        
        @Override
        public <T extends DisplayAdditionReason> boolean has(Class<? extends T> c) {
            for (DisplayAdditionReason reason : reasons) {
                if (Objects.equals(reason.getClass(), c)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public <T extends DisplayAdditionReason> boolean has(T c) {
            for (DisplayAdditionReason reason : reasons) {
                if (Objects.equals(reason, c)) {
                    return true;
                }
            }
            return false;
        }
    }
}
