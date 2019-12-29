package com.junbaor.event.callback;

import org.jetbrains.annotations.NotNull;

public class GoblinCallbackException extends RuntimeException {
    private static final long serialVersionUID = 8739931958023711661L;

    private GoblinCallbackException(Throwable cause) {
        super(cause);
    }

    public static void throwException(@NotNull Throwable cause) {
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        throw new GoblinCallbackException(cause);
    }
}
