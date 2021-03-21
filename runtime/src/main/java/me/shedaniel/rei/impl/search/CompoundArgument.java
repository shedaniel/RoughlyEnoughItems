package me.shedaniel.rei.impl.search;

import com.google.common.collect.ForwardingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
