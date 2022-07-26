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

package me.shedaniel.rei.impl.client.search.argument;

import com.google.common.collect.ForwardingList;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class CompoundArgument extends ForwardingList<AlternativeArgument> {
    public static final CompoundArgument ALWAYS = new CompoundArgument(AlternativeArgument.EMPTY);
    private final AlternativeArgument[] arguments;
    private final List<AlternativeArgument> argumentList;
    
    private CompoundArgument(AlternativeArgument... arguments) {
        this.arguments = arguments;
        this.argumentList = Arrays.asList(arguments);
    }
    
    public static CompoundArgument of(AlternativeArgument... arguments) {
        if (arguments.length == 0) return ALWAYS;
        return new CompoundArgument(arguments);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public final boolean isAlways() {
        return this == ALWAYS;
    }
    
    @Override
    protected List<AlternativeArgument> delegate() {
        return argumentList;
    }
    
    public static class Builder {
        private List<AlternativeArgument> arguments;
        
        public <T, R> Builder add(Argument<T, R> argument) {
            return add(new AlternativeArgument(Collections.singletonList(argument)));
        }
        
        public Builder add(AlternativeArgument.Builder builder) {
            return add(builder.build());
        }
        
        public Builder add(AlternativeArgument argument) {
            if (arguments == null) {
                this.arguments = new ArrayList<>();
            }
            
            arguments.add(argument);
            return this;
        }
        
        public CompoundArgument build() {
            if (arguments == null) return CompoundArgument.ALWAYS;
            return CompoundArgument.of(arguments.toArray(new AlternativeArgument[0]));
        }
    }
}
