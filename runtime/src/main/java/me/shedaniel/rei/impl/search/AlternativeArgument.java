package me.shedaniel.rei.impl.search;

import com.google.common.collect.ForwardingList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        
        public AlternativeArgument build() {
            if (arguments == null) return AlternativeArgument.EMPTY;
            if (arguments.size() == 1) return new AlternativeArgument(Collections.singletonList(arguments.get(0)));
            return new AlternativeArgument(arguments);
        }
    }
}
