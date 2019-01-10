package me.shedaniel.rei.client;

public class SearchArgument {
    
    public enum ArgumentType {
        TEXT, MOD, TOOLTIP
    }
    
    private ArgumentType argumentType;
    private String text;
    private boolean include;
    
    public SearchArgument(ArgumentType argumentType, String text, boolean include) {
        this.argumentType = argumentType;
        this.text = text;
        this.include = include;
    }
    
    public ArgumentType getArgumentType() {
        return argumentType;
    }
    
    public String getText() {
        return text;
    }
    
    public boolean isInclude() {
        return include;
    }
    
    @Override
    public String toString() {
        return String.format("Argument[%s]: name = %s, include = %b", argumentType.name(), text, include);
    }
    
}
