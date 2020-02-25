package me.shedaniel.rei.gui;

import me.shedaniel.rei.api.EntryStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface StackToNoticeScreen {
    @ApiStatus.Internal
    void addIngredientStackToNotice(EntryStack stack);
    
    @ApiStatus.Internal
    void addResultStackToNotice(EntryStack stack);
}