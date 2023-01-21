/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.client.search.argument;

import com.google.common.collect.ForwardingList;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class AlternativeArgument extends ForwardingList<Argument<?, ?>> {
    static final AlternativeArgument EMPTY = new AlternativeArgument(Collections.emptyList());
    
    private final List<Argument<?, ?>> arguments;
    
    public AlternativeArgument(List<Argument<?, ?>> arguments) {
        this.arguments = arguments;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    protected List<Argument<?, ?>> delegate() {
        return arguments;
    }
    
    public static class Builder {
        private List<Argument<?, ?>> arguments;
        
        public <T, R> Builder add(Argument<T, R> argument) {
            if (arguments == null) {
                this.arguments = new ArrayList<>();
            }
            
            arguments.add(argument);
            return this;
        }
        
        public boolean isEmpty() {
            return arguments == null;
        }
        
        public AlternativeArgument build() {
            if (arguments == null) return AlternativeArgument.EMPTY;
            if (arguments.size() == 1) return new AlternativeArgument(Collections.singletonList(arguments.get(0)));
            return new AlternativeArgument(arguments);
        }
    }
}
