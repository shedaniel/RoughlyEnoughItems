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
