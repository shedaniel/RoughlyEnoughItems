package me.shedaniel.rei.api.client.gui.widgets;

import org.jetbrains.annotations.ApiStatus;

import java.io.Closeable;

@ApiStatus.NonExtendable
@ApiStatus.Experimental
public interface CloseableScissors extends Closeable {
    @Override
    void close();
}
