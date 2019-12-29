package com.junbaor.event.callback;

import com.junbaor.event.GoblinEvent;
import com.junbaor.event.GoblinEventChannel;
import com.junbaor.event.function.GoblinCallback;
import org.jetbrains.annotations.NotNull;

@GoblinEventChannel("/goblin/core")
final public class GoblinCallbackEvent extends GoblinEvent {

    private final GoblinCallback<?> callback;

    public GoblinCallbackEvent(@NotNull GoblinCallback<?> callback) {
        this.callback = callback;
        setRaiseException(true);
    }

    @NotNull
    public GoblinCallback<?> getCallback() {
        return callback;
    }
}
