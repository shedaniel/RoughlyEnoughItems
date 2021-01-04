package me.shedaniel.rei.impl;

import me.shedaniel.rei.gui.OverlaySearchField;
import me.shedaniel.rei.impl.search.ArgumentsRegistry;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
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
        SearchArgument.ProcessedSink sink = new SearchArgument.ProcessedSink() {
            @Override
            public void addQuote(int index) {
                highlighted[index] = -2;
            }
    
            @Override
            public void addSplitter(int index) {
                highlighted[index] = -1;
            }
        };
        List<SearchArgument.SearchArguments> arguments = SearchArgument.processSearchTerm(text, sink);
        for (SearchArgument.SearchArguments argument : arguments) {
            if (!argument.isAlways()) {
                for (SearchArgument<?, ?> searchArgument : argument.getArguments()) {
                    int argIndex = ArgumentsRegistry.ARGUMENT_LIST.indexOf(searchArgument.getArgument()) + 1;
                    for (int i = searchArgument.start(); i < searchArgument.end(); i++) {
                        highlighted[i] = (byte) argIndex;
                    }
                }
            }
        }
    }
}
