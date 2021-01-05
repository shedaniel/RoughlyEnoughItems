package me.shedaniel.rei.impl;

import me.shedaniel.rei.gui.OverlaySearchField;
import me.shedaniel.rei.impl.search.ArgumentsRegistry;
import net.minecraft.util.IntRange;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.Consumer;

@ApiStatus.Internal
public class OverlaySearchFieldSyntaxHighlighter implements Consumer<String> {
    private final OverlaySearchField field;
    public byte[] highlighted;
    
    public OverlaySearchFieldSyntaxHighlighter(OverlaySearchField field) {
        this.field = field;
        this.accept(field.getText());
    }
    
    @Override
    public void accept(String text) {
        this.highlighted = new byte[text.length()];
        SearchArgument.processSearchTerm(text, new SearchArgument.ProcessedSink() {
            @Override
            public void addQuote(int index) {
                highlighted[index] = -2;
            }
            
            @Override
            public void addSplitter(int index) {
                highlighted[index] = -1;
            }
            
            @Override
            public void addPart(SearchArgument<?, ?> argument, Collection<IntRange> grammarRanges, int index) {
                int argIndex = ArgumentsRegistry.ARGUMENT_LIST.indexOf(argument.getArgument()) * 2 + 1;
                for (int i = argument.start(); i < argument.end(); i++) {
                    highlighted[i] = (byte) argIndex;
                }
                for (IntRange grammarRange : grammarRanges) {
                    for (int i = grammarRange.getMinInclusive(); i <= grammarRange.getMaxInclusive(); i++) {
                        highlighted[i + index] = (byte) (argIndex + 1);
                    }
                }
            }
        });
    }
}
